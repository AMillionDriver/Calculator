package com.axoloth.calculator.by.sky.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface HistoryDao {
    @Query("SELECT * FROM calculation_history ORDER BY timestamp DESC")
    suspend fun getAllHistory(): List<HistoryEntity>

    @Insert
    suspend fun insertHistory(history: HistoryEntity)

    @Query("DELETE FROM calculation_history")
    suspend fun clearHistory()

    @Query("DELETE FROM calculation_history WHERE id = :id")
    suspend fun deleteById(id: Int)
}
