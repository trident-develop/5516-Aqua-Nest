package org.example.project.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.components.AquariumBackground
import org.example.project.theme.AquariumColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val TWO_PI: Float = (2.0 * PI).toFloat()

@Composable
fun LoadingScreen() {
    AquariumBackground(bubbleCount = 24, fishCount = 4) {
        Box(modifier = Modifier.fillMaxSize()) {
            BackgroundWaves(modifier = Modifier.fillMaxSize())
            FallingSparks(modifier = Modifier.fillMaxSize())

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                HeroScene(modifier = Modifier.size(280.dp))
                Spacer(Modifier.height(28.dp))
                Text(
                    text = "Diving in",
                    color = AquariumColors.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(10.dp))
                WaveText(text = "Calibrating your aquarium")
            }
        }
    }
}

// --- Hero composite ---

@Composable
private fun HeroScene(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition()
    val ringSpin by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ringSpin",
    )
    val midSpin by transition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "midSpin",
    )
    val breathe by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathe",
    )
    val sweepArc by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "sweepArc",
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        OrbitDots(
            modifier = Modifier.fillMaxSize(),
            rotationDegrees = ringSpin,
            count = 18,
            radiusFraction = 0.48f,
            baseDotPx = 4.5f,
            color = AquariumColors.Lime,
        )
        OrbitDots(
            modifier = Modifier.fillMaxSize().scale(0.78f),
            rotationDegrees = midSpin,
            count = 12,
            radiusFraction = 0.48f,
            baseDotPx = 3.2f,
            color = AquariumColors.LightAqua,
        )
        SweepHalo(
            modifier = Modifier.fillMaxSize().scale(0.95f),
            startAngle = sweepArc,
        )
        Porthole(
            modifier = Modifier
                .size(150.dp)
                .scale(breathe),
        )
        // Bright pulse heart at the dead centre.
        CentrePulse(modifier = Modifier.size(36.dp))
    }
}

// --- Orbiting dots (variable size, glow) ---

@Composable
private fun OrbitDots(
    modifier: Modifier,
    rotationDegrees: Float,
    count: Int,
    radiusFraction: Float,
    baseDotPx: Float,
    color: Color,
) {
    val transition = rememberInfiniteTransition()
    val sizePhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = TWO_PI,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "orbitSize",
    )
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.minDimension * radiusFraction
        rotate(degrees = rotationDegrees, pivot = Offset(cx, cy)) {
            for (i in 0 until count) {
                val angle = (i.toFloat() / count) * TWO_PI
                val x = cx + cos(angle.toDouble()).toFloat() * r
                val y = cy + sin(angle.toDouble()).toFloat() * r
                val sizeMul = 0.6f + 0.6f *
                    (0.5f + 0.5f * sin((angle * 3f + sizePhase).toDouble()).toFloat())
                val radius = baseDotPx * sizeMul
                drawCircle(
                    color = color.copy(alpha = 0.25f),
                    radius = radius * 2.0f,
                    center = Offset(x, y),
                )
                drawCircle(
                    color = color,
                    radius = radius,
                    center = Offset(x, y),
                )
            }
        }
    }
}

// --- Sweep halo (rotating gradient arc on a thin circle) ---

@Composable
private fun SweepHalo(modifier: Modifier, startAngle: Float) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = size.minDimension / 2f - 6f
        rotate(degrees = startAngle, pivot = Offset(cx, cy)) {
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color.Transparent,
                        AquariumColors.Lime.copy(alpha = 0.0f),
                        AquariumColors.Lime,
                        AquariumColors.PaleAqua,
                        Color.Transparent,
                    ),
                    center = Offset(cx, cy),
                ),
                startAngle = 0f,
                sweepAngle = 200f,
                useCenter = false,
                topLeft = Offset(cx - radius, cy - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = 3.5f, cap = StrokeCap.Round),
            )
        }
    }
}

// --- Porthole: water disc with multiple fish on different orbits + caustics ---

@Composable
private fun Porthole(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition()
    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = TWO_PI,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "time",
    )
    val tailWag by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 220, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "tailWag",
    )

    Box(modifier = modifier.clip(CircleShape), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val outerR = (w.coerceAtMost(h)) / 2f
            val ringStroke = 5f
            val innerR = outerR - ringStroke - 2f

            // Water disc.
            val waterCircle = Path().apply {
                addOval(
                    Rect(
                        offset = Offset(cx - innerR, cy - innerR),
                        size = Size(innerR * 2, innerR * 2),
                    )
                )
            }
            clipPath(waterCircle) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0E5479),
                            Color(0xFF1B7AA0),
                            Color(0xFF2C8AB5),
                        ),
                    ),
                    topLeft = Offset(cx - innerR, cy - innerR),
                    size = Size(innerR * 2, innerR * 2),
                )

                // Moving caustic light bands.
                for (i in 0 until 4) {
                    val phase = (time * 0.6f + i * 1.3f) % TWO_PI
                    val y = cy - innerR * 0.8f + i * (innerR * 0.4f) +
                        sin(phase.toDouble()).toFloat() * 6f
                    drawLine(
                        color = Color.White.copy(alpha = 0.05f),
                        start = Offset(cx - innerR, y),
                        end = Offset(cx + innerR, y + sin((phase + 1).toDouble()).toFloat() * 3f),
                        strokeWidth = 14f,
                    )
                }

                // Sun rays from the top.
                for (i in 0 until 4) {
                    val baseX = cx + (i - 1.5f) * innerR * 0.35f +
                        sin((time * 1.4f + i).toDouble()).toFloat() * 5f
                    val rayPath = Path().apply {
                        moveTo(baseX - 5f, cy - innerR)
                        lineTo(baseX + 5f, cy - innerR)
                        lineTo(baseX + 20f, cy + innerR * 0.7f)
                        lineTo(baseX - 20f, cy + innerR * 0.7f)
                        close()
                    }
                    drawPath(
                        path = rayPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.18f),
                                Color.Transparent,
                            ),
                            startY = cy - innerR,
                            endY = cy + innerR * 0.7f,
                        ),
                    )
                }

                // Bubble stream from the bottom.
                for (i in 0 until 10) {
                    val phase = ((time * 0.18f + i * 0.10f) % 1f)
                    val xJitter = sin((time * 1.5f + i).toDouble()).toFloat() * 6f
                    val bx = cx + xJitter + (i % 2 - 0.5f) * 12f
                    val by = (cy + innerR) - phase * (innerR * 1.9f)
                    val r = 1.6f + (i % 3)
                    val a = (1f - phase).coerceIn(0f, 1f) * 0.7f
                    drawCircle(
                        color = Color.White.copy(alpha = a * 0.55f),
                        radius = r,
                        center = Offset(bx, by),
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = a),
                        radius = r * 0.35f,
                        center = Offset(bx - r * 0.3f, by - r * 0.3f),
                    )
                }

                // School of fish on three different orbits.
                drawSchoolFish(
                    cx = cx, cy = cy, innerR = innerR,
                    time = time, tailWag = tailWag,
                )
            }

            // Rim (static thin highlight).
            drawCircle(
                color = AquariumColors.White.copy(alpha = 0.55f),
                radius = innerR,
                center = Offset(cx, cy),
                style = Stroke(width = 1.2f),
            )
            // Outer rim glow.
            drawCircle(
                color = AquariumColors.LightAqua.copy(alpha = 0.35f),
                radius = innerR + 3f,
                center = Offset(cx, cy),
                style = Stroke(width = 4f),
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSchoolFish(
    cx: Float,
    cy: Float,
    innerR: Float,
    time: Float,
    tailWag: Float,
) {
    data class Orbit(
        val rx: Float,
        val ry: Float,
        val speed: Float,
        val phase: Float,
        val color: Color,
        val scale: Float,
        val yOffset: Float,
    )
    val orbits = listOf(
        Orbit(innerR * 0.62f, innerR * 0.34f, 1.0f, 0.0f, AquariumColors.Lime, 1.0f, 4f),
        Orbit(innerR * 0.40f, innerR * 0.20f, -1.4f, 1.2f, AquariumColors.LightAqua, 0.75f, -10f),
        Orbit(innerR * 0.74f, innerR * 0.50f, 0.7f, 2.4f, Color(0xFFFFB347), 0.85f, 12f),
    )
    orbits.forEach { o ->
        val theta = time * o.speed + o.phase
        val px = cx + cos(theta.toDouble()).toFloat() * o.rx
        val py = cy + o.yOffset + sin(theta.toDouble()).toFloat() * o.ry
        val tangentX = -sin(theta.toDouble()).toFloat() * o.rx * o.speed
        val facingRight = tangentX >= 0f
        drawLoadingFish(px, py, facingRight, tailWag, o.color, o.scale)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLoadingFish(
    cx: Float,
    cy: Float,
    facingRight: Boolean,
    tailWag: Float,
    bodyColor: Color,
    scaleFactor: Float,
) {
    val bodyW = 22f * scaleFactor
    val bodyH = 12f * scaleFactor
    val flip = if (facingRight) 1f else -1f
    translate(left = cx, top = cy) {
        scale(scaleX = flip, scaleY = 1f, pivot = Offset.Zero) {
            val tail = Path().apply {
                moveTo(-bodyW * 0.45f, 0f)
                lineTo(-bodyW * 0.95f, -bodyH * 0.6f + tailWag * 3f)
                lineTo(-bodyW * 0.78f, 0f)
                lineTo(-bodyW * 0.95f, bodyH * 0.6f + tailWag * 3f)
                close()
            }
            drawPath(
                path = tail,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        bodyColor.copy(alpha = 0.45f),
                        bodyColor,
                    ),
                    startX = -bodyW * 0.95f,
                    endX = -bodyW * 0.45f,
                ),
            )
            drawOval(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        bodyColor.copy(alpha = 0.95f),
                        bodyColor.copy(alpha = 0.75f),
                    ),
                    startY = -bodyH,
                    endY = bodyH,
                ),
                topLeft = Offset(-bodyW * 0.5f, -bodyH * 0.5f),
                size = Size(bodyW, bodyH),
            )
            // Belly highlight.
            drawOval(
                color = Color.White.copy(alpha = 0.25f),
                topLeft = Offset(-bodyW * 0.3f, -bodyH * 0.35f),
                size = Size(bodyW * 0.5f, bodyH * 0.35f),
            )
            drawCircle(
                color = Color.White,
                radius = bodyH * 0.22f,
                center = Offset(bodyW * 0.28f, -bodyH * 0.08f),
            )
            drawCircle(
                color = Color.Black,
                radius = bodyH * 0.10f,
                center = Offset(bodyW * 0.32f, -bodyH * 0.08f),
            )
        }
    }
}

// --- Pulsing centre with concentric rings ---

@Composable
private fun CentrePulse(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition()
    val pulses = (0 until 3).map { i ->
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(i * 600),
            ),
            label = "centerPulse$i",
        )
    }
    val coreScale by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "coreScale",
    )
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val baseR = size.minDimension / 2f
        pulses.forEach { state ->
            val p = state.value
            val radius = baseR * 0.4f + (baseR * 0.85f - baseR * 0.4f) * p
            val alpha = (1f - p).coerceIn(0f, 1f) * 0.55f
            drawCircle(
                color = AquariumColors.Lime.copy(alpha = alpha),
                radius = radius,
                center = Offset(cx, cy),
                style = Stroke(width = 1.6f),
            )
        }
        // Core glow.
        drawCircle(
            color = AquariumColors.Lime.copy(alpha = 0.25f),
            radius = baseR * 0.5f * coreScale,
            center = Offset(cx, cy),
        )
        drawCircle(
            color = AquariumColors.SoftLime,
            radius = baseR * 0.22f * coreScale,
            center = Offset(cx, cy),
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.9f),
            radius = baseR * 0.10f * coreScale,
            center = Offset(cx - baseR * 0.05f, cy - baseR * 0.05f),
        )
    }
}

// --- Falling sparks (decorative drifting particles in the background) ---

@Composable
private fun FallingSparks(modifier: Modifier) {
    val transition = rememberInfiniteTransition()
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "sparks",
    )
    val specs = remember {
        (0 until 24).map { i ->
            SparkSpec(
                xFraction = ((i * 137) % 100) / 100f,
                speed = 0.5f + ((i * 71) % 100) / 100f,
                radius = 0.8f + ((i * 53) % 25) / 10f,
                phase = ((i * 91) % 100) / 100f,
            )
        }
    }
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        specs.forEach { s ->
            val travel = (t * s.speed + s.phase) % 1f
            val y = h * travel
            val xOscillate = sin((travel * 6f * TWO_PI).toDouble()).toFloat() * 12f
            val x = s.xFraction * w + xOscillate
            val alpha = (sin((travel * PI).toFloat().toDouble()).toFloat()).coerceIn(0f, 1f) * 0.5f
            drawCircle(
                color = AquariumColors.PaleAqua.copy(alpha = alpha),
                radius = s.radius,
                center = Offset(x, y),
            )
        }
    }
}

private data class SparkSpec(
    val xFraction: Float,
    val speed: Float,
    val radius: Float,
    val phase: Float,
)

// --- Background waves ---

@Composable
private fun BackgroundWaves(modifier: Modifier = Modifier) {
    val phase by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = TWO_PI,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "bgWave",
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val baseY = h * 0.7f
        repeat(3) { i ->
            val amplitude = 14f + i * 8f
            val yOffset = baseY + i * 22f
            val path = Path().apply {
                moveTo(0f, yOffset)
                val step = w / 32f
                var x = 0f
                while (x <= w) {
                    val k = (x / w) * 4f * TWO_PI
                    val y = yOffset + sin((k + phase + i).toDouble()).toFloat() * amplitude
                    lineTo(x, y)
                    x += step
                }
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0x33B6E4F0),
                        Color(0x118FCF4D),
                    ),
                    startY = yOffset,
                    endY = h,
                ),
            )
        }
    }
}

// --- Wavy text (per-letter Y bounce) ---

@Composable
private fun WaveText(text: String) {
    val transition = rememberInfiniteTransition()
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = TWO_PI,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "waveText",
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        text.forEachIndexed { i, ch ->
            val k = (i.toFloat() / text.length) * 2f * TWO_PI
            val offsetY = sin((k + phase).toDouble()).toFloat() * 3f
            val alpha = 0.55f + 0.45f *
                (0.5f + 0.5f * sin((k + phase * 1.3f).toDouble()).toFloat())
            Text(
                text = ch.toString(),
                color = AquariumColors.PaleAqua.copy(alpha = alpha),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(
                    top = (offsetY + 3f).dp.coerceAtLeast(0.dp),
                    bottom = (3f - offsetY).dp.coerceAtLeast(0.dp),
                ),
            )
        }
        Spacer(Modifier.size(4.dp))
        // Three end-dots cascading the wave.
        listOf(0f, 1f, 2f).forEach { i ->
            val k = ((text.length + i.toInt()).toFloat() / text.length) * 2f * TWO_PI
            val dotAlpha = 0.4f + 0.6f *
                (0.5f + 0.5f * sin((k + phase * 1.3f).toDouble()).toFloat())
            Box(
                modifier = Modifier
                    .padding(start = 3.dp)
                    .size(4.dp)
                    .clip(CircleShape),
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = AquariumColors.Lime.copy(alpha = dotAlpha),
                        radius = size.minDimension / 2f,
                        center = Offset(size.width / 2f, size.height / 2f),
                    )
                }
            }
        }
    }
}
