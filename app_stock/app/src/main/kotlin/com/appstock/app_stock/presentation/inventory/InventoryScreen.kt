package com.appstock.app_stock.presentation.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appstock.app_stock.domain.model.InventoryMovement
import com.appstock.app_stock.domain.model.MovementType
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun InventoryHistoryScreen(viewModel: InventoryViewModel) {
    val movements by viewModel.movements.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Historial de Movimientos",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (movements.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Sin movimientos",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(movements, key = { it.id }) { movement ->
                    MovementItem(movement, dateFormat)
                }
            }
        }
    }
}

@Composable
fun MovementItem(movement: InventoryMovement, dateFormat: SimpleDateFormat) {
    val colorScheme = MaterialTheme.colorScheme
    val color = when(movement.type) {
        MovementType.ENTRADA -> colorScheme.primary
        MovementType.SALIDA -> colorScheme.error
        MovementType.AJUSTE -> colorScheme.secondary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "${movement.productName} - Talle ${movement.sizeName}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = dateFormat.format(Date(movement.timestamp)),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            val sign = if (movement.type == MovementType.SALIDA) "-" else "+"
            Text(
                text = "$sign${movement.quantity}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
