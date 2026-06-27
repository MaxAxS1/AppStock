package com.appstock.app_stock.domain.repository

import com.appstock.app_stock.domain.model.ProductSize
import kotlinx.coroutines.flow.Flow

interface SizeRepository {
    fun getProductSizes(productId: String): Flow<List<ProductSize>>
    suspend fun updateSizeStock(productId: String, sizeId: Int, newStock: Int): Result<Unit>
    suspend fun initializeDefaultSizes(productId: String): Result<Unit>
    suspend fun saveProductSizes(productId: String, sizes: List<ProductSize>): Result<Unit>
}
