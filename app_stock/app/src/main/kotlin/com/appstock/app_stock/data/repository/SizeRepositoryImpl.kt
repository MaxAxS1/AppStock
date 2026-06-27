package com.appstock.app_stock.data.repository

import com.appstock.app_stock.domain.model.ProductSize
import com.appstock.app_stock.domain.model.SizeTable
import com.appstock.app_stock.domain.repository.SessionManager
import com.appstock.app_stock.domain.repository.SizeRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class SizeRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : SizeRepository {

    private fun getProductsCollection() = firestore
        .collection("stores")
        .document(SessionManager.getStoreId())
        .collection("products")

    override fun getProductSizes(productId: String): Flow<List<ProductSize>> = callbackFlow {
        val subscription = getProductsCollection().document(productId)
            .collection("sizes")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val sizes = snapshot.toObjects(ProductSize::class.java)
                    trySend(sizes.sortedBy { it.id })
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun updateSizeStock(productId: String, sizeId: Int, newStock: Int): Result<Unit> = try {
        getProductsCollection().document(productId)
            .collection("sizes")
            .document(sizeId.toString())
            .update("stock", newStock)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun initializeDefaultSizes(productId: String): Result<Unit> = try {
        val sizesBatch = firestore.batch()
        SizeTable.defaultSizes.forEach { size ->
            val sizeDoc = getProductsCollection().document(productId)
                .collection("sizes")
                .document(size.id.toString())
            sizesBatch.set(sizeDoc, size)
        }
        sizesBatch.commit().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun saveProductSizes(productId: String, sizes: List<ProductSize>): Result<Unit> = try {
        val batch = firestore.batch()
        sizes.forEach { size ->
            val sizeDoc = getProductsCollection().document(productId)
                .collection("sizes")
                .document(size.id.toString())
            batch.set(sizeDoc, size)
        }
        batch.commit().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

