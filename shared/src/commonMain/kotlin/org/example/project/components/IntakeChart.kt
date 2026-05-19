package org.example.project.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.hydration.Bucket
import org.example.project.theme.AquariumColors

@Composable
fun IntakeChart(
    buckets: List<Bucket>,
    targetMl: Int,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 160.dp,
) {
    val maxValue = (buckets.maxOfOrNull { it.totalMl } ?: 0).coerceAtLeast(targetMl).coerceAtLeast(500)
    val animated by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 700),
        label = "chart",
    )
    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .padding(vertical = 4.dp)
        ) {
            val w = size.width
            val h = size.height
            val n = buckets.size.coerceAtLeast(1)
            val barAreaH = h - 14f
            val totalGap = (n - 1) * 4f
            val barW = ((w - totalGap) / n).coerceAtLeast(1f)
            // Faint target line.
            val targetY = barAreaH * (1f - targetMl.toFloat() / maxValue) + 4f
            drawLine(
                color = AquariumColors.Lime.copy(alpha = 0.55f),
                start = Offset(0f, targetY),
                end = Offset(w, targetY),
                strokeWidth = 1.5f,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                    floatArrayOf(8f, 6f), 0f
                ),
            )
            buckets.forEachIndexed { i, b ->
                val ratio = (b.totalMl.toFloat() / maxValue).coerceIn(0f, 1f) * animated
                val barH = (barAreaH * ratio).coerceAtLeast(if (b.totalMl > 0) 2f else 0f)
                val x = i * (barW + 4f)
                val y = barAreaH - barH + 4f
                if (b.totalMl > 0) {
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF9FE8FF),
                                Color(0xFF1F9CD8),
                            ),
                            startY = y,
                            endY = y + barH,
                        ),
                        topLeft = Offset(x, y),
                        size = Size(barW, barH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(barW / 3f, barW / 3f),
                    )
                } else {
                    drawRoundRect(
                        color = AquariumColors.GlassBlueStrong.copy(alpha = 0.4f),
                        topLeft = Offset(x, barAreaH - 2f + 4f),
                        size = Size(barW, 2f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(1f, 1f),
                    )
                }
            }
            // Baseline.
            drawLine(
                color = AquariumColors.PaleAqua.copy(alpha = 0.4f),
                start = Offset(0f, h - 8f),
                end = Offset(w, h - 8f),
                strokeWidth = 1f,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Up to 6 evenly-spaced labels to avoid overlap.
            val labels = buildList {
                val n = buckets.size
                if (n == 0) return@buildList
                val step = maxOf(1, n / 6)
                for (i in 0 until n step step) add(buckets[i].label)
                if (last() != buckets.last().label) add(buckets.last().label)
            }
            labels.forEach { label ->
                Text(
                    text = label,
                    color = AquariumColors.PaleAqua,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}
