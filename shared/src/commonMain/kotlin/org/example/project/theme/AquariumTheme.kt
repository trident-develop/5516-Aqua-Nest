package org.example.project.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object AquariumColors {
    val DeepOcean = Color(0xFF0A2A3F)
    val MidOcean = Color(0xFF124D6F)
    val Aqua = Color(0xFF2C8AB5)
    val LightAqua = Color(0xFF6BC8E6)
    val PaleAqua = Color(0xFFB6E4F0)
    val Lime = Color(0xFFC7F583)
    val SoftLime = Color(0xFFD7F9A0)
    val LimeDeep = Color(0xFF8FCF4D)
    val Foam = Color(0xFFEAF8FF)
    val GlassBlue = Color(0x33A6D8E8)
    val GlassBlueStrong = Color(0x55A6D8E8)
    val Stroke = Color(0x66FFFFFF)
    val White = Color(0xFFFFFFFF)
    val MutedAqua = Color(0xFFA9D0DE)
    val Warning = Color(0xFFFFB766)
    val Danger = Color(0xFFFF7F7F)
    val Success = Color(0xFFB7E96F)
}

object AquariumGradients {
    val Background: Brush
        get() = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0B3754),
                Color(0xFF0E5479),
                Color(0xFF1B7AA0),
            )
        )

    val Glass: Brush
        get() = Brush.verticalGradient(
            colors = listOf(
                Color(0x55FFFFFF),
                Color(0x22FFFFFF),
                Color(0x11FFFFFF),
            )
        )

    val Lime: Brush
        get() = Brush.horizontalGradient(
            colors = listOf(
                Color(0xFFC7F583),
                Color(0xFF8FCF4D),
            )
        )
}

@Composable
fun AquariumTheme(content: @Composable () -> Unit) {
    val colors = darkColorScheme(
        primary = AquariumColors.LightAqua,
        onPrimary = AquariumColors.DeepOcean,
        secondary = AquariumColors.Lime,
        onSecondary = AquariumColors.DeepOcean,
        background = AquariumColors.DeepOcean,
        onBackground = AquariumColors.White,
        surface = AquariumColors.MidOcean,
        onSurface = AquariumColors.White,
        surfaceVariant = AquariumColors.Aqua,
        onSurfaceVariant = AquariumColors.Foam,
    )
    MaterialTheme(colorScheme = colors, content = content)
}
