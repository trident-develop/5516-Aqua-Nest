package org.example.project.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.theme.AquariumColors
import kotlin.math.roundToInt

@Composable
fun HealthBar(
    healthPercent: Float,
    hint: String,
    modifier: Modifier = Modifier,
) {
    val clamped = healthPercent.coerceIn(0f, 100f)
    val animated by animateFloatAsState(
        targetValue = clamped / 100f,
        animationSpec = tween(durationMillis = 700),
        label = "health"
    )
    val color by animateColorAsState(
        targetValue = healthColor(clamped),
        animationSpec = tween(durationMillis = 700),
        label = "healthColor"
    )

    GlassCard(modifier = modifier.fillMaxWidth(), padding = 16.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Fish health",
                    color = AquariumColors.MutedAqua,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "${clamped.roundToInt()}%",
                    color = color,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(AquariumColors.GlassBlueStrong)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = animated.coerceAtLeast(0.02f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(color.copy(alpha = 0.75f), color)
                            )
                        )
                )
            }
            Text(
                text = hint,
                color = AquariumColors.PaleAqua,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

private fun healthColor(percent: Float): Color = when {
    percent >= 70f -> AquariumColors.Lime
    percent >= 40f -> AquariumColors.Warning
    else -> AquariumColors.Danger
}
