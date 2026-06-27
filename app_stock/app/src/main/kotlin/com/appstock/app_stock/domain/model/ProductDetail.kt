package com.appstock.app_stock.domain.model

import java.util.Date

data class ProductDetail(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val categoriaId: String = "",
    val subcategoriaId: String = "",
    val marca: String = "",
    val precioCosto: Double = 0.0,
    val precioVenta: Double = 0.0,
    val codigoInterno: String = "",
    val codigoBarras: String = "",
    val imagenUrl: String = "",
    val estado: String = "disponible", // disponible | agotado | descontinuado
    val createdAt: Long = Date().time,
    val stock: Int = 0,
    val colores: List<String> = emptyList()
)
