package com.appstock.app_stock.data.repository

import com.appstock.app_stock.domain.model.InventoryMovement
import com.appstock.app_stock.domain.model.MovementType
import com.appstock.app_stock.domain.repository.InventoryRepository
import com.appstock.app_stock.domain.repository.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import android.util.Log

class InventoryRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : InventoryRepository {

    private fun productsCollection() = firestore
        .collection("stores")
        .document(SessionManager.getStoreIdOrNull() ?: throw IllegalStateException("No hay un storeId en sesión."))
        .collection("products")

    private fun historyCollection() = firestore
        .collection("stores")
        .document(SessionManager.getStoreIdOrNull() ?: throw IllegalStateException("No hay un storeId en sesión."))
        .collection("inventory_history")

    override fun getMovements(productId: String): Flow<List<InventoryMovement>> = callbackFlow {
        val subscription = historyCollection()
            .whereEqualTo("productId", productId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("InventoryRepo", "Error escuchando movimientos", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val movements = snapshot.toObjects(InventoryMovement::class.java)
                    trySend(movements)
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun registerMovement(movement: InventoryMovement): Result<Unit> = try {
        require(movement.quantity > 0) { "La cantidad debe ser mayor a 0." }
        firestore.runTransaction { transaction ->
            // 1. Referencias
            val productRef = productsCollection().document(movement.productId)
            val sizeRef = productRef.collection("sizes").document(movement.sizeId.toString())
            val historyRef = historyCollection().document()

            // 2. Obtener stock actual del talle
            val sizeSnap = transaction.get(sizeRef)
            val currentStock = sizeSnap.getLong("stock") ?: 0L
            
            // 3. Calcular nuevo stock
            val delta = if (movement.type == MovementType.SALIDA) -movement.quantity else movement.quantity
            val newStock = currentStock + delta
            
            if (newStock < 0) throw Exception("Stock insuficiente")

            // 4. Actualizar stock del talle
            transaction.update(sizeRef, "stock", newStock)
            
            // 5. Actualizar stock total del producto (denormalizado para Dashboard)
            val productSnap = transaction.get(productRef)
            val totalStock = productSnap.getLong("stock") ?: 0L
            transaction.update(productRef, "stock", totalStock + delta)

            // 6. Registrar en historial
            transaction.set(historyRef, movement.copy(id = historyRef.id))
        }.await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
