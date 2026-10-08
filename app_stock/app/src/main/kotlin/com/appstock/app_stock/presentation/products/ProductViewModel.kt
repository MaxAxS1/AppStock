package com.appstock.app_stock.presentation.products

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.appstock.app_stock.data.repository.CategoryRepositoryImpl
import com.appstock.app_stock.data.repository.ProductRepositoryImpl
import com.appstock.app_stock.data.repository.SizeRepositoryImpl
import com.appstock.app_stock.data.service.ImageUploadService
import com.appstock.app_stock.domain.model.Category
import com.appstock.app_stock.domain.model.ProductDetail
import com.appstock.app_stock.domain.model.ProductSize
import com.appstock.app_stock.domain.repository.CategoryRepository
import com.appstock.app_stock.domain.repository.ProductRepository
import com.appstock.app_stock.domain.repository.SizeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProductViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProductRepository = ProductRepositoryImpl()
    private val sizeRepository: SizeRepository = SizeRepositoryImpl()
    private val categoryRepository: CategoryRepository = CategoryRepositoryImpl()

    // — Lista de productos —
    private val _products = MutableStateFlow<List<ProductDetail>>(emptyList())
    val products: StateFlow<List<ProductDetail>> = _products

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // — Categorías para el formulario —
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    // — Estado del formulario de alta —
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    private val _saveError = MutableStateFlow<String?>(null)
    val saveError: StateFlow<String?> = _saveError

    // — Estado de edición de producto —
    private val _selectedProduct = MutableStateFlow<ProductDetail?>(null)
    val selectedProduct: StateFlow<ProductDetail?> = _selectedProduct

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess

    private val _updateError = MutableStateFlow<String?>(null)
    val updateError: StateFlow<String?> = _updateError

    // — Estado de eliminación —
    private val _deleteSuccess = MutableStateFlow(false)
    val deleteSuccess: StateFlow<Boolean> = _deleteSuccess

    private val _deleteError = MutableStateFlow<String?>(null)
    val deleteError: StateFlow<String?> = _deleteError

    init {
        // Escuchar la lista de productos según búsqueda
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    _isLoading.value = true
                    if (query.isEmpty()) repository.getProducts() else repository.searchProducts(query)
                }
                .collect {
                    _products.value = it
                    _isLoading.value = false
                }
        }
        loadCategories()
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    private fun loadCategories() {
        viewModelScope.launch {
            com.appstock.app_stock.data.CategorySeeder.seedIfEmpty()
            categoryRepository.getCategories().collect {
                _categories.value = it
            }
        }
    }

    /** Carga un producto por ID en el estado selectedProduct */
    fun loadProductById(productId: String) {
        viewModelScope.launch {
            val cached = _products.value.firstOrNull { it.id == productId }
            if (cached != null) {
                _selectedProduct.value = cached
                com.appstock.app_stock.domain.repository.SessionManager.addViewedProduct(cached)
            } else {
                val fromFlow = repository.getProducts().first().firstOrNull { it.id == productId }
                _selectedProduct.value = fromFlow
                if (fromFlow != null) {
                    com.appstock.app_stock.domain.repository.SessionManager.addViewedProduct(fromFlow)
                }
            }
        }
    }

    fun clearSelectedProduct() {
        _selectedProduct.value = null
    }

    // ── Subida de imagen via ImgBB ────────────────────────────────────────────

    private suspend fun uploadImageIfNeeded(imageUri: Uri?): Result<String?> {
        if (imageUri == null) return Result.success(null)

        if (!ImageUploadService.isConfigured()) {
            return Result.failure(
                Exception("ImgBB no configurado. Ingresá tu API key en ImageUploadService.kt")
            )
        }

        return ImageUploadService.uploadImage(getApplication(), imageUri)
            .map { url -> url } // Result<String> → Result<String?>
    }

    // ── Guardar nuevo producto ────────────────────────────────────────────────

    fun saveProduct(product: ProductDetail, sizes: List<ProductSize>, imageUri: Uri?) {
        viewModelScope.launch {
            _isSaving.value = true
            _saveError.value = null
            _saveSuccess.value = false

            // Subir imagen si hay una seleccionada
            var finalImageUrl = product.imagenUrl
            if (imageUri != null) {
                val uploadResult = uploadImageIfNeeded(imageUri)
                if (uploadResult.isFailure) {
                    _saveError.value = uploadResult.exceptionOrNull()?.message ?: "Error al subir imagen"
                    _isSaving.value = false
                    return@launch
                }
                finalImageUrl = uploadResult.getOrNull() ?: finalImageUrl
            }

            val finalProduct = product.copy(imagenUrl = finalImageUrl)
            val productResult = repository.addProduct(finalProduct)
            if (productResult.isFailure) {
                _saveError.value = productResult.exceptionOrNull()?.message ?: "Error al guardar el producto"
                _isSaving.value = false
                return@launch
            }

            // Guardar talles con el id devuelto por el repositorio
            val productId = productResult.getOrNull()
            val sizesResult = if (sizes.isNotEmpty() && productId != null) {
                try {
                    sizeRepository.saveProductSizes(productId, sizes)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            } else Result.success(Unit)

            if (sizesResult.isFailure) {
                _saveError.value = sizesResult.exceptionOrNull()?.message ?: "Producto guardado, pero error en talles"
            } else {
                _saveSuccess.value = true
            }
            _isSaving.value = false
        }
    }

    // ── Actualizar producto existente ─────────────────────────────────────────

    fun updateProduct(product: ProductDetail, sizes: List<ProductSize>, imageUri: Uri?) {
        viewModelScope.launch {
            _isUpdating.value = true
            _updateError.value = null
            _updateSuccess.value = false

            // Subir imagen nueva si fue seleccionada
            var finalImageUrl = product.imagenUrl
            if (imageUri != null) {
                val uploadResult = uploadImageIfNeeded(imageUri)
                if (uploadResult.isFailure) {
                    _updateError.value = uploadResult.exceptionOrNull()?.message ?: "Error al subir imagen"
                    _isUpdating.value = false
                    return@launch
                }
                finalImageUrl = uploadResult.getOrNull() ?: finalImageUrl
            }

            val finalProduct = product.copy(imagenUrl = finalImageUrl)
            val result = repository.updateProduct(finalProduct)
            if (result.isSuccess) {
                if (sizes.isNotEmpty()) {
                    sizeRepository.saveProductSizes(product.id, sizes)
                }
                _updateSuccess.value = true
                _selectedProduct.value = finalProduct
            } else {
                _updateError.value = result.exceptionOrNull()?.message ?: "Error al actualizar el producto"
            }
            _isUpdating.value = false
        }
    }

    fun resetSaveState() {
        _saveSuccess.value = false
        _saveError.value = null
    }

    fun resetUpdateState() {
        _updateSuccess.value = false
        _updateError.value = null
    }

    fun deleteProduct(id: String) {
        viewModelScope.launch {
            _deleteError.value = null
            val result = repository.deleteProduct(id)
            if (result.isSuccess) {
                _deleteSuccess.value = true
            } else {
                _deleteError.value = result.exceptionOrNull()?.message ?: "Error al eliminar el producto"
            }
        }
    }

    fun resetDeleteState() {
        _deleteSuccess.value = false
        _deleteError.value = null
    }
}
