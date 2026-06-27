package com.appstock.app_stock.data

import com.appstock.app_stock.domain.model.Category
import com.appstock.app_stock.domain.model.Subcategory
import com.appstock.app_stock.domain.repository.CategoryRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Inicializa las categorías predefinidas en Firestore si la colección está vacía.
 * Se ejecuta una sola vez por instalación (o cuando el usuario limpia datos).
 */
object CategorySeeder {

    private val initialCategories = listOf(
        // ——— Categorías Iniciales ———
        Category(
            id = "prendas_vestir",
            name = "Prendas de Vestir",
            description = "Indumentaria general",
            subcategories = listOf(
                Subcategory(id = "remeras", name = "Remeras"),
                Subcategory(id = "camisas", name = "Camisas"),
                Subcategory(id = "pantalones", name = "Pantalones"),
                Subcategory(id = "buzos", name = "Buzos"),
                Subcategory(id = "camperas", name = "Camperas"),
                Subcategory(id = "vestidos", name = "Vestidos"),
                Subcategory(id = "ropa_deportiva", name = "Ropa deportiva")
            )
        ),
        // ——— Categorías Futuras ———
        Category(
            id = "calzado",
            name = "Calzado",
            description = "Zapatos, botas y sandalias",
            subcategories = listOf(
                Subcategory(id = "zapatillas", name = "Zapatillas"),
                Subcategory(id = "botas", name = "Botas"),
                Subcategory(id = "sandalias", name = "Sandalias")
            )
        ),
        Category(
            id = "accesorios",
            name = "Accesorios",
            description = "Bolsos, gorras y complementos",
            subcategories = listOf(
                Subcategory(id = "mochilas", name = "Mochilas"),
                Subcategory(id = "gorras", name = "Gorras"),
                Subcategory(id = "cinturones", name = "Cinturones"),
                Subcategory(id = "carteras", name = "Carteras")
            )
        )
    )

    /**
     * Siembra las categorías si Firestore no tiene ninguna todavía.
     * Usa IDs fijos para que sea idempotente (se puede llamar varias veces sin duplicar).
     */
    suspend fun seedIfEmpty() {
        val storeId = try {
            com.appstock.app_stock.domain.repository.SessionManager.getStoreId()
        } catch (e: Exception) {
            return // Si no hay sesión, no sembramos nada aún
        }

        val firestore = FirebaseFirestore.getInstance()
        val collection = firestore
            .collection("stores")
            .document(storeId)
            .collection("categories")

        val snapshot = collection.limit(1).get().await()
        if (!snapshot.isEmpty) return  // Ya hay datos — no hacer nada

        val batch = firestore.batch()
        initialCategories.forEach { category ->
            val doc = collection.document(category.id)
            batch.set(doc, category)
        }
        batch.commit().await()
    }
}
