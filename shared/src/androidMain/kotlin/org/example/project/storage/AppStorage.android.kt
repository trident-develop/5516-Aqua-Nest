package org.example.project.storage

import android.content.Context
import android.content.SharedPreferences

private var prefs: SharedPreferences? = null

fun initAppStorage(context: Context) {
    if (prefs == null) {
        prefs = context.applicationContext.getSharedPreferences("aqua_nest", Context.MODE_PRIVATE)
    }
}

internal actual fun storageGetBoolean(key: String, default: Boolean): Boolean =
    prefs?.getBoolean(key, default) ?: default

internal actual fun storagePutBoolean(key: String, value: Boolean) {
    prefs?.edit()?.putBoolean(key, value)?.apply()
}

internal actual fun storageGetLong(key: String, default: Long): Long =
    prefs?.getLong(key, default) ?: default

internal actual fun storagePutLong(key: String, value: Long) {
    prefs?.edit()?.putLong(key, value)?.apply()
}

internal actual fun storageGetString(key: String, default: String): String =
    prefs?.getString(key, default) ?: default

internal actual fun storagePutString(key: String, value: String) {
    prefs?.edit()?.putString(key, value)?.apply()
}

actual fun currentTimeMs(): Long = System.currentTimeMillis()
