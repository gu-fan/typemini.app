package com.typemini.app.domain.model

data class HistorySummary(
    val averageWpm: Double,
    val bestWpm: Double,
    val averageAccuracy: Double,
    val sessionCount: Int,
)
