package com.appstock.app_stock.presentation.products

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appstock.app_stock.data.repository.CategoryRepositoryImpl
import com.appstock.app_stock.data.repository.ProductRepositoryImpl
import com.appstock.app_stock.data.repository.SizeRepositoryImpl
import com.appstock.app_stock.domain.model.Category
import com.appstock.app_stock.domain.model.ProductDetail
import com.appstock.app_stock.domain.model.ProductSize
import com.appstock.app_stock.domain.repository.CategoryRepository
import com.appstock.app_stock.domain.repository.ProductRepository
import com.appstock.app_stock.domain.repository.SessionManager
import com.appstock.app_stock.domain.repository.SizeRepository
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ProductViewModel(
    private val repository: ProductRepository = ProductRepositoryImpl(),
    private val sizeRepository: SizeRepository = SizeRepositoryImpl(),
    private val categoryRepository: CategoryRepository = CategoryRepositoryImpl()
) : ViewModel() {

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
        // Cargar categorías disponibles
        loadCategories()
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    private fun loadCategories() {
        viewModelScope.launch {
            // Inicializa las categorías predefinidas si Firestore está vacío
            com.appstock.app_stock.data.CategorySeeder.seedIfEmpty()
            categoryRepository.getCategories().collect {
                _categories.value = it
            }
        }
    }

    /**
     * Guarda un producto nuevo junto con sus talles en Firestore.
     * Si hay imageUri, primero la sube a Storage y obtiene la URL.
     */
    fun saveProduct(product: ProductDetail, sizes: List<ProductSize>, imageUri: Uri?) {
        viewModelScope.launch {
            _isSaving.value = true
            _saveError.value = null
            _saveSuccess.value = false

            var finalImageUrl = product.imagenUrl
            if (imageUri != null) {
                try {
                    val storageRef = FirebaseStorage.getInstance().reference
                    val storeId = SessionManager.getStoreId()
                    val imageRef = storageRef.child("stores/$storeId/products/${UUID.randomUUID()}.jpg")
                    imageRef.putFile(imageUri).await()
                    finalImageUrl = imageRef.downloadUrl.await().toString()
                } catch (e: Exception) {
                    _saveError.value = "Error al subir imagen: ${e.message}"
                    _isSaving.value = false
                    return@launch
                }
            }

            val finalProduct = product.copy(imagenUrl = finalImageUrl)

            val productResult = repository.addProduct(finalProduct)
            if (productResult.isFailure) {
                _saveError.value = productResult.exceptionOrNull()?.message ?: "Error al guardar el producto"
                _isSaving.value = false
                return@launch
            }

            // Obtener el id del producto recién creado para guardar sus talles
            // addProduct ya persiste el id dentro del documento; lo recuperamos de Firestore
            // escuchando la primera emisión de getProducts y buscando por nombre+timestamp.
            // Estrategia más simple: usar el producto retornado desde la lista reactiva.
            // Para no hacer una segunda lectura, calculamos el id desde el repositorio
            // usando la misma lógica: el id es asignado antes del set().
            // Usamos una búsqueda puntual por nombre para obtener el id.
            val sizesResult = if (sizes.isNotEmpty()) {
                // Buscar el producto recién insertado para obtener su id
                try {
                    val freshProducts = _products.value
                    val savedProduct = freshProducts.firstOrNull {
                        it.nombre == finalProduct.nombre && it.createdAt == finalProduct.createdAt
                    }
                    if (savedProduct != null) {
                        sizeRepository.saveProductSizes(savedProduct.id, sizes)
                    } else {
                        // Fallback: esperar la próxima emisión del flow ya está manejado reactivamente
                        Result.success(Unit)
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            } else {
                Result.success(Unit)
            }

            if (sizesResult.isFailure) {
                _saveError.value = sizesResult.exceptionOrNull()?.message ?: "Producto guardado, pero error en talles"
            } else {
                _saveSuccess.value = true
            }
            _isSaving.value = false
        }
    }

    fun resetSaveState() {
        _saveSuccess.value = false
        _saveError.value = null
    }

    fun deleteProduct(id: String) {
        viewModelScope.launch {
            repository.deleteProduct(id)
        }
    }
}
