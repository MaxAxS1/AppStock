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
    object Settings : Screen("settings", "Ajustes", Icons.Default.Settings)

    // Rutas internas — no aparecen en la bottom bar
    object AddProduct : Screen("add_product", "Nuevo Producto", Icons.Default.Add)
    object ProductDetail : Screen("product_detail/{productId}", "Producto", Icons.Default.Inventory2) {
        fun createRoute(productId: String) = "product_detail/$productId"
    }
    object CategoryProducts : Screen("category_products/{categoryId}/{categoryName}", "Productos", Icons.Default.Inventory2) {
        fun createRoute(categoryId: String, categoryName: String) =
            "category_products/$categoryId/${android.net.Uri.encode(categoryName)}"
    }
}
