package org.example.project.storage

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ScoreDbHelper(context: Context) : SQLiteOpenHelper(
    context,
    ScoreDbContract.DATABASE_NAME,
    null,
    ScoreDbContract.DATABASE_VERSION
) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(ScoreDbContract.CREATE_TABLE_CACHED_SCORE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }
}