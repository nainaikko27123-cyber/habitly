package com.naikko.habitly.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

// Calm indigo brand with soft, low-contrast surfaces.

internal val LightColors = lightColorScheme(
    primary = Color(0xFF4B57C9),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDFE0FF),
    onPrimaryContainer = Color(0xFF0E1663),
    secondary = Color(0xFF5C5D72),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE1E0F9),
    onSecondaryContainer = Color(0xFF191A2C),
    tertiary = Color(0xFFB4473B),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDAD4),
    onTertiaryContainer = Color(0xFF410001),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    background = Color(0xFFF9F8FD),
    onBackground = Color(0xFF1B1B21),
    surface = Color(0xFFF9F8FD),
    onSurface = Color(0xFF1B1B21),
    surfaceVariant = Color(0xFFE3E1EC),
    onSurfaceVariant = Color(0xFF5E5D6B),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF3F2F9),
    surfaceContainer = Color(0xFFEDECF4),
    surfaceContainerHigh = Color(0xFFE7E6EE),
    surfaceContainerHighest = Color(0xFFE1E0E9),
    outline = Color(0xFF8F8E9B),
    outlineVariant = Color(0xFFD8D7E2),
)

internal val DarkColors = darkColorScheme(
    primary = Color(0xFFBCC2FF),
    onPrimary = Color(0xFF1D2678),
    primaryContainer = Color(0xFF343FAF),
    onPrimaryContainer = Color(0xFFDFE0FF),
    secondary = Color(0xFFC5C4DD),
    onSecondary = Color(0xFF2E2F42),
    secondaryContainer = Color(0xFF444559),
    onSecondaryContainer = Color(0xFFE1E0F9),
    tertiary = Color(0xFFFFB4A8),
    onTertiary = Color(0xFF690004),
    tertiaryContainer = Color(0xFF912F25),
    onTertiaryContainer = Color(0xFFFFDAD4),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    background = Color(0xFF111116),
    onBackground = Color(0xFFE4E3EC),
    surface = Color(0xFF111116),
    onSurface = Color(0xFFE4E3EC),
    surfaceVariant = Color(0xFF46464F),
    onSurfaceVariant = Color(0xFFC7C6D3),
    surfaceContainerLowest = Color(0xFF0C0C10),
    surfaceContainerLow = Color(0xFF18181E),
    surfaceContainer = Color(0xFF1D1D24),
    surfaceContainerHigh = Color(0xFF26262E),
    surfaceContainerHighest = Color(0xFF313139),
    outline = Color(0xFF91909D),
    outlineVariant = Color(0xFF46464F),
)

/** Accent colors a habit can use. Mid-tones that read well on both light and dark surfaces. */
object HabitPalette {
    val colors = listOf(
        Color(0xFF6C78F0), // indigo
        Color(0xFFF2766A), // coral
        Color(0xFF2FB98A), // mint
        Color(0xFFF2AE3D), // amber
        Color(0xFF45A8F0), // sky
        Color(0xFFB57BEB), // violet
        Color(0xFFEE6AA7), // pink
        Color(0xFF86B84A), // lime
    )

    fun color(index: Int): Color = colors[index.mod(colors.size)]

    /** Readable content color (icons, check marks) on top of a palette color. */
    fun onColor(color: Color): Color = if (color.luminance() > 0.5f) Color(0xFF1B1B21) else Color.White

    /** Stable color for a note tag, picked from its text. */
    fun forTag(tag: String): Color = color(tag.lowercase().hashCode())
}
