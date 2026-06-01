package org.example.project

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import com.github.kittinunf.fuel.httpGet
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.net.URLDecoder
import java.util.Locale

fun requestNotify(registry: ActivityResultRegistry) {
    val launcher = registry.register(
        "requestPermissionKey",
        ActivityResultContracts.RequestPermission()
    ) {  }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

suspend fun regToken() {

    withContext(Dispatchers.IO) {

        try {

            val fcmToken = runCatching {
                FirebaseMessaging.getInstance().token.await()
            }.getOrElse {
                "null"
            }

            val locale = Locale.getDefault().toLanguageTag()

            val url = "${getBaseUrl()}uecyfx/"

            val fullUrl = "$url?" +
                    "nvwo8=${Firebase.analytics.appInstanceId.await()}" +
                    "&jxybuyxt=${decodeUtf8(fcmToken)}"

            fullUrl
                .httpGet()
                .header("Accept-Language" to locale)
                .response()

        } catch (_: Exception) {

        }
    }
}

suspend fun postback(intent: Intent?) {

    withContext(Dispatchers.IO) {

        try {

            val trackingId = intent?.getStringExtra("trackingId")

            if (trackingId.isNullOrEmpty()) {
                return@withContext
            }

            val fcmToken = runCatching {
                FirebaseMessaging.getInstance().token.await()
            }.getOrElse {
                "null"
            }

            val url = "${getBaseUrl()}s171luey0/"

            val fullUrl = "$url?" +
                    "yts3j3d2=$trackingId" +
                    "&v9jfi=${decodeUtf8(fcmToken)}"

            fullUrl
                .httpGet()
                .response()

        } catch (_: Exception) {

        }
    }
}

private const val TAG = "MYTAG"

fun log(message: String) {
    Log.d(TAG, message)
}

fun decodeUtf8(encoded: String?): String =
    URLDecoder.decode(encoded, "UTF-8")

fun getBaseUrl(): String {
    val chars = charArrayOf(
        'h', 't', 't', 'p', 's', ':', '/', '/',
        'a', 'q', 'u', 'a', 'n', 'e', 's', 't',
        '.', 's', 'p', 'a', 'c', 'e', '/'
    )
    return chars.concatToString()
}

@SuppressLint("ServiceCast")
fun Context.isFlowersConnected(): Boolean {
    val ballConnectivityManager =
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeBallNetwork = ballConnectivityManager.activeNetwork
    val ballCapabilities = ballConnectivityManager.getNetworkCapabilities(activeBallNetwork)

    return ballCapabilities?.run {
        hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                hasTransport(NetworkCapabilities.TRANSPORT_VPN)
    } == true
}