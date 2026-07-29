package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SlotDarkColorScheme = darkColorScheme(
    primary = SlotGoldPrimary,
    onPrimary = SlotDarkBackground,
    primaryContainer = SlotSurfaceVariant,
    onPrimaryContainer = SlotGoldPrimary,
    secondary = SlotCyanAccent,
    onSecondary = SlotDarkBackground,
    secondaryContainer = SlotSurfaceVariant,
    onSecondaryContainer = SlotCyanAccent,
    tertiary = SlotEmeraldGreen,
    onTertiary = SlotDarkBackground,
    background = SlotDarkBackground,
    onBackground = TextPrimaryDark,
    surface = SlotSurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SlotSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = SlotCardBorder
)

@Composable
fun SlotEngineTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SlotDarkColorScheme,
        typography = Typography,
        content = content
    )
}

// Alias for backwards compatibility
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    SlotEngineTheme(darkTheme = darkTheme, content = content)
}
