package com.appstock.app_stock.data.repository

import com.appstock.app_stock.domain.model.*
import com.appstock.app_stock.domain.repository.ReportRepository
import com.appstock.app_stock.domain.repository.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.text.SimpleDateFormat
import java.util.*

class ReportRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ReportRepository {

    private fun getProductsCollection() = firestore
        .collection("stores")
        .document(SessionManager.getStoreIdOrNull() ?: throw IllegalStateException("No hay un storeId en sesión."))
        .collection("products")

    override fun getFullInventoryReport(): Flow<FullInventoryReport> = callbackFlow {
        val subscription = getProductsCollection()
            .limit(500)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("ReportRepo", "Error escuchando reporte", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val products = snapshot.toObjects(ProductDetail::class.java)
                    val dayFmt = SimpleDateFormat("dd/MM", Locale.getDefault())

                    // ── 1. Valor del inventario ────────────────────────────────
                    var totalCost = 0.0
                    var totalSale = 0.0
                    products.forEach {
                        totalCost += it.precioCosto * it.stock
                        totalSale += it.precioVenta * it.stock
                    }

                    // ── 2. Stock por categoría ─────────────────────────────────
                    val catGroups = products.groupBy { it.categoriaId }
                    val categoryReports = catGroups.map { (id, prods) ->
                        CategoryReport(
                            categoryName = id.ifBlank { "Sin categoría" },
                            totalStock = prods.sumOf { it.stock },
                            productCount = prods.size
                        )
                    }.sortedByDescending { it.totalStock }

                    // ── 3. Productos agotados ──────────────────────────────────
                    val outOfStock = products
                        .filter { it.stock == 0 }
                        .map { it.nombre }

                    // ── 4. Top productos por valor de stock ────────────────────
                    //    (precio venta × unidades = valor representado en inventario)
                    val topProducts = products
                        .filter { it.stock > 0 }
                        .map { p ->
                            TopProduct(
                                nombre      = p.nombre,
                                marca       = p.marca,
                                stockValue  = p.precioVenta * p.stock,
                                stock       = p.stock,
                                precioVenta = p.precioVenta
                            )
                        }
                        .sortedByDescending { it.stockValue }
                        .take(5)

                    // ── 5. Ganancias diarias potenciales ───────────────────────
                    //    Agrupamos por día de creación (createdAt timestamp)
                    //    Ganancia por producto = (precioVenta - precioCosto) × stock
                    val dailyProfits = products
                        .filter { it.createdAt > 0 }
                        .groupBy { dayFmt.format(Date(it.createdAt)) }
                        .map { (date, prods) ->
                            DailyProfit(
                                date   = date,
                                profit = prods.sumOf {
                                    (it.precioVenta - it.precioCosto) * it.stock
                                }
                            )
                        }
                        .sortedByDescending { it.profit }
                        .take(7) // últimos 7 días con actividad

                    trySend(
                        FullInventoryReport(
                            categoryReports  = categoryReports,
                            valueReport      = InventoryValueReport(
                                totalCostValue  = totalCost,
                                totalSaleValue  = totalSale,
                                potentialProfit = totalSale - totalCost
                            ),
                            outOfStockProducts = outOfStock,
                            topProducts        = topProducts,
                            dailyProfits       = dailyProfits
                        )
                    )
                }
            }
        awaitClose { subscription.remove() }
    }
}
