package org.example.project.media

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import org.example.project.theme.AquariumColors
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
actual fun rememberMediaController(): MediaController {
    val context = LocalContext.current
    val activity = remember(context) { context.findComponentActivity() }
    val lifecycleOwner = LocalLifecycleOwner.current

    var pendingPhoto by remember { mutableStateOf<((CapturedMedia?) -> Unit)?>(null) }
    var pendingPhotoDenied by remember { mutableStateOf<((MediaDenialReason) -> Unit)?>(null) }
    var pendingGallery by remember { mutableStateOf<((CapturedMedia?) -> Unit)?>(null) }
    var showCamera by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        val cb = pendingGallery
        pendingGallery = null
        val persisted = uri?.let { context.persistImageFromUri(it) }
        cb?.invoke(persisted?.let { CapturedMedia(it) })
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            showCamera = true
        } else {
            val denied = pendingPhotoDenied
            pendingPhotoDenied = null
            pendingPhoto = null
            denied?.invoke(MediaDenialReason.CameraPermissionDenied)
        }
    }

    if (showCamera) {
        Dialog(
            onDismissRequest = {
                showCamera = false
                pendingPhoto?.invoke(null)
                pendingPhoto = null
                pendingPhotoDenied = null
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            CameraCaptureSurface(
                lifecycleOwner = lifecycleOwner,
                onCaptured = { uri ->
                    val cb = pendingPhoto
                    pendingPhoto = null
                    pendingPhotoDenied = null
                    showCamera = false
                    cb?.invoke(uri?.let { CapturedMedia(it) })
                },
                onCancel = {
                    pendingPhoto?.invoke(null)
                    pendingPhoto = null
                    pendingPhotoDenied = null
                    showCamera = false
                },
                onError = {
                    val denied = pendingPhotoDenied
                    pendingPhoto?.invoke(null)
                    pendingPhoto = null
                    pendingPhotoDenied = null
                    showCamera = false
                    denied?.invoke(MediaDenialReason.CameraUnavailable)
                }
            )
        }
    }

    return remember(activity) {
        object : MediaController {
            override fun takePhoto(
                onResult: (CapturedMedia?) -> Unit,
                onDenied: (MediaDenialReason) -> Unit
            ) {
                if (activity == null) {
                    onDenied(MediaDenialReason.CameraUnavailable)
                    return
                }
                val granted = ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
                pendingPhoto = onResult
                pendingPhotoDenied = onDenied
                if (granted) {
                    showCamera = true
                } else {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }

            override fun pickFromGallery(
                onResult: (CapturedMedia?) -> Unit,
                onDenied: (MediaDenialReason) -> Unit
            ) {
                pendingGallery = onResult
                galleryLauncher.launch(
                    androidx.activity.result.PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            }
        }
    }
}

private fun Context.findComponentActivity(): ComponentActivity? {
    var ctx: Context? = this
    while (ctx != null) {
        if (ctx is ComponentActivity) return ctx
        ctx = (ctx as? android.content.ContextWrapper)?.baseContext
    }
    return null
}

@Composable
private fun CameraCaptureSurface(
    lifecycleOwner: LifecycleOwner,
    onCaptured: (String?) -> Unit,
    onCancel: () -> Unit,
    onError: () -> Unit,
) {
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    var bindFailed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            try {
                val provider = future.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (_: Throwable) {
                bindFailed = true
            }
        }, ContextCompat.getMainExecutor(context))
    }

    LaunchedEffect(bindFailed) {
        if (bindFailed) onError()
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                val provider = ProcessCameraProvider.getInstance(context).get()
                provider.unbindAll()
            } catch (_: Throwable) {
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding()
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AquariumColors.DeepOcean.copy(alpha = 0.7f))
                    .clickable(onClick = onCancel),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✕",
                    color = AquariumColors.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(AquariumColors.Lime)
                        .clickable {
                            capturePhoto(context, imageCapture, onCaptured)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .background(AquariumColors.White)
                    )
                }
            }
        }
    }
}

private fun capturePhoto(
    context: Context,
    imageCapture: ImageCapture,
    onCaptured: (String?) -> Unit,
) {
    val dir = File(context.filesDir, "fish_photos").apply { mkdirs() }
    val fileName = "aquanest_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
    val outFile = File(dir, fileName)
    val options = ImageCapture.OutputFileOptions.Builder(outFile).build()
    imageCapture.takePicture(
        options,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                // Always return our private file URI so it survives app restarts.
                onCaptured(outFile.toURI().toString())
            }

            override fun onError(exc: ImageCaptureException) {
                onCaptured(null)
            }
        }
    )
}

/**
 * Copies the image referenced by [src] (a content/file URI from the media picker)
 * into the app's private [filesDir]. Returns a stable `file://` URI that remains
 * valid across app restarts even after the source content URI permission is revoked.
 */
private fun Context.persistImageFromUri(src: Uri): String? {
    return try {
        val dir = File(filesDir, "fish_photos").apply { mkdirs() }
        val fileName = "aquanest_${SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())}.jpg"
        val outFile = File(dir, fileName)
        contentResolver.openInputStream(src)?.use { input ->
            outFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: return null
        outFile.toURI().toString()
    } catch (t: Throwable) {
        null
    }
}
