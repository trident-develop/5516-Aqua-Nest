package org.example.project.ev

sealed class LoadingDecision {
    data class OpenWebView(val url: String) : LoadingDecision()
    data class OpenOtherScreen(val reason: String) : LoadingDecision()
}