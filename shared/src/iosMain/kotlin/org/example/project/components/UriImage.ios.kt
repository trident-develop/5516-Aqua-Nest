package org.example.project.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.Image as SkiaImage
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun UriImage(uri: String, modifier: Modifier) {
    val bitmap = remember(uri) {
        runCatching {
            // NSURL.URLWithString rejects URIs with unencoded spaces, and the
            // iOS Application Support directory contains a space — so a naive
            // "file://" + path string fails to parse on iOS 15/16. For "file://"
            // URIs interpret the suffix as a raw path via fileURLWithPath, which
            // handles spaces correctly. Fall back to URLWithString for any
            // other scheme.
            val url: NSURL = if (uri.startsWith("file://")) {
                NSURL.fileURLWithPath(uri.removePrefix("file://"))
            } else {
                NSURL.URLWithString(uri) ?: return@runCatching null
            }
            val data: NSData = NSData.dataWithContentsOfURL(url) ?: return@runCatching null
            val bytes = data.toByteArray()
            SkiaImage.makeFromEncoded(bytes).toComposeImageBitmap()
        }.getOrNull()
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Crop,
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val length = this.length.toInt()
    if (length <= 0) return ByteArray(0)
    val bytes = ByteArray(length)
    bytes.usePinned { pinned ->
        memcpy(pinned.addressOf(0), this.bytes, length.convert())
    }
    return bytes
}
