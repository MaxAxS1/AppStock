package com.appstock.app_stock.presentation.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appstock.app_stock.domain.model.InventoryMovement
import com.appstock.app_stock.domain.model.MovementType
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun InventoryHistoryScreen(viewModel: InventoryViewModel) {
    val movements by viewModel.movements.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Historial de Movimientos",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(movements) { movement ->
                MovementItem(movement)
            }
        }
    }
}

@Composable
fun MovementItem(movement: InventoryMovement) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val color = when(movement.type) {
        MovementType.ENTRADA -> Color(0xFF2E7D32)
        MovementType.SALIDA -> Color(0xFFC62828)
        MovementType.AJUSTE -> Color(0xFF1976D2)
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
                    text = " - Talle ",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = dateFormat.format(Date(movement.timestamp)),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = (if (movement.type == MovementType.SALIDA) "-" else "+") + "",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
