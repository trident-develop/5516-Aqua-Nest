package org.example.project.no

import org.example.project.ev.FlowSignal
import org.example.project.ev.LoadingDecision
import org.example.project.ev.LoadingFlowContext
import org.example.project.ev.LoadingNode

class CachedScoreNode : LoadingNode {
    override val name: String = "CachedScoreNode"

    override suspend fun run(ctx: LoadingFlowContext): Pair<LoadingFlowContext, FlowSignal> {

        val score = ctx.storage.getSavedScore()

        return if (!score.isNullOrBlank()) {
//            log("$name: cached score found = $score")
            ctx to FlowSignal.Finish(
                LoadingDecision.OpenWebView(score)
            )
        } else {
//            log("$name: cached score empty")
            ctx to FlowSignal.Continue
        }
    }
}