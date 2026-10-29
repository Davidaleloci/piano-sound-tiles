package com.pianosound.tiles.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    secondary = NeonGold,
    tertiary = NeonPurple,
    background = DeepBackground,
    surface = SurfaceDark,
    onPrimary = DeepBackground,
    onSecondary = DeepBackground,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun PianoSoundTilesTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
