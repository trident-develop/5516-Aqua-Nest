package org.example.project.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Renders an image referenced by a content/file URI using platform-native loaders.
 * Returns nothing visible if the image cannot be loaded.
 */
@Composable
expect fun UriImage(uri: String, modifier: Modifier = Modifier)
