package org.example.project.ev

import android.content.Context
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import org.example.project.storage.ScoreStorage

data class LoadingFlowContext(
    val context: Context,
    val storage: ScoreStorage,
    val data: PersistentMap<String, String> = persistentMapOf()
) {
    fun put(key: String, value: String): LoadingFlowContext {
//        log("FlowContext: put $key = $value")
        return copy(data = data.put(key, value))
    }

    fun get(key: String): String? = data[key]
}