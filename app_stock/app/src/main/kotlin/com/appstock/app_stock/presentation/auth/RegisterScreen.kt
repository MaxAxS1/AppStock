package com.appstock.app_stock.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appstock.app_stock.R
import com.appstock.app_stock.presentation.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit
) {
    val error by viewModel.error.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var nombre by rememberSaveable { mutableStateOf("") }
    var apellido by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var isEmployee by rememberSaveable { mutableStateOf(false) }
    var storeCode by rememberSaveable { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), MaterialTheme.colorScheme.background)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            
            // ── Logo y Título ──
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Stockify Logo",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .shadow(8.dp, RoundedCornerShape(20.dp))
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Nueva Tienda",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))

            // ── Tarjeta de Registro ──
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (isEmployee) "Ingresá el código para unirte a una tienda." else "Tu tienda se creará automáticamente.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    if (error != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = error!!,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it; viewModel.clearError() },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = apellido,
                        onValueChange = { apellido = it; viewModel.clearError() },
                        label = { Text("Apellido") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it; viewModel.clearError() },
                        label = { Text("Nombre de usuario") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; viewModel.clearError() },
                        label = { Text("Correo electrónico") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; viewModel.clearError() },
                        label = { Text("Contraseña (mín 6)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; viewModel.clearError() },
                        label = { Text("Confirmar contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isEmployee,
                            onCheckedChange = { isEmployee = it },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                        )
                        Text("Unirme como empleado", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    }

                    if (isEmployee) {
                        OutlinedTextField(
                            value = storeCode,
                            onValueChange = { storeCode = it; viewModel.clearError() },
                            label = { Text("Código de Tienda") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            ),
                            singleLine = true
                        )
                    }

                    Button(
                        onClick = { 
                            val validationError = viewModel.validateRegister(
                                password = password,
                                confirmPassword = confirmPassword,
                                isEmployee = isEmployee,
                                storeCode = storeCode
                            )
                            if (validationError != null) {
                                viewModel.setError(validationError)
                                return@Button
                            }
                            viewModel.register(email, password, nombre, apellido, username, if (isEmployee) storeCode else null) 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Registrarse", color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(onClick = onNavigateToLogin) {
                Text(
                    "¿Ya tienes cuenta? ",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                Text(
                    "Inicia sesión",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
