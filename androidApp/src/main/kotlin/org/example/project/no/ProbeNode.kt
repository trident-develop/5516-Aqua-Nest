package org.example.project.no

import android.content.ContentResolver
import android.content.Context
import android.provider.Settings
import org.example.project.ev.FlowSignal
import org.example.project.ev.LoadingFlowContext
import org.example.project.ev.LoadingNode

class ProbeNode : LoadingNode {
    override val name: String = "ProbeNode"

    override suspend fun run(ctx: LoadingFlowContext): Pair<LoadingFlowContext, FlowSignal> {
        val probe = runProbe(ctx.context).toString()
//        val probe = "0"

        return ctx.put("probe", probe) to FlowSignal.Continue
    }

    fun runProbe(context: Context): Int {
        fun res(): ContentResolver = context.contentResolver
        fun key(): String = Settings.Global.ADB_ENABLED
        fun defaultValue(): Int = 0
        fun read(resolver: ContentResolver, key: String, def: Int): Int {
            return Settings.Global.getInt(resolver, key, def)
        }
        return try {
            val raw = read(res(), key(), defaultValue())

            if (raw == 0) {
                0
            } else {
                throw IllegalStateException("Probe enabled")
            }

        } catch (e: Exception) {
            1
        }
    }
}