package com.appstock.app_stock.presentation.reports

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appstock.app_stock.domain.model.*
import com.appstock.app_stock.presentation.ui.theme.*

import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: ReportViewModel, navController: NavController) {
    val report by viewModel.report.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf("Resumen", "Más vendidos", "Ganancias")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (isLoading || report == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            val r = report!!
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {

                // ── Header ────────────────────────────────────────────────────
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))
                            )
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { navController.popBackStack() },
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = MaterialTheme.colorScheme.onPrimary)
                                }
                                Text(
                                    "Estadísticas",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                "Resumen de tu inventario",
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(start = 48.dp)
                            )
                        }
                    }
                }

                // ── Tabs ──────────────────────────────────────────────────────
                item {
                    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                        ScrollableTabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary,
                            edgePadding = 16.dp,
                            indicator = { tabPositions ->
                                TabRowDefaults.Indicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = MaterialTheme.colorScheme.primary,
                                    height = 3.dp
                                )
                            }
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    text = {
                                        Text(
                                            title,
                                            fontWeight = if (selectedTab == index)
                                                FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 14.sp
                                        )
                                    },
                                    selectedContentColor = MaterialTheme.colorScheme.primary,
                                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // ── Contenido según tab ───────────────────────────────────────
                when (selectedTab) {

                    // ── TAB 0: RESUMEN ────────────────────────────────────────
                    0 -> {
                        item { Spacer(modifier = Modifier.height(16.dp)) }

                        // Tarjetas de valor
                        item {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ValueBigCard(
                                    label = "Ganancia potencial",
                                    value = formatMoney(r.valueReport.potentialProfit),
                                    subtitle = "Si vendieras todo el stock actual",
                                    gradientColors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary),
                                    icon = Icons.Default.TrendingUp
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    SmallStatCard(
                                        label = "Costo total",
                                        value = formatMoney(r.valueReport.totalCostValue),
                                        icon = Icons.Default.AccountBalance,
                                        iconColor = Color(0xFFF59E0B),
                                        modifier = Modifier.weight(1f)
                                    )
                                    SmallStatCard(
                                        label = "Venta potencial",
                                        value = formatMoney(r.valueReport.totalSaleValue),
                                        icon = Icons.Default.Sell,
                                        iconColor = SuccessGreen,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Stock por categoría
                        if (r.categoryReports.isNotEmpty()) {
                            item {
                                SectionHeader(
                                    "Stock por categoría",
                                    modifier = Modifier.padding(
                                        start = 16.dp, end = 16.dp,
                                        top = 24.dp, bottom = 8.dp
                                    )
                                )
                            }
                            val maxStock = r.categoryReports.maxOfOrNull { it.totalStock } ?: 1
                            items(r.categoryReports) { cat ->
                                CategoryProgressRow(cat, maxStock)
                            }
                        }

                        // Agotados
                        if (r.outOfStockProducts.isNotEmpty()) {
                            item {
                                SectionHeader(
                                    "⚠ Productos agotados (${r.outOfStockProducts.size})",
                                    modifier = Modifier.padding(
                                        start = 16.dp, end = 16.dp,
                                        top = 24.dp, bottom = 8.dp
                                    ),
                                    titleColor = MaterialTheme.colorScheme.error
                                )
                            }
                            items(r.outOfStockProducts) { name ->
                                OutOfStockRow(name)
                            }
                        }
                    }

                    // ── TAB 1: MÁS VENDIDOS ───────────────────────────────────
                    1 -> {
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                        item {
                            Text(
                                "Top 5 por valor en inventario",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                        if (r.topProducts.isEmpty()) {
                            item { EmptyState("Sin productos con stock disponible") }
                        } else {
                            val maxVal = r.topProducts.maxOfOrNull { it.stockValue } ?: 1.0
                            items(r.topProducts.size) { index ->
                                TopProductRow(r.topProducts[index], index + 1, maxVal)
                            }
                        }
                    }

                    // ── TAB 2: GANANCIAS ──────────────────────────────────────
                    2 -> {
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                        item {
                            Text(
                                "Ganancia potencial por día de carga",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                        if (r.dailyProfits.isEmpty()) {
                            item { EmptyState("Aún no hay datos suficientes") }
                        } else {
                            val maxProfit = r.dailyProfits.maxOfOrNull { it.profit } ?: 1.0
                            items(r.dailyProfits) { daily ->
                                DailyProfitRow(daily, maxProfit)
                            }

                            // Totalizador
                            item {
                                val total = r.dailyProfits.sumOf { it.profit }
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                        .shadow(6.dp, RoundedCornerShape(20.dp)),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                                                )
                                            )
                                            .padding(20.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                "Ganancia total acumulada",
                                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                formatMoney(total),
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                fontSize = 26.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                        Icon(
                                            Icons.Default.TrendingUp,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Componentes ───────────────────────────────────────────────────────────────

@Composable
private fun ValueBigCard(
    label: String,
    value: String,
    subtitle: String,
    gradientColors: List<Color>,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(gradientColors))
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f), fontSize = 13.sp)
                Text(
                    value,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(subtitle, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f), fontSize = 11.sp)
            }
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(30.dp))
            }
        }
    }
}

@Composable
private fun SmallStatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.shadow(4.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    titleColor: Color = MaterialTheme.colorScheme.onBackground
) {
    Text(
        title,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = titleColor,
        modifier = modifier
    )
}

@Composable
private fun CategoryProgressRow(report: CategoryReport, maxStock: Int) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (maxStock > 0) report.totalStock.toFloat() / maxStock else 0f,
        animationSpec = tween(800),
        label = "progress"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    report.categoryName.ifBlank { "Sin categoría" },
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "${report.totalStock} uds",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = animatedProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer,
                strokeCap = StrokeCap.Round
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "${report.productCount} productos",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OutOfStockRow(name: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.06f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.RemoveShoppingCart,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(name, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun TopProductRow(product: TopProduct, rank: Int, maxValue: Double) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (maxValue > 0) (product.stockValue / maxValue).toFloat() else 0f,
        animationSpec = tween(800),
        label = "top_progress"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .shadow(3.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Número de ranking
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (rank == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "#$rank",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (rank == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    product.nombre,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    product.marca.ifBlank { "Sin marca" } + " · ${product.stock} uds",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer,
                    strokeCap = StrokeCap.Round
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                formatMoney(product.stockValue),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun DailyProfitRow(daily: DailyProfit, maxProfit: Double) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (maxProfit > 0) (daily.profit / maxProfit).toFloat() else 0f,
        animationSpec = tween(800),
        label = "daily_progress"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    daily.date,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                LinearProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer,
                    strokeCap = StrokeCap.Round
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                formatMoney(daily.profit),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = SuccessGreen
            )
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.BarChart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                modifier = Modifier.size(56.dp)
            )
            Text(message, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun formatMoney(value: Double): String {
    return if (value >= 1_000_000) {
        "$${String.format("%.1f", value / 1_000_000)}M"
    } else if (value >= 1_000) {
        "$${String.format("%.1f", value / 1_000)}K"
    } else {
        "$${String.format("%.0f", value)}"
    }
}
