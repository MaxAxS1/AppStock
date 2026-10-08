package com.appstock.app_stock.domain.repository

import com.appstock.app_stock.domain.model.ProductDetail
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getProducts(): Flow<List<ProductDetail>>
    fun searchProducts(query: String): Flow<List<ProductDetail>>
    suspend fun addProduct(product: ProductDetail): Result<String>
    suspend fun updateProduct(product: ProductDetail): Result<Unit>
    suspend fun deleteProduct(productId: String): Result<Unit>
}
