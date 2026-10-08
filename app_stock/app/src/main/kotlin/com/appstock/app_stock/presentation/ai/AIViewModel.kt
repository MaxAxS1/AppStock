package com.appstock.app_stock.presentation.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appstock.app_stock.data.service.AIServiceImpl
import com.appstock.app_stock.domain.model.AIRecommendation
import com.appstock.app_stock.domain.service.AIService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import kotlinx.coroutines.delay

data class ChatMessage(val text: String, val isUser: Boolean)

class AIViewModel(
    private val service: AIService = AIServiceImpl()
) : ViewModel() {

    private val _recommendations = MutableStateFlow<List<AIRecommendation>>(emptyList())
    val recommendations: StateFlow<List<AIRecommendation>> = _recommendations

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage("¡Hola! Soy tu asistente inteligente. ¿En qué te puedo ayudar hoy con tu inventario?", false))
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages

    init {
        viewModelScope.launch {
            service.getSmartRecommendations().collect {
                _recommendations.value = it
            }
        }
    }

    fun triggerAnalysis() {
        viewModelScope.launch {
            _isAnalyzing.value = true
            service.analyzeTrends()
            _isAnalyzing.value = false
        }
    }

    fun sendMessage(query: String) {
        if (query.isBlank()) return
        val currentList = _chatMessages.value.toMutableList()
        currentList.add(ChatMessage(query, true))
        _chatMessages.value = currentList

        viewModelScope.launch {
            _isAnalyzing.value = true
            delay(1500) // Simulación de procesamiento de IA
            
            val response = when {
                query.lowercase().contains("stock") -> "Revisé tu inventario y noté que tienes algunos productos con stock bajo. Te sugiero revisar la pantalla de Inicio para ver el detalle."
                query.lowercase().contains("mas vendido") || query.lowercase().contains("más vendido") -> "Tus productos más vendidos son aquellos con mayor ganancia potencial en la pestaña de Reportes."
                else -> "Entiendo. Puedo ayudarte a analizar tus reportes de ganancias, verificar productos sin stock, o darte sugerencias sobre qué comprar. ¿Quieres ver alguna de estas opciones?"
            }
            
            val updatedList = _chatMessages.value.toMutableList()
            updatedList.add(ChatMessage(response, false))
            _chatMessages.value = updatedList
            _isAnalyzing.value = false
        }
    }
}
