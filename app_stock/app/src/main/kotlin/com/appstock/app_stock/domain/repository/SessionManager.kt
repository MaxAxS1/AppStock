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

    fun getStoreIdOrNull(): String? = _currentStoreId.value

    fun requireStoreIdResult(): Result<String> =
        _currentStoreId.value?.let { Result.success(it) }
            ?: Result.failure(IllegalStateException("No hay un storeId en sesión."))

    /**
     * @deprecated Lanza [IllegalStateException] si no hay store en sesión.
     * Preferir [getStoreIdOrNull] o [requireStoreIdResult].
     */
    @Deprecated(
        "Lanza IllegalStateException si no hay store. Usar getStoreIdOrNull() o requireStoreIdResult().",
        ReplaceWith("getStoreIdOrNull()")
    )
    fun getStoreId(): String {
        return _currentStoreId.value ?: throw IllegalStateException("No hay un storeId en sesión.")
    }

    fun setRole(role: String?) {
        _currentRole.value = role
    }

    fun getRole(): String {
        return _currentRole.value ?: "employee"
    }

    private val _recentViewedProducts = MutableStateFlow<List<com.appstock.app_stock.domain.model.ProductDetail>>(emptyList())
    val recentViewedProducts: StateFlow<List<com.appstock.app_stock.domain.model.ProductDetail>> = _recentViewedProducts

    fun addViewedProduct(product: com.appstock.app_stock.domain.model.ProductDetail?) {
        if (product == null) return
        val currentList = _recentViewedProducts.value.toMutableList()
        // Remover si ya existía para ponerlo primero
        currentList.removeAll { it.id == product.id }
        // Insertar al inicio
        currentList.add(0, product)
        // Mantener solo los últimos 3
        if (currentList.size > 3) {
            _recentViewedProducts.value = currentList.take(3)
        } else {
            _recentViewedProducts.value = currentList
        }
    }

    private val _isDarkMode = MutableStateFlow<Boolean?>(null)
    val isDarkMode: StateFlow<Boolean?> = _isDarkMode

    fun setDarkMode(isDark: Boolean) {
        _isDarkMode.value = isDark
    }
}
