package com.appstock.app_stock.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Inicio", Icons.Default.Dashboard)
    object Products : Screen("products", "Productos", Icons.Default.Inventory2)
    object Categories : Screen("categories", "Categorías", Icons.Default.Category)
    object Reports : Screen("reports", "Reportes", Icons.Default.BarChart)
    object AI : Screen("ai", "Asistente", Icons.Default.AutoAwesome)
    object Profile : Screen("profile", "Perfil", Icons.Default.Person)

    // Ruta interna — no aparece en la bottom bar
    object AddProduct : Screen("add_product", "Nuevo Producto", Icons.Default.Add)
}
