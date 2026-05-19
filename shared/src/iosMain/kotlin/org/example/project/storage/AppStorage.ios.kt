package org.example.project.storage

import platform.Foundation.NSDate
import platform.Foundation.NSUserDefaults
import platform.Foundation.timeIntervalSince1970

private val defaults: NSUserDefaults
    get() = NSUserDefaults.standardUserDefaults

internal actual fun storageGetBoolean(key: String, default: Boolean): Boolean =
    if (defaults.objectForKey(key) != null) defaults.boolForKey(key) else default

internal actual fun storagePutBoolean(key: String, value: Boolean) {
    defaults.setBool(value, forKey = key)
}

internal actual fun storageGetLong(key: String, default: Long): Long =
    if (defaults.objectForKey(key) != null) defaults.integerForKey(key) else default

internal actual fun storagePutLong(key: String, value: Long) {
    defaults.setInteger(value, forKey = key)
}

internal actual fun storageGetString(key: String, default: String): String =
    defaults.stringForKey(key) ?: default

internal actual fun storagePutString(key: String, value: String) {
    defaults.setObject(value, forKey = key)
}

actual fun currentTimeMs(): Long =
    (NSDate().timeIntervalSince1970 * 1000.0).toLong()
