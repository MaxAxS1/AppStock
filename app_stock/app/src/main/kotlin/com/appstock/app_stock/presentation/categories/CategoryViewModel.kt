package com.appstock.app_stock.presentation.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appstock.app_stock.data.repository.CategoryRepositoryImpl
import com.appstock.app_stock.data.repository.ProductRepositoryImpl
import com.appstock.app_stock.domain.model.Category
import com.appstock.app_stock.domain.model.ProductDetail
import com.appstock.app_stock.domain.repository.CategoryRepository
import com.appstock.app_stock.domain.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CategoryViewModel(
    private val repository: CategoryRepository = CategoryRepositoryImpl(),
    private val productRepository: ProductRepository = ProductRepositoryImpl()
) : ViewModel() {

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Productos filtrados por la categoría seleccionada
    private val _categoryProducts = MutableStateFlow<List<ProductDetail>>(emptyList())
    val categoryProducts: StateFlow<List<ProductDetail>> = _categoryProducts

    private val _isLoadingProducts = MutableStateFlow(false)
    val isLoadingProducts: StateFlow<Boolean> = _isLoadingProducts

    init {
        viewModelScope.launch {
            // Inicializa las categorías predefinidas si Firestore está vacío
            com.appstock.app_stock.data.CategorySeeder.seedIfEmpty()
            repository.getCategories().collect {
                _categories.value = it
                _isLoading.value = false
            }
        }
    }

    fun loadProductsByCategory(categoryId: String) {
        viewModelScope.launch {
            _isLoadingProducts.value = true
            productRepository.getProducts().collect { allProducts ->
                _categoryProducts.value = allProducts.filter { it.categoriaId == categoryId }
                _isLoadingProducts.value = false
            }
        }
    }

    fun addCategory(name: String, description: String) {
        viewModelScope.launch {
            repository.addCategory(Category(name = name, description = description))
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            repository.deleteCategory(id)
        }
    }
}
