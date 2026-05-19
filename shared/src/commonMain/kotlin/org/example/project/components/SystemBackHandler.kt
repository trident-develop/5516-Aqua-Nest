package org.example.project.components

import androidx.compose.runtime.Composable

/**
 * Reacts to the platform's system back action.
 *
 * - Android: hooks into `OnBackPressedDispatcher` via Compose's `BackHandler`.
 * - iOS: no-op (iOS has no system back button; left as a future hook for the
 *   swipe-back gesture).
 *
 * BackHandlers are stacked by composition order — the most recently added one
 * fires first. So a nested screen's handler will swallow the event before the
 * parent's handler runs.
 */
@Composable
expect fun SystemBackHandler(enabled: Boolean = true, onBack: () -> Unit)
