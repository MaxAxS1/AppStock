package com.appstock.app_stock.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Paleta principal extraída de la imagen de referencia ──────────────────────
val OrangeRed     = Color(0xFFFF5200)   // primario: naranja vibrante
val OrangeLight   = Color(0xFFFF7A45)   // secundario: naranja claro
val OrangeSurface = Color(0xFFFFF0EA)   // fondo: durazno muy suave
val OrangeChip    = Color(0xFFFFE0D0)   // chips / tags

val White         = Color(0xFFFFFFFF)
val DarkText      = Color(0xFF1A1A2E)   // texto principal
val MediumGray    = Color(0xFF7B7B93)   // texto secundario
val LightSurface  = Color(0xFFF9F9F9)  // superficie de cards

val SuccessGreen  = Color(0xFF22C55E)
val ErrorRed      = Color(0xFFEF4444)
// ──────────────────────────────────────────────────────────────────────────────

private val AppColorScheme = lightColorScheme(
    primary            = OrangeRed,
    onPrimary          = White,
    primaryContainer   = OrangeChip,
    onPrimaryContainer = OrangeRed,
    secondary          = OrangeLight,
    onSecondary        = White,
    tertiary           = OrangeSurface,
    background         = OrangeSurface,
    surface            = White,
    surfaceVariant     = OrangeChip,
    onBackground       = DarkText,
    onSurface          = DarkText,
    onSurfaceVariant   = MediumGray,
    error              = ErrorRed,
    onError            = White
)

@Composable
fun AppStockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        content = content
    )
}
