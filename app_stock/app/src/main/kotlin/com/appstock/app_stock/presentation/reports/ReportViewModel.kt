package com.appstock.app_stock.presentation.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appstock.app_stock.data.repository.ReportRepositoryImpl
import com.appstock.app_stock.domain.model.FullInventoryReport
import com.appstock.app_stock.domain.model.InventoryValueReport
import com.appstock.app_stock.domain.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReportViewModel(
    private val repository: ReportRepository = ReportRepositoryImpl()
) : ViewModel() {

    private val _report = MutableStateFlow<FullInventoryReport?>(null)
    val report: StateFlow<FullInventoryReport?> = _report

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        viewModelScope.launch {
            repository.getFullInventoryReport().collect {
                _report.value = it
                _isLoading.value = false
            }
        }
    }
}
