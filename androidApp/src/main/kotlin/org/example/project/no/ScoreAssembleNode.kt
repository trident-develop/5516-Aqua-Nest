package org.example.project.no

import org.example.project.ScoreBuilder
import org.example.project.ev.FlowSignal
import org.example.project.ev.LoadingDecision
import org.example.project.ev.LoadingFlowContext
import org.example.project.ev.LoadingNode

class ScoreAssembleNode(
    private val scoreBuilder: ScoreBuilder
) : LoadingNode {

    override val name: String = "LinkAssembleNode"

    override suspend fun run(ctx: LoadingFlowContext): Pair<LoadingFlowContext, FlowSignal> {

        val url = scoreBuilder.build(ctx.data)

//        log("$name: link ready = $url")

        return ctx.put("final_url", url) to FlowSignal.Finish(
            LoadingDecision.OpenWebView(url)
        )
    }
}