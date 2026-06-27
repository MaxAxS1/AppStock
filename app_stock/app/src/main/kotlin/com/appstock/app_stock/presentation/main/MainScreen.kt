package com.appstock.app_stock.presentation.main

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
import com.appstock.app_stock.presentation.products.ProductListScreen
import com.appstock.app_stock.presentation.products.ProductViewModel
import com.appstock.app_stock.presentation.categories.CategoryScreen
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

// Rutas que NO deben mostrar la bottom bar
private val fullScreenRoutes = setOf(Screen.AddProduct.route)

// Rutas visibles en la bottom bar
private val bottomBarScreens = listOf(
    Screen.Dashboard,
    Screen.Products,
    Screen.Categories,
    Screen.Reports,
    Screen.AI
)

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
    NavHost(navController = navController, startDestination = "login") {
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
    val productViewModel = remember { ProductViewModel() }
    val dashboardViewModel = remember { DashboardViewModel() }
    
    // Le pasamos la funcion de logout al dashboard si la necesita
    // (Por ahora la podríamos poner en el header del dashboard)

    Scaffold(
        bottomBar = {
            if (currentRoute !in fullScreenRoutes) {
                NavigationBar(
                    containerColor = White,
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
                                selectedIconColor = OrangeRed,
                                selectedTextColor = OrangeRed,
                                indicatorColor = OrangeChip,
                                unselectedIconColor = MediumGray,
                                unselectedTextColor = MediumGray
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
            Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) { 
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onLogoutClick = { authViewModel.logout() },
                    onProfileClick = { navController.navigate(Screen.Profile.route) }
                ) 
            }
            composable(Screen.Products.route) {
                ProductListScreen(productViewModel, navController)
            }
            composable(Screen.AddProduct.route) {
                AddProductScreen(productViewModel, navController)
            }
            composable(Screen.Categories.route) { CategoryScreen(CategoryViewModel(), navController) }
            composable(Screen.Reports.route) { ReportsScreen(ReportViewModel(), navController) }
            composable(Screen.AI.route) { AIScreen(AIViewModel(), navController) }
            composable(Screen.Profile.route) { ProfileScreen(viewModel(), navController) }
        }
    }
}
