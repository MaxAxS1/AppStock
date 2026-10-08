package com.appstock.app_stock.data.service

import com.appstock.app_stock.domain.model.*
import com.appstock.app_stock.domain.repository.SessionManager
import com.appstock.app_stock.domain.service.AIService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import android.util.Log

class AIServiceImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AIService {

    companion object {
        const val LOW_STOCK = 10
        const val CRITICAL = 0
        const val MEDIUM_STOCK = 3
        const val RESTOCK_CRITICAL = 20
        const val RESTOCK_MEDIUM = 15
        const val RESTOCK_LOW = 10
    }

    private fun productsCollection() = firestore
        .collection("stores")
        .document(SessionManager.getStoreIdOrNull() ?: throw IllegalStateException("No hay un storeId en sesión."))
        .collection("products")

    override fun getSmartRecommendations(): Flow<List<AIRecommendation>> = callbackFlow {
        val subscription = productsCollection()
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("AIService", "Error escuchando productos", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val products = snapshot.toObjects(ProductDetail::class.java)
                    
                    // Lógica de "IA" / Heurística:
                    // 1. Identificar productos con bajo stock.
                    // 2. Simular análisis de ventas (en prod usaríamos historial real).
                    // 3. Generar insights conversacionales.
                    
                    val recommendations = products.filter { it.stock < LOW_STOCK }.map { product ->
                        val (priority, restockQty, insight) = when {
                            product.stock == CRITICAL -> Triple(
                                RecommendationPriority.CRITICAL,
                                RESTOCK_CRITICAL,
                                "El producto se encuentra agotado. Basado en la demanda histórica, se recomienda reponer 20 unidades de forma urgente."
                            )
                            product.stock <= MEDIUM_STOCK -> Triple(
                                RecommendationPriority.MEDIUM,
                                RESTOCK_MEDIUM,
                                "Quedan solo unidades. Se estima quiebre de stock en 4 días si no se repone pronto."
                            )
                            else -> Triple(
                                RecommendationPriority.LOW,
                                RESTOCK_LOW,
                                "Stock preventivo: Reponer 10 unidades para mantener niveles óptimos."
                            )
                        }

                        AIRecommendation(
                            productId = product.id,
                            productName = product.nombre,
                            currentStock = product.stock,
                            suggestedRestock = restockQty,
                            priority = priority,
                            insight = insight
                        )
                    }
                    trySend(recommendations.sortedBy { it.priority })
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun analyzeTrends(): Result<Unit> {
        // Simulación de procesamiento de datos complejos
        kotlinx.coroutines.delay(2000)
        return Result.success(Unit)
    }
}
