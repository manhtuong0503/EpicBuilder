package com.epicbuilder.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.epicbuilder.data.model.Element

val PurplePrimary = Color(0xFFA29BFE)
val PurpleDeep = Color(0xFF6C5CE7)
val GoldAccent = Color(0xFFF3C969)
val DarkBackground = Color(0xFF12131F)
val DarkSurface = Color(0xFF1C1E2E)

private val DarkScheme = darkColorScheme(
    primary = PurplePrimary,
    onPrimary = Color(0xFF1A1B2E),
    secondary = GoldAccent,
    onSecondary = Color(0xFF1A1B2E),
    background = DarkBackground,
    onBackground = Color(0xFFE8E8F0),
    surface = DarkSurface,
    onSurface = Color(0xFFE8E8F0),
    surfaceVariant = Color(0xFF272A3F),
    onSurfaceVariant = Color(0xFFB9BAC9)
)

private val LightScheme = lightColorScheme(
    primary = PurpleDeep,
    secondary = Color(0xFFB8860B)
)

@Composable
fun EpicBuilderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        content = content
    )
}

/** Màu đại diện cho từng hệ nguyên tố. */
fun Element.color(): Color = when (this) {
    Element.FIRE -> Color(0xFFE74C3C)
    Element.ICE -> Color(0xFF4AA3DF)
    Element.EARTH -> Color(0xFF2ECC71)
    Element.LIGHT -> Color(0xFFF1C40F)
    Element.DARK -> Color(0xFFB07CC6)
}
