package com.appstock.app_stock.domain.model

import java.util.Date

enum class MovementType {
    ENTRADA, SALIDA, AJUSTE
}

data class InventoryMovement(
    val id: String = "",
    val productId: String = "",
    val productName: String = "",
    val sizeId: Int = 0,
    val sizeName: String = "",
    val quantity: Int = 0,
    val type: MovementType = MovementType.ENTRADA,
    val reason: String = "",
    val timestamp: Long = Date().time,
    val userId: String = ""
)
