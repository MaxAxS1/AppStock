package com.appstock.app_stock.presentation.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.appstock.app_stock.domain.model.ProductDetail
import com.appstock.app_stock.presentation.navigation.Screen
import com.appstock.app_stock.presentation.products.ProductCard
import com.appstock.app_stock.presentation.products.ProductExpandedCard
import com.appstock.app_stock.presentation.products.ProductViewModel
import com.appstock.app_stock.presentation.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryProductsScreen(
    viewModel: CategoryViewModel,
    navController: NavController,
    categoryId: String,
    categoryName: String,
    productViewModel: ProductViewModel = viewModel()
) {
    val products  by viewModel.categoryProducts.collectAsState()
    val isLoading by viewModel.isLoadingProducts.collectAsState()

    // Bottom Sheet
    val sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope            = rememberCoroutineScope()
    var selectedProduct  by remember { mutableStateOf<ProductDetail?>(null) }
    var showSheet        by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(categoryId) {
        viewModel.loadProductsByCategory(categoryId)
    }

    val style = getCategoryStyle(categoryId, categoryName)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header oscuro con ícono de categoría ──────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1A1A2E), Color(0xFF16213E))
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                // Círculos decorativos
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .offset(x = 260.dp, y = (-30).dp)
                        .clip(CircleShape)
                        .background(style.gradientStart.copy(alpha = 0.2f))
                )

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        // Ícono de la categoría
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(style.gradientStart, style.gradientEnd)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = style.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                categoryName,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.3).sp
                            )
                            Text(
                                if (products.isEmpty() && !isLoading) "Sin productos en esta categoría"
                                else "${products.size} producto${if (products.size != 1) "s" else ""} en stock",
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // ── Contenido ─────────────────────────────────────────────────────
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (products.isEmpty()) {
                // ── Estado vacío ───────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.padding(horizontal = 40.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            style.gradientStart.copy(alpha = 0.15f),
                                            style.gradientEnd.copy(alpha = 0.15f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = style.icon,
                                contentDescription = null,
                                tint = style.accentColor.copy(alpha = 0.6f),
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        Text(
                            "Sin productos en stock",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Aún no hay artículos en \"$categoryName\". Agregá un producto y asignale esta categoría.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 19.sp
                        )
                    }
                }
                } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        ProductCard(
                            product  = product,
                            onTap    = {
                                selectedProduct = product
                                showSheet = true
                            },
                            onDelete = {
                                selectedProduct = product
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // ── Modal Bottom Sheet ────────────────────────────────────────────────────
    if (showSheet && selectedProduct != null) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false; selectedProduct = null },
            sheetState       = sheetState,
            containerColor   = MaterialTheme.colorScheme.surface,
            shape            = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
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
                product   = selectedProduct!!,
                onEdit    = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showSheet = false
                        navController.navigate(Screen.ProductDetail.createRoute(selectedProduct!!.id))
                    }
                },
                onDelete  = { showDeleteDialog = true },
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion { showSheet = false }
                }
            )
        }
    }

    // ── Diálogo de eliminación ────────────────────────────────────────────────
    if (showDeleteDialog && selectedProduct != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(30.dp))
                }
            },
            title  = { Text("\u00bfEliminar producto?", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp) },
            text   = {
                Text(
                    "Se eliminará \"${selectedProduct!!.nombre}\". Esta acción no se puede deshacer.",
                    fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        productViewModel.deleteProduct(selectedProduct!!.id)
                        showDeleteDialog = false
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            showSheet = false
                            selectedProduct = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape  = RoundedCornerShape(14.dp)
                ) {
                    Text("S\u00ed, eliminar", color = MaterialTheme.colorScheme.onError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }, shape = RoundedCornerShape(14.dp)) {
                    Text("Cancelar")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

