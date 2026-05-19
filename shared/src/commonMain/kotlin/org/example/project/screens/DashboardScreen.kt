package org.example.project.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.example.project.care.computeHealth
import org.example.project.care.rememberTankCare
import org.example.project.components.GlassCard
import org.example.project.data.SampleData
import org.example.project.hydration.rememberHydrationStore
import org.example.project.hydration.todayTotalMl
import org.example.project.storage.currentTimeMs
import org.example.project.theme.AquariumColors
import kotlin.math.PI
import kotlin.math.sin

private enum class AttentionSeverity { Soon, Critical }

private data class AttentionItem(
    val tankIndex: Int?,
    val title: String,
    val subtitle: String,
    val severity: AttentionSeverity,
)

@Composable
fun DashboardScreen() {
    val hydration = rememberHydrationStore()
    val tanks = remember { (0 until SampleData.tanks.size).toList() }
        .map { rememberTankCare(it) }

    var nowMs by remember { mutableLongStateOf(currentTimeMs()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L)
            nowMs = currentTimeMs()
        }
    }

    val hour = (((nowMs / 3_600_000L) + 0L) % 24).toInt()
    val greeting = when (hour) {
        in 5..11 -> "Good morning"
        in 12..17 -> "Good afternoon"
        in 18..22 -> "Good evening"
        else -> "Hi, night owl"
    }
    val motto = when (hour) {
        in 5..11 -> "A fresh day of care ahead."
        in 12..17 -> "Mid-day check on your tanks."
        in 18..22 -> "Wind-down — feed and refill before bed."
        else -> "The tanks never really sleep."
    }

    val hydrTarget = hydration.profile.dailyTargetMl
    val hydrToday = remember(hydration.drinks, nowMs) { todayTotalMl(hydration.drinks, nowMs) }
    val hydrProgress = if (hydrTarget > 0) hydrToday.toFloat() / hydrTarget else 0f

    val healths = tanks.map { care ->
        computeHealth(care.devices, care.feeds, nowMs)
    }
    val avgHealth = if (healths.isNotEmpty()) healths.average().toFloat() else 0f

    val attentions = remember(tanks.map { it.devices to it.feeds }, hydrToday, nowMs) {
        buildAttention(tanks, hydrProgress, hour, nowMs)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(top = 16.dp, bottom = 4.dp)
            ) {
                HeroGreeting(greeting = greeting, motto = motto)
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                DailySummary(
                    hydrProgress = hydrProgress,
                    drunk = hydrToday,
                    target = hydrTarget,
                    avgHealth = avgHealth,
                )
                TanksPager(tanks = tanks, healths = healths, nowMs = nowMs)
                AttentionPanel(items = attentions)
                QuickStatsGrid(
                    tanksCount = tanks.size,
                    fishCatalog = SampleData.fish.size,
                    totalDrinks = hydration.drinks.size,
                    drunkToday = hydrToday,
                )
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

// --- Hero ---

@Composable
private fun HeroGreeting(greeting: String, motto: String) {
    val phase by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        )
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF0E6A92),
                        Color(0xFF06A5C4),
                        Color(0xFF1F9CD8),
                    )
                )
            )
            .border(1.dp, AquariumColors.Stroke, RoundedCornerShape(24.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Two soft waves drifting across the hero.
            for (i in 0 until 2) {
                val path = Path().apply {
                    moveTo(0f, h)
                    var x = 0f
                    val step = 8f
                    while (x <= w) {
                        val k = (x / w) * 2f * (2.0 * PI).toFloat()
                        val baseY = h * (0.5f - i * 0.15f)
                        val amp = 10f - i * 4f
                        val y = baseY + sin((k + phase * (1f + i * 0.4f)).toDouble()).toFloat() * amp
                        lineTo(x, y)
                        x += step
                    }
                    lineTo(w, h)
                    close()
                }
                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.10f - i * 0.04f),
                )
            }
            // A few drifting bubble specks for life.
            for (i in 0 until 5) {
                val xFrac = ((phase * (0.05f + i * 0.02f)) % 1f + i * 0.18f) % 1f
                val cy = h * (0.25f + (i % 3) * 0.20f) +
                    sin((phase * 1.5f + i).toDouble()).toFloat() * 3f
                drawCircle(
                    color = Color.White.copy(alpha = 0.25f),
                    radius = 2.5f + (i % 2),
                    center = Offset(xFrac * w, cy),
                )
            }
        }
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = greeting,
                color = AquariumColors.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = motto,
                color = AquariumColors.PaleAqua,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

// --- Daily summary: hydration ring + tank health pill ---

@Composable
private fun DailySummary(
    hydrProgress: Float,
    drunk: Int,
    target: Int,
    avgHealth: Float,
) {
    val progressAnim by animateFloatAsState(
        targetValue = hydrProgress.coerceIn(0f, 1.2f),
        animationSpec = tween(900),
        label = "ring",
    )
    val healthAnim by animateFloatAsState(
        targetValue = (avgHealth / 100f).coerceIn(0f, 1f),
        animationSpec = tween(900),
        label = "healthPill",
    )

    GlassCard(modifier = Modifier.fillMaxWidth(), padding = 16.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            HydrationRing(
                progress = progressAnim.coerceAtMost(1f),
                exceed = (progressAnim - 1f).coerceAtLeast(0f),
                modifier = Modifier.size(96.dp),
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Today",
                    color = AquariumColors.MutedAqua,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "$drunk / $target ml",
                    color = AquariumColors.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Tanks",
                        color = AquariumColors.MutedAqua,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    HealthPill(progress = healthAnim, percent = avgHealth.toInt())
                }
            }
        }
    }
}

@Composable
private fun HydrationRing(progress: Float, exceed: Float, modifier: Modifier = Modifier) {
    val ringColor = when {
        progress + exceed >= 1.0f -> AquariumColors.Lime
        progress >= 0.6f -> AquariumColors.SoftLime
        progress >= 0.3f -> AquariumColors.Warning
        else -> AquariumColors.Danger
    }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 10f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(stroke / 2f, stroke / 2f)
            // Background ring.
            drawArc(
                color = AquariumColors.GlassBlueStrong,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke),
            )
            // Foreground ring.
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round),
            )
            // Optional "exceed" arc layered on top.
            if (exceed > 0f) {
                drawArc(
                    color = AquariumColors.Lime.copy(alpha = 0.55f),
                    startAngle = -90f,
                    sweepAngle = 360f * exceed.coerceAtMost(1f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke / 2f, cap = androidx.compose.ui.graphics.StrokeCap.Round),
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${((progress + exceed) * 100f).toInt()}%",
                color = AquariumColors.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "hydrated",
                color = AquariumColors.PaleAqua,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun HealthPill(progress: Float, percent: Int) {
    val color = when {
        percent >= 70 -> AquariumColors.Lime
        percent >= 40 -> AquariumColors.Warning
        else -> AquariumColors.Danger
    }
    Box(
        modifier = Modifier
            .height(20.dp)
            .width(110.dp)
            .clip(RoundedCornerShape(50))
            .background(AquariumColors.GlassBlueStrong)
    ) {
        Box(
            modifier = Modifier
                .height(20.dp)
                .width((110 * progress).dp)
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.horizontalGradient(listOf(color.copy(alpha = 0.7f), color))
                )
        )
        Text(
            text = "$percent% avg",
            color = AquariumColors.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

// --- Tanks pager ---

@Composable
private fun TanksPager(
    tanks: List<org.example.project.care.TankCare>,
    healths: List<Float>,
    nowMs: Long,
) {
    val state = rememberPagerState(pageCount = { tanks.size })
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Your tanks",
                color = AquariumColors.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Swipe to switch",
                color = AquariumColors.PaleAqua,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
            )
        }
        HorizontalPager(
            state = state,
            pageSpacing = 10.dp,
        ) { page ->
            TankPageCard(
                tankIndex = page,
                care = tanks[page],
                health = healths.getOrElse(page) { 0f },
                nowMs = nowMs,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            repeat(tanks.size) { i ->
                val active = i == state.currentPage
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(6.dp)
                        .width(if (active) 18.dp else 6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (active) AquariumColors.Lime else AquariumColors.GlassBlueStrong
                        )
                )
            }
        }
    }
}

@Composable
private fun TankPageCard(
    tankIndex: Int,
    care: org.example.project.care.TankCare,
    health: Float,
    nowMs: Long,
) {
    val tank = SampleData.tanks[tankIndex]
    val deviceLabels = SampleData.devices.map { it.name }
    val lastFeed = care.feeds.maxOrNull()
    val feedAgo = if (lastFeed != null) (nowMs - lastFeed) / 3_600_000f else null
    val animHealth by animateFloatAsState(
        targetValue = health / 100f,
        animationSpec = tween(700),
        label = "tankHealth",
    )
    GlassCard(modifier = Modifier.fillMaxWidth(), padding = 16.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = tank.name,
                        color = AquariumColors.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${tank.sizeLiters} L · ${tank.waterType}",
                        color = AquariumColors.PaleAqua,
                        fontSize = 11.sp,
                    )
                }
                MiniHealthRing(progress = animHealth, percent = health.toInt())
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                care.devices.forEachIndexed { i, dev ->
                    DeviceDot(
                        on = dev.on,
                        label = deviceLabels.getOrElse(i) { "?" }.take(1),
                    )
                }
            }
            Text(
                text = when {
                    feedAgo == null -> "Not fed yet"
                    feedAgo < 1f -> "Fed ${(feedAgo * 60).toInt().coerceAtLeast(1)} min ago"
                    feedAgo < 24f -> "Fed ${feedAgo.toInt().coerceAtLeast(1)}h ago"
                    else -> "Fed ${(feedAgo / 24f).toInt()}d ago"
                },
                color = AquariumColors.MutedAqua,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun MiniHealthRing(progress: Float, percent: Int) {
    val color = when {
        percent >= 70 -> AquariumColors.Lime
        percent >= 40 -> AquariumColors.Warning
        else -> AquariumColors.Danger
    }
    Box(modifier = Modifier.size(56.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 6f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(stroke / 2f, stroke / 2f)
            drawArc(
                color = AquariumColors.GlassBlueStrong,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke),
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round),
            )
        }
        Text(
            text = "$percent",
            color = AquariumColors.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun DeviceDot(on: Boolean, label: String) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(
                if (on) AquariumColors.Lime.copy(alpha = 0.28f)
                else AquariumColors.GlassBlueStrong
            )
            .border(
                1.dp,
                if (on) AquariumColors.Lime.copy(alpha = 0.7f) else AquariumColors.Stroke,
                CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (on) AquariumColors.Lime else AquariumColors.MutedAqua,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

// --- Attention panel ---

@Composable
private fun AttentionPanel(items: List<AttentionItem>) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "attentionChev",
    )
    val criticalCount = items.count { it.severity == AttentionSeverity.Critical }
    val pulse by rememberInfiniteTransition().animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse,
        )
    )

    GlassCard(modifier = Modifier.fillMaxWidth(), padding = 16.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (items.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .padding(end = 10.dp)
                                .size((10 * pulse).dp)
                                .clip(CircleShape)
                                .background(
                                    if (criticalCount > 0) AquariumColors.Danger else AquariumColors.Warning
                                )
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .padding(end = 10.dp)
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(AquariumColors.Lime)
                        )
                    }
                    Column {
                        Text(
                            text = if (items.isEmpty()) "All good" else "Needs attention",
                            color = AquariumColors.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = if (items.isEmpty()) "Nothing urgent right now."
                            else "${items.size} item${if (items.size == 1) "" else "s"} to check.",
                            color = AquariumColors.PaleAqua,
                            fontSize = 12.sp,
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(AquariumColors.GlassBlueStrong, CircleShape)
                        .border(1.dp, AquariumColors.Stroke, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "⌄",
                        color = AquariumColors.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.rotate(rotation),
                    )
                }
            }
            AnimatedVisibility(visible = expanded && items.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items.forEach { item ->
                        AttentionRow(item)
                    }
                }
            }
        }
    }
}

@Composable
private fun AttentionRow(item: AttentionItem) {
    val dotColor = when (item.severity) {
        AttentionSeverity.Critical -> AquariumColors.Danger
        AttentionSeverity.Soon -> AquariumColors.Warning
    }
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp, end = 10.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = item.title,
                color = AquariumColors.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = item.subtitle,
                color = dotColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

// --- Quick stats ---

@Composable
private fun QuickStatsGrid(
    tanksCount: Int,
    fishCatalog: Int,
    totalDrinks: Int,
    drunkToday: Int,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(
                modifier = Modifier.weight(1f),
                label = "Tanks",
                value = tanksCount,
                accent = AquariumColors.LightAqua,
            )
            StatTile(
                modifier = Modifier.weight(1f),
                label = "Fish catalog",
                value = fishCatalog,
                accent = AquariumColors.PaleAqua,
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(
                modifier = Modifier.weight(1f),
                label = "Drinks logged",
                value = totalDrinks,
                accent = AquariumColors.SoftLime,
            )
            StatTile(
                modifier = Modifier.weight(1f),
                label = "Drunk today",
                value = drunkToday,
                suffix = " ml",
                accent = AquariumColors.Lime,
            )
        }
    }
}

@Composable
private fun StatTile(
    modifier: Modifier = Modifier,
    label: String,
    value: Int,
    suffix: String = "",
    accent: Color,
) {
    // Number count-up animation.
    val animated by animateFloatAsState(
        targetValue = value.toFloat(),
        animationSpec = tween(900),
        label = "stat-$label",
    )
    GlassCard(modifier = modifier.fillMaxWidth(), padding = 14.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accent)
                )
                Text(
                    text = label,
                    color = AquariumColors.MutedAqua,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
            Text(
                text = "${animated.toInt()}$suffix",
                color = AquariumColors.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

// --- Attention computation ---

private fun buildAttention(
    tanks: List<org.example.project.care.TankCare>,
    hydrProgress: Float,
    hour: Int,
    nowMs: Long,
): List<AttentionItem> {
    val items = mutableListOf<AttentionItem>()
    tanks.forEachIndexed { i, care ->
        val tankName = SampleData.tanks.getOrNull(i)?.name ?: "Tank ${i + 1}"
        care.devices.forEachIndexed { j, dev ->
            if (!dev.on) {
                val deviceName = SampleData.devices.getOrNull(j)?.name ?: "Device"
                val critical = j == 0 || j == 1 // filter / heater
                val hoursOff = (nowMs - dev.changedAtMs) / 3_600_000f
                val sev = if (critical) AttentionSeverity.Critical else AttentionSeverity.Soon
                items += AttentionItem(
                    tankIndex = i,
                    title = "$tankName · $deviceName is off",
                    subtitle = if (hoursOff < 1f) "Just switched off"
                    else "Off for ${formatHours(hoursOff)}",
                    severity = sev,
                )
            }
        }
        val lastFeed = care.feeds.maxOrNull()
        val hoursSinceFeed = if (lastFeed != null) (nowMs - lastFeed) / 3_600_000f else 999f
        when {
            hoursSinceFeed > 24f -> items += AttentionItem(
                tankIndex = i,
                title = "$tankName · Feed fish now",
                subtitle = "Last fed ${formatHours(hoursSinceFeed)} ago",
                severity = AttentionSeverity.Critical,
            )
            hoursSinceFeed > 14f -> items += AttentionItem(
                tankIndex = i,
                title = "$tankName · Time to feed",
                subtitle = "Last fed ${formatHours(hoursSinceFeed)} ago",
                severity = AttentionSeverity.Soon,
            )
        }
    }
    if (hydrProgress < 0.4f && hour >= 14) {
        items += AttentionItem(
            tankIndex = null,
            title = "Drink water",
            subtitle = "Only ${(hydrProgress * 100).toInt()}% of today's goal",
            severity = AttentionSeverity.Soon,
        )
    }
    return items
}

private fun formatHours(hours: Float): String {
    val abs = hours.coerceAtLeast(0f)
    return when {
        abs < 1f -> "${(abs * 60).toInt().coerceAtLeast(1)} min"
        abs < 24f -> "${abs.toInt().coerceAtLeast(1)}h"
        else -> "${(abs / 24f).toInt()}d"
    }
}

