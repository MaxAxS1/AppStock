package com.appstock.app_stock.domain.repository

import com.appstock.app_stock.domain.model.InventoryMovement
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {
    fun getMovements(productId: String): Flow<List<InventoryMovement>>
    suspend fun registerMovement(movement: InventoryMovement): Result<Unit>
}
