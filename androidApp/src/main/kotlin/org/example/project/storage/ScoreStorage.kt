package org.example.project.storage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScoreStorage(
    private val scoreDao: ScoreDao
) {

    suspend fun getSavedScore(): String? = withContext(Dispatchers.IO) {
        val result = scoreDao.getCachedScore()?.score
//        log("Storage: result = $result")
        result
    }

    suspend fun saveScore(score: String) = withContext(Dispatchers.IO) {
//        log("Storage: saveScoreOnce = $score")
        scoreDao.saveCachedScoreOnce(score)
    }

    suspend fun getBrokenScore(): String? = withContext(Dispatchers.IO) {
        val result = scoreDao.getBrokenScore()
//        log("Storage: brokenScore = $result")
        result
    }

    suspend fun saveBrokenScore(score: String) = withContext(Dispatchers.IO) {
//        log("Storage: saveBrokenScore = $score")
        scoreDao.saveBrokenScore(score)
    }

    suspend fun isNotificationShown(): Boolean = withContext(Dispatchers.IO) {
        scoreDao.isNotificationShown()
    }

    suspend fun saveNotificationShown(isShown: Boolean) = withContext(Dispatchers.IO) {
        scoreDao.saveNotificationShown(isShown)
    }
}