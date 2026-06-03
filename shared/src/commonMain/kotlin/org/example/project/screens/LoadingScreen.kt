package org.example.project.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import aquanest.shared.generated.resources.Res
import aquanest.shared.generated.resources.loading
import aquanest.shared.generated.resources.zeus
import org.example.project.components.AquariumBackground
import org.example.project.components.BlockSystemBack
import org.example.project.theme.AquariumColors
import org.jetbrains.compose.resources.painterResource
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val TWO_PI: Float = (2.0 * PI).toFloat()

@Composable
fun LoadingScreen() {
    BlockSystemBack()

    val infiniteTransition = rememberInfiniteTransition()

    val lightningAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1800
                0f at 0
                0.9f at 80
                0f at 160
                0f at 700
                0.7f at 780
                0f at 880
                0f at 1800
            }
        )
    )

    val lightningAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2400
                0f at 0
                0f at 900
                0.8f at 980
                0f at 1080
                0.6f at 1280
                0f at 1380
                0f at 2400
            }
        )
    )

    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }

        while (true) {
            val now = withFrameNanos { it }
            val elapsedMs = (now - start) / 1_000_000f

            progress = when {
                elapsedMs <= 2_000f -> {
                    50f * (elapsedMs / 2_000f)
                }

                elapsedMs <= 32_000f -> {
                    50f + 49f * ((elapsedMs - 2_000f) / 30_000f)
                }

                else -> 99f
            }.coerceIn(0f, 99f)
        }
    }

    val progressValue = progress / 100f
    val progressText = progress.toInt().coerceAtMost(99)

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.loading),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = lightningAlpha1 * 0.22f))
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF79DFFF).copy(alpha = lightningAlpha2 * 0.18f))
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            drawLine(
                color = Color(0xFFBEEFFF).copy(alpha = lightningAlpha1),
                start = Offset(w * 0.18f, 0f),
                end = Offset(w * 0.34f, h * 0.22f),
                strokeWidth = 5f
            )
            drawLine(
                color = Color(0xFFFFD66B).copy(alpha = lightningAlpha1),
                start = Offset(w * 0.34f, h * 0.22f),
                end = Offset(w * 0.25f, h * 0.38f),
                strokeWidth = 5f
            )
            drawLine(
                color = Color(0xFFBEEFFF).copy(alpha = lightningAlpha2),
                start = Offset(w * 0.82f, 0f),
                end = Offset(w * 0.68f, h * 0.26f),
                strokeWidth = 5f
            )
            drawLine(
                color = Color(0xFFFFD66B).copy(alpha = lightningAlpha2),
                start = Offset(w * 0.68f, h * 0.26f),
                end = Offset(w * 0.77f, h * 0.44f),
                strokeWidth = 5f
            )
        }

        Image(
            painter = painterResource(Res.drawable.zeus),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.8f)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 32.dp)
                .padding(bottom = 72.dp)
        ) {
            Text(
                text = "Loading $progressText%",
                color = AquariumColors.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(24.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.22f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progressValue)
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF4DEBFF),
                                    Color(0xFF3A9DFF),
                                    Color(0xFFFFD66B),
                                    Color(0xFFFFB800)
                                )
                            )
                        )
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true
)

@Preview(
    showBackground = true,
    showSystemUi = true,
    widthDp = 360,
    heightDp = 640
)

@Preview(
    name = "mdpi (160)",
    widthDp = 320,
    heightDp = 680,
    fontScale = 1.0f,
    showBackground = true,
    showSystemUi = true
)

@Preview(
    name = "hdpi (240)",
    widthDp = 450,
    heightDp = 800,
    fontScale = 1.0f,
    showBackground = true,
    showSystemUi = true
)

@Composable
private fun ScreenPreview() {
    LoadingScreen()
}