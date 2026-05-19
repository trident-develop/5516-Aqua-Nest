package org.example.project.care

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.example.project.data.SampleData
import org.example.project.storage.AppStorage
import org.example.project.storage.currentTimeMs
import kotlin.math.max
import kotlin.math.min

data class DeviceCare(
    val on: Boolean,
    val changedAtMs: Long,
)

/**
 * Per-device sensitivity weights for the health model.
 *
 * Aquarium care reference roughly:
 * - Filter must run 24/7 — biofilter dies after ~24h offline.
 * - Heater needed continuously for tropical fish.
 * - Light cycle ~10–12h on / 12–14h off; long-term darkness is fine, no light at all over weeks is bad.
 * - Air pump optional, helps oxygen at night.
 */
private data class DevicePenalty(
    val penaltyPerHourOff: Float,
    val maxPenalty: Float,
    val gracePeriodHours: Float = 0f,
)

private val devicePenalties = listOf(
    DevicePenalty(penaltyPerHourOff = 1.5f, maxPenalty = 40f),       // 0: Filter
    DevicePenalty(penaltyPerHourOff = 1.0f, maxPenalty = 30f),       // 1: Heater
    DevicePenalty(penaltyPerHourOff = 0.4f, maxPenalty = 12f, gracePeriodHours = 14f), // 2: Light
    DevicePenalty(penaltyPerHourOff = 0.15f, maxPenalty = 6f, gracePeriodHours = 8f), // 3: Air pump
)

class TankCare(val tankIndex: Int) {
    var devices by mutableStateOf<List<DeviceCare>>(loadDevices())
        private set
    var feeds by mutableStateOf<List<Long>>(loadFeeds())
        private set

    fun toggle(deviceIndex: Int, on: Boolean) {
        val now = currentTimeMs()
        devices = devices.toMutableList().also {
            it[deviceIndex] = DeviceCare(on = on, changedAtMs = now)
        }
        AppStorage.putBoolean(deviceKey(deviceIndex), on)
        AppStorage.putLong(deviceTimeKey(deviceIndex), now)
    }

    fun feed() {
        val now = currentTimeMs()
        val updated = (feeds + now).takeLast(MAX_FEED_HISTORY)
        feeds = updated
        AppStorage.putString(feedsKey(), updated.joinToString(","))
    }

    private fun loadDevices(): List<DeviceCare> {
        val now = currentTimeMs()
        return SampleData.devices.mapIndexed { i, d ->
            DeviceCare(
                on = AppStorage.getBoolean(deviceKey(i), d.initiallyOn),
                changedAtMs = AppStorage.getLong(deviceTimeKey(i), now),
            )
        }
    }

    private fun loadFeeds(): List<Long> {
        val raw = AppStorage.getString(feedsKey(), "")
        if (raw.isEmpty()) return emptyList()
        return raw.split(",").mapNotNull { it.trim().toLongOrNull() }
    }

    private fun deviceKey(i: Int) = "t${tankIndex}_dev_$i"
    private fun deviceTimeKey(i: Int) = "t${tankIndex}_dev_${i}_at"
    private fun feedsKey() = "t${tankIndex}_feeds"

    companion object {
        private const val MAX_FEED_HISTORY = 12
    }
}

@Composable
fun rememberTankCare(tankIndex: Int): TankCare =
    remember(tankIndex) { TankCare(tankIndex) }

/**
 * Computes a 0..100 health score for the active tank.
 *
 * Penalties:
 * - Device off duration (per-device weighted).
 * - Time since last feed (hunger). Healthy interval ≈ 8–14h. >24h → starvation penalty grows.
 * - Overfeeding: more than 3 feeds in the last 6 hours.
 */
fun computeHealth(
    devices: List<DeviceCare>,
    feeds: List<Long>,
    nowMs: Long,
): Float {
    var penalty = 0f

    devices.forEachIndexed { i, dev ->
        if (!dev.on) {
            val hoursOff = (nowMs - dev.changedAtMs).coerceAtLeast(0L) / 3_600_000f
            val cfg = devicePenalties.getOrElse(i) {
                DevicePenalty(penaltyPerHourOff = 0.2f, maxPenalty = 5f)
            }
            val effectiveHours = max(0f, hoursOff - cfg.gracePeriodHours)
            penalty += min(effectiveHours * cfg.penaltyPerHourOff, cfg.maxPenalty)
        }
    }

    val lastFeed = feeds.maxOrNull()
    val hungerPenalty = when {
        lastFeed == null -> 18f
        else -> {
            val hours = (nowMs - lastFeed).coerceAtLeast(0L) / 3_600_000f
            when {
                hours < 14f -> 0f
                hours < 24f -> (hours - 14f) * 0.8f          // up to ~8
                hours < 48f -> 8f + (hours - 24f) * 1.0f     // up to ~32
                hours < 72f -> 32f + (hours - 48f) * 0.8f    // up to ~51
                else -> min(51f + (hours - 72f) * 0.4f, 70f)
            }
        }
    }
    penalty += hungerPenalty

    val sixHoursAgo = nowMs - 6 * 3_600_000L
    val recentFeeds = feeds.count { it >= sixHoursAgo }
    if (recentFeeds > 3) {
        penalty += (recentFeeds - 3) * 6f
    }

    return (100f - penalty).coerceIn(0f, 100f)
}

/**
 * Returns next-action suggestion text and how urgent it is (0..1).
 */
data class CareHint(val message: String, val urgency: Float)

enum class CareUrgency { Ok, Soon, Urgent }

data class FeedSchedule(
    val statusText: String,
    val advice: String,
    val urgency: CareUrgency,
)

data class DeviceSchedule(
    val deviceIndex: Int,
    val deviceName: String,
    val statusText: String,
    val advice: String,
    val urgency: CareUrgency,
)

data class CareSchedule(
    val feed: FeedSchedule,
    val devices: List<DeviceSchedule>,
)

private const val IDEAL_FEED_INTERVAL_H = 8f
private const val LIGHT_ON_HOURS = 12f
private const val LIGHT_OFF_HOURS = 12f
private const val PUMP_TOLERATED_OFF_HOURS = 12f

private fun formatDuration(hours: Float): String {
    val absH = hours.coerceAtLeast(0f)
    return when {
        absH < 1f -> {
            val minutes = (absH * 60f).toInt().coerceAtLeast(1)
            "${minutes}min"
        }
        absH < 24f -> "${absH.toInt().coerceAtLeast(1)}h"
        else -> "${(absH / 24f).toInt()}d"
    }
}

fun computeSchedule(
    devices: List<DeviceCare>,
    feeds: List<Long>,
    nowMs: Long,
): CareSchedule {
    val feed = computeFeedSchedule(feeds, nowMs)
    val deviceSchedules = devices.mapIndexed { i, dev -> computeDeviceSchedule(i, dev, nowMs) }
    return CareSchedule(feed, deviceSchedules)
}

private fun computeFeedSchedule(feeds: List<Long>, nowMs: Long): FeedSchedule {
    val last = feeds.maxOrNull() ?: return FeedSchedule(
        statusText = "Not fed yet",
        advice = "Feed now",
        urgency = CareUrgency.Urgent,
    )
    val hoursAgo = ((nowMs - last).coerceAtLeast(0L)) / 3_600_000f
    val status = "Fed ${formatDuration(hoursAgo)} ago"
    val sixHoursAgo = nowMs - 6 * 3_600_000L
    val recent = feeds.count { it >= sixHoursAgo }
    return when {
        recent > 3 -> FeedSchedule(status, "Wait — already fed ${recent}× in 6h", CareUrgency.Soon)
        hoursAgo >= 24f -> FeedSchedule(status, "Hungry — feed now", CareUrgency.Urgent)
        hoursAgo >= 14f -> FeedSchedule(status, "Feed now", CareUrgency.Urgent)
        hoursAgo >= IDEAL_FEED_INTERVAL_H -> FeedSchedule(status, "Ready to feed", CareUrgency.Soon)
        else -> {
            val nextIn = IDEAL_FEED_INTERVAL_H - hoursAgo
            FeedSchedule(status, "Next feed in ${formatDuration(nextIn)}", CareUrgency.Ok)
        }
    }
}

private fun computeDeviceSchedule(index: Int, dev: DeviceCare, nowMs: Long): DeviceSchedule {
    val name = SampleData.devices.getOrNull(index)?.name ?: "Device"
    val hours = ((nowMs - dev.changedAtMs).coerceAtLeast(0L)) / 3_600_000f
    return when (index) {
        0 -> if (dev.on) {
            DeviceSchedule(index, name, "Running", "Keep on 24/7", CareUrgency.Ok)
        } else {
            DeviceSchedule(
                index, name,
                "Off for ${formatDuration(hours)}",
                "Turn on ASAP — biofilter at risk",
                CareUrgency.Urgent,
            )
        }
        1 -> if (dev.on) {
            DeviceSchedule(index, name, "Holding temperature", "Keep on 24/7", CareUrgency.Ok)
        } else {
            DeviceSchedule(
                index, name,
                "Off for ${formatDuration(hours)}",
                "Turn on — fish need warmth",
                CareUrgency.Urgent,
            )
        }
        2 -> if (dev.on) {
            val remaining = LIGHT_ON_HOURS - hours
            if (remaining <= 0f) DeviceSchedule(
                index, name,
                "On ${formatDuration(hours)}",
                "Turn off — daylight cycle done",
                CareUrgency.Soon,
            ) else DeviceSchedule(
                index, name,
                "On ${formatDuration(hours)}",
                "Turn off in ${formatDuration(remaining)}",
                CareUrgency.Ok,
            )
        } else {
            val remaining = LIGHT_OFF_HOURS - hours
            if (remaining <= 0f) DeviceSchedule(
                index, name,
                "Off ${formatDuration(hours)}",
                "Turn on — start daylight",
                CareUrgency.Soon,
            ) else DeviceSchedule(
                index, name,
                "Off ${formatDuration(hours)}",
                "Resting — ${formatDuration(remaining)} to morning",
                CareUrgency.Ok,
            )
        }
        3 -> if (dev.on) {
            DeviceSchedule(index, name, "Bubbling air", "Fine to leave on", CareUrgency.Ok)
        } else if (hours > PUMP_TOLERATED_OFF_HOURS) {
            DeviceSchedule(
                index, name,
                "Off ${formatDuration(hours)}",
                "Consider turning on for oxygen",
                CareUrgency.Soon,
            )
        } else {
            DeviceSchedule(index, name, "Off ${formatDuration(hours)}", "Optional — off is fine", CareUrgency.Ok)
        }
        else -> DeviceSchedule(index, name, if (dev.on) "On" else "Off", "", CareUrgency.Ok)
    }
}

fun nextCareHint(
    devices: List<DeviceCare>,
    feeds: List<Long>,
    nowMs: Long,
): CareHint {
    val offDevices = devices.mapIndexedNotNull { i, d -> if (!d.on) i else null }
    if (offDevices.isNotEmpty()) {
        val critical = offDevices.firstOrNull { it == 0 || it == 1 }
        if (critical != null) {
            val name = SampleData.devices[critical].name
            return CareHint("Turn on $name", urgency = 0.9f)
        }
    }
    val lastFeed = feeds.maxOrNull()
    if (lastFeed == null) {
        return CareHint("Fish haven't been fed yet", urgency = 0.7f)
    }
    val hours = (nowMs - lastFeed) / 3_600_000f
    val sixHoursAgo = nowMs - 6 * 3_600_000L
    val recentFeeds = feeds.count { it >= sixHoursAgo }
    return when {
        recentFeeds > 3 -> CareHint("Slow down on feeding", urgency = 0.6f)
        hours > 24f -> CareHint("Fish are hungry — feed now", urgency = 0.9f)
        hours > 14f -> CareHint("It's about feeding time", urgency = 0.5f)
        offDevices.isNotEmpty() -> {
            val name = SampleData.devices[offDevices.first()].name
            CareHint("Consider turning on $name", urgency = 0.3f)
        }
        else -> CareHint("All good — fish are happy", urgency = 0f)
    }
}
