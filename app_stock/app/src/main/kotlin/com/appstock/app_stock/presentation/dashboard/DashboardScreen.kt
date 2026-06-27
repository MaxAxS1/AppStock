package com.appstock.app_stock.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.appstock.app_stock.presentation.ui.components.*
import com.appstock.app_stock.presentation.ui.theme.*

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalContext

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel, 
    onLogoutClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val stats by viewModel.stats.collectAsState()
    
    val context = LocalContext.current
    val activity = context as? Activity
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler {
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
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Salir", color = White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Cancelar", color = MediumGray)
                }
            },
            containerColor = White
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OrangeSurface)
    ) {
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
                            Brush.verticalGradient(listOf(OrangeRed, OrangeLight))
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
                                    .background(White.copy(alpha = 0.25f))
                            ) {
                                Icon(Icons.Default.Person, contentDescription = "Perfil", tint = White)
                            }
                            IconButton(
                                onClick = onLogoutClick,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(White.copy(alpha = 0.25f))
                            ) {
                                Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesión", tint = White)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("¡Hola! 👋", color = White.copy(alpha = 0.85f), fontSize = 14.sp)
                        Text(
                            "Tu inventario de hoy",
                            color = White,
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
                        colors = CardDefaults.cardColors(containerColor = White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(listOf(OrangeRed, OrangeLight))
                                )
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Stock Total",
                                    color = White.copy(alpha = 0.85f),
                                    fontSize = 13.sp
                                )
                                Text(
                                    "${stats.totalProducts} productos",
                                    color = White,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = White.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        "Ver detalles →",
                                        color = White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Inventory2,
                                    contentDescription = null,
                                    tint = White,
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
                        iconBgColor = ErrorRed
                    )
                    StatCard(
                        icon = Icons.Default.Category,
                        label = "Categorías",
                        value = "${stats.totalCategories}",
                        modifier = Modifier.weight(1f),
                        iconBgColor = OrangeRed
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
                    color = DarkText
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QuickAction(Icons.Default.Add, "Agregar") {}
                    QuickAction(Icons.Default.Search, "Buscar") {}
                    QuickAction(Icons.Default.Analytics, "Reportes") {}
                    QuickAction(Icons.Default.Settings, "Ajustes") {}
                }
            }

            // ── Últimos productos ─────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Últimos productos",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = DarkText
                    )
                    Text(
                        "Ver todos",
                        fontSize = 13.sp,
                        color = OrangeRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            items(stats.recentProducts.size) { index ->
                val product = stats.recentProducts[index]
                RecentProductRow(product)
                if (index < stats.recentProducts.lastIndex) {
                    Divider(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        color = OrangeChip
                    )
                }
            }
        }
    }
}

@Composable
fun RecentProductRow(product: com.appstock.app_stock.domain.model.Product) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(OrangeChip),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.ShoppingBag,
                contentDescription = null,
                tint = OrangeRed,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                product.nombre,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = DarkText
            )
            Text(
                "Stock: ${product.stock}",
                fontSize = 12.sp,
                color = MediumGray
            )
        }
        Text(
            "$${product.precioVenta}",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = OrangeRed
        )
    }
}
