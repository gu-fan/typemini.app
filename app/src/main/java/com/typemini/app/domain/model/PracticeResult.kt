package com.typemini.app.domain.model

data class PracticeResult(
    val id: Long,
    val practiceTextId: String,
    val practiceTextTitle: String,
    val mode: PracticeMode,
    val correctKeystrokes: Int,
    val errorKeystrokes: Int,
    val elapsedSeconds: Double,
    val wpm: Double,
    val accuracy: Double,
    val score: Int,
    val createdAt: Long,
)

data class PracticeResultDraft(
    val practiceTextId: String,
    val practiceTextTitle: String,
    val mode: PracticeMode,
    val correctKeystrokes: Int,
    val errorKeystrokes: Int,
    val elapsedSeconds: Double,
    val wpm: Double,
    val accuracy: Double,
    val score: Int,
    val createdAt: Long,
)
