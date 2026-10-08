package com.appstock.app_stock.presentation.sizes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.appstock.app_stock.data.repository.SizeRepositoryImpl
import com.appstock.app_stock.domain.model.ProductSize
import com.appstock.app_stock.domain.repository.SizeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SizeViewModel(
    private val repository: SizeRepository = SizeRepositoryImpl(),
    private val productId: String
) : ViewModel() {

    private val _sizes = MutableStateFlow<List<ProductSize>>(emptyList())
    val sizes: StateFlow<List<ProductSize>> = _sizes

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadSizes()
    }

    private fun loadSizes() {
        viewModelScope.launch {
            try {
                repository.getProductSizes(productId).collect {
                    if (it.isEmpty()) {
                        repository.initializeDefaultSizes(productId)
                    } else {
                        _sizes.value = it
                        _isLoading.value = false
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateStock(sizeId: Int, currentStock: Int, delta: Int) {
        viewModelScope.launch {
            val newStock = (currentStock + delta).coerceAtLeast(0)
            repository.updateSizeStock(productId, sizeId, newStock)
        }
    }

    class Factory(private val productId: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SizeViewModel(productId = productId) as T
        }
    }
}
