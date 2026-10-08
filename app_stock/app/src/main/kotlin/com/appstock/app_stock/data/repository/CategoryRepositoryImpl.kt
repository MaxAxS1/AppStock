package com.appstock.app_stock.data.repository

import com.appstock.app_stock.domain.model.Category
import com.appstock.app_stock.domain.model.Subcategory
import com.appstock.app_stock.domain.repository.CategoryRepository
import com.appstock.app_stock.domain.repository.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CategoryRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : CategoryRepository {

    private fun getCollection() = firestore
        .collection("stores")
        .document(SessionManager.getStoreIdOrNull() ?: throw IllegalStateException("No hay un storeId en sesión."))
        .collection("categories")

    override fun getCategories(): Flow<List<Category>> = callbackFlow {
        val subscription = getCollection().limit(200).addSnapshotListener { snapshot, error ->
            if (error != null) {
                android.util.Log.e("CategoryRepo", "Error escuchando categorías", error)
                trySend(emptyList())
                return@addSnapshotListener
            }
            if (snapshot != null) {
                // Parseamos manualmente para resolver la limitación de Firestore
                // con List<CustomObject> anidados y generics de Kotlin.
                val categories = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null

                    @Suppress("UNCHECKED_CAST")
                    val rawSubs = data["subcategories"] as? List<Map<String, Any?>>
                    val subcategories = rawSubs?.map { map ->
                        Subcategory(
                            id   = map["id"]   as? String ?: "",
                            name = map["name"] as? String ?: ""
                        )
                    } ?: emptyList()

                    Category(
                        id            = doc.id,
                        name          = data["name"]        as? String ?: "",
                        description   = data["description"] as? String ?: "",
                        subcategories = subcategories
                    )
                }
                trySend(categories)
            }
        }
        awaitClose { subscription.remove() }
    }

    override suspend fun addCategory(category: Category): Result<Unit> = try {
        require(category.name.isNotBlank()) { "El nombre de la categoría es obligatorio." }
        val doc = getCollection().document()
        doc.set(category.copy(id = doc.id)).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateCategory(category: Category): Result<Unit> = try {
        require(category.id.isNotBlank()) { "ID de categoría vacío." }
        require(category.name.isNotBlank()) { "El nombre de la categoría es obligatorio." }
        getCollection().document(category.id).set(category).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteCategory(categoryId: String): Result<Unit> = try {
        getCollection().document(categoryId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
