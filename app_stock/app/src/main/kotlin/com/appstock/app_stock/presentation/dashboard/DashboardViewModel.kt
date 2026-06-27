package com.appstock.app_stock.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appstock.app_stock.data.repository.DashboardRepositoryImpl
import com.appstock.app_stock.domain.model.InventoryStats
import com.appstock.app_stock.domain.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: DashboardRepository = DashboardRepositoryImpl()
) : ViewModel() {

    private val _stats = MutableStateFlow(InventoryStats())
    val stats: StateFlow<InventoryStats> = _stats

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        viewModelScope.launch {
            repository.getInventoryStats().collect {
                _stats.value = it
                _isLoading.value = false
            }
        }
    }
}
