package com.appstock.app_stock.domain.repository

import com.appstock.app_stock.domain.model.InventoryStats
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    fun getInventoryStats(): Flow<InventoryStats>
}
