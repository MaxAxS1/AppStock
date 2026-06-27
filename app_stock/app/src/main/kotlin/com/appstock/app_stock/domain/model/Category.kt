package com.appstock.app_stock.domain.model

data class Category(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val subcategories: List<Subcategory> = emptyList()
)

data class Subcategory(
    val id: String = "",
    val name: String = ""
)
