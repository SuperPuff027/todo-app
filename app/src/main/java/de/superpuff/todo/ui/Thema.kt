package de.superpuff.todo.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Neutrale Flächen, eine Akzentfarbe (Salbeigrün), ein warmer Ton nur für das „seit X Tagen offen“-Label. */
val HelleFarben: ColorScheme = lightColorScheme(
    primary = Color(0xFF2F6B5E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD3E8E1),
    onPrimaryContainer = Color(0xFF0F2A24),
    secondary = Color(0xFF5E6B67),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFECEAE5),
    onSecondaryContainer = Color(0xFF1C1B19),
    tertiary = Color(0xFFA35A30),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF8E3D6),
    onTertiaryContainer = Color(0xFF5A2C12),
    background = Color(0xFFF6F5F2),
    onBackground = Color(0xFF1C1B19),
    surface = Color(0xFFF6F5F2),
    onSurface = Color(0xFF1C1B19),
    surfaceVariant = Color(0xFFECEAE5),
    onSurfaceVariant = Color(0xFF6B6A66),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF1F0EC),
    surfaceContainer = Color(0xFFEEEDE9),
    surfaceContainerHigh = Color(0xFFE8E6E1),
    surfaceContainerHighest = Color(0xFFE2E0DB),
    outline = Color(0xFFA9A7A1),
    outlineVariant = Color(0xFFD9D7D1),
    inverseSurface = Color(0xFF2F302E),
    inverseOnSurface = Color(0xFFF2F0EB),
    inversePrimary = Color(0xFF8FCFBD),
)

val DunkleFarben: ColorScheme = darkColorScheme(
    primary = Color(0xFF8FCFBD),
    onPrimary = Color(0xFF0F2A24),
    primaryContainer = Color(0xFF1F4A41),
    onPrimaryContainer = Color(0xFFD3E8E1),
    secondary = Color(0xFFB7C3BF),
    onSecondary = Color(0xFF1E2A27),
    secondaryContainer = Color(0xFF262927),
    onSecondaryContainer = Color(0xFFE6E4DF),
    tertiary = Color(0xFFE9A97F),
    onTertiary = Color(0xFF4A2A18),
    tertiaryContainer = Color(0xFF4A2A18),
    onTertiaryContainer = Color(0xFFF8E3D6),
    background = Color(0xFF151716),
    onBackground = Color(0xFFE6E4DF),
    surface = Color(0xFF151716),
    onSurface = Color(0xFFE6E4DF),
    surfaceVariant = Color(0xFF262927),
    onSurfaceVariant = Color(0xFFA3A19C),
    surfaceContainerLowest = Color(0xFF101211),
    surfaceContainerLow = Color(0xFF1A1C1B),
    surfaceContainer = Color(0xFF1E201F),
    surfaceContainerHigh = Color(0xFF262927),
    surfaceContainerHighest = Color(0xFF303331),
    outline = Color(0xFF6E716F),
    outlineVariant = Color(0xFF3A3D3B),
    inverseSurface = Color(0xFFE6E4DF),
    inverseOnSurface = Color(0xFF2F302E),
    inversePrimary = Color(0xFF2F6B5E),
)

/** Keine eckigen Kanten: auch die kleinste Form ist deutlich gerundet. */
val RundeFormen = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun TodoThema(inhalt: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DunkleFarben else HelleFarben,
        shapes = RundeFormen,
        content = inhalt,
    )
}
