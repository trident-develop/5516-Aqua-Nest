package org.example.project.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.theme.AquariumColors

enum class StatusKind { Stable, NeedsAttention, WaterChangeSoon, Healthy, Warning }

@Composable
fun StatusChip(
    text: String,
    kind: StatusKind,
    pulse: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val (bg, fg, dot) = when (kind) {
        StatusKind.Stable, StatusKind.Healthy -> Triple(
            AquariumColors.SoftLime.copy(alpha = 0.22f),
            AquariumColors.SoftLime,
            AquariumColors.LimeDeep
        )
        StatusKind.NeedsAttention, StatusKind.Warning -> Triple(
            AquariumColors.Warning.copy(alpha = 0.22f),
            AquariumColors.Warning,
            AquariumColors.Warning
        )
        StatusKind.WaterChangeSoon -> Triple(
            AquariumColors.LightAqua.copy(alpha = 0.22f),
            AquariumColors.PaleAqua,
            AquariumColors.LightAqua
        )
    }

    val pulseScale = if (pulse) {
        val transition = rememberInfiniteTransition()
        val value by transition.animateFloat(
            initialValue = 0.9f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(1100),
                repeatMode = RepeatMode.Reverse
            )
        )
        value
    } else 1f

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(bg, RoundedCornerShape(50))
            .border(1.dp, fg.copy(alpha = 0.35f), RoundedCornerShape(50))
            .padding(PaddingValues(horizontal = 12.dp, vertical = 6.dp))
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .scale(pulseScale)
                .background(dot, CircleShape)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            color = fg,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
