package com.appstock.app_stock.domain.repository

import com.appstock.app_stock.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun login(email: String, pass: String): Result<User>
    suspend fun register(
        email: String,
        pass: String,
        nombre: String,
        apellido: String,
        username: String,
        storeCode: String? = null
    ): Result<User>
    suspend fun logout()
}
