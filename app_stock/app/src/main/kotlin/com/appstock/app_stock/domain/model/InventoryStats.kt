package com.appstock.app_stock.domain.model

data class InventoryStats(
    val totalProducts: Int = 0,
    val lowStockCount: Int = 0,
    val outOfStockCount: Int = 0,
    val totalCategories: Int = 0,
    val recentProducts: List<Product> = emptyList()
)

data class Product(
    val id: String = "",
    val nombre: String = "",
    val categoriaId: String = "",
    val stock: Int = 0,
    val precioVenta: Double = 0.0
)
