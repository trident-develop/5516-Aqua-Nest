package org.example.project.components

import androidx.compose.runtime.Composable

@Composable
actual fun SystemBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS has no system back button — left as a no-op. Could later be wired to
    // the swipe-back gesture via a custom UIScreenEdgePanGestureRecognizer.
}
