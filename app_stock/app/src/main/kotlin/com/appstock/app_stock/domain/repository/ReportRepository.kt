package com.appstock.app_stock.domain.repository

import com.appstock.app_stock.domain.model.FullInventoryReport
import kotlinx.coroutines.flow.Flow

interface ReportRepository {
    fun getFullInventoryReport(): Flow<FullInventoryReport>
}
