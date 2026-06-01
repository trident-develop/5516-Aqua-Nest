package org.example.project

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import org.example.project.ev.LoadingDecision
import org.example.project.privacy.MainClient
import org.example.project.screens.LoadingScreen
import org.example.project.screens.NoInternetScreen
import org.example.project.storage.ScoreStorage

@SuppressLint("ContextCastToActivity")
@Composable
fun App(
    mainClient: MainClient,
    scoreStorage: ScoreStorage,
) {
    var showContent by remember { mutableStateOf(false) }
    val context = LocalContext.current as MainActivity
    var retryKey by remember { mutableIntStateOf(0) }
    val isConnected = remember(retryKey) { context.isFlowersConnected() }
    val openOtherScreen = remember {
        {
            showContent = true
        }
    }

    LaunchedEffect(Unit) {
        mainClient.setOnOpenOtherScreenCallback(openOtherScreen)
    }

    if (showContent) {
        AppNavGraph()
    } else {
        if (isConnected) {
            LoadingScreen()

            LaunchedEffect(Unit) {
                val engine = LoadingFlowFactory.create(getBaseUrl())

                engine.execute(
                    context = context,
                    storage = scoreStorage
                ).collect { decision ->
                    when (decision) {
                        is LoadingDecision.OpenWebView -> {
                            log("LoadingScreen: open webview = ${decision.url}")
                            mainClient.loadUrl(decision.url)
                        }

                        is LoadingDecision.OpenOtherScreen -> {
                            log("LoadingScreen: open other screen, reason = ${decision.reason}")
                            openOtherScreen()
                        }
                    }
                }
            }

        } else {
            NoInternetScreen {
                retryKey++
            }
        }
    }
}