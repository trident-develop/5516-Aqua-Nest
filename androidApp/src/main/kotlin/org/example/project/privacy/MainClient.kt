package org.example.project.privacy

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.example.project.getBaseUrl
import org.example.project.postback
import org.example.project.regToken
import org.example.project.requestNotify
import org.example.project.storage.ScoreStorage

@SuppressLint("ViewConstructor")
class MainClient(
    private val activity: ComponentActivity,
    private val scoreRepo: ScoreStorage,
) : WebView(activity) {
    private val contentRoot: FrameLayout = FrameLayout(activity)
    private var validTarget = false
    private var latestIntent: Intent? = null

    fun updateIntent(intent: Intent?) {
        latestIntent = intent
    }
    val popupContainer: FrameLayout = FrameLayout(activity).apply {
        isVisible = false
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
    }

    private var onOpenOtherScreen: (() -> Unit)? = null

    fun setOnOpenOtherScreenCallback(callback: () -> Unit) {
        onOpenOtherScreen = callback
    }


    val fullscreenContainer: FrameLayout = FrameLayout(activity).apply {
        isVisible = false
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
    }

    private var contentCallback: ValueCallback<Array<Uri>>? = null

    private val viewClient = SecondClient(
        activity = activity,
        onStarted = { _, _ ->
            contentCallback?.onReceiveValue(null)
            contentCallback = null
        },
        onFinished = { _, url ->
            CoroutineScope(Dispatchers.IO).launch {
                runCatching { CookieManager.getInstance().flush() }

                handleUrl(url)
            }
        }
    )
    private val chromeClient = FirstClient(activity, this, viewClient)


    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {

            if (popupContainer.childCount > 0) {
                val top = popupContainer.getChildAt(popupContainer.childCount - 1) as WebView

                if (top.canGoBack()) {
                    top.goBack()
                } else {
                    top.stopLoading()
                    popupContainer.removeView(top)
                    top.destroy()
                    popupContainer.isVisible = popupContainer.childCount > 0
                }
                return
            }

            if (canGoBack()) {
                goBack()
            }
        }
    }

    init {
        contentRoot.addView(
            this,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        contentRoot.addView(popupContainer)
        contentRoot.addView(fullscreenContainer)

        contentRoot.isVisible = false

        activity.onBackPressedDispatcher.addCallback(activity, backPressedCallback)

        boardSett(this, chromeClient, viewClient)
    }


    override fun destroy() {
        chromeClient.onDestroy()
        backPressedCallback.remove()
        super.destroy()
    }

    fun setViewVisibility(isVisible: Boolean) {
        activity.runOnUiThread {

            val content = activity.findViewById<ViewGroup>(android.R.id.content)

            content.removeAllViews()

            if (contentRoot.parent == null) {
                content.addView(
                    contentRoot,
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
            } else {
                content.bringChildToFront(contentRoot)
            }

            contentRoot.isVisible = isVisible

            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            this@MainClient.requestFocus()

            CoroutineScope(Dispatchers.IO).launch {
                if (!scoreRepo.isNotificationShown()) {
                    requestNotify(activity.activityResultRegistry)
                    scoreRepo.saveNotificationShown(true)
                }
            }
        }
    }

    private suspend fun handleUrl(url: String?) {
        when {
            url?.startsWith(getBaseUrl()) == true -> {
                scoreRepo.saveBrokenScore(url)
                withContext(Dispatchers.Main) {
                    onOpenOtherScreen?.invoke()
                }
            }

            !validTarget && url?.startsWith(getBaseUrl()) == false -> {
                validTarget = true
                scoreRepo.saveScore(url)
                regToken()
                postback(latestIntent)
                setViewVisibility(true)
            }
        }
    }
}