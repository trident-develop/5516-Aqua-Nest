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
import platform.Foundation.NSFileManager
import platform.Foundation.NSItemProvider
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDomainMask
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
import platform.UIKit.UIViewController
import platform.darwin.NSObject

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

private fun rootViewController(): UIViewController? {
    val window = UIApplication.sharedApplication.keyWindow ?: return null
    var controller = window.rootViewController
    while (controller?.presentedViewController != null) {
        controller = controller.presentedViewController
    }
    return controller
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

@OptIn(ExperimentalForeignApi::class)
private fun copyToTemp(source: NSURL): String? {
    val srcPath = source.path ?: return null
    val destPath = "${fishPhotosDirectory()}/aquanest_${NSUUID().UUIDString}.jpg"
    NSFileManager.defaultManager.removeItemAtPath(destPath, error = null)
    val ok = NSFileManager.defaultManager.copyItemAtPath(srcPath, toPath = destPath, error = null)
    return if (ok) "file://$destPath" else null
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
                val uri = url?.let { copyToTemp(it) }
                NSOperationQueue.mainQueue.addOperationWithBlock {
                    retainedDelegates.remove(this)
                    onComplete(uri)
                }
            }
        }
    }
}
