package com.appstock.app_stock.presentation.products

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.appstock.app_stock.domain.model.ProductDetail
import com.appstock.app_stock.domain.model.ProductSize
import com.appstock.app_stock.domain.model.SizeTable
import java.util.Date

// Colores movidos adentro del composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(viewModel: ProductViewModel, navController: NavController) {

    val Primary   = MaterialTheme.colorScheme.primary
    val Secondary = MaterialTheme.colorScheme.secondary
    val Success   = Color(0xFF22C55E)
    val ErrorRed  = MaterialTheme.colorScheme.error
    val ChipBg    = MaterialTheme.colorScheme.primaryContainer
    val BgColor   = MaterialTheme.colorScheme.background

    // ——— Estados del formulario ———
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var marca by remember { mutableStateOf("") }
    var precioCosto by remember { mutableStateOf("") }
    var precioVenta by remember { mutableStateOf("") }
    var codigoInterno by remember { mutableStateOf("") }
    var codigoBarras by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("disponible") }

    // Colores y foto
    var currentColor by remember { mutableStateOf("") }
    var colores by remember { mutableStateOf(listOf<String>()) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }

    // Categoría / subcategoría
    val categories by viewModel.categories.collectAsState()
    var selectedCategoryId by remember { mutableStateOf("") }
    var selectedSubcategoryId by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var subcategoryExpanded by remember { mutableStateOf(false) }

    val selectedCategory = categories.firstOrNull { it.id == selectedCategoryId }
    val subcategories = selectedCategory?.subcategories ?: emptyList()

    // Talles: mapa de sizeId → (stock, minStock, activo)
    data class SizeEntry(val stock: String = "0", val minStock: String = "0", val active: Boolean = false)
    val sizeEntries = remember {
        mutableStateMapOf<Int, SizeEntry>().apply {
            SizeTable.defaultSizes.forEach { put(it.id, SizeEntry()) }
        }
    }

    // Validaciones
    var nombreError by remember { mutableStateOf(false) }
    var precioVentaError by remember { mutableStateOf(false) }

    // Estados del ViewModel
    val isSaving by viewModel.isSaving.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()
    val saveError by viewModel.saveError.collectAsState()

    // Navegar de vuelta al guardar exitosamente
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            viewModel.resetSaveState()
            navController.popBackStack()
        }
    }

    // Función de guardado
    fun onSave() {
        nombreError = nombre.isBlank()
        precioVentaError = precioVenta.isBlank() || precioVenta.toDoubleOrNull() == null
        if (nombreError || precioVentaError) return

        val activeSizes: List<ProductSize> = SizeTable.defaultSizes
            .filter { sizeEntries[it.id]?.active == true }
            .map { size ->
                val entry = sizeEntries[size.id]!!
                size.copy(
                    stock = entry.stock.toIntOrNull() ?: 0,
                    minStock = entry.minStock.toIntOrNull() ?: 0
                )
            }

        val totalStock = activeSizes.sumOf { it.stock }

        val product = ProductDetail(
            nombre = nombre.trim(),
            descripcion = descripcion.trim(),
            marca = marca.trim(),
            categoriaId = selectedCategoryId,
            subcategoriaId = selectedSubcategoryId,
            precioCosto = precioCosto.toDoubleOrNull() ?: 0.0,
            precioVenta = precioVenta.toDoubleOrNull() ?: 0.0,
            codigoInterno = codigoInterno.trim(),
            codigoBarras = codigoBarras.trim(),
            estado = estado,
            createdAt = Date().time,
            stock = totalStock,
            colores = colores
        )

        viewModel.saveProduct(product, activeSizes, imageUri)
    }

    Scaffold(
        containerColor = BgColor,
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Producto", color = MaterialTheme.colorScheme.onPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { if (!isSaving) onSave() },
                icon = {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                text = { Text(if (isSaving) "Guardando..." else "Guardar Producto", color = MaterialTheme.colorScheme.onPrimary) },
                containerColor = Primary
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ————— ERROR BANNER —————
            saveError?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f)),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Text(
                        text = "⚠ $error",
                        modifier = Modifier.padding(12.dp),
                        color = ErrorRed,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // ————— SECCIÓN: Foto del producto —————
            SectionTitle("Foto del producto")
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Foto del producto",
                        modifier = Modifier
                            .size(120.dp)
                            .padding(bottom = 8.dp)
                    )
                }
                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text(if (imageUri == null) "Agregar Foto" else "Cambiar Foto")
                }
            }

            // ————— SECCIÓN: Información básica —————
            SectionTitle("Información del producto")

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it; nombreError = false },
                label = { Text("Nombre *") },
                isError = nombreError,
                supportingText = if (nombreError) ({ Text("El nombre es obligatorio") }) else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            OutlinedTextField(
                value = marca,
                onValueChange = { marca = it },
                label = { Text("Marca") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // ————— SECCIÓN: Categoría —————
            SectionTitle("Categoría")

            // Dropdown Categoría
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    if (categories.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Sin categorías disponibles", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            onClick = { categoryExpanded = false }
                        )
                    } else {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    selectedCategoryId = cat.id
                                    selectedSubcategoryId = ""
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Dropdown Subcategoría — solo visible si hay categoría seleccionada con subcategorías
            AnimatedVisibility(
                visible = subcategories.isNotEmpty(),
                enter = expandVertically(),
                exit = shrinkVertically()
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = subcategoryExpanded,
                        onDismissRequest = { subcategoryExpanded = false }
                    ) {
                        subcategories.forEach { sub ->
                            DropdownMenuItem(
                                text = { Text(sub.name) },
                                onClick = {
                                    selectedSubcategoryId = sub.id
                                    subcategoryExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // ————— SECCIÓN: Precios —————
            SectionTitle("Precios")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = precioCosto,
                    onValueChange = { precioCosto = it },
                    label = { Text("Precio Costo") },
                    prefix = { Text("$") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = precioVenta,
                    onValueChange = { precioVenta = it; precioVentaError = false },
                    label = { Text("Precio Venta *") },
                    prefix = { Text("$") },
                    isError = precioVentaError,
                    supportingText = if (precioVentaError) ({ Text("Precio inválido") }) else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // ————— SECCIÓN: Códigos —————
            SectionTitle("Códigos de identificación")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = codigoInterno,
                    onValueChange = { codigoInterno = it },
                    label = { Text("Código Interno") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = codigoBarras,
                    onValueChange = { codigoBarras = it },
                    label = { Text("Código de Barras") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // ————— SECCIÓN: Estado —————
            SectionTitle("Estado del producto")

            val estadoOptions = listOf("disponible", "agotado", "descontinuado")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                estadoOptions.forEach { option ->
                    val selected = estado == option
                    FilterChip(
                        selected = selected,
                        onClick = { estado = option },
                        label = {
                            Text(
                                option.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when (option) {
                                "disponible" -> Success.copy(alpha = 0.15f)
                                "agotado" -> ErrorRed.copy(alpha = 0.15f)
                                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                            },
                            selectedLabelColor = when (option) {
                                "disponible" -> Success
                                "agotado" -> ErrorRed
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    )
                }
            }

            // ————— SECCIÓN: Colores —————
            SectionTitle("Colores disponibles")
            
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
                    singleLine = true
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
            
            // Chips de colores
            if (colores.isNotEmpty()) {
                @OptIn(ExperimentalLayoutApi::class)
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
                                Icon(Icons.Default.Close, contentDescription = "Eliminar color", modifier = Modifier.size(16.dp))
                            },
                            colors = InputChipDefaults.inputChipColors(
                                selectedContainerColor = ChipBg,
                                selectedLabelColor = Primary,
                                selectedTrailingIconColor = Primary
                            )
                        )
                    }
                }
            }

            // ————— SECCIÓN: Talles —————
            SectionTitle("Talles y stock")

            Text(
                text = "Activá cada talle e ingresá el stock disponible",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Una fila por talle — siempre visible, switch para activar
            SizeTable.defaultSizes.forEach { size ->
                val entry = sizeEntries[size.id] ?: SizeEntry()

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (entry.active)
                            Secondary.copy(alpha = 0.07f)
                        else
                            MaterialTheme.colorScheme.surface
                    ),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {

                        // Encabezado: nombre del talle + switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Talle  ${size.name}",
                                style = MaterialTheme.typography.titleSmall,
                                color = if (entry.active) Primary else Color.Gray
                            )
                            Switch(
                                checked = entry.active,
                                onCheckedChange = {
                                    sizeEntries[size.id] = entry.copy(active = it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Primary
                                )
                            )
                        }

                        // Campos de stock — solo visibles cuando el talle está activo
                        AnimatedVisibility(
                            visible = entry.active,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = entry.stock,
                                    onValueChange = { sizeEntries[size.id] = entry.copy(stock = it) },
                                    label = { Text("Stock actual") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = entry.minStock,
                                    onValueChange = { sizeEntries[size.id] = entry.copy(minStock = it) },
                                    label = { Text("Stock mínimo") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }
            }

            // Espacio final para que el FAB no tape el último campo
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Divider(
            modifier = Modifier.padding(top = 4.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
    }
}
