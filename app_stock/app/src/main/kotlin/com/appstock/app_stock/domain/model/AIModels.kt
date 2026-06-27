package com.appstock.app_stock.domain.model

data class AIRecommendation(
    val productId: String,
    val productName: String,
    val currentStock: Int,
    val suggestedRestock: Int,
    val priority: RecommendationPriority,
    val insight: String
)

enum class RecommendationPriority {
    CRITICAL, MEDIUM, LOW
}

data class AssistantState(
    val recommendations: List<AIRecommendation> = emptyList(),
    val isAnalyzing: Boolean = false,
    val lastAnalysisDate: Long = 0L
)
