package com.appstock.app_stock.presentation.sizes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appstock.app_stock.domain.model.ProductSize

@Composable
fun SizeStockSelector(
    sizes: List<ProductSize>,
    onStockChange: (Int, Int, Int) -> Unit // sizeId, currentStock, delta
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = "Gestión de Stock por Talle",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(sizes, key = { it.id }) { size ->
                SizeCard(size, onStockChange)
            }
        }
    }
}

@Composable
fun SizeCard(
    size: ProductSize,
    onStockChange: (Int, Int, Int) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.width(120.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (size.stock <= size.minStock) MaterialTheme.colorScheme.errorContainer 
                             else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = size.name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text(
                text = "Stock: ${size.stock}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (size.stock <= size.minStock) MaterialTheme.colorScheme.error else Color.Unspecified
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SmallFloatingActionButton(
                    onClick = { onStockChange(size.id, size.stock, -1) },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(32.dp).semantics { contentDescription = "Disminuir stock" }
                ) {
                    Text("-")
                }
                SmallFloatingActionButton(
                    onClick = { onStockChange(size.id, size.stock, 1) },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(32.dp).semantics { contentDescription = "Aumentar stock" }
                ) {
                    Text("+")
                }
            }
        }
    }
}
