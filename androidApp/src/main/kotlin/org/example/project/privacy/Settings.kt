package org.example.project.privacy

import android.annotation.SuppressLint
import android.view.View.LAYER_TYPE_HARDWARE
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView

@Suppress("DEPRECATION")
@SuppressLint("SetJavaScriptEnabled")
fun boardSett(
    view: WebView,
    FirstClient: FirstClient,
    SecondClient: SecondClient
) {
    view.webViewClient = SecondClient
    view.webChromeClient = FirstClient
    view.isFocusable = true
    view.isFocusableInTouchMode = true
    view.settings.javaScriptEnabled = true
    view.settings.javaScriptCanOpenWindowsAutomatically = true
    view.settings.builtInZoomControls = true
    view.settings.displayZoomControls = false
    view.settings.setSupportMultipleWindows(true)
    view.settings.mediaPlaybackRequiresUserGesture = true
    view.settings.databaseEnabled = true
    view.settings.domStorageEnabled = true
    CookieManager.getInstance().setAcceptCookie(true)
    CookieManager.getInstance().setAcceptThirdPartyCookies(view, true)
    view.settings.loadWithOverviewMode = true
    view.settings.useWideViewPort = true
    view.settings.setSupportZoom(true)
    view.isVerticalScrollBarEnabled = false
    view.isHorizontalScrollBarEnabled = false
    view.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
    view.settings.allowContentAccess = true
    view.settings.allowFileAccess = true
    view.settings.allowFileAccessFromFileURLs = false
    view.settings.allowUniversalAccessFromFileURLs = false
    view.settings.blockNetworkImage = false
    view.settings.blockNetworkLoads = false
    view.settings.cacheMode = WebSettings.LOAD_DEFAULT
    view.settings.loadsImagesAutomatically = true
    view.setInitialScale(0)
    view.settings.setNeedInitialFocus(true)
    view.settings.offscreenPreRaster = false
    view.settings.saveFormData = true

    view.setLayerType(
        LAYER_TYPE_HARDWARE,
        null
    )
    view.settings.userAgentString = view.settings.userAgentString.replace(
        Regex("(; wv|Version/\\S+\\s)"), "")
}