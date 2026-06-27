package com.appstock.app_stock.domain.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Mantiene la sesión global en memoria (el storeId actual) para que 
 * los repositorios de datos sepan de dónde leer y escribir.
 */
object SessionManager {
    private val _currentStoreId = MutableStateFlow<String?>(null)
    val currentStoreId: StateFlow<String?> = _currentStoreId

    private val _currentRole = MutableStateFlow<String?>("employee")
    val currentRole: StateFlow<String?> = _currentRole

    fun setStoreId(storeId: String?) {
        _currentStoreId.value = storeId
    }

    fun getStoreId(): String {
        return _currentStoreId.value ?: throw IllegalStateException("No hay un storeId en sesión.")
    }

    fun setRole(role: String?) {
        _currentRole.value = role
    }

    fun getRole(): String {
        return _currentRole.value ?: "employee"
    }
}
