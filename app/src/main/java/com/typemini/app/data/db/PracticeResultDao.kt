package com.typemini.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PracticeResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(result: PracticeResultEntity): Long

    @Query("SELECT * FROM practice_results ORDER BY createdAt DESC")
    fun observeResultsNewest(): Flow<List<PracticeResultEntity>>

    @Query("SELECT * FROM practice_results ORDER BY wpm DESC, createdAt DESC")
    fun observeResultsFastest(): Flow<List<PracticeResultEntity>>

    @Query("SELECT * FROM practice_results WHERE id = :id LIMIT 1")
    fun observeResult(id: Long): Flow<PracticeResultEntity?>

    @Query("SELECT * FROM practice_results")
    suspend fun getAllResults(): List<PracticeResultEntity>
}
