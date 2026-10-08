package com.appstock.app_stock.data.repository

import com.appstock.app_stock.domain.model.InventoryStats
import com.appstock.app_stock.domain.model.Product
import com.appstock.app_stock.domain.repository.DashboardRepository
import com.appstock.app_stock.domain.repository.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class DashboardRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : DashboardRepository {

    private fun getProductsCollection() = firestore
        .collection("stores")
        .document(SessionManager.getStoreIdOrNull() ?: throw IllegalStateException("No hay un storeId en sesión."))
        .collection("products")

    companion object {
        const val LOW_STOCK_MAX = 5
        const val QUERY_LIMIT = 200L
    }

    override fun getInventoryStats(): Flow<InventoryStats> = callbackFlow {
        val subscription = getProductsCollection()
            .limit(QUERY_LIMIT)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("DashboardRepo", "Error escuchando stats", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val products = snapshot.toObjects(Product::class.java)
                    val stats = InventoryStats(
                        totalProducts = products.size,
                        lowStockCount = products.count { it.stock in 1..LOW_STOCK_MAX },
                        outOfStockCount = products.count { it.stock == 0 },
                        totalCategories = products.map { it.categoriaId }.distinct().size,
                        recentProducts = products.take(5)
                    )
                    trySend(stats)
                }
            }
        awaitClose { subscription.remove() }
    }
}
