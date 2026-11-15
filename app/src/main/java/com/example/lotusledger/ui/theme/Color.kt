package com.example.lotusledger.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// D4: my own brand. Lotus rose (primary), jade green (secondary), temple gold (tertiary).
// These are the ONLY places where colours are written. Composables use the theme roles.

val LightColors = lightColorScheme(
    primary = Color(0xFFA03B63),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFD9E4),
    onPrimaryContainer = Color(0xFF3E001D),
    secondary = Color(0xFF2F6B5B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFB3F0DC),
    onSecondaryContainer = Color(0xFF00201A),
    tertiary = Color(0xFF8A6A00),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDF92),
    onTertiaryContainer = Color(0xFF2A1F00),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFFF8F8),
    onBackground = Color(0xFF22191B),
    surface = Color(0xFFFFF8F8),
    onSurface = Color(0xFF22191B),
    surfaceVariant = Color(0xFFF4DDE2),
    onSurfaceVariant = Color(0xFF524347),
    outline = Color(0xFF857377),
    outlineVariant = Color(0xFFD7C1C6),
    inverseSurface = Color(0xFF372E30),
    inverseOnSurface = Color(0xFFFDEDEF),
    inversePrimary = Color(0xFFFFB0CB)
)

val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB0CB),
    onPrimary = Color(0xFF620B33),
    primaryContainer = Color(0xFF7F2249),
    onPrimaryContainer = Color(0xFFFFD9E4),
    secondary = Color(0xFF97D4C0),
    onSecondary = Color(0xFF00382E),
    secondaryContainer = Color(0xFF0F5043),
    onSecondaryContainer = Color(0xFFB3F0DC),
    tertiary = Color(0xFFE9C349),
    onTertiary = Color(0xFF3F2E00),
    tertiaryContainer = Color(0xFF5D4400),
    onTertiaryContainer = Color(0xFFFFDF92),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1A1113),
    onBackground = Color(0xFFF0DEE1),
    surface = Color(0xFF1A1113),
    onSurface = Color(0xFFF0DEE1),
    surfaceVariant = Color(0xFF524347),
    onSurfaceVariant = Color(0xFFD7C1C6),
    outline = Color(0xFF9F8C90),
    outlineVariant = Color(0xFF524347),
    inverseSurface = Color(0xFFF0DEE1),
    inverseOnSurface = Color(0xFF372E30),
    inversePrimary = Color(0xFFA03B63)
)
