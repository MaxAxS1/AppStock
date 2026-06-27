package com.appstock.app_stock.domain.model

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String? = null,
    val nombre: String = "",
    val apellido: String = "",
    val username: String = "",
    val storeId: String = "",
    val role: String = "owner" // "owner" o "employee"
)
