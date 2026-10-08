package com.appstock.app_stock.domain.model

/**
 * Representa una variante de talle para un producto específico.
 * Sigue la tabla de talles definida en DATA_MODEL.md
 */
data class ProductSize(
    val id: Int = 0,
    val name: String = "", // XS/S, S/M, etc.
    val stock: Int = 0,
    val minStock: Int = 0
)

data class ProductWithSizes(
    val productId: String,
    val sizes: List<ProductSize> = emptyList()
)

object SizeTable {
    val defaultSizes = listOf(
        ProductSize(1, "XS"),
        ProductSize(2, "S"),
        ProductSize(3, "M"),
        ProductSize(4, "L"),
        ProductSize(5, "XL"),
        ProductSize(6, "XXL")
    )
}
