package org.example.project.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.theme.AquariumColors

@Composable
fun DeviceToggleCard(
    name: String,
    iconLabel: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    description: String? = null,
    modifier: Modifier = Modifier,
) {
    val dotColor = if (enabled) AquariumColors.SoftLime else AquariumColors.MutedAqua
    GlassCard(
        modifier = modifier,
        cornerRadius = 18.dp,
        padding = 14.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(AquariumColors.LightAqua.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iconLabel,
                    fontSize = 18.sp
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).background(dotColor, CircleShape))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = name,
                        color = AquariumColors.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = description ?: if (enabled) "Running" else "Standby",
                    color = AquariumColors.MutedAqua,
                    fontSize = 12.sp
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AquariumColors.DeepOcean,
                    checkedTrackColor = AquariumColors.Lime,
                    uncheckedThumbColor = AquariumColors.PaleAqua,
                    uncheckedTrackColor = AquariumColors.MidOcean,
                    uncheckedBorderColor = AquariumColors.Stroke
                )
            )
        }
    }
}
