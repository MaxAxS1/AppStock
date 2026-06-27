package com.appstock.app_stock.data.service

import com.appstock.app_stock.domain.model.*
import com.appstock.app_stock.domain.service.AIService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AIServiceImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AIService {

    override fun getSmartRecommendations(): Flow<List<AIRecommendation>> = callbackFlow {
        val subscription = firestore.collection("products")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val products = snapshot.toObjects(ProductDetail::class.java)
                    
                    // Lógica de "IA" / Heurística:
                    // 1. Identificar productos con bajo stock.
                    // 2. Simular análisis de ventas (en prod usaríamos historial real).
                    // 3. Generar insights conversacionales.
                    
                    val recommendations = products.filter { it.stock < 10 }.map { product ->
                        val (priority, restockQty, insight) = when {
                            product.stock == 0 -> Triple(
                                RecommendationPriority.CRITICAL, 
                                20, 
                                "El producto se encuentra agotado. Basado en la demanda histórica, se recomienda reponer 20 unidades de forma urgente."
                            )
                            product.stock <= 3 -> Triple(
                                RecommendationPriority.MEDIUM, 
                                15, 
                                "Quedan solo unidades. Se estima quiebre de stock en 4 días si no se repone pronto."
                            )
                            else -> Triple(
                                RecommendationPriority.LOW, 
                                10, 
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
