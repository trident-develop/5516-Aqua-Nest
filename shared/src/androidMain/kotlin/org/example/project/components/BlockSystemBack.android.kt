package org.example.project.components

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@Composable
actual fun BlockSystemBack() {
    BackHandler(enabled = true) { /* intentionally swallowed */ }
}