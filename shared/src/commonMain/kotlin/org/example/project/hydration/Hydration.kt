package org.example.project.hydration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.example.project.storage.AppStorage
import org.example.project.storage.currentTimeMs
import kotlin.math.roundToInt

enum class Sex(val label: String) { Male("Male"), Female("Female") }

enum class ActivityLevel(val label: String, val extraMl: Int) {
    Low("Sedentary", 0),
    Moderate("Active", 400),
    High("Athletic", 900),
}

data class HydrationProfile(
    val ageYears: Int,
    val weightKg: Int,
    val sex: Sex,
    val activityLevel: ActivityLevel,
    val climateHot: Boolean,
) {
    /**
     * Daily intake target in millilitres. Rough but realistic formula based on
     * body weight (~33 ml/kg), activity, hot climate, and sex/age modifiers.
     */
    val dailyTargetMl: Int
        get() {
            val base = (weightKg * 33).coerceAtLeast(1500)
            val activityBonus = activityLevel.extraMl
            val climateBonus = if (climateHot) 500 else 0
            val sexFactor = when (sex) {
                Sex.Male -> 1.05f
                Sex.Female -> 0.95f
            }
            val ageFactor = when {
                ageYears < 18 -> 0.9f
                ageYears > 60 -> 0.92f
                else -> 1.0f
            }
            return ((base + activityBonus + climateBonus) * sexFactor * ageFactor)
                .roundToInt()
                .let { (it / 50) * 50 } // round to nearest 50 ml for nicer numbers
        }
}

data class DrinkEntry(
    val timestampMs: Long,
    val volumeMl: Int,
)

private const val K_AGE = "hydration_age"
private const val K_WEIGHT = "hydration_weight"
private const val K_SEX = "hydration_sex"
private const val K_ACTIVITY = "hydration_activity"
private const val K_HOT = "hydration_hot"
private const val K_DRINKS = "hydration_drinks"
private const val K_PROFILE_SET = "hydration_profile_set"

private val defaultProfile = HydrationProfile(
    ageYears = 30,
    weightKg = 70,
    sex = Sex.Male,
    activityLevel = ActivityLevel.Moderate,
    climateHot = false,
)

class HydrationStore {
    var profile by mutableStateOf(loadProfile())
        private set
    var profileSet by mutableStateOf(AppStorage.getBoolean(K_PROFILE_SET, false))
        private set
    var drinks by mutableStateOf(loadDrinks())
        private set

    fun saveProfile(p: HydrationProfile) {
        profile = p
        profileSet = true
        AppStorage.putLong(K_AGE, p.ageYears.toLong())
        AppStorage.putLong(K_WEIGHT, p.weightKg.toLong())
        AppStorage.putLong(K_SEX, p.sex.ordinal.toLong())
        AppStorage.putLong(K_ACTIVITY, p.activityLevel.ordinal.toLong())
        AppStorage.putBoolean(K_HOT, p.climateHot)
        AppStorage.putBoolean(K_PROFILE_SET, true)
    }

    fun addDrink(volumeMl: Int) {
        if (volumeMl <= 0) return
        val entry = DrinkEntry(currentTimeMs(), volumeMl)
        // Cap stored history at the last ~3 years to keep the string small.
        val cutoff = entry.timestampMs - 3L * 365 * 86_400_000L
        drinks = (drinks + entry).filter { it.timestampMs >= cutoff }
        persistDrinks()
    }

    fun removeLast() {
        if (drinks.isEmpty()) return
        drinks = drinks.dropLast(1)
        persistDrinks()
    }

    fun clearToday(nowMs: Long) {
        val startOfDay = startOfDayMs(nowMs)
        drinks = drinks.filter { it.timestampMs < startOfDay }
        persistDrinks()
    }

    private fun persistDrinks() {
        AppStorage.putString(K_DRINKS, drinks.joinToString(",") { "${it.timestampMs}:${it.volumeMl}" })
    }

    private fun loadProfile(): HydrationProfile {
        if (!AppStorage.getBoolean(K_PROFILE_SET, false)) return defaultProfile
        val age = AppStorage.getLong(K_AGE, defaultProfile.ageYears.toLong()).toInt()
        val weight = AppStorage.getLong(K_WEIGHT, defaultProfile.weightKg.toLong()).toInt()
        val sex = Sex.values().getOrElse(AppStorage.getLong(K_SEX, 0L).toInt()) { Sex.Male }
        val activity = ActivityLevel.values().getOrElse(
            AppStorage.getLong(K_ACTIVITY, ActivityLevel.Moderate.ordinal.toLong()).toInt()
        ) { ActivityLevel.Moderate }
        val hot = AppStorage.getBoolean(K_HOT, false)
        return HydrationProfile(age, weight, sex, activity, hot)
    }

    private fun loadDrinks(): List<DrinkEntry> {
        val raw = AppStorage.getString(K_DRINKS, "")
        if (raw.isEmpty()) return emptyList()
        return raw.split(",").mapNotNull { token ->
            val parts = token.split(":")
            if (parts.size != 2) return@mapNotNull null
            val ts = parts[0].toLongOrNull() ?: return@mapNotNull null
            val ml = parts[1].toIntOrNull() ?: return@mapNotNull null
            DrinkEntry(ts, ml)
        }
    }
}

@Composable
fun rememberHydrationStore(): HydrationStore = remember { HydrationStore() }

fun todayTotalMl(drinks: List<DrinkEntry>, nowMs: Long): Int {
    val startOfDay = startOfDayMs(nowMs)
    val endOfDay = startOfDay + DAY_MS
    return drinks.filter { it.timestampMs >= startOfDay && it.timestampMs < endOfDay }
        .sumOf { it.volumeMl }
}

// --- bucketing for charts ---

data class Bucket(val label: String, val totalMl: Int)

fun bucketsForDay(drinks: List<DrinkEntry>, nowMs: Long): List<Bucket> {
    val start = startOfDayMs(nowMs)
    val end = start + DAY_MS
    val hourly = IntArray(24)
    drinks.forEach { d ->
        if (d.timestampMs in start until end) {
            val hour = ((d.timestampMs - start) / 3_600_000L).toInt().coerceIn(0, 23)
            hourly[hour] += d.volumeMl
        }
    }
    return List(24) { i ->
        Bucket(label = "${i.toString().padStart(2, '0')}", totalMl = hourly[i])
    }
}

fun bucketsForWeek(drinks: List<DrinkEntry>, nowMs: Long): List<Bucket> {
    val end = startOfDayMs(nowMs) + DAY_MS
    val start = end - 7L * DAY_MS
    val perDay = IntArray(7)
    drinks.forEach { d ->
        if (d.timestampMs in start until end) {
            val day = ((d.timestampMs - start) / DAY_MS).toInt().coerceIn(0, 6)
            perDay[day] += d.volumeMl
        }
    }
    val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    // Compute weekday of the leftmost bucket and rotate labels.
    val leftDayOfWeek = dayOfWeekIndex(start)
    return List(7) { i ->
        Bucket(label = labels[(leftDayOfWeek + i) % 7], totalMl = perDay[i])
    }
}

fun bucketsForMonth(drinks: List<DrinkEntry>, nowMs: Long): List<Bucket> {
    val end = startOfDayMs(nowMs) + DAY_MS
    val start = end - 30L * DAY_MS
    val perDay = IntArray(30)
    drinks.forEach { d ->
        if (d.timestampMs in start until end) {
            val day = ((d.timestampMs - start) / DAY_MS).toInt().coerceIn(0, 29)
            perDay[day] += d.volumeMl
        }
    }
    return List(30) { i ->
        // Show every 5th label to avoid clutter.
        val label = if (i % 5 == 0) "${i + 1}" else ""
        Bucket(label = label, totalMl = perDay[i])
    }
}

fun bucketsForYear(drinks: List<DrinkEntry>, nowMs: Long): List<Bucket> {
    // 12 buckets, each ≈ a 30-day window ending at "now".
    val end = startOfDayMs(nowMs) + DAY_MS
    val bucketMs = 30L * DAY_MS
    val perMonth = IntArray(12)
    val start = end - 12L * bucketMs
    drinks.forEach { d ->
        if (d.timestampMs in start until end) {
            val idx = ((d.timestampMs - start) / bucketMs).toInt().coerceIn(0, 11)
            perMonth[idx] += d.volumeMl
        }
    }
    return List(12) { i ->
        Bucket(label = "${i + 1}m", totalMl = perMonth[i])
    }
}

// --- date helpers ---

private const val DAY_MS = 86_400_000L

/** Local "midnight" approximation using UTC math + a fixed offset is too imprecise,
 *  so we use simple truncation to whole days using the device's UTC timestamp. The
 *  charts therefore align with UTC days, which is good enough for this MVP and
 *  consistent across platforms without an external date library. */
private fun startOfDayMs(ms: Long): Long = (ms / DAY_MS) * DAY_MS

/** Day-of-week index (0 = Monday) for the day that starts at [startMs]. We treat
 *  1970-01-01 (UTC, ms=0) as a Thursday — the historical reality — and offset. */
private fun dayOfWeekIndex(startMs: Long): Int {
    val daysSinceEpoch = (startMs / DAY_MS).toInt()
    // 1970-01-01 was a Thursday; in our 0=Monday scheme that is day 3.
    return ((daysSinceEpoch + 3) % 7 + 7) % 7
}
