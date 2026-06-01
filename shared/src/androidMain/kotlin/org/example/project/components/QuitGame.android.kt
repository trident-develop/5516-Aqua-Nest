package org.example.project.components

import android.annotation.SuppressLint
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@SuppressLint("ContextCastToActivity")
@Composable
actual fun QuitGame() {
    val activity = LocalContext.current as? Activity

    BackHandler(enabled = true) {
        activity?.finish()
    }
}