package com.appstock.app_stock.presentation.ai

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appstock.app_stock.domain.model.AIRecommendation
import com.appstock.app_stock.domain.model.RecommendationPriority

import androidx.navigation.NavController
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIScreen(viewModel: AIViewModel, navController: NavController) {
    val recommendations by viewModel.recommendations.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    var currentQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Asistente Inteligente") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.triggerAnalysis() }) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Analizar", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = currentQuery,
                        onValueChange = { currentQuery = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Escribe una pregunta...") },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { 
                            viewModel.sendMessage(currentQuery)
                            currentQuery = ""
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Enviar", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Mensajes del chat
                items(chatMessages) { msg ->
                    ChatBubble(msg)
                }
                
                // Indicador de escritura
                if (isAnalyzing) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                            Surface(
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.padding(end = 48.dp)
                            ) {
                                Text("...", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(msg: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            color = if (msg.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.padding(
                start = if (msg.isUser) 48.dp else 0.dp,
                end = if (!msg.isUser) 48.dp else 0.dp
            )
        ) {
            Text(
                text = msg.text,
                modifier = Modifier.padding(16.dp),
                color = if (msg.isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AssistantHeader(isAnalyzing: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(8.dp)
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = if (isAnalyzing) "Analizando tendencias..." else "Hola, soy tu asistente de Stockify",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isAnalyzing) "Esto tomará unos segundos" else "He analizado tu stock y tengo sugerencias para ti.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationCard(rec: AIRecommendation) {
    val color = when(rec.priority) {
        RecommendationPriority.CRITICAL -> MaterialTheme.colorScheme.error
        RecommendationPriority.MEDIUM -> Color(0xFFF97316) // Naranja
        RecommendationPriority.LOW -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {      
                Text(text = rec.productName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Badge(containerColor = color) {
                    Text(rec.priority.name, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = rec.insight, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Sugerencia: Reponer unidades",
                    style = MaterialTheme.typography.labelLarge,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
