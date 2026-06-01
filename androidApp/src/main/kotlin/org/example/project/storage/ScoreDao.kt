package org.example.project.storage

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import kotlin.text.insert
import kotlin.toString

class ScoreDao(
    private val dbHelper: ScoreDbHelper
) {

    fun getCachedScore(): CachedScoreEntity? {
        val db = dbHelper.readableDatabase

        db.rawQuery(
            "SELECT * FROM ${ScoreDbContract.TABLE_CACHED_SCORE} WHERE ${ScoreDbContract.COLUMN_ID} = ?",
            arrayOf(ScoreDbContract.ID_SCORE.toString())
        ).use { cursor ->
            return if (cursor.moveToFirst()) {
                CachedScoreEntity(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(ScoreDbContract.COLUMN_ID)),
                    score = cursor.getString(cursor.getColumnIndexOrThrow(ScoreDbContract.COLUMN_SCORE)),
                    createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(ScoreDbContract.COLUMN_CREATED_AT))
                )
            } else {
                null
            }
        }
    }

    fun saveCachedScoreOnce(score: String) {
        if (getCachedScore() != null) return

        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(ScoreDbContract.COLUMN_ID, ScoreDbContract.ID_SCORE)
            put(ScoreDbContract.COLUMN_SCORE, score)
            put(ScoreDbContract.COLUMN_CREATED_AT, System.currentTimeMillis())
        }

        db.insert(
            ScoreDbContract.TABLE_CACHED_SCORE,
            null,
            values
        )
    }

    fun getBrokenScore(): String? {
        val db = dbHelper.readableDatabase

        db.rawQuery(
            "SELECT ${ScoreDbContract.COLUMN_SCORE} FROM ${ScoreDbContract.TABLE_CACHED_SCORE} WHERE ${ScoreDbContract.COLUMN_ID} = ?",
            arrayOf(ScoreDbContract.ID_BROKEN_SCORE.toString())
        ).use { cursor ->
            return if (cursor.moveToFirst()) {
                cursor.getString(0)
            } else {
                null
            }
        }
    }

    fun saveBrokenScore(score: String) {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(ScoreDbContract.COLUMN_ID, ScoreDbContract.ID_BROKEN_SCORE)
            put(ScoreDbContract.COLUMN_SCORE, score)
            put(ScoreDbContract.COLUMN_CREATED_AT, System.currentTimeMillis())
        }

        db.insertWithOnConflict(
            ScoreDbContract.TABLE_CACHED_SCORE,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun isNotificationShown(): Boolean {
        val db = dbHelper.readableDatabase

        db.rawQuery(
            "SELECT ${ScoreDbContract.COLUMN_SCORE} FROM ${ScoreDbContract.TABLE_CACHED_SCORE} WHERE ${ScoreDbContract.COLUMN_ID} = ?",
            arrayOf(ScoreDbContract.ID_NOTIFICATION_SHOWN.toString())
        ).use { cursor ->
            return if (cursor.moveToFirst()) {
                cursor.getString(0).toBoolean()
            } else {
                false
            }
        }
    }

    fun saveNotificationShown(isShown: Boolean) {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(ScoreDbContract.COLUMN_ID, ScoreDbContract.ID_NOTIFICATION_SHOWN)
            put(ScoreDbContract.COLUMN_SCORE, isShown.toString())
            put(ScoreDbContract.COLUMN_CREATED_AT, System.currentTimeMillis())
        }

        db.insertWithOnConflict(
            ScoreDbContract.TABLE_CACHED_SCORE,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }
}