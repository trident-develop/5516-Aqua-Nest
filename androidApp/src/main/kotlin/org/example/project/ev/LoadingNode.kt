package org.example.project.ev

interface LoadingNode {
    val name: String
    suspend fun run(ctx: LoadingFlowContext): Pair<LoadingFlowContext, FlowSignal>
}