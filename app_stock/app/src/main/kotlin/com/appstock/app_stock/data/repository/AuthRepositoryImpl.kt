package com.appstock.app_stock.data.repository

import com.appstock.app_stock.domain.model.User
import com.appstock.app_stock.domain.repository.AuthRepository
import com.appstock.app_stock.domain.repository.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthRepository {

    override val currentUser: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                // El usuario está en Auth, buscamos sus datos adicionales (storeId) en Firestore
                firestore.collection("users").document(firebaseUser.uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            trySend(null)
                            return@addSnapshotListener
                        }
                        if (snapshot != null && snapshot.exists()) {
                            val user = snapshot.toObject(User::class.java)
                            if (user != null) {
                                SessionManager.setStoreId(user.storeId)
                                SessionManager.setRole(user.role)
                                trySend(user)
                            }
                        }
                    }
            } else {
                // Usuario deslogueado
                SessionManager.setStoreId(null)
                SessionManager.setRole(null)
                trySend(null)
            }
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun login(email: String, pass: String): Result<User> = try {
        firebaseAuth.signInWithEmailAndPassword(email, pass).await()
        // El Flow currentUser se encargará de notificar y setear la sesión
        Result.success(User()) // Devolvemos un dummy, el ViewModel usará el Flow
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun register(
        email: String,
        pass: String,
        nombre: String,
        apellido: String,
        username: String,
        storeCode: String?
    ): Result<User> = try {
        // Validación básica si se provee un código de tienda
        if (!storeCode.isNullOrBlank()) {
            val storeDoc = firestore.collection("stores").document(storeCode.trim()).get().await()
            if (!storeDoc.exists()) {
                return Result.failure(Exception("El código de tienda no existe o es inválido."))
            }
        }

        val result = firebaseAuth.createUserWithEmailAndPassword(email.trim(), pass).await()
        val firebaseUser = result.user

        if (firebaseUser != null) {
            try {
            val isOwner = storeCode.isNullOrBlank()
            val finalStoreId = if (isOwner) UUID.randomUUID().toString() else storeCode!!.trim()
            val initialRole = if (isOwner) "owner" else "employee"
            
            val newUser = User(
                uid = firebaseUser.uid,
                email = email.trim().lowercase(),
                displayName = email.substringBefore("@"),
                nombre = nombre.trim(),
                apellido = apellido.trim(),
                username = username.trim(),
                storeId = finalStoreId,
                role = initialRole
            )

            // Guardamos el perfil en Firestore
            firestore.collection("users").document(firebaseUser.uid).set(newUser).await()
            
            // Si es dueño, creamos un documento base para la tienda
            if (isOwner) {
                val storeData = mapOf("ownerUid" to firebaseUser.uid, "createdAt" to System.currentTimeMillis())
                firestore.collection("stores").document(finalStoreId).set(storeData).await()
            }

            Result.success(newUser)
            } catch (e: Exception) {
                // Compensación: evita usuario Auth huérfano sin perfil/store.
                try { firebaseUser.delete().await() } catch (_: Exception) { }
                Result.failure(e)
            }
        } else {
            Result.failure(Exception("Error al registrarse (user null)"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun logout() {
        firebaseAuth.signOut()
        SessionManager.setStoreId(null)
        SessionManager.setRole(null)
    }
}
