package com.appstock.app_stock.presentation.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appstock.app_stock.domain.model.Category
import com.appstock.app_stock.presentation.ui.theme.*

import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(viewModel: CategoryViewModel, navController: NavController) {
    val categories by viewModel.categories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = White)
                        }
                        Text(
                            "Categorías",
                            color = White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        "${categories.size} categorías disponibles",
                        color = White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                }
            }

            // ── Lista ─────────────────────────────────────────────────────────
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = OrangeRed)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(White),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(categories) { category ->
                        CategoryRow(
                            category = category,
                            onDelete = { viewModel.deleteCategory(category.id) }
                        )
                        Divider(color = OrangeChip, thickness = 1.dp)
                    }
                }
            }
        }

        // ── FAB ───────────────────────────────────────────────────────────────
        FloatingActionButton(
            onClick = { showDialog = true },
            containerColor = OrangeRed,
            contentColor = White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Añadir Categoría")
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

@Composable
fun CategoryRow(category: Category, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono categoría estilo imagen
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(OrangeChip),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Category,
                contentDescription = null,
                tint = OrangeRed,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                category.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = DarkText
            )
            if (category.description.isNotEmpty()) {
                Text(
                    category.description,
                    fontSize = 12.sp,
                    color = MediumGray
                )
            }
            if (category.subcategories.isNotEmpty()) {
                Text(
                    category.subcategories.joinToString(" · ") { it.name },
                    fontSize = 11.sp,
                    color = OrangeRed.copy(alpha = 0.7f)
                )
            }
        }

        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MediumGray,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun AddCategoryDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Nueva Categoría", fontWeight = FontWeight.Bold, color = DarkText)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangeRed,
                        focusedLabelColor = OrangeRed,
                        cursorColor = OrangeRed
                    )
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
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
                onClick = { onConfirm(name, desc) },
                colors = ButtonDefaults.buttonColors(containerColor = OrangeRed)
            ) {
                Text("Guardar", color = White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = OrangeRed)
            }
        },
        containerColor = White
    )
}
