package com.appstock.app_stock.domain.model

data class CategoryReport(
    val categoryName: String,
    val totalStock: Int,
    val productCount: Int
)

data class InventoryValueReport(
    val totalCostValue: Double,
    val totalSaleValue: Double,
    val potentialProfit: Double
)

// Producto destacado por valor de stock (precio venta × unidades)
data class TopProduct(
    val nombre: String,
    val marca: String,
    val stockValue: Double,   // precioVenta × stock
    val stock: Int,
    val precioVenta: Double
)

// Ganancia potencial agrupada por día de creación del producto
data class DailyProfit(
    val date: String,         // "dd/MM" formateado
    val profit: Double        // suma de (precioVenta - precioCosto) × stock de ese día
)

data class FullInventoryReport(
    val categoryReports: List<CategoryReport> = emptyList(),
    val valueReport: InventoryValueReport = InventoryValueReport(0.0, 0.0, 0.0),
    val outOfStockProducts: List<String> = emptyList(),
    val topProducts: List<TopProduct> = emptyList(),
    val dailyProfits: List<DailyProfit> = emptyList()
)
