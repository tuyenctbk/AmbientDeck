package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AmbientDeckColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004D56),
    onPrimaryContainer = Color(0xFF97F0FF),
    secondary = SunsetAmber,
    onSecondary = Color(0xFF452B00),
    secondaryContainer = Color(0xFF633F00),
    onSecondaryContainer = Color(0xFFFFDF9E),
    tertiary = EmeraldGreen,
    onTertiary = Color(0xFF003915),
    background = VoidBlack,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorderHighlight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AmbientDeckColorScheme,
        typography = Typography,
        content = content
    )
}
