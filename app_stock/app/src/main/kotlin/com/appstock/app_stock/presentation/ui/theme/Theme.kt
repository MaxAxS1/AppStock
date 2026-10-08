package com.appstock.app_stock.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.graphics.Color
import com.appstock.app_stock.domain.repository.SessionManager

// ── Paleta principal extraída de la imagen de referencia ──────────────────────
val OrangeRed     = Color(0xFFFF5200)   // primario: naranja vibrante
val OrangeLight   = Color(0xFFFF7A45)   // secundario: naranja claro
val OrangeSurface = Color(0xFFFFF0EA)   // fondo: durazno muy suave
val OrangeChip    = Color(0xFFFFE0D0)   // chips / tags

val White         = Color(0xFFFFFFFF)
val DarkText      = Color(0xFF1A1A2E)   // texto principal
val MediumGray    = Color(0xFF7B7B93)   // texto secundario
val LightSurface  = Color(0xFFF9F9F9)   // superficie de cards

val SuccessGreen  = Color(0xFF22C55E)
val ErrorRed      = Color(0xFFEF4444)

// ── Colores específicos para Modo Oscuro ──────────────────────────────────────
val DarkBackground = Color(0xFF121212)
val DarkSurface    = Color(0xFF1E1E2E)
val LightText      = Color(0xFFF0F0F0)
val DarkChip       = Color(0xFF2A2A3E)
// ──────────────────────────────────────────────────────────────────────────────

private val AppColorScheme = lightColorScheme(
    primary            = OrangeRed,
    onPrimary          = White,
    primaryContainer   = OrangeChip,
    onPrimaryContainer = OrangeRed,
    secondary          = OrangeLight,
    onSecondary        = White,
    tertiary           = OrangeSurface,
    background         = OrangeSurface, // Fondo principal
    surface            = White,         // Fondo de tarjetas
    surfaceVariant     = LightSurface,
    onBackground       = DarkText,
    onSurface          = DarkText,
    onSurfaceVariant   = MediumGray,
    error              = ErrorRed,
    onError            = White
)

private val DarkColorScheme = darkColorScheme(
    primary            = OrangeRed,
    onPrimary          = White,
    primaryContainer   = DarkChip,
    onPrimaryContainer = OrangeLight,
    secondary          = OrangeLight,
    onSecondary        = White,
    tertiary           = DarkSurface,
    background         = DarkBackground,
    surface            = DarkSurface,
    surfaceVariant     = DarkChip,
    onBackground       = LightText,
    onSurface          = LightText,
    onSurfaceVariant   = MediumGray,
    error              = ErrorRed,
    onError            = White
)

@Composable
fun AppStockTheme(
    content: @Composable () -> Unit
) {
    val isDarkModeState by SessionManager.isDarkMode.collectAsStateWithLifecycle()
    val isSystemDark = isSystemInDarkTheme()
    
    val darkTheme = isDarkModeState ?: isSystemDark

    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        AppColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
