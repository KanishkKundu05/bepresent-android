package com.bepresent.android.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        AppIntention::class,
        PresentSession::class,
        PresentSessionAction::class
    ],
    version = 1,
    exportSchema = true
)
abstract class BePresentDatabase : RoomDatabase() {
    abstract fun appIntentionDao(): AppIntentionDao
    abstract fun presentSessionDao(): PresentSessionDao
}
