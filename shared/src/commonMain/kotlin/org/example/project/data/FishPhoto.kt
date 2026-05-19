package org.example.project.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.example.project.storage.AppStorage

class FishPhoto(val fishName: String) {
    var uri by mutableStateOf<String?>(loadUri())
        private set

    fun set(newUri: String) {
        uri = newUri
        AppStorage.putString(key(), newUri)
    }

    fun clear() {
        uri = null
        AppStorage.putString(key(), "")
    }

    private fun loadUri(): String? {
        val raw = AppStorage.getString(key(), "")
        return raw.takeIf { it.isNotEmpty() }
    }

    private fun key(): String = "fish_photo_" + fishName.lowercase().replace(' ', '_').replace('-', '_')
}

@Composable
fun rememberFishPhoto(fishName: String): FishPhoto =
    remember(fishName) { FishPhoto(fishName) }
