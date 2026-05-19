package org.example.project.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.theme.AquariumColors
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun WaterLevel(
    progress: Float,
    currentMl: Int,
    targetMl: Int,
    modifier: Modifier = Modifier,
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 900),
        label = "level",
    )
    val phase by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        )
    )

    Box(modifier = modifier.width(220.dp).height(240.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Glass outline: a tall rounded rectangle, slightly narrower at the top.
            val left = w * 0.18f
            val right = w * 0.82f
            val top = h * 0.06f
            val bottom = h * 0.94f
            val width = right - left

            val outline = Path().apply {
                moveTo(left + 6f, top)
                lineTo(right - 6f, top)
                quadraticTo(right, top, right + 4f, top + 18f)
                lineTo(right - 4f, bottom - 14f)
                quadraticTo(right - 6f, bottom, right - 22f, bottom)
                lineTo(left + 22f, bottom)
                quadraticTo(left + 6f, bottom, left + 4f, bottom - 14f)
                lineTo(left - 4f, top + 18f)
                quadraticTo(left, top, left + 6f, top)
                close()
            }

            // Water region clipped to the glass shape.
            val waterTopY = bottom - animated * (bottom - top - 16f)
            clipPath(outline) {
                // Water gradient fill from current level to bottom.
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF6BD7FF),
                            Color(0xFF1F9CD8),
                        ),
                        startY = waterTopY,
                        endY = bottom,
                    ),
                    topLeft = Offset(left - 8f, waterTopY),
                    size = Size(width + 16f, bottom - waterTopY),
                )
                // Wavy surface.
                if (animated > 0.001f) {
                    val twoPi = (2.0 * PI).toFloat()
                    val wavePath = Path().apply {
                        moveTo(left - 8f, waterTopY)
                        var x = left - 8f
                        val step = 6f
                        while (x <= right + 8f) {
                            val arg = (x / width) * 2f * twoPi + phase
                            val y = waterTopY + sin(arg.toDouble()).toFloat() * 4f
                            lineTo(x, y)
                            x += step
                        }
                        lineTo(right + 8f, bottom)
                        lineTo(left - 8f, bottom)
                        close()
                    }
                    drawPath(
                        path = wavePath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF9FE8FF),
                                Color(0xFF6BD7FF),
                            ),
                            startY = waterTopY - 8f,
                            endY = waterTopY + 24f,
                        ),
                    )
                    // Secondary phase wave on top for depth.
                    val wave2 = Path().apply {
                        moveTo(left - 8f, waterTopY + 2f)
                        var x = left - 8f
                        val step = 6f
                        while (x <= right + 8f) {
                            val arg = (x / width) * 1.5f * twoPi - phase * 1.4f
                            val y = waterTopY + 2f + sin(arg.toDouble()).toFloat() * 3f
                            lineTo(x, y)
                            x += step
                        }
                        lineTo(right + 8f, waterTopY + 16f)
                        lineTo(left - 8f, waterTopY + 16f)
                        close()
                    }
                    drawPath(
                        path = wave2,
                        color = Color.White.copy(alpha = 0.25f),
                    )
                }
            }

            // Glass border.
            drawPath(
                path = outline,
                color = AquariumColors.White.copy(alpha = 0.85f),
                style = Stroke(width = 3f),
            )
            // Subtle inner highlight on the left.
            drawLine(
                color = Color.White.copy(alpha = 0.45f),
                start = Offset(left + 8f, top + 24f),
                end = Offset(left + 12f, bottom - 36f),
                strokeWidth = 3f,
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$currentMl ml",
                color = AquariumColors.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "of $targetMl ml",
                color = AquariumColors.PaleAqua,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

