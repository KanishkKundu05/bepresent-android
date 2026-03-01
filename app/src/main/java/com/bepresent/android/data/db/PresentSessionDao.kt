package com.bepresent.android.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PresentSessionDao {
    @Query("SELECT * FROM present_sessions WHERE state IN ('active', 'goalReached') LIMIT 1")
    suspend fun getActiveSession(): PresentSession?

    @Query("SELECT * FROM present_sessions WHERE state IN ('active', 'goalReached') LIMIT 1")
    fun observeActiveSession(): Flow<PresentSession?>

    @Query("SELECT * FROM present_sessions ORDER BY createdAt DESC")
    fun getAllSessions(): Flow<List<PresentSession>>

    @Query("SELECT * FROM present_sessions WHERE id = :id")
    suspend fun getById(id: String): PresentSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(session: PresentSession)

    @Insert
    suspend fun insertAction(action: PresentSessionAction)

    @Query(
        """
        SELECT * FROM present_sessions
        WHERE state IN ('completed', 'gaveUp')
        AND startedAt >= :dayStartMs AND startedAt < :dayEndMs
        """
    )
    suspend fun getCompletedSessionsForDate(dayStartMs: Long, dayEndMs: Long): List<PresentSession>

    @Query(
        """
        SELECT * FROM present_sessions
        WHERE state = 'completed'
        AND startedAt >= :dayStartMs AND startedAt < :dayEndMs
        """
    )
    suspend fun getCompletedSessionsForDateRange(dayStartMs: Long, dayEndMs: Long): List<PresentSession>
}
