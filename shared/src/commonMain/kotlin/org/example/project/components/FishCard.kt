package org.example.project.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.data.FishItem
import org.example.project.theme.AquariumColors

@Composable
fun FishCard(
    fish: FishItem,
    modifier: Modifier = Modifier,
    photoUri: String? = null,
    onClick: () -> Unit = {},
) {
    GlassCard(
        modifier = modifier.clickable(onClick = onClick),
        cornerRadius = 22.dp,
        padding = 12.dp,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                fish.accent.copy(alpha = 0.55f),
                                AquariumColors.MidOcean.copy(alpha = 0.7f),
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (photoUri != null) {
                    UriImage(uri = photoUri, modifier = Modifier.fillMaxSize())
                } else {
                    FishGlyph(color = fish.accent, modifier = Modifier.fillMaxSize().padding(12.dp))
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            AquariumColors.DeepOcean.copy(alpha = 0.55f),
                            RoundedCornerShape(50)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = fish.type,
                        color = AquariumColors.PaleAqua,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Text(
                text = fish.name,
                color = AquariumColors.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Care: ${fish.careLevel}",
                    color = AquariumColors.MutedAqua,
                    fontSize = 11.sp
                )
                Spacer(Modifier.weight(1f))
                CompatibilityIndicator(fish.compatibility)
            }
        }
    }
}

@Composable
private fun CompatibilityIndicator(level: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(3) { i ->
            val on = i < level
            Box(
                modifier = Modifier
                    .padding(start = 3.dp)
                    .size(8.dp)
                    .background(
                        if (on) AquariumColors.Lime else AquariumColors.MutedAqua.copy(alpha = 0.4f),
                        CircleShape
                    )
            )
        }
    }
}
