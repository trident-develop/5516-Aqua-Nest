package org.example.project.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.example.project.components.GlassCard
import org.example.project.components.IntakeChart
import org.example.project.components.PrimaryButton
import org.example.project.components.SecondaryButton
import org.example.project.components.SectionHeader
import org.example.project.components.SystemBackHandler
import org.example.project.components.WaterLevel
import org.example.project.hydration.ActivityLevel
import org.example.project.hydration.HydrationProfile
import org.example.project.hydration.Sex
import org.example.project.hydration.bucketsForDay
import org.example.project.hydration.bucketsForMonth
import org.example.project.hydration.bucketsForWeek
import org.example.project.hydration.bucketsForYear
import org.example.project.hydration.rememberHydrationStore
import org.example.project.hydration.todayTotalMl
import org.example.project.storage.currentTimeMs
import org.example.project.theme.AquariumColors

private enum class ChartPeriod(val label: String) { Day("Day"), Week("Week"), Month("Month"), Year("Year") }

@Composable
fun CareAnalyticsScreen() {
    val store = rememberHydrationStore()
    val focusManager = LocalFocusManager.current

    var nowMs by remember { mutableLongStateOf(currentTimeMs()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L)
            nowMs = currentTimeMs()
        }
    }

    val target = store.profile.dailyTargetMl
    val drunk = remember(store.drinks, nowMs) { todayTotalMl(store.drinks, nowMs) }
    val progress = if (target > 0) drunk.toFloat() / target else 0f

    var showProfileEditor by rememberSaveable { mutableStateOf(!store.profileSet) }
    var period by rememberSaveable { mutableStateOf(ChartPeriod.Day) }
    var showCustomDrink by rememberSaveable { mutableStateOf(false) }

    // Auxiliary panels intercept system back. Order matters — the custom drink
    // sheet is registered last so it closes first if both happen to be open.
    SystemBackHandler(enabled = showProfileEditor && store.profileSet) {
        showProfileEditor = false
        focusManager.clearFocus()
    }
    SystemBackHandler(enabled = showCustomDrink) {
        showCustomDrink = false
        focusManager.clearFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
    ) {
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (!store.profileSet) {
                    AssistantTipCard(
                        "Set up your profile below so I can calculate your personal daily water target."
                    )
                }
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    WaterLevel(
                        progress = progress,
                        currentMl = drunk,
                        targetMl = target,
                    )
                }
                ProgressSummary(drunk = drunk, target = target, progress = progress)
                QuickAddRow(
                    onAdd = { store.addDrink(it) },
                    onAddCustom = { showCustomDrink = true },
                    onUndo = { store.removeLast() },
                )
                if (showCustomDrink) {
                    CustomDrinkInput(
                        onAdd = {
                            store.addDrink(it)
                            showCustomDrink = false
                            focusManager.clearFocus()
                        },
                        onCancel = {
                            showCustomDrink = false
                            focusManager.clearFocus()
                        },
                    )
                }
                ChartCard(
                    period = period,
                    onPeriod = {
                        period = it
                        focusManager.clearFocus()
                    },
                    drinks = store.drinks,
                    target = target,
                    nowMs = nowMs,
                )
                ProfileCard(
                    profile = store.profile,
                    isEditing = showProfileEditor,
                    canCancel = store.profileSet,
                    onEdit = { showProfileEditor = true },
                    onSave = { p ->
                        store.saveProfile(p)
                        showProfileEditor = false
                        focusManager.clearFocus()
                    },
                    onCancel = {
                        if (store.profileSet) showProfileEditor = false
                    },
                )
                InfoCard()
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun HeaderRow() {
    Column {
        Text(
            text = "Water Assistant",
            color = AquariumColors.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Hydration plan personalised for you",
            color = AquariumColors.PaleAqua,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun AssistantTipCard(message: String) {
    GlassCard(modifier = Modifier.fillMaxWidth(), padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(36.dp)
                    .background(AquariumColors.Lime.copy(alpha = 0.25f), CircleShape)
                    .border(1.dp, AquariumColors.Lime.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "✦", color = AquariumColors.Lime, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                text = message,
                color = AquariumColors.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun ProgressSummary(drunk: Int, target: Int, progress: Float) {
    val animatedPercent by animateFloatAsState(
        targetValue = (progress * 100f).coerceAtLeast(0f),
        animationSpec = tween(900),
        label = "percent",
    )
    val remaining = (target - drunk).coerceAtLeast(0)
    val percentColor = when {
        progress >= 1.0f -> AquariumColors.Lime
        progress >= 0.6f -> AquariumColors.SoftLime
        progress >= 0.3f -> AquariumColors.Warning
        else -> AquariumColors.Danger
    }
    GlassCard(modifier = Modifier.fillMaxWidth(), padding = 16.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Today",
                    color = AquariumColors.MutedAqua,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "${animatedPercent.toInt()}% of goal",
                    color = percentColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (remaining == 0) "Goal reached" else "$remaining ml to go",
                    color = AquariumColors.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${(drunk / 250f).let { (kotlin.math.round(it * 10) / 10) }} glasses (~250 ml each)",
                    color = AquariumColors.PaleAqua,
                    fontSize = 11.sp,
                )
            }
        }
    }
}

@Composable
private fun QuickAddRow(
    onAdd: (Int) -> Unit,
    onAddCustom: () -> Unit,
    onUndo: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(title = "Log a drink")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            QuickAddButton("+200", onClick = { onAdd(200) }, modifier = Modifier.weight(1f))
            QuickAddButton("+300", onClick = { onAdd(300) }, modifier = Modifier.weight(1f))
            QuickAddButton("+500", onClick = { onAdd(500) }, modifier = Modifier.weight(1f))
            QuickAddButton("…", onClick = onAddCustom, modifier = Modifier.weight(1f))
        }
        SecondaryButton(
            text = "Undo last drink",
            onClick = onUndo,
            leadingGlyph = "↶",
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun QuickAddButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(46.dp)
            .background(AquariumColors.Lime.copy(alpha = 0.16f), RoundedCornerShape(14.dp))
            .border(1.dp, AquariumColors.Lime.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = AquariumColors.Lime,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun CustomDrinkInput(onAdd: (Int) -> Unit, onCancel: () -> Unit) {
    var text by remember { mutableStateOf("") }
    GlassCard(modifier = Modifier.fillMaxWidth(), padding = 14.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Custom amount (ml)",
                color = AquariumColors.MutedAqua,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AquariumColors.GlassBlueStrong, RoundedCornerShape(12.dp))
                    .border(1.dp, AquariumColors.Stroke, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                if (text.isEmpty()) {
                    Text("e.g. 350", color = AquariumColors.MutedAqua, fontSize = 14.sp)
                }
                BasicTextField(
                    value = text,
                    onValueChange = { new -> text = new.filter { it.isDigit() }.take(5) },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        color = AquariumColors.White,
                        fontSize = 14.sp,
                    ),
                    cursorBrush = SolidColor(AquariumColors.Lime),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        text.toIntOrNull()?.takeIf { it > 0 }?.let { onAdd(it) }
                    }),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SecondaryButton(
                    text = "Cancel",
                    onClick = onCancel,
                    leadingGlyph = "✕",
                    modifier = Modifier.weight(1f),
                )
                PrimaryButton(
                    text = "Add",
                    onClick = {
                        text.toIntOrNull()?.takeIf { it > 0 }?.let { onAdd(it) }
                    },
                    leadingGlyph = "+",
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ChartCard(
    period: ChartPeriod,
    onPeriod: (ChartPeriod) -> Unit,
    drinks: List<org.example.project.hydration.DrinkEntry>,
    target: Int,
    nowMs: Long,
) {
    val buckets = remember(period, drinks, nowMs) {
        when (period) {
            ChartPeriod.Day -> bucketsForDay(drinks, nowMs)
            ChartPeriod.Week -> bucketsForWeek(drinks, nowMs)
            ChartPeriod.Month -> bucketsForMonth(drinks, nowMs)
            ChartPeriod.Year -> bucketsForYear(drinks, nowMs)
        }
    }
    val periodTarget = when (period) {
        ChartPeriod.Day -> target
        ChartPeriod.Week -> target
        ChartPeriod.Month -> target
        ChartPeriod.Year -> target * 30
    }
    GlassCard(modifier = Modifier.fillMaxWidth(), padding = 14.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Intake history",
                    color = AquariumColors.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${buckets.sumOf { it.totalMl }} ml",
                    color = AquariumColors.PaleAqua,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ChartPeriod.values().toList()) { p ->
                    PeriodChip(label = p.label, active = p == period) { onPeriod(p) }
                }
            }
            IntakeChart(buckets = buckets, targetMl = periodTarget)
        }
    }
}

@Composable
private fun PeriodChip(label: String, active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                if (active) AquariumColors.Lime else AquariumColors.GlassBlueStrong,
                RoundedCornerShape(50)
            )
            .border(
                1.dp,
                if (active) AquariumColors.Lime else AquariumColors.Stroke,
                RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        Text(
            text = label,
            color = if (active) AquariumColors.DeepOcean else AquariumColors.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun ProfileCard(
    profile: HydrationProfile,
    isEditing: Boolean,
    canCancel: Boolean,
    onEdit: () -> Unit,
    onSave: (HydrationProfile) -> Unit,
    onCancel: () -> Unit,
) {
    if (isEditing) ProfileEditor(
        initial = profile,
        canCancel = canCancel,
        onSave = onSave,
        onCancel = onCancel,
    )
    else ProfileSummary(profile = profile, onEdit = onEdit)
}

@Composable
private fun ProfileSummary(profile: HydrationProfile, onEdit: () -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth(), padding = 16.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Your profile",
                    color = AquariumColors.MutedAqua,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "Edit",
                    color = AquariumColors.Lime,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(onClick = onEdit),
                )
            }
            Text(
                text = "Daily target: ${profile.dailyTargetMl} ml",
                color = AquariumColors.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                ProfileStat("Age", "${profile.ageYears}")
                ProfileStat("Weight", "${profile.weightKg} kg")
                ProfileStat("Sex", profile.sex.label)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                ProfileStat("Activity", profile.activityLevel.label)
                ProfileStat("Climate", if (profile.climateHot) "Hot" else "Mild")
            }
        }
    }
}

@Composable
private fun ProfileStat(label: String, value: String) {
    Column {
        Text(label, color = AquariumColors.MutedAqua, fontSize = 11.sp)
        Text(
            text = value,
            color = AquariumColors.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun ProfileEditor(
    initial: HydrationProfile,
    canCancel: Boolean,
    onSave: (HydrationProfile) -> Unit,
    onCancel: () -> Unit,
) {
    var age by remember { mutableStateOf(initial.ageYears.toString()) }
    var weight by remember { mutableStateOf(initial.weightKg.toString()) }
    var sex by remember { mutableStateOf(initial.sex) }
    var activity by remember { mutableStateOf(initial.activityLevel) }
    var hot by remember { mutableStateOf(initial.climateHot) }

    GlassCard(modifier = Modifier.fillMaxWidth(), padding = 16.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Set up profile",
                color = AquariumColors.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            NumberField("Age (years)", age, onChange = { age = it }, max = 3)
            NumberField("Weight (kg)", weight, onChange = { weight = it }, max = 3)

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Sex", color = AquariumColors.MutedAqua, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Sex.values().forEach { s ->
                        SegmentChip(s.label, active = s == sex) { sex = s }
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Activity level", color = AquariumColors.MutedAqua, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActivityLevel.values().forEach { a ->
                        SegmentChip(a.label, active = a == activity) { activity = a }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Hot climate", color = AquariumColors.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "Adds ~500 ml to your target",
                        color = AquariumColors.PaleAqua,
                        fontSize = 11.sp,
                    )
                }
                Switch(
                    checked = hot,
                    onCheckedChange = { hot = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AquariumColors.DeepOcean,
                        checkedTrackColor = AquariumColors.Lime,
                        uncheckedThumbColor = AquariumColors.PaleAqua,
                        uncheckedTrackColor = AquariumColors.GlassBlueStrong,
                    ),
                )
            }

            // Live target preview.
            val previewAge = age.toIntOrNull()?.coerceIn(1, 120) ?: initial.ageYears
            val previewWeight = weight.toIntOrNull()?.coerceIn(20, 300) ?: initial.weightKg
            val preview = HydrationProfile(
                ageYears = previewAge,
                weightKg = previewWeight,
                sex = sex,
                activityLevel = activity,
                climateHot = hot,
            )
            Text(
                text = "Projected daily target: ${preview.dailyTargetMl} ml",
                color = AquariumColors.Lime,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (canCancel) {
                    SecondaryButton(
                        text = "Cancel",
                        onClick = onCancel,
                        leadingGlyph = "✕",
                        modifier = Modifier.weight(1f),
                    )
                }
                PrimaryButton(
                    text = "Save",
                    onClick = { onSave(preview) },
                    leadingGlyph = "✓",
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun NumberField(label: String, value: String, onChange: (String) -> Unit, max: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = AquariumColors.MutedAqua, fontSize = 12.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AquariumColors.GlassBlueStrong, RoundedCornerShape(12.dp))
                .border(1.dp, AquariumColors.Stroke, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            if (value.isEmpty()) {
                Text("—", color = AquariumColors.MutedAqua, fontSize = 14.sp)
            }
            BasicTextField(
                value = value,
                onValueChange = { v -> onChange(v.filter { it.isDigit() }.take(max)) },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(color = AquariumColors.White, fontSize = 14.sp),
                cursorBrush = SolidColor(AquariumColors.Lime),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SegmentChip(label: String, active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                if (active) AquariumColors.Lime else AquariumColors.GlassBlueStrong,
                RoundedCornerShape(50)
            )
            .border(
                1.dp,
                if (active) AquariumColors.Lime else AquariumColors.Stroke,
                RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            text = label,
            color = if (active) AquariumColors.DeepOcean else AquariumColors.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun InfoCard() {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "chevron",
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Why water matters",
                        color = AquariumColors.MutedAqua,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    if (!expanded) {
                        Text(
                            text = "Tap to learn how hydration affects you",
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
            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Your body is roughly 60% water. Even a 1–2% drop hits focus, " +
                            "mood and reaction time before you feel thirsty.",
                        color = AquariumColors.White,
                        fontSize = 14.sp,
                    )
                    InfoBullet(
                        title = "Daily benefits",
                        body = "Regulates body temperature, lubricates joints, helps kidneys flush toxins, " +
                            "supports skin elasticity and digestion.",
                    )
                    InfoBullet(
                        title = "Spread it out",
                        body = "Sip 200–300 ml every 1–2 hours instead of chugging large amounts at once — " +
                            "your kidneys absorb water more efficiently in smaller portions.",
                    )
                    InfoBullet(
                        title = "Signs you're behind",
                        body = "Dark yellow urine, dry mouth, headaches, dizziness or low energy in the afternoon " +
                            "usually mean you're already mildly dehydrated.",
                    )
                    InfoBullet(
                        title = "Don't overdo it",
                        body = "Drinking far more than your body needs can dilute electrolytes (hyponatremia). " +
                            "Stick to your personalised target unless training in heat.",
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoBullet(title: String, body: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 7.dp, end = 10.dp)
                .size(6.dp)
                .background(AquariumColors.Lime, CircleShape)
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                color = AquariumColors.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = body,
                color = AquariumColors.PaleAqua,
                fontSize = 12.sp,
            )
        }
    }
}
