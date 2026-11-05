package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class AppThemeOption(val displayName: String, val description: String) {
    OBSIDIAN("Obsidian Eclipse", "Deep obsidian with royal violet accents"),
    CYBERPUNK("Cyberpunk Neon", "High-contrast neon cyan and electric magenta"),
    STUDIO_GOLD("Studio Gold", "Analog warm charcoal with rich acoustic brass")
}

private val DarkColorScheme = darkColorScheme(
    primary = DarkAccentLight,
    onPrimary = DarkBackground,
    primaryContainer = DarkAccentContainer,
    onPrimaryContainer = DarkTextPrimary,
    secondary = DarkAccent,
    onSecondary = DarkBackground,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceSecondary,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkOutline,
    error = DarkError
)

private val LightColorScheme = lightColorScheme(
    primary = LightAccent,
    onPrimary = LightSurface,
    primaryContainer = LightAccentContainer,
    onPrimaryContainer = LightTextPrimary,
    secondary = LightAccent,
    onSecondary = LightSurface,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceSecondary,
    onSurfaceVariant = LightTextSecondary,
    outline = LightOutline,
    error = LightError
)

private val CyberpunkColorScheme = darkColorScheme(
    primary = Color(0xFF00F0FF),
    onPrimary = Color(0xFF090A0F),
    primaryContainer = Color(0xFF16233B),
    onPrimaryContainer = Color(0xFF00F0FF),
    secondary = Color(0xFFFF003C),
    onSecondary = Color(0xFF090A0F),
    background = Color(0xFF090A0F),
    onBackground = Color(0xFFF0F6FC),
    surface = Color(0xFF12141F),
    onSurface = Color(0xFFF0F6FC),
    surfaceVariant = Color(0xFF1B1E30),
    onSurfaceVariant = Color(0xFFA5B4FC),
    outline = Color(0xFF2E344E),
    error = Color(0xFFFF003C)
)

private val StudioGoldColorScheme = darkColorScheme(
    primary = Color(0xFFE5B869),
    onPrimary = Color(0xFF141312),
    primaryContainer = Color(0xFF332A1C),
    onPrimaryContainer = Color(0xFFF5EFE6),
    secondary = Color(0xFFD4A350),
    onSecondary = Color(0xFF141312),
    background = Color(0xFF141312),
    onBackground = Color(0xFFF5EFE6),
    surface = Color(0xFF1C1A18),
    onSurface = Color(0xFFF5EFE6),
    surfaceVariant = Color(0xFF272421),
    onSurfaceVariant = Color(0xFFC7BDB3),
    outline = Color(0xFF423C36),
    error = Color(0xFFF2A09A)
)

@Composable
fun MyApplicationTheme(
    appTheme: AppThemeOption = AppThemeOption.OBSIDIAN,
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when (appTheme) {
        AppThemeOption.OBSIDIAN -> if (darkTheme) DarkColorScheme else LightColorScheme
        AppThemeOption.CYBERPUNK -> CyberpunkColorScheme
        AppThemeOption.STUDIO_GOLD -> StudioGoldColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
