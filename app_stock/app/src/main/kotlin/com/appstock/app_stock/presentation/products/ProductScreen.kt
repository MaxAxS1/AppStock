package com.appstock.app_stock.presentation.products

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
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
import androidx.navigation.NavController
import com.appstock.app_stock.domain.model.ProductDetail
import com.appstock.app_stock.presentation.navigation.Screen
import com.appstock.app_stock.presentation.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(viewModel: ProductViewModel, navController: NavController) {
    val products by viewModel.products.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val userRole = com.appstock.app_stock.domain.repository.SessionManager.currentRole.collectAsState().value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OrangeSurface)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header naranja ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(OrangeRed, OrangeLight)))
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = White)
                        }
                        Text(
                            "Productos",
                            color = White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        "${products.size} artículos en inventario",
                        color = White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(start = 48.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Barra de búsqueda blanca estilo imagen
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = MediumGray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.onSearchQueryChange(it) },
                                singleLine = true,
                                decorationBox = { inner ->
                                    Box {
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                "Buscar producto...",
                                                color = MediumGray,
                                                fontSize = 14.sp
                                            )
                                        }
                                        inner()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 12.dp)
                            )
                        }
                    }
                }
            }

            // ── Lista de productos ────────────────────────────────────────────
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = OrangeRed)
                }
            } else if (products.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = OrangeRed.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            "Sin productos aún",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = DarkText
                        )
                        Text(
                            "Tocá el + para agregar el primero",
                            fontSize = 13.sp,
                            color = MediumGray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(products) { product ->
                        ProductCard(product)
                    }
                }
            }
        }

        // ── FAB naranja ───────────────────────────────────────────────────────
        if (userRole != "employee") {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddProduct.route) },
                containerColor = OrangeRed,
                contentColor = White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Producto")
            }
        }
    }
}

@Composable
fun ProductCard(product: ProductDetail) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono en caja naranja redondeada o Imagen del producto
            if (product.imagenUrl.isNotEmpty()) {
                AsyncImage(
                    model = product.imagenUrl,
                    contentDescription = product.nombre,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(OrangeChip),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = OrangeRed,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    product.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = DarkText
                )
                Text(
                    product.marca.ifBlank { "Sin marca" },
                    fontSize = 12.sp,
                    color = MediumGray
                )
                if (product.colores.isNotEmpty()) {
                    Text(
                        "Colores: ${product.colores.joinToString(", ")}",
                        fontSize = 11.sp,
                        color = MediumGray
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                // Chip de estado
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (product.estado) {
                        "disponible" -> SuccessGreen.copy(alpha = 0.12f)
                        "agotado"    -> ErrorRed.copy(alpha = 0.12f)
                        else         -> MediumGray.copy(alpha = 0.12f)
                    }
                ) {
                    Text(
                        product.estado.replaceFirstChar { it.uppercase() },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when (product.estado) {
                            "disponible" -> SuccessGreen
                            "agotado"    -> ErrorRed
                            else         -> MediumGray
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "$${product.precioVenta}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = OrangeRed
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Stock: ${product.stock}",
                    fontSize = 12.sp,
                    color = if (product.stock <= 5) ErrorRed else MediumGray,
                    fontWeight = if (product.stock <= 5) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}


