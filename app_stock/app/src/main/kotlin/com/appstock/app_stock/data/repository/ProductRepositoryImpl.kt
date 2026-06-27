package com.appstock.app_stock.data.repository

import com.appstock.app_stock.domain.model.ProductDetail
import com.appstock.app_stock.domain.repository.ProductRepository
import com.appstock.app_stock.domain.repository.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ProductRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ProductRepository {

    private fun getCollection() = firestore
        .collection("stores")
        .document(SessionManager.getStoreId())
        .collection("products")

    override fun getProducts(): Flow<List<ProductDetail>> = callbackFlow {
        val subscription = getCollection().orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val products = snapshot.toObjects(ProductDetail::class.java)
                    trySend(products)
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun searchProducts(query: String): Flow<List<ProductDetail>> = callbackFlow {
        val subscription = getCollection().whereGreaterThanOrEqualTo("nombre", query)
            .whereLessThanOrEqualTo("nombre", query + "\uf8ff")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val products = snapshot.toObjects(ProductDetail::class.java)
                    trySend(products)
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun addProduct(product: ProductDetail): Result<Unit> = try {
        val doc = getCollection().document()
        getCollection().document(doc.id).set(product.copy(id = doc.id)).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateProduct(product: ProductDetail): Result<Unit> = try {
        getCollection().document(product.id).set(product).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteProduct(productId: String): Result<Unit> = try {
        getCollection().document(productId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
