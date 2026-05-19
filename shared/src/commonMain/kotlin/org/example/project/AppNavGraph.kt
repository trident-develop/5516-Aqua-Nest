package org.example.project

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.components.AquariumBackground
import org.example.project.components.BottomNavigationBar
import org.example.project.components.BottomTab
import org.example.project.screens.AquariumScreen
import org.example.project.screens.CareAnalyticsScreen
import org.example.project.screens.DashboardScreen
import org.example.project.screens.FishLibraryScreen
import org.example.project.components.SystemBackHandler
import org.example.project.screens.WebViewSheet
import org.example.project.theme.AquariumTheme

@Composable
fun AppNavGraph() {
    val tabs = remember {
        listOf(
            BottomTab("Dashboard", "◉"),
            BottomTab("Aquarium", "❄"),
            BottomTab("Fish Library", "✦"),
            BottomTab("Care", "♡"),
        )
    }
    var selected by rememberSaveable { mutableStateOf(0) }
    var webViewUrl by remember { mutableStateOf<String?>(null) }
    var webViewTitle by remember { mutableStateOf("") }

    // System back: from any non-Dashboard tab go to Dashboard; from Dashboard let
    // the system handle it (i.e. exit). Disabled while an overlay (WebView) is
    // showing — overlay registers its own deeper handler that runs first.
    SystemBackHandler(enabled = selected != 0 && webViewUrl == null) {
        selected = 0
    }

    AquariumTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            AquariumBackground(bubbleCount = 10, fishCount = 2) {
                Box(modifier = Modifier.fillMaxSize().safeContentPadding()) {
                    AnimatedContent(
                        targetState = selected,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(280)) +
                                slideInVertically(animationSpec = tween(280)) { it / 12 }) togetherWith
                                (fadeOut(animationSpec = tween(160)) +
                                    slideOutVertically(animationSpec = tween(160)) { -it / 12 })
                        },
                        modifier = Modifier.fillMaxSize().padding(bottom = 96.dp)
                    ) { index ->
                        when (index) {
                            0 -> DashboardScreen()
                            1 -> AquariumScreen(
                                onOpenWebView = { title, url ->
                                    webViewTitle = title
                                    webViewUrl = url
                                }
                            )
                            2 -> FishLibraryScreen()
                            else -> CareAnalyticsScreen()
                        }
                    }

                    BottomNavigationBar(
                        tabs = tabs,
                        selectedIndex = selected,
                        onSelected = { selected = it },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }

            if (webViewUrl != null) {
                // Deeper handler — closes the overlay first if back is pressed.
                SystemBackHandler(enabled = true) { webViewUrl = null }
                WebViewSheet(
                    title = webViewTitle,
                    url = webViewUrl!!,
                    onClose = { webViewUrl = null }
                )
            }
        }
    }
}
