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

private val DarkColorScheme = darkColorScheme(
    primary = YtRed,
    onPrimary = Color.White,
    primaryContainer = YtRedDark,
    onPrimaryContainer = Color.White,
    secondary = GoldAccent,
    onSecondary = Color.Black,
    tertiary = NeonCyan,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFF3E3E3E)
)

private val LightColorScheme = lightColorScheme(
    primary = YtRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD8D6),
    onPrimaryContainer = Color(0xFF410002),
    secondary = Color(0xFF6B5D00),
    onSecondary = Color.White,
    background = Color(0xFFFBF8F8),
    onBackground = Color(0xFF1F1A1A),
    surface = Color.White,
    onSurface = Color(0xFF1F1A1A),
    surfaceVariant = Color(0xFFF0E0DF),
    onSurfaceVariant = Color(0xFF4E4444),
    outline = Color(0xFF857372)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek dark mode like YouTube Music
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
