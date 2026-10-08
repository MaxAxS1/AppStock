package com.appstock.app_stock.presentation.products

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import com.appstock.app_stock.domain.model.ProductDetail
import com.appstock.app_stock.presentation.navigation.Screen
import com.appstock.app_stock.presentation.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(viewModel: ProductViewModel, navController: NavController) {
    val products    by viewModel.products.collectAsState()
    val isLoading   by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val userRole = com.appstock.app_stock.domain.repository.SessionManager.currentRole.collectAsState().value

    // Bottom Sheet state
    val sheetState      = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope           = rememberCoroutineScope()
    var selectedProduct by remember { mutableStateOf<ProductDetail?>(null) }
    var showSheet       by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    fun openSheet(product: ProductDetail) {
        selectedProduct = product
        showSheet = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header oscuro ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(Color(0xFF1A1A2E), Color(0xFF16213E)))
                    )
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                // Círculo decorativo
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .offset(x = 270.dp, y = (-40).dp)
                        .clip(CircleShape)
                        .background(OrangeRed.copy(alpha = 0.12f))
                )
                Column {
                    Text(
                        "Inventario",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        if (products.isEmpty() && !isLoading) "Sin productos aún"
                        else "${products.size} producto${if (products.size != 1) "s" else ""} en stock",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.55f),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Buscador
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.10f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.onSearchQueryChange(it) },
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 14.sp
                                ),
                                decorationBox = { inner ->
                                    Box {
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                "Buscar producto...",
                                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.45f),
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

            // ── Contenido ─────────────────────────────────────────────────────
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (products.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        Text("Sin productos aún", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
                        Text("Tocá el + para agregar el primero", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        ProductCard(
                            product  = product,
                            onTap    = { openSheet(product) },
                            onDelete = { viewModel.deleteProduct(product.id) }
                        )
                    }
                }
            }
        }

        // FAB
        if (userRole != "employee") {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddProduct.route) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor   = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .shadow(12.dp, CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Producto")
            }
        }
    }

    // ── Modal Bottom Sheet — Vista ampliada del producto ──────────────────────
    if (showSheet && selectedProduct != null) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false; selectedProduct = null },
            sheetState       = sheetState,
            containerColor   = MaterialTheme.colorScheme.surface,
            shape            = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle       = {
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
                onDelete  = {
                    showDeleteDialog = true
                },
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion { showSheet = false }
                }
            )
        }
    }

    // ── Diálogo de confirmación de eliminación ────────────────────────────────
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
            title = {
                Text("¿Eliminar producto?", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, textAlign = TextAlign.Center)
            },
            text = {
                Text(
                    "Se eliminará \"${selectedProduct!!.nombre}\" del inventario. Esta acción no se puede deshacer.",
                    fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp, textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProduct(selectedProduct!!.id)
                        showDeleteDialog = false
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            showSheet = false
                            selectedProduct = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape  = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sí, eliminar", color = MaterialTheme.colorScheme.onError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = false },
                    shape   = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Cancelar") }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Card compacta en la lista
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ProductCard(product: ProductDetail, onTap: () -> Unit = {}, onDelete: () -> Unit = {}) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "card_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(5.dp, RoundedCornerShape(20.dp))
            .clickable(interactionSource = interactionSource, indication = null) { onTap() },
        shape  = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen o placeholder
            ProductThumbnail(product, size = 62.dp)

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    product.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    product.marca.ifBlank { "Sin marca" },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                StatusChip(product.estado)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "$${product.precioVenta}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Stock: ${product.stock}",
                    fontSize = 11.sp,
                    color = if (product.stock <= 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (product.stock <= 5) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Vista EXPANDIDA en el Bottom Sheet
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ProductExpandedCard(
    product: ProductDetail,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {

        // ── Imagen hero ───────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(listOf(MaterialTheme.colorScheme.primaryContainer, Color(0xFFFFD4B8)))
                ),
            contentAlignment = Alignment.Center
        ) {
            if (product.imagenUrl.isNotEmpty()) {
                AsyncImage(
                    model             = product.imagenUrl,
                    contentDescription = product.nombre,
                    contentScale      = ContentScale.Crop,
                    modifier          = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp))
                )
                // Gradiente inferior para legibilidad
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f)),
                                startY = 300f
                            )
                        )
                )
            } else {
                Icon(
                    Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint   = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    modifier = Modifier.size(80.dp)
                )
            }

            // Badge de estado
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)) {
                StatusChip(product.estado, large = true)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Info principal ────────────────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        product.nombre,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 22.sp,
                        color      = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.5).sp,
                        lineHeight = 27.sp
                    )
                    if (product.marca.isNotBlank()) {
                        Text(
                            product.marca,
                            fontSize  = 14.sp,
                            color     = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "$${product.precioVenta}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 28.sp,
                        color      = MaterialTheme.colorScheme.primary,
                        letterSpacing = (-0.5).sp
                    )
                    if (product.precioCosto > 0) {
                        Text(
                            "Costo: $${product.precioCosto}",
                            fontSize = 11.sp,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Fila de métricas ──────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricChip(
                    label = "Stock",
                    value = "${product.stock}",
                    icon  = Icons.Default.Inventory2,
                    color = if (product.stock <= 5) ErrorRed else SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
                if (product.codigoInterno.isNotBlank()) {
                    MetricChip(
                        label = "Código",
                        value = product.codigoInterno,
                        icon  = Icons.Default.Tag,
                        color = Color(0xFF6366F1),
                        modifier = Modifier.weight(1f)
                    )
                }
                if (product.codigoBarras.isNotBlank()) {
                    MetricChip(
                        label = "Barras",
                        value = product.codigoBarras,
                        icon  = Icons.Default.QrCode,
                        color = Color(0xFF0EA5E9),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── Colores disponibles ───────────────────────────────────────────
            if (product.colores.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Colores disponibles",
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(product.colores) { color ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                        ) {
                            Text(
                                color,
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color      = MaterialTheme.colorScheme.primary,
                                modifier   = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // ── Descripción ───────────────────────────────────────────────────
            if (product.descripcion.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Descripción",
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    product.descripcion,
                    fontSize   = 13.sp,
                    color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    lineHeight = 19.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Botones de acción ─────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Eliminar
                OutlinedButton(
                    onClick = onDelete,
                    shape   = RoundedCornerShape(16.dp),
                    colors  = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border  = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar", fontWeight = FontWeight.SemiBold)
                }
                // Editar
                Button(
                    onClick = onEdit,
                    shape   = RoundedCornerShape(16.dp),
                    colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .weight(2f)
                        .height(52.dp)
                        .shadow(8.dp, RoundedCornerShape(16.dp))
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Editar producto", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Componentes compartidos
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ProductThumbnail(product: ProductDetail, size: androidx.compose.ui.unit.Dp) {
    if (product.imagenUrl.isNotEmpty()) {
        AsyncImage(
            model             = product.imagenUrl,
            contentDescription = product.nombre,
            contentScale      = ContentScale.Crop,
            modifier          = Modifier.size(size).clip(RoundedCornerShape(16.dp))
        )
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.ShoppingBag,
                contentDescription = null,
                tint     = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(size * 0.5f)
            )
        }
    }
}

@Composable
fun StatusChip(estado: String, large: Boolean = false) {
    val bg    = when (estado) {
        "disponible"    -> SuccessGreen.copy(alpha = 0.15f)
        "agotado"       -> ErrorRed.copy(alpha = 0.15f)
        else            -> MediumGray.copy(alpha = 0.15f)
    }
    val color = when (estado) {
        "disponible"    -> SuccessGreen
        "agotado"       -> ErrorRed
        else            -> MediumGray
    }
    Surface(shape = RoundedCornerShape(20.dp), color = bg) {
        Text(
            estado.replaceFirstChar { it.uppercase() },
            fontSize   = if (large) 12.sp else 10.sp,
            fontWeight = FontWeight.Bold,
            color      = color,
            modifier   = Modifier.padding(
                horizontal = if (large) 14.dp else 8.dp,
                vertical   = if (large) 6.dp else 3.dp
            )
        )
    }
}

@Composable
fun MetricChip(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape    = RoundedCornerShape(14.dp),
        color    = color.copy(alpha = 0.08f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(label, fontSize = 10.sp, color = color.copy(alpha = 0.7f), fontWeight = FontWeight.Medium)
        }
    }
}
