package com.appstock.app_stock.presentation.main

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.appstock.app_stock.presentation.navigation.Screen
import com.appstock.app_stock.presentation.dashboard.DashboardScreen
import com.appstock.app_stock.presentation.dashboard.DashboardViewModel
import com.appstock.app_stock.presentation.products.AddProductScreen
import com.appstock.app_stock.presentation.products.ProductDetailScreen
import com.appstock.app_stock.presentation.products.ProductListScreen
import com.appstock.app_stock.presentation.products.ProductViewModel
import com.appstock.app_stock.presentation.categories.CategoryScreen
import com.appstock.app_stock.presentation.categories.CategoryProductsScreen
import com.appstock.app_stock.presentation.categories.CategoryViewModel
import com.appstock.app_stock.presentation.reports.ReportsScreen
import com.appstock.app_stock.presentation.reports.ReportViewModel
import com.appstock.app_stock.presentation.ai.AIScreen
import com.appstock.app_stock.presentation.ai.AIViewModel
import com.appstock.app_stock.presentation.auth.AuthViewModel
import com.appstock.app_stock.presentation.auth.LoginScreen
import com.appstock.app_stock.presentation.auth.RegisterScreen
import com.appstock.app_stock.presentation.profile.ProfileScreen
import com.appstock.app_stock.presentation.profile.ProfileViewModel
import com.appstock.app_stock.presentation.ui.theme.*


// Rutas visibles en la bottom bar
private val bottomBarScreens = listOf(
    Screen.Dashboard,
    Screen.Products,
    Screen.Categories,
    Screen.Reports,
    Screen.AI
)

// Orden de las tabs: define hacia dónde desliza la transición
private val bottomBarOrder = bottomBarScreens.map { it.route }

// Duraciones de las animaciones de navegación (ms)
private const val NAV_DURATION = 250
private const val NAV_SLIDE_FRACTION = 5 // 1/5 del ancho: sutil entre tabs

/** Desliza hacia adelante/atras según el orden de tabs, o desde la derecha si es detalle. */
private fun tabDirection(from: String?, to: String?): Int {
    val fromIndex = bottomBarOrder.indexOf(from)
    val toIndex = bottomBarOrder.indexOf(to)
    if (fromIndex == -1 || toIndex == -1 || fromIndex == toIndex) return 0
    return if (toIndex > fromIndex) 1 else -1
}

@Composable
fun AppNavigator(
    authViewModel: AuthViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    if (currentUser == null) {
        AuthNavigation(authViewModel)
    } else {
        MainScreen(authViewModel)
    }
}

@Composable
fun AuthNavigation(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "login",
        enterTransition = { fadeIn(tween(NAV_DURATION)) },
        exitTransition = { fadeOut(tween(NAV_DURATION)) },
        popEnterTransition = { fadeIn(tween(NAV_DURATION)) },
        popExitTransition = { fadeOut(tween(NAV_DURATION)) }
    ) {
        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Instancias compartidas de ViewModels para que el estado se preserve
    val productViewModel: ProductViewModel = viewModel()
    val dashboardViewModel: DashboardViewModel = viewModel()
    val categoryViewModel: CategoryViewModel = viewModel()
    
    // Le pasamos la funcion de logout al dashboard si la necesita
    // (Por ahora la podríamos poner en el header del dashboard)

    Scaffold(
        bottomBar = {
            val hideBottomBar = currentRoute == Screen.AddProduct.route ||
                currentRoute?.contains("category_products") == true ||
                currentRoute?.contains("product_detail") == true
            if (!hideBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    val currentDestination = navBackStackEntry?.destination
                    bottomBarScreens.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = {
                                Text(
                                    screen.title,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = Screen.Dashboard.route,
            Modifier.padding(innerPadding),
            // Tabs: fundido + deslizamiento sutil según orden. Detalle: empuje desde derecha.
            enterTransition = {
                val dir = tabDirection(
                    initialState.destination.route,
                    targetState.destination.route
                )
                fadeIn(tween(NAV_DURATION)) +
                    slideInHorizontally(tween(NAV_DURATION)) { dir * it / NAV_SLIDE_FRACTION }
            },
            exitTransition = {
                val dir = tabDirection(
                    initialState.destination.route,
                    targetState.destination.route
                )
                fadeOut(tween(NAV_DURATION)) +
                    slideOutHorizontally(tween(NAV_DURATION)) { -dir * it / NAV_SLIDE_FRACTION }
            },
            popEnterTransition = {
                fadeIn(tween(NAV_DURATION)) +
                    slideInHorizontally(tween(NAV_DURATION)) { -it / NAV_SLIDE_FRACTION }
            },
            popExitTransition = {
                fadeOut(tween(NAV_DURATION)) +
                    slideOutHorizontally(tween(NAV_DURATION)) { it / NAV_SLIDE_FRACTION }
            }
        ) {
            composable(Screen.Dashboard.route) { 
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    navController = navController,
                    isStartDestination = currentRoute == Screen.Dashboard.route,
                    onLogoutClick = { authViewModel.logout() },
                    onProfileClick = { navController.navigate(Screen.Profile.route) }
                ) 
            }
            composable(Screen.Products.route) {
                ProductListScreen(productViewModel, navController)
            }
            composable(
                Screen.AddProduct.route,
                enterTransition = {
                    fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it }
                },
                popExitTransition = {
                    fadeOut(tween(300)) + slideOutHorizontally(tween(300)) { it }
                }
            ) {
                AddProductScreen(productViewModel, navController)
            }
            composable(
                route = Screen.ProductDetail.route,
                enterTransition = {
                    fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it }
                },
                popExitTransition = {
                    fadeOut(tween(300)) + slideOutHorizontally(tween(300)) { it }
                },
                arguments = listOf(
                    androidx.navigation.navArgument("productId") { type = androidx.navigation.NavType.StringType }
                )
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: ""
                ProductDetailScreen(
                    viewModel     = productViewModel,
                    navController = navController,
                    productId     = productId
                )
            }
            composable(Screen.Categories.route) {
                CategoryScreen(categoryViewModel, navController)
            }
            composable(
                route = Screen.CategoryProducts.route,
                enterTransition = {
                    fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it }
                },
                popExitTransition = {
                    fadeOut(tween(300)) + slideOutHorizontally(tween(300)) { it }
                },
                arguments = listOf(
                    androidx.navigation.navArgument("categoryId") { type = androidx.navigation.NavType.StringType },
                    androidx.navigation.navArgument("categoryName") { type = androidx.navigation.NavType.StringType }
                )
            ) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
                val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
                CategoryProductsScreen(
                    viewModel = categoryViewModel,
                    navController = navController,
                    categoryId = categoryId,
                    categoryName = categoryName
                )
            }
            composable(Screen.Reports.route) { ReportsScreen(viewModel(), navController) }
            composable(Screen.Settings.route) {
                com.appstock.app_stock.presentation.settings.SettingsScreen(
                    navController = navController,
                    onLogoutClick = { authViewModel.logout() }
                )
            }
            composable(Screen.AI.route) { AIScreen(viewModel(), navController) }
            composable(Screen.Profile.route) { ProfileScreen(viewModel(), navController) }
        }
    }
}
