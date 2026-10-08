package com.appstock.app_stock.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appstock.app_stock.domain.model.ProductDetail
import com.appstock.app_stock.domain.repository.SessionManager
import com.appstock.app_stock.presentation.navigation.Screen
import com.appstock.app_stock.presentation.products.ProductExpandedCard
import com.appstock.app_stock.presentation.ui.components.*
import com.appstock.app_stock.presentation.ui.theme.*
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.launch

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel, 
    navController: NavController,
    isStartDestination: Boolean = true,
    onLogoutClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val stats by viewModel.stats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val recentViewedProducts by SessionManager.recentViewedProducts.collectAsState()

    val context = LocalContext.current
    val activity = context as? Activity
    var showExitDialog by remember { mutableStateOf(false) }

    // Bottom Sheet state
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<ProductDetail?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    BackHandler(enabled = isStartDestination) {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Salir", fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro de que querés salir de la aplicación?") },
            confirmButton = {
                Button(
                    onClick = { activity?.finish() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Salir", color = MaterialTheme.colorScheme.onError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {

                // ── Header ────────────────────────────────────────────────────────
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                            )
                            .padding(horizontal = 24.dp, vertical = 20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = onProfileClick,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f))
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = "Perfil",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                                IconButton(
                                    onClick = onLogoutClick,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f))
                                ) {
                                    Icon(
                                        Icons.Default.ExitToApp,
                                        contentDescription = "Cerrar sesión",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "¡Hola! 👋",
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                                fontSize = 14.sp
                            )
                            Text(
                                "Tu inventario de hoy",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // ── Banner hero (estilo oferta de la imagen) ──────────────────────
                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = (-20).dp)
                                .shadow(10.dp, RoundedCornerShape(24.dp)),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.secondary
                                            )
                                        )
                                    )
                                    .padding(20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "Stock Total",
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        "${stats.totalProducts} productos",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f),
                                        modifier = Modifier.clickable {
                                            navController.navigate(
                                                Screen.Products.route
                                            )
                                        }
                                    ) {
                                        Text(
                                            "Ver detalles →",
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(
                                                horizontal = 12.dp,
                                                vertical = 6.dp
                                            )
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Inventory2,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Tarjetas de estadísticas ──────────────────────────────────────
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .offset(y = (-8).dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            icon = Icons.Default.Warning,
                            label = "Bajo stock",
                            value = "${stats.lowStockCount}",
                            modifier = Modifier.weight(1f),
                            iconBgColor = Color(0xFFF59E0B)
                        )
                        StatCard(
                            icon = Icons.Default.RemoveShoppingCart,
                            label = "Agotados",
                            value = "${stats.outOfStockCount}",
                            modifier = Modifier.weight(1f),
                            iconBgColor = MaterialTheme.colorScheme.error
                        )
                        StatCard(
                            icon = Icons.Default.Category,
                            label = "Categorías",
                            value = "${stats.totalCategories}",
                            modifier = Modifier.weight(1f),
                            iconBgColor = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // ── Acciones rápidas ──────────────────────────────────────────────
                item {
                    Text(
                        "Acciones rápidas",
                        modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 12.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        QuickAction(
                            Icons.Default.Add,
                            "Agregar"
                        ) { navController.navigate(Screen.AddProduct.route) }
                        QuickAction(
                            Icons.Default.Search,
                            "Buscar"
                        ) { navController.navigate(Screen.Products.route) }
                        QuickAction(Icons.Default.Analytics, "Reportes") {
                            navController.navigate(
                                Screen.Reports.route
                            )
                        }
                        QuickAction(Icons.Default.Settings, "Ajustes") {
                            navController.navigate(
                                Screen.Settings.route
                            )
                        }
                    }
                }

                // ── Tarjeta de Últimos Vistos (hasta 3) ───────────────────────────
                if (recentViewedProducts.isNotEmpty()) {
                    item {
                        Text(
                            "Últimos vistos",
                            modifier = Modifier.padding(start = 24.dp, top = 20.dp, bottom = 12.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    items(recentViewedProducts) { product ->
                        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(6.dp, RoundedCornerShape(20.dp))
                                    .clickable {
                                        selectedProduct = product
                                        showSheet = true
                                    },
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (product.imagenUrl.isNotEmpty()) {
                                        AsyncImage(
                                            model = product.imagenUrl,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.size(64.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier.size(64.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.ShoppingBag,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            product.nombre,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            "Stock: ${product.stock}",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        "$${product.precioVenta}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            } // else (!isLoading)
        }

        // ── Modal Bottom Sheet para Último Visto ──────────────────────────────────
        if (showSheet && selectedProduct != null) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false; selectedProduct = null },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(top = 12.dp, bottom = 4.dp)
                            .size(width = 40.dp, height = 4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
                    )
                }
            ) {
                ProductExpandedCard(
                    product = selectedProduct!!,
                    onEdit = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            showSheet = false
                            navController.navigate(Screen.ProductDetail.createRoute(selectedProduct!!.id))
                        }
                    },
                    onDelete = {
                        showDeleteConfirm = true
                    },
                    onDismiss = {
                        scope.launch { sheetState.hide() }
                            .invokeOnCompletion { showSheet = false; selectedProduct = null }
                    }
                )
            }
        }

        // ── Confirmación de eliminación ─────────────────────────────────────────
        // No se elimina desde el dashboard para no romper la lista de recientes;
        // se deriva a la pantalla de detalle del producto.
        if (showDeleteConfirm && selectedProduct != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Eliminar producto", fontWeight = FontWeight.Bold) },
                text = { Text("Para eliminar \"${selectedProduct!!.nombre}\" irás a su detalle. ¿Continuar?") },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteConfirm = false
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                showSheet = false
                                navController.navigate(
                                    Screen.ProductDetail.createRoute(
                                        selectedProduct!!.id
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Continuar", color = MaterialTheme.colorScheme.onError)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
