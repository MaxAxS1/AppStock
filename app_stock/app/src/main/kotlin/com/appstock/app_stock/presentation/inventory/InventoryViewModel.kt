package com.appstock.app_stock.presentation.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appstock.app_stock.data.repository.InventoryRepositoryImpl
import com.appstock.app_stock.domain.model.InventoryMovement
import com.appstock.app_stock.domain.model.MovementType
import com.appstock.app_stock.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InventoryViewModel(
    private val repository: InventoryRepository = InventoryRepositoryImpl(),
    private val productId: String
) : ViewModel() {

    private val _movements = MutableStateFlow<List<InventoryMovement>>(emptyList())
    val movements: StateFlow<List<InventoryMovement>> = _movements

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        viewModelScope.launch {
            repository.getMovements(productId).collect {
                _movements.value = it
            }
        }
    }

    fun registerMovement(
        sizeId: Int, 
        sizeName: String, 
        quantity: Int, 
        type: MovementType,
        productName: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val movement = InventoryMovement(
                productId = productId,
                productName = productName,
                sizeId = sizeId,
                sizeName = sizeName,
                quantity = quantity,
                type = type
            )
            repository.registerMovement(movement)
            _isLoading.value = false
        }
    }
}
