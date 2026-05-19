package org.example.project.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import org.example.project.theme.AquariumColors
import org.example.project.theme.AquariumGradients
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class Bubble(
    val startX: Float,
    val radius: Float,
    val speed: Float,
    val phase: Float,
    val drift: Float,
)

private data class FishSilhouette(
    val yFraction: Float,
    val speed: Float,
    val phase: Float,
    val size: Float,
    val flip: Boolean,
)

@Composable
fun AquariumBackground(
    modifier: Modifier = Modifier,
    bubbleCount: Int = 16,
    fishCount: Int = 4,
    content: @Composable () -> Unit
) {
    val bubbles = remember {
        val r = Random(7)
        List(bubbleCount) {
            Bubble(
                startX = r.nextFloat(),
                radius = 4f + r.nextFloat() * 14f,
                speed = 6f + r.nextFloat() * 10f,
                phase = r.nextFloat(),
                drift = (r.nextFloat() - 0.5f) * 0.06f,
            )
        }
    }
    val fish = remember {
        val r = Random(13)
        List(fishCount) {
            FishSilhouette(
                yFraction = 0.15f + r.nextFloat() * 0.7f,
                speed = 12f + r.nextFloat() * 14f,
                phase = r.nextFloat(),
                size = 32f + r.nextFloat() * 28f,
                flip = r.nextBoolean(),
            )
        }
    }

    val transition = rememberInfiniteTransition()
    val bubbleProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val fishProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val rayShimmer by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(modifier = modifier.fillMaxSize().background(AquariumGradients.Background)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
//            drawLightRays(rayShimmer)
            fish.forEach { drawFish(it, fishProgress) }
            bubbles.forEach { drawBubble(it, bubbleProgress) }
        }
        content()
    }
}

private fun DrawScope.drawLightRays(shimmer: Float) {
    val w = size.width
    val h = size.height
    val rayCount = 5
    repeat(rayCount) { i ->
        val baseX = w * (0.15f + i * 0.18f)
        val offset = sin((shimmer + i * 0.31f) * PI.toFloat() * 2f) * 30f
        val path = Path().apply {
            moveTo(baseX + offset - 30f, 0f)
            lineTo(baseX + offset + 30f, 0f)
            lineTo(baseX + offset + 160f, h * 0.7f)
            lineTo(baseX + offset - 160f, h * 0.7f)
            close()
        }
        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0x55D6F2FF),
                    Color(0x00D6F2FF),
                ),
                startY = 0f,
                endY = h * 0.7f
            )
        )
    }
}

private fun DrawScope.drawBubble(bubble: Bubble, progress: Float) {
    val w = size.width
    val h = size.height
    val cycle = (bubble.phase + progress * (bubble.speed / 10f)) % 1f
    val y = h - cycle * h
    val swayPhase = (progress + bubble.phase) * 2f * PI.toFloat()
    val x = bubble.startX * w + sin(swayPhase) * (bubble.drift * w)
    drawCircle(
        color = Color(0x44FFFFFF),
        radius = bubble.radius,
        center = Offset(x, y)
    )
    drawCircle(
        color = Color(0x77FFFFFF),
        radius = bubble.radius,
        center = Offset(x, y),
        style = Stroke(width = 1.5f)
    )
}

private fun DrawScope.drawFish(fish: FishSilhouette, progress: Float) {
    val w = size.width
    val h = size.height
    val direction = if (fish.flip) -1f else 1f
    val travel = ((progress * (fish.speed / 20f) + fish.phase) % 1.4f) - 0.2f
    val x = (if (direction > 0) travel else 1f - travel) * w
    val y = fish.yFraction * h + sin(progress * 2f * PI.toFloat() + fish.phase * 6f) * 12f
    val s = fish.size

    val body = Path().apply {
        moveTo(x, y)
        cubicTo(
            x + direction * s * 0.4f, y - s * 0.35f,
            x + direction * s * 1.2f, y - s * 0.2f,
            x + direction * s * 1.5f, y
        )
        cubicTo(
            x + direction * s * 1.2f, y + s * 0.2f,
            x + direction * s * 0.4f, y + s * 0.35f,
            x, y
        )
        close()
    }
    val tail = Path().apply {
        moveTo(x, y)
        lineTo(x - direction * s * 0.5f, y - s * 0.4f)
        lineTo(x - direction * s * 0.5f, y + s * 0.4f)
        close()
    }
    val color = Color(0x22FFFFFF)
    drawPath(body, color)
    drawPath(tail, color)
}

@Composable
fun AnimatedBubbleStrip(
    modifier: Modifier = Modifier,
    bubbleCount: Int = 6,
    color: Color = AquariumColors.PaleAqua.copy(alpha = 0.35f)
) {
    val bubbles = remember {
        val r = Random(31)
        List(bubbleCount) {
            Bubble(
                startX = r.nextFloat(),
                radius = 3f + r.nextFloat() * 6f,
                speed = 8f + r.nextFloat() * 8f,
                phase = r.nextFloat(),
                drift = (r.nextFloat() - 0.5f) * 0.04f,
            )
        }
    }
    val transition = rememberInfiniteTransition()
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    Canvas(modifier = modifier) {
        bubbles.forEach { b ->
            val cycle = (b.phase + progress * (b.speed / 8f)) % 1f
            val y = size.height - cycle * size.height
            val x = b.startX * size.width + cos(progress * 2f * PI.toFloat()) * 6f
            drawCircle(color, b.radius, Offset(x, y))
        }
    }
}
