package org.example.project.components

import androidx.compose.runtime.Composable

@Composable
actual fun BlockSystemBack() {
    // iOS has no system back button; navigation is gesture-driven.
    // Nothing to suppress here.
}