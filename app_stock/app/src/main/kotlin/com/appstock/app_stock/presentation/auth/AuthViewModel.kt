package com.appstock.app_stock.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appstock.app_stock.data.repository.AuthRepositoryImpl
import com.appstock.app_stock.domain.model.User
import com.appstock.app_stock.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository = AuthRepositoryImpl()
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        viewModelScope.launch {
            repository.currentUser.collect { user ->
                _currentUser.value = user
            }
        }
    }

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _error.value = "Completá todos los campos"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = repository.login(email.trim(), pass)
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message ?: "Error al iniciar sesión"
            }
            _isLoading.value = false
        }
    }

    fun register(
        email: String,
        pass: String,
        nombre: String,
        apellido: String,
        username: String,
        storeCode: String? = null
    ) {
        if (email.isBlank() || pass.isBlank() || nombre.isBlank() || apellido.isBlank() || username.isBlank()) {
            _error.value = "Completá todos los campos"
            return
        }
        if (pass.length < 6) {
            _error.value = "La contraseña debe tener al menos 6 caracteres"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = repository.register(email.trim(), pass, nombre.trim(), apellido.trim(), username.trim(), storeCode)
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message ?: "Error al registrarse"
            }
            _isLoading.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun setError(msg: String) {
        _error.value = msg
    }
}
