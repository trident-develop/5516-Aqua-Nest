package org.example.project.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.example.project.storage.currentTimeMs
import org.example.project.theme.AquariumColors
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private data class FishSpec(
    val baseY: Float,
    val speed: Float,
    val phase: Float,
    val size: Float,
    val color: Color,
    val bobAmp: Float,
    val bobSpeed: Float
)

private data class BubbleSpec(
    val xFraction: Float,
    val speed: Float,
    val phase: Float,
    val radius: Float,
    val wobble: Float
)

private data class PlantSpec(
    val xFraction: Float,
    val height: Float,
    val swaySpeed: Float,
    val phase: Float,
    val color: Color,
    val blades: Int
)

private data class FoodParticle(
    val xFraction: Float,
    val fallSpeed: Float,
    val maxDepth: Float,
    val lifetimeSec: Float,
    val startDelaySec: Float,
)

private const val FOOD_LIFETIME_SEC: Float = 16f

val DefaultAquariumPalette: List<Color> = listOf(
    Color(0xFF0A4B6D),
    Color(0xFF0E6A92),
    Color(0xFF1389B5),
    Color(0xFF0E6A92),
)

private val DefaultFishPalette: List<Color> = listOf(
    AquariumColors.Lime,
    AquariumColors.LightAqua,
    AquariumColors.SoftLime,
    Color(0xFFFFC36B),
    AquariumColors.PaleAqua,
    Color(0xFFFF9DB6),
)

private val DefaultPlantPalette: List<Color> = listOf(
    Color(0xFF3FA66E),
    Color(0xFF4FB07A),
    Color(0xFF38935F),
    Color(0xFF4DA672),
    Color(0xFF358B58),
    Color(0xFF2F7A4D),
)

@Composable
fun AnimatedAquarium(
    modifier: Modifier = Modifier,
    height: Dp = 220.dp,
    fishCount: Int = 6,
    plantCount: Int = 5,
    bubbleCount: Int = 14,
    waterPalette: List<Color> = DefaultAquariumPalette,
    fishPalette: List<Color> = DefaultFishPalette,
    plantPalette: List<Color> = DefaultPlantPalette,
    sandTop: Color = Color(0xFFE8D4A0),
    sandBottom: Color = Color(0xFFB89968),
    pebbleColor: Color = Color(0xFF7A6446),
    fishMinSize: Float = 0.55f,
    fishMaxSize: Float = 1.15f,
    fishSpeedScale: Float = 1f,
    lightIntensity: Float = 1f,
    filterOn: Boolean = true,
    heaterOn: Boolean = true,
    pumpOn: Boolean = true,
    lightOn: Boolean = true,
    foodSpawnedAtMs: Long = 0L,
    seed: Int = 7
) {
    val cloudiness by animateFloatAsState(
        targetValue = if (filterOn) 0f else 1f,
        animationSpec = tween(durationMillis = 900),
        label = "cloudiness",
    )
    val coolTint by animateFloatAsState(
        targetValue = if (heaterOn) 0f else 1f,
        animationSpec = tween(durationMillis = 900),
        label = "coolTint",
    )
    val dimAmount by animateFloatAsState(
        targetValue = if (lightOn) 0f else 0.45f,
        animationSpec = tween(durationMillis = 700),
        label = "dim",
    )
    val lightFactor by animateFloatAsState(
        targetValue = if (lightOn) 1f else 0f,
        animationSpec = tween(durationMillis = 700),
        label = "lightFactor",
    )
    val effectiveLight = lightIntensity * lightFactor
    val filterIndicator by animateFloatAsState(
        targetValue = if (filterOn) 1f else 0f,
        animationSpec = tween(durationMillis = 700),
        label = "filterInd",
    )
    val heaterIndicator by animateFloatAsState(
        targetValue = if (heaterOn) 1f else 0f,
        animationSpec = tween(durationMillis = 700),
        label = "heaterInd",
    )
    val pumpIndicator by animateFloatAsState(
        targetValue = if (pumpOn) 1f else 0f,
        animationSpec = tween(durationMillis = 700),
        label = "pumpInd",
    )

    val foodElapsedSec: Float by produceState(initialValue = -1f, key1 = foodSpawnedAtMs) {
        if (foodSpawnedAtMs <= 0L) {
            value = -1f
            return@produceState
        }
        val initialElapsed = (currentTimeMs() - foodSpawnedAtMs) / 1000f
        if (initialElapsed > FOOD_LIFETIME_SEC) {
            value = -1f
            return@produceState
        }
        while (true) {
            withFrameNanos { }
            val elapsed = (currentTimeMs() - foodSpawnedAtMs) / 1000f
            if (elapsed > FOOD_LIFETIME_SEC) {
                value = -1f
                break
            }
            value = elapsed
        }
    }
    val foodParticles = remember(foodSpawnedAtMs) {
        if (foodSpawnedAtMs <= 0L) emptyList()
        else {
            val rnd = Random(foodSpawnedAtMs.toInt() xor 0x5EED)
            List(14) {
                FoodParticle(
                    xFraction = 0.08f + rnd.nextFloat() * 0.84f,
                    fallSpeed = 0.05f + rnd.nextFloat() * 0.05f,
                    maxDepth = 0.55f + rnd.nextFloat() * 0.30f,
                    lifetimeSec = 5f + rnd.nextFloat() * 8f,
                    startDelaySec = rnd.nextFloat() * 1.2f,
                )
            }
        }
    }
    val fish = remember(fishCount, fishMinSize, fishMaxSize, fishSpeedScale, fishPalette, seed) {
        val rnd = Random(seed)
        val sizeSpan = (fishMaxSize - fishMinSize).coerceAtLeast(0.0f)
        List(fishCount) { i ->
            // Speed is always positive — left/right direction is derived from
            // the back-and-forth position cycle, so heads always lead the motion.
            // Phase is uniform across [0, 2) so each fish enters the cycle from
            // a different point (some moving right, some moving left).
            FishSpec(
                baseY = 0.18f + rnd.nextFloat() * 0.6f,
                speed = (0.08f + rnd.nextFloat() * 0.18f) * fishSpeedScale,
                phase = rnd.nextFloat() * 2f,
                size = fishMinSize + rnd.nextFloat() * sizeSpan,
                color = fishPalette[(i + seed) % fishPalette.size],
                bobAmp = 3f + rnd.nextFloat() * 7f,
                bobSpeed = 1.0f + rnd.nextFloat() * 1.4f,
            )
        }
    }

    val bubbles = remember(bubbleCount, seed) {
        val rnd = Random(seed + 13)
        List(bubbleCount) {
            BubbleSpec(
                xFraction = rnd.nextFloat(),
                speed = 0.35f + rnd.nextFloat() * 0.5f,
                phase = rnd.nextFloat(),
                radius = 2f + rnd.nextFloat() * 4f,
                wobble = 4f + rnd.nextFloat() * 8f
            )
        }
    }

    val plants = remember(plantCount, plantPalette, seed) {
        val rnd = Random(seed + 99)
        val count = plantCount.coerceAtLeast(1)
        List(count) { i ->
            PlantSpec(
                xFraction = (i + 0.5f) / count + (rnd.nextFloat() - 0.5f) * 0.05f,
                height = 0.35f + rnd.nextFloat() * 0.30f,
                swaySpeed = 0.5f + rnd.nextFloat() * 0.4f,
                phase = rnd.nextFloat(),
                color = plantPalette[(i + seed) % plantPalette.size],
                blades = 4 + (rnd.nextInt(2)),
            )
        }
    }

    // Monotonically-increasing time so wraps inside drawing math stay continuous
    // across frames. Scaled the same as the previous 12s-cycle `t` (i.e. seconds/12).
    val t by produceState(initialValue = 0f) {
        var startNanos = -1L
        while (true) {
            withFrameNanos { nanos ->
                if (startNanos < 0L) startNanos = nanos
                value = (nanos - startNanos) / 1_000_000_000f / 12f
            }
        }
    }
    // Shimmer keeps the smooth Reverse ping-pong (no discontinuity).
    val shimmer by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val frameShape = RoundedCornerShape(24.dp)
    Box(
        modifier = modifier
            .height(height)
            .clip(frameShape)
            .background(
                Brush.verticalGradient(colors = waterPalette)
            )
            .border(
                width = 2.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.85f),
                        AquariumColors.LightAqua.copy(alpha = 0.55f),
                        AquariumColors.Lime.copy(alpha = 0.70f),
                    )
                ),
                shape = frameShape
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Sunlight rays from the top
            drawLightRays(w, h, t, shimmer, effectiveLight)

            // Subtle water caustics overlay
            drawCaustics(w, h, t, effectiveLight)

            // Background plants
            plants.forEachIndexed { i, p ->
                drawSeaweed(
                    centerX = p.xFraction * w,
                    bottomY = h - 6f,
                    height = p.height * h,
                    blades = p.blades,
                    color = p.color.copy(alpha = 0.85f),
                    time = t,
                    speed = p.swaySpeed,
                    phase = p.phase
                )
            }

            // Sand floor
            drawSand(w, h, sandTop, sandBottom, pebbleColor)

            // Fish
            fish.forEach { f ->
                val travel = (t * f.speed * 4f + f.phase) % 2f
                val xFrac = if (travel < 1f) travel else 2f - travel
                val swimRight = travel < 1f
                val x = xFrac * (w + 80f) - 40f
                val bob = sin((t * f.bobSpeed * 2f * PI + f.phase * 6.28f).toFloat()) * f.bobAmp
                val y = f.baseY * h + bob
                drawFish(
                    cx = x,
                    cy = y,
                    scale = f.size,
                    color = f.color,
                    facingRight = swimRight,
                    tailWag = sin((t * 18f + f.phase * 6.28f).toFloat())
                )
            }

            // Bubbles
            bubbles.forEach { b ->
                val progress = ((t * b.speed) + b.phase) % 1f
                val y = h - 12f - progress * (h - 20f)
                val wobbleX = sin((progress * 6f * PI).toFloat()) * b.wobble
                val x = b.xFraction * w + wobbleX
                val alpha = (1f - progress).coerceIn(0f, 1f) * 0.7f
                drawCircle(
                    color = Color.White.copy(alpha = alpha * 0.6f),
                    radius = b.radius,
                    center = Offset(x, y)
                )
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = b.radius * 0.4f,
                    center = Offset(x - b.radius * 0.3f, y - b.radius * 0.3f)
                )
            }

            // Glass highlight at the top
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.18f * effectiveLight.coerceAtMost(1f)),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = h * 0.18f
                ),
                size = Size(w, h * 0.18f)
            )

            // Glass reflection streak
            val streakX = w * 0.18f + shimmer * w * 0.05f
            drawLine(
                color = Color.White.copy(alpha = 0.10f * effectiveLight.coerceAtMost(1f)),
                start = Offset(streakX, h * 0.08f),
                end = Offset(streakX + w * 0.1f, h * 0.85f),
                strokeWidth = 24f
            )

            // Pump ON: dedicated bubble column from a bottom-left air stone
            if (pumpIndicator > 0.01f) {
                drawPumpStream(w, h, t, pumpIndicator)
            }

            // Filter ON: small outflow stream at top-right
            if (filterIndicator > 0.01f) {
                drawFilterOutflow(w, h, t, filterIndicator)
            }

            // Heater ON: red LED + warm glow on the right wall
            if (heaterIndicator > 0.01f) {
                drawHeater(w, h, t, heaterIndicator)
            }

            // Food particles falling after a Feed action
            if (foodElapsedSec >= 0f && foodParticles.isNotEmpty()) {
                drawFood(w, h, foodElapsedSec, foodParticles)
            }

            // Filter-off: murky cloudy overlay (greenish-white haze)
            if (cloudiness > 0.01f) {
                drawRect(
                    color = Color(0xFFB8C9A8).copy(alpha = (cloudiness * 0.35f).coerceIn(0f, 0.4f)),
                    size = Size(w, h),
                )
            }

            // Heater-off: cool grey-blue overlay
            if (coolTint > 0.01f) {
                drawRect(
                    color = Color(0xFF6F8AA0).copy(alpha = (coolTint * 0.3f).coerceIn(0f, 0.35f)),
                    size = Size(w, h),
                )
            }

            // Light-off: dim everything
            if (dimAmount > 0.01f) {
                drawRect(
                    color = Color.Black.copy(alpha = dimAmount.coerceIn(0f, 0.7f)),
                    size = Size(w, h),
                )
            }
        }

        // Glass frame border
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Transparent)
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLightRays(
    w: Float,
    h: Float,
    t: Float,
    shimmer: Float,
    intensity: Float
) {
    if (intensity <= 0f) return
    val rayCount = 5
    for (i in 0 until rayCount) {
        val baseX = w * (0.15f + i * 0.18f) + sin((t * 2f * PI + i).toFloat()) * 8f
        val path = Path().apply {
            moveTo(baseX - 18f, 0f)
            lineTo(baseX + 18f, 0f)
            lineTo(baseX + 60f, h * 0.85f)
            lineTo(baseX - 60f, h * 0.85f)
            close()
        }
        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = (0.10f + shimmer * 0.05f) * intensity),
                    Color.White.copy(alpha = 0.0f)
                ),
                startY = 0f,
                endY = h * 0.85f
            )
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCaustics(
    w: Float,
    h: Float,
    t: Float,
    intensity: Float
) {
    if (intensity <= 0f) return
    for (i in 0 until 3) {
        val y = h * (0.15f + i * 0.18f) + sin((t * 2f * PI + i * 1.3f).toFloat()) * 6f
        drawLine(
            color = Color.White.copy(alpha = 0.05f * intensity),
            start = Offset(0f, y),
            end = Offset(w, y + sin((t * 4f * PI).toFloat()) * 4f),
            strokeWidth = 14f
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSand(
    w: Float,
    h: Float,
    sandTop: Color,
    sandBottom: Color,
    pebbleColor: Color
) {
    val sandPath = Path().apply {
        moveTo(0f, h - 6f)
        var x = 0f
        while (x <= w) {
            val y = h - 6f - (sin((x / w) * PI * 2.5f).toFloat() * 4f) - 4f
            lineTo(x, y)
            x += 12f
        }
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(
        path = sandPath,
        brush = Brush.verticalGradient(
            colors = listOf(sandTop, sandBottom),
            startY = h - 18f,
            endY = h
        )
    )
    // pebbles
    val pebbles = listOf(
        Triple(0.12f, 0.97f, 4f),
        Triple(0.28f, 0.965f, 3f),
        Triple(0.55f, 0.97f, 5f),
        Triple(0.78f, 0.968f, 4f),
        Triple(0.92f, 0.972f, 3f),
    )
    pebbles.forEach { (xf, yf, r) ->
        drawCircle(
            color = pebbleColor,
            radius = r,
            center = Offset(xf * w, yf * h)
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSeaweed(
    centerX: Float,
    bottomY: Float,
    height: Float,
    blades: Int,
    color: Color,
    time: Float,
    speed: Float,
    phase: Float
) {
    for (b in 0 until blades) {
        val offsetX = (b - blades / 2f) * 6f
        val sway = sin((time * speed * 2f * PI + phase * 6.28f + b * 0.4f).toFloat())
        val path = Path().apply {
            moveTo(centerX + offsetX, bottomY)
            val segments = 6
            for (s in 1..segments) {
                val fraction = s / segments.toFloat()
                val y = bottomY - fraction * height
                val swayAmt = sway * fraction * 14f
                val cx = centerX + offsetX + swayAmt
                quadraticBezierTo(
                    centerX + offsetX + swayAmt * 0.5f,
                    bottomY - (fraction - 0.5f / segments) * height,
                    cx,
                    y
                )
            }
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 4f)
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFish(
    cx: Float,
    cy: Float,
    scale: Float,
    color: Color,
    facingRight: Boolean,
    tailWag: Float
) {
    val bodyW = 36f * scale
    val bodyH = 18f * scale
    val flip = if (facingRight) 1f else -1f

    translate(left = cx, top = cy) {
        scale(scaleX = flip, scaleY = 1f, pivot = Offset.Zero) {
            // Tail
            val tailPath = Path().apply {
                moveTo(-bodyW * 0.45f, 0f)
                lineTo(-bodyW * 0.9f, -bodyH * 0.6f + tailWag * 4f)
                lineTo(-bodyW * 0.75f, 0f)
                lineTo(-bodyW * 0.9f, bodyH * 0.6f + tailWag * 4f)
                close()
            }
            drawPath(
                path = tailPath,
                brush = Brush.horizontalGradient(
                    colors = listOf(color.copy(alpha = 0.55f), color),
                    startX = -bodyW * 0.9f,
                    endX = -bodyW * 0.45f
                )
            )

            // Body
            drawOval(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        color.copy(alpha = 0.95f),
                        color.copy(alpha = 0.7f)
                    ),
                    startY = -bodyH,
                    endY = bodyH
                ),
                topLeft = Offset(-bodyW * 0.5f, -bodyH * 0.5f),
                size = Size(bodyW, bodyH)
            )

            // Belly highlight
            drawOval(
                color = Color.White.copy(alpha = 0.25f),
                topLeft = Offset(-bodyW * 0.3f, -bodyH * 0.35f),
                size = Size(bodyW * 0.5f, bodyH * 0.35f)
            )

            // Top fin
            val finPath = Path().apply {
                moveTo(-bodyW * 0.1f, -bodyH * 0.45f)
                quadraticBezierTo(
                    -bodyW * 0.05f, -bodyH * 1.0f - tailWag * 2f,
                    bodyW * 0.18f, -bodyH * 0.45f
                )
                close()
            }
            drawPath(
                path = finPath,
                color = color.copy(alpha = 0.75f)
            )

            // Eye
            drawCircle(
                color = Color.White,
                radius = bodyH * 0.18f,
                center = Offset(bodyW * 0.28f, -bodyH * 0.08f)
            )
            drawCircle(
                color = Color.Black,
                radius = bodyH * 0.09f,
                center = Offset(bodyW * 0.32f, -bodyH * 0.08f)
            )
            drawCircle(
                color = Color.White,
                radius = bodyH * 0.035f,
                center = Offset(bodyW * 0.34f, -bodyH * 0.12f)
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPumpStream(
    w: Float,
    h: Float,
    t: Float,
    indicator: Float
) {
    val sx = w * 0.08f
    val stoneR = 7f
    // air stone at the bottom
    drawCircle(
        color = Color(0xFF2E2E2E).copy(alpha = 0.7f * indicator),
        radius = stoneR,
        center = Offset(sx, h - stoneR - 2f)
    )
    drawCircle(
        color = Color(0xFF555555).copy(alpha = 0.6f * indicator),
        radius = stoneR * 0.55f,
        center = Offset(sx - 1.5f, h - stoneR - 4f)
    )
    // bubble column
    val count = 7
    for (i in 0 until count) {
        val phase = (t * 5f + i.toFloat() / count) % 1f
        val y = h - stoneR - 6f - phase * (h - stoneR - 14f)
        val wobble = sin((t * 14f + i).toFloat()) * 3.5f
        val r = 2.5f + (1f - phase) * 2f
        val a = (1f - phase * 0.85f) * 0.85f * indicator
        drawCircle(
            color = Color.White.copy(alpha = a * 0.6f),
            radius = r,
            center = Offset(sx + wobble, y)
        )
        drawCircle(
            color = Color.White.copy(alpha = a),
            radius = r * 0.35f,
            center = Offset(sx + wobble - r * 0.3f, y - r * 0.3f)
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFilterOutflow(
    w: Float,
    h: Float,
    t: Float,
    indicator: Float
) {
    val nozzleX = w * 0.92f
    val nozzleY = h * 0.10f
    // outflow nozzle silhouette
    drawRect(
        color = Color(0xFF2C2C2C).copy(alpha = 0.6f * indicator),
        topLeft = Offset(nozzleX - 4f, 0f),
        size = Size(8f, nozzleY)
    )
    drawLine(
        color = Color(0xFF2C2C2C).copy(alpha = 0.65f * indicator),
        start = Offset(nozzleX, nozzleY),
        end = Offset(nozzleX - 16f, nozzleY + 8f),
        strokeWidth = 6f
    )
    // diagonal stream of small bubbles
    val count = 6
    for (i in 0 until count) {
        val phase = (t * 4f + i.toFloat() / count) % 1f
        val px = nozzleX - 18f - phase * 42f
        val py = nozzleY + 10f + phase * 32f + sin((t * 6f + i).toFloat()) * 2f
        val a = (1f - phase) * 0.6f * indicator
        drawCircle(
            color = Color.White.copy(alpha = a),
            radius = 1.8f,
            center = Offset(px, py)
        )
    }
    // soft ripple arc
    drawCircle(
        color = Color.White.copy(alpha = 0.12f * indicator),
        radius = 12f + sin((t * 6f * PI).toFloat()) * 1.5f,
        center = Offset(nozzleX - 22f, nozzleY + 14f),
        style = Stroke(width = 1.2f)
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHeater(
    w: Float,
    h: Float,
    t: Float,
    indicator: Float
) {
    val hx = w - 14f
    val topY = h * 0.30f
    val botY = h - 14f
    // heater tube
    drawRect(
        color = Color(0xFF3A3A3A).copy(alpha = 0.65f * indicator),
        topLeft = Offset(hx - 4f, topY),
        size = Size(8f, botY - topY)
    )
    drawRect(
        color = Color(0xFF6E6E6E).copy(alpha = 0.4f * indicator),
        topLeft = Offset(hx - 2.5f, topY + 4f),
        size = Size(2f, botY - topY - 8f)
    )
    // pulsing red LED
    val pulse = 0.65f + 0.35f * sin((t * 5f * PI).toFloat())
    val ledY = topY + 9f
    drawCircle(
        color = Color(0xFFFF7060).copy(alpha = 0.20f * pulse * indicator),
        radius = 16f,
        center = Offset(hx, ledY)
    )
    drawCircle(
        color = Color(0xFFFF3030).copy(alpha = 0.92f * pulse * indicator),
        radius = 4f,
        center = Offset(hx, ledY)
    )
    // warm shimmer rising next to the heater
    for (i in 0 until 4) {
        val rise = (t * 0.35f + i * 0.25f) % 1f
        val sy = botY - rise * (botY - topY - 10f)
        val wobble = sin((t * 6f + i * 1.7f).toFloat()) * 4f
        val a = (rise * (1f - rise) * 4f).coerceIn(0f, 1f) * 0.22f * indicator
        drawCircle(
            color = Color(0xFFFFAE6E).copy(alpha = a),
            radius = 3.5f,
            center = Offset(hx - 11f + wobble, sy)
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFood(
    w: Float,
    h: Float,
    elapsedSec: Float,
    particles: List<FoodParticle>
) {
    val foodHalo = Color(0xFFFFE0A0)
    val foodOuter = Color(0xFFE08440)
    val foodInner = Color(0xFFFFC880)
    particles.forEach { p ->
        val local = elapsedSec - p.startDelaySec
        if (local < 0f || local > p.lifetimeSec) return@forEach
        val fallY = (local * p.fallSpeed * h).coerceAtMost(p.maxDepth * h)
        val sway = sin((local * 2f * PI + p.xFraction * 6.28f).toFloat()) * 4f
        val fadeRemaining = p.lifetimeSec - local
        val alpha = (if (fadeRemaining < 0.7f) fadeRemaining / 0.7f else 1f).coerceIn(0f, 1f)
        val cx = p.xFraction * w + sway
        val cy = fallY
        drawCircle(
            color = foodHalo.copy(alpha = alpha * 0.35f),
            radius = 9f,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = foodOuter.copy(alpha = alpha),
            radius = 5f,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = foodInner.copy(alpha = alpha),
            radius = 2.2f,
            center = Offset(cx - 1f, cy - 1f)
        )
    }
}

