package org.example.project.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import org.example.project.theme.AquariumColors

@Composable
fun FishGlyph(
    color: Color = AquariumColors.Lime,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w * 0.45f
        val cy = h * 0.55f
        val bodyR = (w.coerceAtMost(h)) * 0.32f

        val body = Path().apply {
            moveTo(cx - bodyR * 1.4f, cy)
            cubicTo(
                cx - bodyR * 0.9f, cy - bodyR,
                cx + bodyR * 0.6f, cy - bodyR * 0.95f,
                cx + bodyR * 1.05f, cy
            )
            cubicTo(
                cx + bodyR * 0.6f, cy + bodyR * 0.95f,
                cx - bodyR * 0.9f, cy + bodyR,
                cx - bodyR * 1.4f, cy
            )
            close()
        }
        val tail = Path().apply {
            moveTo(cx - bodyR * 1.4f, cy)
            lineTo(cx - bodyR * 2.2f, cy - bodyR * 0.9f)
            lineTo(cx - bodyR * 2.2f, cy + bodyR * 0.9f)
            close()
        }
        drawPath(body, color.copy(alpha = 0.85f))
        drawPath(tail, color.copy(alpha = 0.7f))

        drawCircle(
            color = AquariumColors.DeepOcean,
            radius = bodyR * 0.13f,
            center = Offset(cx + bodyR * 0.6f, cy - bodyR * 0.18f)
        )
        drawCircle(
            color = AquariumColors.White,
            radius = bodyR * 0.05f,
            center = Offset(cx + bodyR * 0.62f, cy - bodyR * 0.22f)
        )
    }
}
