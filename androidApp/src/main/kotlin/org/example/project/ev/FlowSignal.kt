package org.example.project.ev

sealed class FlowSignal {
    data object Continue : FlowSignal()
    data class Finish(val decision: LoadingDecision) : FlowSignal()
}