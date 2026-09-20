package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TriggerLogDao {
    @Query("SELECT * FROM trigger_logs ORDER BY timestamp DESC LIMIT 200")
    fun getRecentLogs(): Flow<List<TriggerLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: TriggerLogEntity): Long

    @Query("DELETE FROM trigger_logs")
    suspend fun clearLogs()

    @Query("DELETE FROM trigger_logs WHERE id NOT IN (SELECT id FROM trigger_logs ORDER BY timestamp DESC LIMIT 200)")
    suspend fun trimOldLogs()
}
