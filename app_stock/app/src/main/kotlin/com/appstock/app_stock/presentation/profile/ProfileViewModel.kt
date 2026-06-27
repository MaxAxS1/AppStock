package com.appstock.app_stock.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appstock.app_stock.domain.model.User
import com.appstock.app_stock.domain.repository.AuthRepository
import com.appstock.app_stock.data.repository.AuthRepositoryImpl
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _employees = MutableStateFlow<List<User>>(emptyList())
    val employees: StateFlow<List<User>> = _employees

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _currentUser.value = user
                if (user != null && user.role == "owner") {
                    loadEmployees(user.storeId, user.uid)
                }
            }
        }
    }

    private fun loadEmployees(storeId: String, currentUid: String) {
        firestore.collection("users")
            .whereEqualTo("storeId", storeId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(User::class.java) }
                    // Excluimos al propio dueño de la lista de empleados a gestionar
                    _employees.value = list.filter { it.uid != currentUid }
                }
            }
    }

    fun updateUserRole(uid: String, newRole: String) {
        viewModelScope.launch {
            try {
                firestore.collection("users").document(uid)
                    .update("role", newRole).await()
            } catch (e: Exception) {
                // Manejo de errores
            }
        }
    }

    fun updateUserProfile(nombre: String, apellido: String, username: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            try {
                val updates = mapOf(
                    "nombre" to nombre,
                    "apellido" to apellido,
                    "username" to username
                )
                firestore.collection("users").document(user.uid).update(updates).await()
            } catch (e: Exception) {
                // Manejo de errores
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
