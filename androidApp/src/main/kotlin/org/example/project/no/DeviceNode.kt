package org.example.project.no

import android.os.Build
import org.example.project.ev.FlowSignal
import org.example.project.ev.LoadingFlowContext
import org.example.project.ev.LoadingNode
import java.util.Locale

class DeviceNode : LoadingNode {
    override val name: String = "DeviceNode"

    override suspend fun run(ctx: LoadingFlowContext): Pair<LoadingFlowContext, FlowSignal> {
        val device = getDeviceString()

        return ctx.put("device", device) to FlowSignal.Continue
    }

    fun getDeviceString(): String {
        return try {
            buildString {
                val brand = Build.BRAND.replaceFirstChar { it.titlecase(Locale.getDefault()) }
                append(brand).append(' ').append(Build.MODEL)
            }
        } catch (_: Throwable) {
            "unknown_device"
        }
    }
}