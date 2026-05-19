package org.example.project.media

import androidx.compose.runtime.Composable

enum class MediaDenialReason { CameraPermissionDenied, GalleryPermissionDenied, CameraUnavailable }

data class CapturedMedia(val uri: String)

interface MediaController {
    fun takePhoto(onResult: (CapturedMedia?) -> Unit, onDenied: (MediaDenialReason) -> Unit)
    fun pickFromGallery(onResult: (CapturedMedia?) -> Unit, onDenied: (MediaDenialReason) -> Unit)
}

@Composable
expect fun rememberMediaController(): MediaController
