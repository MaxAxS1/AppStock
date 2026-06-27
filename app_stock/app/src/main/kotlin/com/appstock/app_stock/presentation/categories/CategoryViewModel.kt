package com.appstock.app_stock.presentation.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appstock.app_stock.data.repository.CategoryRepositoryImpl
import com.appstock.app_stock.domain.model.Category
import com.appstock.app_stock.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CategoryViewModel(
    private val repository: CategoryRepository = CategoryRepositoryImpl()
) : ViewModel() {

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

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
