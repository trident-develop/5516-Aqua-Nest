package org.example.project.no

import android.content.Context
import org.example.project.ev.FlowSignal
import org.example.project.ev.LoadingFlowContext
import org.example.project.ev.LoadingNode

class FirstInstNode : LoadingNode {
    override val name: String = "PackageNode"

    override suspend fun run(ctx: LoadingFlowContext): Pair<LoadingFlowContext, FlowSignal> {
        val firstInst = readInstallTime(ctx.context)

        return ctx.put("package", firstInst) to FlowSignal.Continue
    }

    private fun readInstallTime(context: Context): String {
        return runCatching {
            context.packageManager
                .getPackageInfo(context.packageName, 0)
                .firstInstallTime
                .toString()
        }.getOrDefault("0")
    }
}