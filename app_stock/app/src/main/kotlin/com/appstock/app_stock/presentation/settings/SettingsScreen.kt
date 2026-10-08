package com.appstock.app_stock.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.appstock.app_stock.domain.repository.SessionManager
import com.appstock.app_stock.presentation.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    onLogoutClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Estado del modo oscuro conectado al SessionManager
    val isDarkModeState by SessionManager.isDarkMode.collectAsState()
    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val darkModeEnabled = isDarkModeState ?: isSystemDark

    // UI states para toggles (mock)
    var lowStockAlerts by remember { mutableStateOf(true) }
    var emailNotifications by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Ajustes", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            
            // Sección: Preferencias de Pantalla
            item {
                SettingsSectionTitle("Preferencias de Pantalla")
                SettingsCard {
                    SettingsSwitchRow(
                        icon = Icons.Default.DarkMode,
                        iconColor = Color(0xFF5C6BC0),
                        title = "Modo Oscuro",
                        subtitle = "Cambia la apariencia de la app",
                        checked = darkModeEnabled,
                        onCheckedChange = { SessionManager.setDarkMode(it) }
                    )
                }
            }

            // Sección: Notificaciones y Alertas
            item {
                SettingsSectionTitle("Notificaciones y Alertas")
                SettingsCard {
                    SettingsSwitchRow(
                        icon = Icons.Default.NotificationsActive,
                        iconColor = OrangeRed,
                        title = "Alertas de Stock Bajo",
                        subtitle = "Notificar cuando un producto se agote",
                        checked = lowStockAlerts,
                        onCheckedChange = { lowStockAlerts = it }
                    )
                    Divider(color = MediumGray.copy(alpha = 0.1f), modifier = Modifier.padding(start = 56.dp))
                    SettingsSwitchRow(
                        icon = Icons.Default.Email,
                        iconColor = Color(0xFF26A69A),
                        title = "Reportes por Email",
                        subtitle = "Recibir resumen semanal de inventario",
                        checked = emailNotifications,
                        onCheckedChange = { emailNotifications = it }
                    )
                }
            }

            // Sección: Datos y Exportación
            item {
                SettingsSectionTitle("Datos y Exportación")
                SettingsCard {
                    SettingsClickableRow(
                        icon = Icons.Default.FileDownload,
                        iconColor = SuccessGreen,
                        title = "Exportar Inventario (CSV)",
                        subtitle = "Descarga un Excel con todo tu stock",
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Exportación simulada con éxito")
                            }
                        }
                    )
                    Divider(color = MediumGray.copy(alpha = 0.1f), modifier = Modifier.padding(start = 56.dp))
                    SettingsClickableRow(
                        icon = Icons.Default.CloudSync,
                        iconColor = Color(0xFF29B6F6),
                        title = "Sincronizar en la nube",
                        subtitle = "Respaldar datos manualmente",
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Sincronización simulada...")
                            }
                        }
                    )
                }
            }

            // Sección: Cuenta y Seguridad
            item {
                SettingsSectionTitle("Cuenta y Seguridad")
                SettingsCard {
                    SettingsClickableRow(
                        icon = Icons.Default.LockReset,
                        iconColor = Color(0xFFFFB300),
                        title = "Cambiar Contraseña",
                        subtitle = "Actualiza tu clave de acceso",
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Función de cambio de contraseña en desarrollo")
                            }
                        }
                    )
                    Divider(color = MediumGray.copy(alpha = 0.1f), modifier = Modifier.padding(start = 56.dp))
                    SettingsClickableRow(
                        icon = Icons.Default.ExitToApp,
                        iconColor = ErrorRed,
                        title = "Cerrar Sesión",
                        subtitle = "Salir de tu cuenta actual",
                        onClick = onLogoutClick
                    )
                }
            }
            
            // Versión de la app
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("AppStock v1.0.0", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                    Text("Hecho con ♥ para tu inventario", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 14.sp,
        color = OrangeRed,
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        content = content
    )
}

@Composable
fun SettingsSwitchRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = Color.LightGray
            )
        )
    }
}

@Composable
fun SettingsClickableRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (iconColor == ErrorRed) ErrorRed else MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
