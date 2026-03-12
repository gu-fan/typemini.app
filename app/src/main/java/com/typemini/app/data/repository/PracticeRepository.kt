package com.typemini.app.data.repository

import com.typemini.app.domain.model.HistorySortMode
import com.typemini.app.domain.model.HistorySummary
import com.typemini.app.domain.model.CompletionNextDestination
import com.typemini.app.domain.model.CourseOverview
import com.typemini.app.domain.model.PracticeArticle
import com.typemini.app.domain.model.PracticeResult
import com.typemini.app.domain.model.PracticeResultDraft
import com.typemini.app.domain.model.PracticeUnit
import com.typemini.app.domain.model.UnitSummary
import kotlinx.coroutines.flow.Flow

interface PracticeRepository {
    fun getPracticeUnits(): List<PracticeUnit>

    fun getUnit(unitId: String): PracticeUnit?

    fun getArticle(unitId: String, articleId: String): PracticeArticle?

    fun observeCourseOverview(): Flow<CourseOverview>

    fun observeUnitSummary(unitId: String): Flow<UnitSummary?>

    suspend fun getUnitSummary(unitId: String): UnitSummary?

    suspend fun getCompletionNextDestination(
        unitId: String,
        articleId: String,
    ): CompletionNextDestination?

    suspend fun savePracticeResult(result: PracticeResultDraft): Long

    fun observeHistory(sortMode: HistorySortMode): Flow<List<PracticeResult>>

    fun observePracticeResult(id: Long): Flow<PracticeResult?>

    fun observeHistorySummary(): Flow<HistorySummary>
}
