package org.example.project.no

import org.example.project.ev.FlowSignal
import org.example.project.ev.LoadingDecision
import org.example.project.ev.LoadingFlowContext
import org.example.project.ev.LoadingNode

class BrokenScoreNode : LoadingNode {
    override val name: String = "BrokenScoreNode"

    override suspend fun run(ctx: LoadingFlowContext): Pair<LoadingFlowContext, FlowSignal> {

        val brokenScore = ctx.storage.getBrokenScore()

        return if (!brokenScore.isNullOrBlank()) {
//            log("$name: broken score found = $brokenScore")
            ctx.put("broken_score", brokenScore) to FlowSignal.Finish(
                LoadingDecision.OpenOtherScreen("broken_score_exists")
            )
        } else {
//            log("$name: broken score empty")
            ctx to FlowSignal.Continue
        }
    }
}