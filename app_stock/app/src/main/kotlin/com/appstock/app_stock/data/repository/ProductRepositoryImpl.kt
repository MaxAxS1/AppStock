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
import android.util.Log

class ProductRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ProductRepository {

    private fun getCollection() = firestore
        .collection("stores")
        .document(SessionManager.getStoreIdOrNull() ?: throw IllegalStateException("No hay un storeId en sesión."))
        .collection("products")

    override fun getProducts(): Flow<List<ProductDetail>> = callbackFlow {
        val subscription = getCollection().orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ProductRepo", "Error escuchando productos", error)
                    trySend(emptyList())
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
            .orderBy("nombre")
            .limit(20)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ProductRepo", "Error buscando productos", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val products = snapshot.toObjects(ProductDetail::class.java)
                    trySend(products)
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun addProduct(product: ProductDetail): Result<String> = try {
        require(product.nombre.isNotBlank()) { "El nombre del producto no puede estar vacío." }
        require(product.precioCosto >= 0) { "El precio de costo no puede ser negativo." }
        require(product.precioVenta >= 0) { "El precio de venta no puede ser negativo." }
        val doc = getCollection().document()
        doc.set(product.copy(id = doc.id)).await()
        Result.success(doc.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateProduct(product: ProductDetail): Result<Unit> = try {
        require(product.id.isNotBlank()) { "El id del producto no puede estar vacío." }
        require(product.nombre.isNotBlank()) { "El nombre del producto no puede estar vacío." }
        require(product.precioCosto >= 0) { "El precio de costo no puede ser negativo." }
        require(product.precioVenta >= 0) { "El precio de venta no puede ser negativo." }
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
