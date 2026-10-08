package com.appstock.app_stock.presentation.categories

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.appstock.app_stock.domain.model.Category
import com.appstock.app_stock.presentation.navigation.Screen
import com.appstock.app_stock.presentation.ui.theme.*

// ── Paleta de colores y íconos por categoría ──────────────────────────────────

data class CategoryStyle(
    val icon: ImageVector,
    val gradientStart: Color,
    val gradientEnd: Color,
    val accentColor: Color
)

fun getCategoryStyle(categoryId: String, categoryName: String): CategoryStyle {
    // Primero intentamos por ID (categorías del seeder)
    val byId = when (categoryId.lowercase()) {
        "prendas_vestir"    -> CategoryStyle(Icons.Default.Checkroom,    Color(0xFFFF6B6B), Color(0xFFFF8E53), Color(0xFFFF6B6B))
        "calzado"           -> CategoryStyle(Icons.Default.Hiking,        Color(0xFF667EEA), Color(0xFF764BA2), Color(0xFF667EEA))
        "accesorios"        -> CategoryStyle(Icons.Default.Watch,         Color(0xFF43E97B), Color(0xFF38F9D7), Color(0xFF38C97B))
        "electronica"       -> CategoryStyle(Icons.Default.PhoneAndroid,  Color(0xFF4FACFE), Color(0xFF00F2FE), Color(0xFF4FACFE))
        "deportes"          -> CategoryStyle(Icons.Default.SportsBasketball, Color(0xFFF093FB), Color(0xFFF5576C), Color(0xFFF093FB))
        "hogar"             -> CategoryStyle(Icons.Default.Home,          Color(0xFF43CBFF), Color(0xFF9708CC), Color(0xFF43CBFF))
        "juguetes"          -> CategoryStyle(Icons.Default.Toys,          Color(0xFFFFD700), Color(0xFFFFA500), Color(0xFFFFD700))
        "libros"            -> CategoryStyle(Icons.Default.MenuBook,      Color(0xFF96FBC4), Color(0xFFF9F586), Color(0xFF66BB6A))
        "belleza"           -> CategoryStyle(Icons.Default.Spa,           Color(0xFFFECFEF), Color(0xFFFF9A9E), Color(0xFFE91E8C))
        "alimentos"         -> CategoryStyle(Icons.Default.Restaurant,    Color(0xFFFDA085), Color(0xFFF6D365), Color(0xFFF44336))
        "tecnologia"        -> CategoryStyle(Icons.Default.Computer,      Color(0xFF4FACFE), Color(0xFF00F2FE), Color(0xFF4FACFE))
        "muebles"           -> CategoryStyle(Icons.Default.Weekend,       Color(0xFFA18CD1), Color(0xFFFBC2EB), Color(0xFFA18CD1))
        "mascotas"          -> CategoryStyle(Icons.Default.Pets,          Color(0xFFFDDB92), Color(0xFFD1FDFF), Color(0xFFFF9800))
        "automotor"         -> CategoryStyle(Icons.Default.DirectionsCar, Color(0xFF30CFD0), Color(0xFF330867), Color(0xFF30CFD0))
        "herramientas"      -> CategoryStyle(Icons.Default.Build,         Color(0xFF8B9EA7), Color(0xFF4A4A6A), Color(0xFF8B9EA7))
        else -> null
    }
    if (byId != null) return byId

    // Si no hay coincidencia por ID, intentamos por nombre (categorías personalizadas)
    val nameLower = categoryName.lowercase()
    return when {
        nameLower.contains("ropa") || nameLower.contains("vestir") || nameLower.contains("prenda") || nameLower.contains("indumentaria") ->
            CategoryStyle(Icons.Default.Checkroom,       Color(0xFFFF6B6B), Color(0xFFFF8E53), Color(0xFFFF6B6B))
        nameLower.contains("calzado") || nameLower.contains("zapato") || nameLower.contains("zapatilla") ->
            CategoryStyle(Icons.Default.Hiking,          Color(0xFF667EEA), Color(0xFF764BA2), Color(0xFF667EEA))
        nameLower.contains("accesorio") || nameLower.contains("bolso") || nameLower.contains("cartera") ->
            CategoryStyle(Icons.Default.Watch,           Color(0xFF43E97B), Color(0xFF38F9D7), Color(0xFF38C97B))
        nameLower.contains("electr") || nameLower.contains("tecnol") || nameLower.contains("celular") || nameLower.contains("computad") ->
            CategoryStyle(Icons.Default.PhoneAndroid,    Color(0xFF4FACFE), Color(0xFF00F2FE), Color(0xFF4FACFE))
        nameLower.contains("deporte") || nameLower.contains("fitness") || nameLower.contains("gym") ->
            CategoryStyle(Icons.Default.SportsBasketball,Color(0xFFF093FB), Color(0xFFF5576C), Color(0xFFF093FB))
        nameLower.contains("hogar") || nameLower.contains("casa") || nameLower.contains("mueble") ->
            CategoryStyle(Icons.Default.Home,            Color(0xFF43CBFF), Color(0xFF9708CC), Color(0xFF43CBFF))
        nameLower.contains("juguete") || nameLower.contains("niño") || nameLower.contains("bebe") ->
            CategoryStyle(Icons.Default.Toys,            Color(0xFFFFD700), Color(0xFFFFA500), Color(0xFFFFD700))
        nameLower.contains("libro") || nameLower.contains("papeler") || nameLower.contains("educac") ->
            CategoryStyle(Icons.Default.MenuBook,        Color(0xFF96FBC4), Color(0xFFF9F586), Color(0xFF66BB6A))
        nameLower.contains("belleza") || nameLower.contains("cosmet") || nameLower.contains("perfume") ->
            CategoryStyle(Icons.Default.Spa,             Color(0xFFFECFEF), Color(0xFFFF9A9E), Color(0xFFE91E8C))
        nameLower.contains("aliment") || nameLower.contains("comida") || nameLower.contains("bebida") ->
            CategoryStyle(Icons.Default.Restaurant,      Color(0xFFFDA085), Color(0xFFF6D365), Color(0xFFF44336))
        nameLower.contains("mascota") || nameLower.contains("animal") ->
            CategoryStyle(Icons.Default.Pets,            Color(0xFFFDDB92), Color(0xFFD1FDFF), Color(0xFFFF9800))
        nameLower.contains("auto") || nameLower.contains("moto") || nameLower.contains("vehic") ->
            CategoryStyle(Icons.Default.DirectionsCar,   Color(0xFF30CFD0), Color(0xFF330867), Color(0xFF30CFD0))
        nameLower.contains("herramienta") || nameLower.contains("ferreteri") ->
            CategoryStyle(Icons.Default.Build,           Color(0xFF8B9EA7), Color(0xFF4A4A6A), Color(0xFF8B9EA7))
        nameLower.contains("medicament") || nameLower.contains("salud") || nameLower.contains("farmac") ->
            CategoryStyle(Icons.Default.LocalHospital,   Color(0xFFFF9A9E), Color(0xFFFECFEF), Color(0xFFE91E63))
        nameLower.contains("musica") || nameLower.contains("instrument") ->
            CategoryStyle(Icons.Default.LibraryMusic,    Color(0xFFA18CD1), Color(0xFFFBC2EB), Color(0xFFA18CD1))
        // Fallback: icono genérico con color naranja de la app
        else ->
            CategoryStyle(Icons.Default.Category,        OrangeRed, OrangeLight, OrangeRed)
    }
}

// ── Pantalla principal de Categorías ─────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(viewModel: CategoryViewModel, navController: NavController) {
    val categories by viewModel.categories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header ────────────────────────────────────────────────────────
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
                // Círculos decorativos de fondo
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .offset(x = 260.dp, y = (-30).dp)
                        .clip(CircleShape)
                        .background(OrangeRed.copy(alpha = 0.15f))
                )
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .offset(x = (-20).dp, y = 40.dp)
                        .clip(CircleShape)
                        .background(OrangeLight.copy(alpha = 0.1f))
                )

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        Column {
                            Text(
                                "Categorías",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                "${categories.size} categorías • Tocá una para ver sus productos",
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // ── Lista ─────────────────────────────────────────────────────────
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (categories.isEmpty()) {
                EmptyCategoriesState()
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(categories) { category ->
                        CategoryCard(
                            category = category,
                            onDelete = { viewModel.deleteCategory(category.id) },
                            onClick = {
                                navController.navigate(
                                    Screen.CategoryProducts.createRoute(category.id, category.name)
                                )
                            }
                        )
                    }
                }
            }
        }

        // ── FAB ───────────────────────────────────────────────────────────────
        FloatingActionButton(
            onClick = { showDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .shadow(12.dp, RoundedCornerShape(18.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Text("Nueva", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        if (showDialog) {
            AddCategoryDialog(
                onDismiss = { showDialog = false },
                onConfirm = { name, desc ->
                    viewModel.addCategory(name, desc)
                    showDialog = false
                }
            )
        }
    }
}

// ── Card de categoría (grid 2 columnas) ──────────────────────────────────────

@Composable
fun CategoryCard(category: Category, onDelete: () -> Unit, onClick: () -> Unit = {}) {
    val style = getCategoryStyle(category.id, category.name)

    // Animación de escala al pulsar
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "card_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.88f)
            .shadow(8.dp, RoundedCornerShape(22.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // Franja de color superior (gradiente)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.52f)
                    .background(
                        Brush.linearGradient(listOf(style.gradientStart, style.gradientEnd))
                    )
            )

            // Círculo decorativo en esquina superior derecha
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .offset(x = 50.dp, y = (-20).dp)
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .background(White.copy(alpha = 0.15f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Ícono con fondo blanco translúcido
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = style.icon,
                        contentDescription = category.name,
                        tint = White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Texto en la parte inferior (sobre fondo blanco)
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = category.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                    if (category.subcategories.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${category.subcategories.size} subcategorías",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else if (category.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = category.description,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    // Chip "Ver productos"
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = style.gradientStart.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                "Ver productos",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = style.accentColor
                            )
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = style.accentColor,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Estado vacío ─────────────────────────────────────────────────────────────

@Composable
fun EmptyCategoriesState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(OrangeRed.copy(alpha = 0.15f), OrangeLight.copy(alpha = 0.15f)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Category,
                    contentDescription = null,
                    tint = OrangeRed.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp)
                )
            }
            Text(
                "Sin categorías aún",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Text(
                "Crea tu primera categoría para organizar mejor tu inventario",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp
            )
        }
    }
}

// ── Dialog nueva categoría ───────────────────────────────────────────────────

@Composable
fun AddCategoryDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(OrangeChip),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = OrangeRed, modifier = Modifier.size(20.dp))
                }
                Text("Nueva Categoría", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurface)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    leadingIcon = { Icon(Icons.Default.Label, contentDescription = null, tint = OrangeRed) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangeRed,
                        focusedLabelColor = OrangeRed,
                        cursorColor = OrangeRed
                    )
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Descripción (opcional)") },
                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null, tint = OrangeRed) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangeRed,
                        focusedLabelColor = OrangeRed,
                        cursorColor = OrangeRed
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim(), desc.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = OrangeRed),
                shape = RoundedCornerShape(12.dp),
                enabled = name.isNotBlank()
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Guardar", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp)
    )
}
