package org.example.project.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.example.project.Platform
import org.example.project.care.CareSchedule
import org.example.project.care.CareUrgency
import org.example.project.care.computeHealth
import org.example.project.care.computeSchedule
import org.example.project.care.nextCareHint
import org.example.project.care.rememberTankCare
import org.example.project.components.AnimatedAquarium
import org.example.project.components.DeviceToggleCard
import org.example.project.components.GlassCard
import org.example.project.components.HealthBar
import org.example.project.components.PrimaryButton
import org.example.project.components.SecondaryButton
import org.example.project.components.SectionHeader
import org.example.project.components.StatusChip
import org.example.project.components.StatusKind
import org.example.project.data.SampleData
import org.example.project.storage.currentTimeMs
import org.example.project.theme.AquariumColors

private const val ANDROID_PRIVACY_URL = "https://telegra.ph/Privacy-Policy-for-Aqua-Nest-05-15"
private const val IOS_PRIVACY_URL = "https://telegra.ph/Privacy-Policy-for-Deep-Lake-Story-05-20"
private const val IOS_TERMS_URL = "https://telegra.ph/Terms--Conditions-for-Deep-Lake-Story-05-15"

private data class AquariumVisuals(
    val fishCount: Int,
    val plantCount: Int,
    val bubbleCount: Int,
    val palette: List<Color>,
    val fishPalette: List<Color>,
    val plantPalette: List<Color>,
    val sandTop: Color,
    val sandBottom: Color,
    val pebbleColor: Color,
    val fishMinSize: Float,
    val fishMaxSize: Float,
    val fishSpeedScale: Float,
    val lightIntensity: Float,
    val seed: Int,
)

private val tankVisuals = listOf(
    // Coral Reef — bright tropical lagoon: turquoise water, white sand,
    // big colorful saltwater fish, lush coral-pink + green plants, fast & sunny.
    AquariumVisuals(
        fishCount = 9,
        plantCount = 7,
        bubbleCount = 22,
        palette = listOf(
            Color(0xFF07677F),
            Color(0xFF0FB0C9),
            Color(0xFF2BE3E0),
            Color(0xFF0FB0C9),
        ),
        fishPalette = listOf(
            Color(0xFFFFB347),
            Color(0xFFFF6B6B),
            Color(0xFFFFD86B),
            Color(0xFFFF9DB6),
            Color(0xFF6EE7FF),
            Color(0xFFFFFFFF),
        ),
        plantPalette = listOf(
            Color(0xFFFF8AA8),
            Color(0xFFFFB347),
            Color(0xFF4FE0A8),
            Color(0xFF66D9B0),
            Color(0xFFFFD06B),
        ),
        sandTop = Color(0xFFFFF1D6),
        sandBottom = Color(0xFFE6C58A),
        pebbleColor = Color(0xFFC79263),
        fishMinSize = 0.8f,
        fishMaxSize = 1.5f,
        fishSpeedScale = 1.3f,
        lightIntensity = 1.6f,
        seed = 11,
    ),
    // Living Room — calm freshwater: deep blue, green plants, mid-sized fish, mellow pace.
    AquariumVisuals(
        fishCount = 6,
        plantCount = 5,
        bubbleCount = 14,
        palette = listOf(
            Color(0xFF0A4B6D),
            Color(0xFF0E6A92),
            Color(0xFF1389B5),
            Color(0xFF0E6A92),
        ),
        fishPalette = listOf(
            AquariumColors.Lime,
            AquariumColors.LightAqua,
            AquariumColors.SoftLime,
            AquariumColors.PaleAqua,
            Color(0xFFFFC36B),
        ),
        plantPalette = listOf(
            Color(0xFF3FA66E),
            Color(0xFF4FB07A),
            Color(0xFF38935F),
            Color(0xFF4DA672),
            Color(0xFF358B58),
        ),
        sandTop = Color(0xFFE0C690),
        sandBottom = Color(0xFFA88150),
        pebbleColor = Color(0xFF7A6446),
        fishMinSize = 0.6f,
        fishMaxSize = 1.1f,
        fishSpeedScale = 1f,
        lightIntensity = 1f,
        seed = 22,
    ),
    // Office Nano — moody twilight tank: dark navy water, dark gravel, tiny pale fish, slow.
    AquariumVisuals(
        fishCount = 3,
        plantCount = 3,
        bubbleCount = 6,
        palette = listOf(
            Color(0xFF010F1F),
            Color(0xFF052640),
            Color(0xFF0B405E),
            Color(0xFF052640),
        ),
        fishPalette = listOf(
            Color(0xFFB6A0FF),
            Color(0xFF6EE7FF),
            Color(0xFFFFFFFF),
        ),
        plantPalette = listOf(
            Color(0xFF1F5F40),
            Color(0xFF26764F),
            Color(0xFF184E33),
        ),
        sandTop = Color(0xFF4A4338),
        sandBottom = Color(0xFF1F1B16),
        pebbleColor = Color(0xFF111111),
        fishMinSize = 0.4f,
        fishMaxSize = 0.7f,
        fishSpeedScale = 0.55f,
        lightIntensity = 0.35f,
        seed = 33,
    ),
)

@Composable
fun AquariumScreen(
    onOpenWebView: (title: String, url: String) -> Unit = { _, _ -> }
) {
    var activeTankIndex by rememberSaveable { mutableStateOf(0) }
    val care = rememberTankCare(activeTankIndex)

    // Ticking "now" so the health bar updates over time without user input.
    var nowMs by remember { mutableLongStateOf(currentTimeMs()) }
    LaunchedEffect(activeTankIndex) {
        while (true) {
            nowMs = currentTimeMs()
            delay(30_000L)
        }
    }

    val health = remember(care.devices, care.feeds, nowMs) {
        computeHealth(care.devices, care.feeds, nowMs)
    }
    val hint = remember(care.devices, care.feeds, nowMs) {
        nextCareHint(care.devices, care.feeds, nowMs)
    }
    val schedule = remember(care.devices, care.feeds, nowMs) {
        computeSchedule(care.devices, care.feeds, nowMs)
    }

    val filterOn = care.devices.getOrNull(0)?.on ?: true
    val heaterOn = care.devices.getOrNull(1)?.on ?: true
    val lightOn = care.devices.getOrNull(2)?.on ?: true
    val pumpOn = care.devices.getOrNull(3)?.on ?: true

    val baseVisuals = tankVisuals.getOrElse(activeTankIndex) { tankVisuals.first() }
    val effectiveBubbles = (baseVisuals.bubbleCount * 0.5f).toInt() // ambient bubbles, halved — pump column carries the rest
    val effectiveSpeed = baseVisuals.fishSpeedScale * (if (filterOn) 1f else 0.6f) *
        (0.5f + 0.5f * (health / 100f))
    val lastFeedMs = care.feeds.lastOrNull() ?: 0L

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(top = 16.dp, bottom = 8.dp)
            ) {
                HeaderRow()
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val activeTank = SampleData.tanks.getOrElse(activeTankIndex) { SampleData.tanks.first() }
                AnimatedAquarium(
                    modifier = Modifier.fillMaxWidth(),
                    fishCount = activeTank.fishCount,
                    plantCount = baseVisuals.plantCount,
                    bubbleCount = effectiveBubbles,
                    waterPalette = baseVisuals.palette,
                    fishPalette = baseVisuals.fishPalette,
                    plantPalette = baseVisuals.plantPalette,
                    sandTop = baseVisuals.sandTop,
                    sandBottom = baseVisuals.sandBottom,
                    pebbleColor = baseVisuals.pebbleColor,
                    fishMinSize = baseVisuals.fishMinSize,
                    fishMaxSize = baseVisuals.fishMaxSize,
                    fishSpeedScale = effectiveSpeed,
                    lightIntensity = baseVisuals.lightIntensity,
                    filterOn = filterOn,
                    heaterOn = heaterOn,
                    pumpOn = pumpOn,
                    lightOn = lightOn,
                    foodSpawnedAtMs = lastFeedMs,
                    seed = baseVisuals.seed,
                )
                TankListRow(activeIndex = activeTankIndex) { activeTankIndex = it }
                ActiveTankCard(activeTankIndex, schedule)
                HealthBar(healthPercent = health, hint = hint.message)
                PrimaryButton(
                    text = "Feed fish",
                    onClick = { care.feed() },
                    leadingGlyph = "✦",
                    modifier = Modifier.fillMaxWidth(),
                )
                DevicesSection(
                    states = care.devices.map { it.on },
                    onToggle = { i, v -> care.toggle(i, v) },
                )
                LegalSection(
                    onPrivacy = {
                        val url = if (Platform.isAndroid) ANDROID_PRIVACY_URL else IOS_PRIVACY_URL
                        onOpenWebView("Privacy Policy", url)
                    },
                    onTerms = {
                        onOpenWebView("Terms of Use", IOS_TERMS_URL)
                    }
                )
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun HeaderRow() {
    Column {
        Text(
            text = "Your aquariums",
            color = AquariumColors.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Manage tanks, devices and care routines",
            color = AquariumColors.PaleAqua,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun TankListRow(activeIndex: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SampleData.tanks.forEachIndexed { index, tank ->
            val isActive = index == activeIndex
            GlassCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = !isActive) { onSelect(index) },
                padding = 12.dp,
                accent = if (isActive) AquariumColors.Lime else null
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = tank.name,
                        color = AquariumColors.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Text(
                        text = "${tank.sizeLiters} L",
                        color = AquariumColors.PaleAqua,
                        fontSize = 11.sp
                    )
                    if (isActive) {
                        StatusChip("Active", StatusKind.Stable, pulse = true)
                    } else {
                        Box(
                            modifier = Modifier
                                .background(
                                    AquariumColors.GlassBlueStrong,
                                    RoundedCornerShape(50)
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Idle",
                                color = AquariumColors.MutedAqua,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveTankCard(index: Int, schedule: CareSchedule) {
    val tank = SampleData.tanks[index]
    GlassCard(modifier = Modifier.fillMaxWidth(), padding = 18.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Active tank",
                color = AquariumColors.MutedAqua,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = tank.name,
                color = AquariumColors.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                StatColumn("Size", "${tank.sizeLiters} L")
                StatColumn("Fish", tank.fishCount.toString())
                StatColumn("Water", tank.waterType)
            }
            CareScheduleSection(schedule)
        }
    }
}

@Composable
private fun CareScheduleSection(schedule: CareSchedule) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(AquariumColors.GlassBlueStrong)
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Care schedule",
            color = AquariumColors.MutedAqua,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        )
        ScheduleRow(
            title = "Feeding",
            status = schedule.feed.statusText,
            advice = schedule.feed.advice,
            urgency = schedule.feed.urgency,
        )
        schedule.devices.forEach { dev ->
            ScheduleRow(
                title = dev.deviceName,
                status = dev.statusText,
                advice = dev.advice,
                urgency = dev.urgency,
            )
        }
    }
}

@Composable
private fun ScheduleRow(
    title: String,
    status: String,
    advice: String,
    urgency: CareUrgency,
) {
    val dotColor = when (urgency) {
        CareUrgency.Ok -> AquariumColors.Lime
        CareUrgency.Soon -> AquariumColors.Warning
        CareUrgency.Urgent -> AquariumColors.Danger
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp, end = 10.dp)
                .size(8.dp)
                .background(dotColor, CircleShape)
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    color = AquariumColors.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = status,
                    color = AquariumColors.PaleAqua,
                    fontSize = 12.sp,
                )
            }
            Text(
                text = advice,
                color = dotColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String) {
    Column {
        Text(
            text = label,
            color = AquariumColors.MutedAqua,
            fontSize = 11.sp
        )
        Text(
            text = value,
            color = AquariumColors.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun DevicesSection(states: List<Boolean>, onToggle: (Int, Boolean) -> Unit) {
    SectionHeader(title = "Connected devices")
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SampleData.devices.forEachIndexed { i, device ->
            DeviceToggleCard(
                name = device.name,
                iconLabel = device.glyph,
                enabled = states[i],
                onToggle = { onToggle(i, it) },
                description = device.description,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun LegalSection(onPrivacy: () -> Unit, onTerms: () -> Unit) {
    SectionHeader(title = "Legal")
    if (Platform.isAndroid) {
        SecondaryButton(
            text = "Privacy Policy",
            onClick = onPrivacy,
            leadingGlyph = "▣",
            modifier = Modifier.fillMaxWidth()
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SecondaryButton(
                text = "Privacy Policy",
                onClick = onPrivacy,
                leadingGlyph = "▣",
                modifier = Modifier.fillMaxWidth()
            )
            SecondaryButton(
                text = "Terms of Use",
                onClick = onTerms,
                leadingGlyph = "❑",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
