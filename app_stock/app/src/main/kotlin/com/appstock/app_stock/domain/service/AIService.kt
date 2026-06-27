package com.appstock.app_stock.domain.service

import com.appstock.app_stock.domain.model.AIRecommendation
import kotlinx.coroutines.flow.Flow

interface AIService {
    fun getSmartRecommendations(): Flow<List<AIRecommendation>>
    suspend fun analyzeTrends(): Result<Unit>
}
