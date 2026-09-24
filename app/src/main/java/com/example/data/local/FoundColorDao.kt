package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FoundColorDao {

    @Query("SELECT * FROM found_colors ORDER BY timestamp DESC")
    fun getAllFound(): Flow<List<FoundColorEntity>>

    @Query("SELECT COUNT(DISTINCT colorId) FROM found_colors")
    fun getDistinctColorCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM found_colors")
    fun getTotalFoundCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFound(color: FoundColorEntity): Long

    @Query("DELETE FROM found_colors")
    suspend fun clearAll()
}
