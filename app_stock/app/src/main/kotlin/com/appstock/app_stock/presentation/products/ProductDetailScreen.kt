package com.appstock.app_stock.presentation.products

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.appstock.app_stock.domain.model.ProductDetail
import com.appstock.app_stock.domain.model.ProductSize
import com.appstock.app_stock.domain.model.SizeTable

private val Primary    = Color(0xFFFF5200)
private val PrimaryDark = Color(0xFFCC4100)
private val Secondary  = Color(0xFFFF7A45)
private val SuccessG   = Color(0xFF22C55E)
private val ErrRed     = Color(0xFFEF4444)
private val ChipBg     = Color(0xFFFFE0D0)
private val BgPage     = Color(0xFFF4F6F8)
private val DarkNav    = Color(0xFF1A1A2E)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProductDetailScreen(
    viewModel: ProductViewModel,
    navController: NavController,
    productId: String
) {
    val selectedProduct by viewModel.selectedProduct.collectAsState()
    val isUpdating     by viewModel.isUpdating.collectAsState()
    val updateSuccess  by viewModel.updateSuccess.collectAsState()
    val updateError    by viewModel.updateError.collectAsState()
    val deleteSuccess  by viewModel.deleteSuccess.collectAsState()
    val categories     by viewModel.categories.collectAsState()

    // Cargar el producto al entrar
    LaunchedEffect(productId) {
        viewModel.loadProductById(productId)
    }

    // Navegar tras actualizar exitosamente
    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            viewModel.resetUpdateState()
            navController.popBackStack()
        }
    }

    // Navegar tras eliminar
    LaunchedEffect(deleteSuccess) {
        if (deleteSuccess) {
            viewModel.resetDeleteState()
            navController.popBackStack()
        }
    }

    val product = selectedProduct

    if (product == null) {
        // Cargando
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Primary)
        }
        return
    }

    // ── Estados del formulario prellenados ────────────────────────────────────
    var nombre        by remember(product.id) { mutableStateOf(product.nombre) }
    var descripcion   by remember(product.id) { mutableStateOf(product.descripcion) }
    var marca         by remember(product.id) { mutableStateOf(product.marca) }
    var precioCosto   by remember(product.id) { mutableStateOf(product.precioCosto.toString()) }
    var precioVenta   by remember(product.id) { mutableStateOf(product.precioVenta.toString()) }
    var codigoInterno by remember(product.id) { mutableStateOf(product.codigoInterno) }
    var codigoBarras  by remember(product.id) { mutableStateOf(product.codigoBarras) }
    var estado        by remember(product.id) { mutableStateOf(product.estado) }
    var colores       by remember(product.id) { mutableStateOf(product.colores) }
    var currentColor  by remember { mutableStateOf("") }
    var imageUri      by remember { mutableStateOf<Uri?>(null) }

    // Categoría
    var selectedCategoryId    by remember(product.id) { mutableStateOf(product.categoriaId) }
    var selectedSubcategoryId by remember(product.id) { mutableStateOf(product.subcategoriaId) }
    var categoryExpanded      by remember { mutableStateOf(false) }
    var subcategoryExpanded   by remember { mutableStateOf(false) }
    val selectedCategory = categories.firstOrNull { it.id == selectedCategoryId }
    val subcategories    = selectedCategory?.subcategories ?: emptyList()

    // Talles
    data class SizeEntry(val stock: String = "0", val minStock: String = "0", val active: Boolean = false)
    val sizeEntries = remember(product.id) {
        mutableStateMapOf<Int, SizeEntry>().apply {
            SizeTable.defaultSizes.forEach { put(it.id, SizeEntry()) }
        }
    }

    // Validaciones
    var nombreError     by remember { mutableStateOf(false) }
    var precioError     by remember { mutableStateOf(false) }

    // Diálogo de confirmación de eliminación
    var showDeleteDialog by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }

    fun onSaveChanges() {
        nombreError = nombre.isBlank()
        precioError = precioVenta.isBlank() || precioVenta.toDoubleOrNull() == null
        if (nombreError || precioError) return

        val activeSizes: List<ProductSize> = SizeTable.defaultSizes
            .filter { sizeEntries[it.id]?.active == true }
            .map { size ->
                val entry = sizeEntries[size.id]!!
                size.copy(
                    stock    = entry.stock.toIntOrNull() ?: 0,
                    minStock = entry.minStock.toIntOrNull() ?: 0
                )
            }

        val totalStock = if (activeSizes.isNotEmpty()) activeSizes.sumOf { it.stock } else product.stock

        val updated = product.copy(
            nombre        = nombre.trim(),
            descripcion   = descripcion.trim(),
            marca         = marca.trim(),
            categoriaId   = selectedCategoryId,
            subcategoriaId = selectedSubcategoryId,
            precioCosto   = precioCosto.toDoubleOrNull() ?: 0.0,
            precioVenta   = precioVenta.toDoubleOrNull() ?: 0.0,
            codigoInterno = codigoInterno.trim(),
            codigoBarras  = codigoBarras.trim(),
            estado        = estado,
            stock         = totalStock,
            colores       = colores
        )
        viewModel.updateProduct(updated, activeSizes, imageUri)
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Scaffold(
        containerColor = BgPage,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(DarkNav, Color(0xFF16213E))))
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Editar Producto",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.3).sp
                        )
                        Text(
                            product.nombre,
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                    // Botón eliminar
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(ErrRed.copy(alpha = 0.15f))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = ErrRed)
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { if (!isUpdating) onSaveChanges() },
                icon = {
                    if (isUpdating) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                    }
                },
                text = {
                    Text(
                        if (isUpdating) "Guardando..." else "Guardar cambios",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                containerColor = Primary,
                modifier = Modifier.shadow(12.dp, RoundedCornerShape(16.dp))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Error banner ──────────────────────────────────────────────────
            updateError?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = ErrRed.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = ErrRed, modifier = Modifier.size(18.dp))
                        Text(error, color = ErrRed, fontSize = 13.sp)
                    }
                }
            }

            // ── Foto del producto ─────────────────────────────────────────────
            DetailSection("Foto del producto")
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val displayImage = imageUri ?: product.imagenUrl.takeIf { it.isNotEmpty() }
                if (displayImage != null) {
                    AsyncImage(
                        model = displayImage,
                        contentDescription = "Foto del producto",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(130.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .border(2.dp, Primary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(ChipBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = Primary, modifier = Modifier.size(40.dp))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (imageUri == null && product.imagenUrl.isEmpty()) "Agregar Foto" else "Cambiar Foto")
                }
            }

            // ── Información básica ────────────────────────────────────────────
            DetailSection("Información del producto")

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it; nombreError = false },
                label = { Text("Nombre *") },
                isError = nombreError,
                supportingText = if (nombreError) ({ Text("El nombre es obligatorio") }) else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = outlinedColors()
            )
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2, maxLines = 4,
                colors = outlinedColors()
            )
            OutlinedTextField(
                value = marca,
                onValueChange = { marca = it },
                label = { Text("Marca") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = outlinedColors()
            )

            // ── Categoría ─────────────────────────────────────────────────────
            DetailSection("Categoría")

            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value = selectedCategory?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = outlinedColors()
                )
                ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name) },
                            onClick = {
                                selectedCategoryId    = cat.id
                                selectedSubcategoryId = ""
                                categoryExpanded      = false
                            }
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = subcategories.isNotEmpty(),
                enter = expandVertically(),
                exit  = shrinkVertically()
            ) {
                ExposedDropdownMenuBox(
                    expanded = subcategoryExpanded,
                    onExpandedChange = { subcategoryExpanded = !subcategoryExpanded }
                ) {
                    OutlinedTextField(
                        value = subcategories.firstOrNull { it.id == selectedSubcategoryId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Subcategoría") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subcategoryExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = outlinedColors()
                    )
                    ExposedDropdownMenu(expanded = subcategoryExpanded, onDismissRequest = { subcategoryExpanded = false }) {
                        subcategories.forEach { sub ->
                            DropdownMenuItem(
                                text = { Text(sub.name) },
                                onClick = {
                                    selectedSubcategoryId = sub.id
                                    subcategoryExpanded   = false
                                }
                            )
                        }
                    }
                }
            }

            // ── Precios ───────────────────────────────────────────────────────
            DetailSection("Precios")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = precioCosto,
                    onValueChange = { precioCosto = it },
                    label = { Text("Precio Costo") },
                    prefix = { Text("$") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = outlinedColors()
                )
                OutlinedTextField(
                    value = precioVenta,
                    onValueChange = { precioVenta = it; precioError = false },
                    label = { Text("Precio Venta *") },
                    prefix = { Text("$") },
                    isError = precioError,
                    supportingText = if (precioError) ({ Text("Precio inválido") }) else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = outlinedColors()
                )
            }

            // ── Códigos ───────────────────────────────────────────────────────
            DetailSection("Códigos de identificación")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = codigoInterno,
                    onValueChange = { codigoInterno = it },
                    label = { Text("Código Interno") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = outlinedColors()
                )
                OutlinedTextField(
                    value = codigoBarras,
                    onValueChange = { codigoBarras = it },
                    label = { Text("Código de Barras") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = outlinedColors()
                )
            }

            // ── Estado ────────────────────────────────────────────────────────
            DetailSection("Estado del producto")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("disponible", "agotado", "descontinuado").forEach { option ->
                    val selected = estado == option
                    FilterChip(
                        selected = selected,
                        onClick = { estado = option },
                        label = { Text(option.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelMedium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when (option) {
                                "disponible"    -> SuccessG.copy(alpha = 0.15f)
                                "agotado"       -> ErrRed.copy(alpha = 0.15f)
                                else            -> Color.Gray.copy(alpha = 0.15f)
                            },
                            selectedLabelColor = when (option) {
                                "disponible"    -> SuccessG
                                "agotado"       -> ErrRed
                                else            -> Color.Gray
                            }
                        )
                    )
                }
            }

            // ── Colores ───────────────────────────────────────────────────────
            DetailSection("Colores disponibles")
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = currentColor,
                    onValueChange = { currentColor = it },
                    label = { Text("Color") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = outlinedColors()
                )
                IconButton(
                    onClick = {
                        val c = currentColor.trim()
                        if (c.isNotEmpty() && !colores.contains(c)) {
                            colores = colores + c
                            currentColor = ""
                        }
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar color", tint = Primary)
                }
            }
            if (colores.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colores.forEach { color ->
                        InputChip(
                            selected = true,
                            onClick = { colores = colores - color },
                            label = { Text(color) },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Eliminar color",
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = InputChipDefaults.inputChipColors(
                                selectedContainerColor   = ChipBg,
                                selectedLabelColor       = Primary,
                                selectedTrailingIconColor = Primary
                            )
                        )
                    }
                }
            }

            // ── Talles y stock ────────────────────────────────────────────────
            DetailSection("Talles y stock")
            Text(
                "Activá un talle para modificar su stock",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            SizeTable.defaultSizes.forEach { size ->
                val entry = sizeEntries[size.id] ?: SizeEntry()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (entry.active) Secondary.copy(alpha = 0.07f)
                        else Color.White
                    ),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Talle  ${size.name}",
                                style = MaterialTheme.typography.titleSmall,
                                color = if (entry.active) Primary else Color.Gray
                            )
                            Switch(
                                checked = entry.active,
                                onCheckedChange = { sizeEntries[size.id] = entry.copy(active = it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor  = Color.White,
                                    checkedTrackColor  = Primary
                                )
                            )
                        }
                        AnimatedVisibility(visible = entry.active, enter = expandVertically(), exit = shrinkVertically()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = entry.stock,
                                    onValueChange = { sizeEntries[size.id] = entry.copy(stock = it) },
                                    label = { Text("Stock actual") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    colors = outlinedColors()
                                )
                                OutlinedTextField(
                                    value = entry.minStock,
                                    onValueChange = { sizeEntries[size.id] = entry.copy(minStock = it) },
                                    label = { Text("Stock mínimo") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    colors = outlinedColors()
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(90.dp))
        }
    }

    // ── Diálogo de confirmación de eliminación ────────────────────────────────
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(ErrRed.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = ErrRed, modifier = Modifier.size(28.dp))
                }
            },
            title = {
                Text(
                    "¿Eliminar producto?",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Color(0xFF1A1A2E)
                )
            },
            text = {
                Text(
                    "Se eliminará \"${product.nombre}\" de tu inventario. Esta acción no se puede deshacer.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteProduct(product.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sí, eliminar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = false },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
                ) {
                    Text("Cancelar")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
private fun DetailSection(title: String) {
    Column {
        Text(
            text  = title,
            style = MaterialTheme.typography.titleSmall,
            color = Primary,
            fontWeight = FontWeight.Bold
        )
        Divider(modifier = Modifier.padding(top = 4.dp), color = Primary.copy(alpha = 0.2f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun outlinedColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor  = Primary,
    focusedLabelColor   = Primary,
    cursorColor         = Primary
)
