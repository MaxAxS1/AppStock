package com.appstock.app_stock.presentation.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.appstock.app_stock.domain.model.User
import com.appstock.app_stock.presentation.navigation.Screen
import com.appstock.app_stock.presentation.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    navController: NavController
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val employees by viewModel.employees.collectAsState()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OrangeSurface)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // ── Header naranja ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(OrangeRed, OrangeLight)))
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = White)
                        }
                        Column {
                            Text(
                                "Perfil",
                                color = White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                currentUser?.email ?: "Cargando...",
                                color = White.copy(alpha = 0.8f),
                                fontSize = 14.sp
                            )
                        }
                    }
                    IconButton(onClick = { 
                        viewModel.logout()
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Cerrar sesión", tint = White)
                    }
                }
            }

            // ── Contenido Principal ────────────────────────────────────────────
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Info del Usuario
                item {
                    UserInfoCard(
                        user = currentUser, 
                        context = context,
                        onSaveProfile = { n, a, u -> viewModel.updateUserProfile(n, a, u) }
                    )
                }

                // Lista de Empleados (solo si es owner)
                if (currentUser?.role == "owner") {
                    item {
                        Text(
                            "Empleados de la Tienda",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = DarkText,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }

                    if (employees.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = White),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    "No tenés empleados registrados. Compartí tu código de tienda para que se unan.",
                                    color = MediumGray,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    } else {
                        items(employees) { employee ->
                            EmployeeCard(
                                employee = employee,
                                onRoleChange = { newRole ->
                                    viewModel.updateUserRole(employee.uid, newRole)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserInfoCard(
    user: User?, 
    context: Context,
    onSaveProfile: (String, String, String) -> Unit = { _, _, _ -> }
) {
    var isEditing by remember { mutableStateOf(false) }
    var editNombre by remember(user) { mutableStateOf(user?.nombre ?: "") }
    var editApellido by remember(user) { mutableStateOf(user?.apellido ?: "") }
    var editUsername by remember(user) { mutableStateOf(user?.username ?: "") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(OrangeChip),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = OrangeRed)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    if (!isEditing) {
                        Column {
                            val displayName = listOfNotNull(
                                user?.nombre?.takeIf { it.isNotBlank() },
                                user?.apellido?.takeIf { it.isNotBlank() }
                            ).joinToString(" ").ifBlank { user?.email ?: "" }
                            
                            Text(displayName, fontWeight = FontWeight.Bold, color = DarkText, fontSize = 16.sp)
                            
                            if (!user?.username.isNullOrBlank()) {
                                Text("@${user?.username}", color = MediumGray, fontSize = 13.sp)
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Rol: ${if (user?.role == "owner") "Dueño" else if (user?.role == "admin") "Administrador" else "Empleado"}",
                                color = OrangeRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        Column(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = editNombre,
                                onValueChange = { editNombre = it },
                                label = { Text("Nombre", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = editApellido,
                                onValueChange = { editApellido = it },
                                label = { Text("Apellido", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = editUsername,
                                onValueChange = { editUsername = it },
                                label = { Text("Usuario", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { 
                                    isEditing = false
                                    editNombre = user?.nombre ?: ""
                                    editApellido = user?.apellido ?: ""
                                    editUsername = user?.username ?: ""
                                }) {
                                    Text("Cancelar", color = MediumGray)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { 
                                        onSaveProfile(editNombre, editApellido, editUsername)
                                        isEditing = false 
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = OrangeRed)
                                ) {
                                    Text("Guardar", color = White)
                                }
                            }
                        }
                    }
                }
                
                if (!isEditing) {
                    Text(
                        "Editar",
                        color = OrangeRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { isEditing = true }
                            .padding(8.dp)
                    )
                }
            }
            
            if (user?.role == "owner") {
                Divider(modifier = Modifier.padding(vertical = 12.dp), color = MediumGray.copy(alpha = 0.2f))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Código de Tienda (Para Empleados)", color = MediumGray, fontSize = 12.sp)
                        Text(
                            text = user.storeId,
                            fontWeight = FontWeight.SemiBold,
                            color = OrangeRed,
                            fontSize = 14.sp
                        )
                    }
                    IconButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Código de Tienda", user.storeId)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Código copiado", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", tint = MediumGray)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeCard(employee: User, onRoleChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Store, contentDescription = null, tint = MediumGray, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                val disp = listOfNotNull(
                    employee.nombre.takeIf { it.isNotBlank() },
                    employee.apellido.takeIf { it.isNotBlank() }
                ).joinToString(" ")
                
                val title = disp.ifBlank { employee.email }
                Text(title, fontWeight = FontWeight.SemiBold, color = DarkText, fontSize = 14.sp)
                
                if (disp.isNotBlank()) {
                    Text(employee.email, color = MediumGray, fontSize = 11.sp)
                }

                Text(
                    text = when (employee.role) {
                        "admin" -> "Admin"
                        "employee" -> "Empleado"
                        "disabled" -> "Bloqueado"
                        else -> employee.role
                    }, 
                    color = MediumGray, 
                    fontSize = 12.sp
                )
            }
            
            Box {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = OrangeLight.copy(alpha = 0.2f),
                    modifier = Modifier.clickable { expanded = true }
                ) {
                    Text(
                        "Cambiar Rol",
                        color = OrangeRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Administrador (Edita productos)") },
                        onClick = { onRoleChange("admin"); expanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Empleado (Solo ver)") },
                        onClick = { onRoleChange("employee"); expanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Bloquear acceso", color = ErrorRed) },
                        onClick = { onRoleChange("disabled"); expanded = false }
                    )
                }
            }
        }
    }
}
