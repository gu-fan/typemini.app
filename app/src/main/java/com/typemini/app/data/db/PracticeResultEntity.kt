package com.typemini.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "practice_results")
data class PracticeResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val practiceTextId: String,
    val practiceTextTitle: String,
    val mode: String,
    val correctKeystrokes: Int,
    val errorKeystrokes: Int,
    val elapsedSeconds: Double,
    val wpm: Double,
    val accuracy: Double,
    val score: Int,
    val createdAt: Long,
)
