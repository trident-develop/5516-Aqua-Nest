package org.example.project.storage

internal expect fun storageGetBoolean(key: String, default: Boolean): Boolean
internal expect fun storagePutBoolean(key: String, value: Boolean)
internal expect fun storageGetLong(key: String, default: Long): Long
internal expect fun storagePutLong(key: String, value: Long)
internal expect fun storageGetString(key: String, default: String): String
internal expect fun storagePutString(key: String, value: String)

expect fun currentTimeMs(): Long

object AppStorage {
    fun getBoolean(key: String, default: Boolean = false): Boolean = storageGetBoolean(key, default)
    fun putBoolean(key: String, value: Boolean) = storagePutBoolean(key, value)
    fun getLong(key: String, default: Long = 0L): Long = storageGetLong(key, default)
    fun putLong(key: String, value: Long) = storagePutLong(key, value)
    fun getString(key: String, default: String = ""): String = storageGetString(key, default)
    fun putString(key: String, value: String) = storagePutString(key, value)
}
