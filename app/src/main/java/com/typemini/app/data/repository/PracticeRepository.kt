package com.typemini.app.data.repository

import com.typemini.app.domain.model.HistorySortMode
import com.typemini.app.domain.model.HistorySummary
import com.typemini.app.domain.model.PracticeResult
import com.typemini.app.domain.model.PracticeResultDraft
import com.typemini.app.domain.model.PracticeText
import kotlinx.coroutines.flow.Flow

interface PracticeRepository {
    fun getPracticeTexts(): List<PracticeText>

    suspend fun savePracticeResult(result: PracticeResultDraft): Long

    fun observeHistory(sortMode: HistorySortMode): Flow<List<PracticeResult>>

    fun observePracticeResult(id: Long): Flow<PracticeResult?>

    fun observeHistorySummary(): Flow<HistorySummary>
}
