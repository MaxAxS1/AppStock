package com.appstock.app_stock.presentation.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appstock.app_stock.data.service.AIServiceImpl
import com.appstock.app_stock.domain.model.AIRecommendation
import com.appstock.app_stock.domain.service.AIService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AIViewModel(
    private val service: AIService = AIServiceImpl()
) : ViewModel() {

    private val _recommendations = MutableStateFlow<List<AIRecommendation>>(emptyList())
    val recommendations: StateFlow<List<AIRecommendation>> = _recommendations

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing

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
}
