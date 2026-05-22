package org.example.project.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVAuthorizationStatusRestricted
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSItemProvider
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDomainMask
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.writeToFile
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.darwin.NSObject
import org.example.project.composeHostViewController

private val retainedDelegates = mutableListOf<NSObject>()

@Composable
actual fun rememberMediaController(): MediaController = remember { IosMediaController() }

private class IosMediaController : MediaController {

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    override fun takePhoto(
        onResult: (CapturedMedia?) -> Unit,
        onDenied: (MediaDenialReason) -> Unit
    ) {
        val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
        when (status) {
            AVAuthorizationStatusAuthorized -> presentCamera(onResult, onDenied)
            AVAuthorizationStatusNotDetermined -> {
                AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                    NSOperationQueue.mainQueue.addOperationWithBlock {
                        if (granted) {
                            presentCamera(onResult, onDenied)
                        } else {
                            onDenied(MediaDenialReason.CameraPermissionDenied)
                        }
                    }
                }
            }
            AVAuthorizationStatusDenied, AVAuthorizationStatusRestricted -> {
                onDenied(MediaDenialReason.CameraPermissionDenied)
            }
            else -> {
                onDenied(MediaDenialReason.CameraPermissionDenied)
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    override fun pickFromGallery(
        onResult: (CapturedMedia?) -> Unit,
        onDenied: (MediaDenialReason) -> Unit
    ) {
        val root = rootViewController()
        if (root == null) {
            onResult(null)
            return
        }
        val config = PHPickerConfiguration().apply {
            setSelectionLimit(1)
            setFilter(PHPickerFilter.imagesFilter())
        }
        val picker = PHPickerViewController(configuration = config)
        val delegate = GalleryDelegate { uri ->
            onResult(uri?.let { CapturedMedia(it) })
        }
        picker.delegate = delegate
        retainedDelegates.add(delegate)
        root.presentViewController(picker, animated = true, completion = null)
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    private fun presentCamera(
        onResult: (CapturedMedia?) -> Unit,
        onDenied: (MediaDenialReason) -> Unit,
    ) {
        val cameraType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
        if (!UIImagePickerController.isSourceTypeAvailable(cameraType)) {
            onDenied(MediaDenialReason.CameraUnavailable)
            return
        }
        val root = rootViewController()
        if (root == null) {
            onResult(null)
            return
        }
        val picker = UIImagePickerController()
        picker.sourceType = cameraType
        val delegate = CameraDelegate { uri ->
            onResult(uri?.let { CapturedMedia(it) })
        }
        picker.delegate = delegate
        retainedDelegates.add(delegate)
        root.presentViewController(picker, animated = true, completion = null)
    }
}

// Prefer presenting from the Compose host view controller — it's guaranteed to
// be in the active view hierarchy on iOS 15/16, where UIApplication.keyWindow
// is unreliable under SwiftUI's scene-based lifecycle. If for some reason the
// Compose host is not registered yet, fall back to walking connectedScenes.
private fun rootViewController(): UIViewController? {
    val base = composeHostViewController ?: findKeyWindow()?.rootViewController ?: return null
    var controller: UIViewController = base
    while (controller.presentedViewController != null) {
        controller = controller.presentedViewController ?: break
    }
    return controller
}

private fun findKeyWindow(): UIWindow? {
    val app = UIApplication.sharedApplication
    val scenes = app.connectedScenes
    var fallbackScene: UIWindowScene? = null
    var activeScene: UIWindowScene? = null
    for (scene in scenes) {
        val windowScene = scene as? UIWindowScene ?: continue
        if (windowScene.activationState == UISceneActivationStateForegroundActive) {
            activeScene = windowScene
            break
        }
        if (fallbackScene == null) fallbackScene = windowScene
    }
    val chosen = activeScene ?: fallbackScene
    if (chosen != null) {
        val windows = chosen.windows
        for (w in windows) {
            val window = w as? UIWindow ?: continue
            if (window.isKeyWindow()) return window
        }
        (windows.firstOrNull() as? UIWindow)?.let { return it }
    }
    return app.keyWindow
}

@OptIn(ExperimentalForeignApi::class)
private fun fishPhotosDirectory(): String {
    val supportDirs = NSSearchPathForDirectoriesInDomains(
        NSApplicationSupportDirectory,
        NSUserDomainMask,
        true
    )
    val base = (supportDirs.firstOrNull() as? String) ?: NSTemporaryDirectory()
    val dir = "$base/fish_photos"
    NSFileManager.defaultManager.createDirectoryAtPath(
        dir, withIntermediateDirectories = true, attributes = null, error = null
    )
    return dir
}

private fun saveImageToTemp(image: UIImage): String? {
    val data = UIImageJPEGRepresentation(image, 0.9) ?: return null
    val path = "${fishPhotosDirectory()}/aquanest_${NSUUID().UUIDString}.jpg"
    val ok = data.writeToFile(path, atomically = true)
    return if (ok) "file://$path" else null
}

// Decode the picked file as a UIImage and re-encode to JPEG. Photos picked
// from the iOS 15/16 photo library are typically HEIC, which Skia (used by
// Compose Multiplatform for image decoding) cannot read. Re-encoding ensures
// the saved file is a JPEG that UriImage can actually render.
@OptIn(ExperimentalForeignApi::class)
private fun saveItemProviderFileAsJpeg(source: NSURL): String? {
    val data = NSData.dataWithContentsOfURL(source) ?: return null
    val image = UIImage(data = data)
    return saveImageToTemp(image)
}

@OptIn(BetaInteropApi::class)
private class CameraDelegate(
    private val onComplete: (String?) -> Unit,
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {

    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>,
    ) {
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        picker.dismissViewControllerAnimated(true) {
            retainedDelegates.remove(this)
            val uri = image?.let { saveImageToTemp(it) }
            onComplete(uri)
        }
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true) {
            retainedDelegates.remove(this)
            onComplete(null)
        }
    }
}

@OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
private class GalleryDelegate(
    private val onComplete: (String?) -> Unit,
) : NSObject(), PHPickerViewControllerDelegateProtocol {

    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        val result = didFinishPicking.firstOrNull() as? PHPickerResult
        picker.dismissViewControllerAnimated(true) {
            if (result == null) {
                retainedDelegates.remove(this)
                onComplete(null)
                return@dismissViewControllerAnimated
            }
            val provider: NSItemProvider = result.itemProvider
            provider.loadFileRepresentationForTypeIdentifier("public.image") { url, _ ->
                val uri = url?.let { saveItemProviderFileAsJpeg(it) }
                NSOperationQueue.mainQueue.addOperationWithBlock {
                    retainedDelegates.remove(this)
                    onComplete(uri)
                }
            }
        }
    }
}
