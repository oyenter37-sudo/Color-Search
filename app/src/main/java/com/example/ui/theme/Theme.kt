package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ChromaDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color.Black,
    primaryContainer = NeonViolet,
    onPrimaryContainer = Color.White,
    secondary = NeonPink,
    onSecondary = Color.White,
    tertiary = NeonAmber,
    background = CyberObsidian,
    onBackground = TextPrimary,
    surface = CyberSurface,
    onSurface = TextPrimary,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = CyberBorder,
    error = NeonRed
)

@Composable
fun ChromaHuntTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ChromaDarkColorScheme,
        typography = Typography,
        content = content
    )
}
