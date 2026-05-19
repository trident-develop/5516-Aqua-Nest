package org.example.project

import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIColor
import platform.UIKit.UIViewController

@OptIn(ExperimentalForeignApi::class)
fun MainViewController(): UIViewController {
    val controller = ComposeUIViewController { App() }
    // Match the app's gradient background top stop so there's no white flash
    // between the launch screen and the first Compose frame.
    controller.view.backgroundColor = UIColor.colorWithRed(
        red = 0x0B / 255.0,
        green = 0x37 / 255.0,
        blue = 0x54 / 255.0,
        alpha = 1.0,
    )
    return controller
}
