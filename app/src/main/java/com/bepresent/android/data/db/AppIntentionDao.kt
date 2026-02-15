package com.bepresent.android.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppIntentionDao {
    @Query("SELECT * FROM app_intentions")
    fun getAll(): Flow<List<AppIntention>>

    @Query("SELECT * FROM app_intentions")
    suspend fun getAllOnce(): List<AppIntention>

    @Query("SELECT * FROM app_intentions WHERE packageName = :packageName")
    suspend fun getByPackage(packageName: String): AppIntention?

    @Query("SELECT * FROM app_intentions WHERE id = :id")
    suspend fun getById(id: String): AppIntention?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(intention: AppIntention)

    @Delete
    suspend fun delete(intention: AppIntention)

    @Query("UPDATE app_intentions SET totalOpensToday = totalOpensToday + 1 WHERE id = :id")
    suspend fun incrementOpens(id: String)

    @Query("UPDATE app_intentions SET currentlyOpen = :open, openedAt = :openedAt WHERE id = :id")
    suspend fun setOpenState(id: String, open: Boolean, openedAt: Long?)

    @Query("SELECT * FROM app_intentions WHERE currentlyOpen = 0")
    suspend fun getBlockedIntentions(): List<AppIntention>

    @Query("SELECT COUNT(*) FROM app_intentions")
    suspend fun getCount(): Int
}
