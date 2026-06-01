package org.example.project

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import org.example.project.privacy.MainClient
import org.example.project.storage.ScoreDao
import org.example.project.storage.ScoreDbHelper
import org.example.project.storage.ScoreStorage
import org.example.project.storage.initAppStorage

class MainActivity : ComponentActivity() {

    private val scoreStorage by lazy {
        ScoreStorage(
            scoreDao = ScoreDao(
                dbHelper = ScoreDbHelper(this)
            )
        )
    }
    private lateinit var mainClient: MainClient

    fun hideSystemBars() {
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        initAppStorage(applicationContext)
        mainClient = MainClient(this, scoreStorage)
        mainClient.updateIntent(intent)
        hideSystemBars()
        setContent {

            App(mainClient, scoreStorage)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        setIntent(intent)

        if (::mainClient.isInitialized) {
            mainClient.updateIntent(intent)
        }
    }

    override fun onDestroy() {
        if (::mainClient.isInitialized) {
            mainClient.destroy()
        }
        super.onDestroy()
    }
}