package com.typemini.app.data.repository

import com.typemini.app.data.db.PracticeResultDao
import com.typemini.app.data.db.PracticeResultEntity
import com.typemini.app.data.seed.defaultPracticeUnits
import com.typemini.app.domain.model.ArticleProgress
import com.typemini.app.domain.model.CompletionNextDestination
import com.typemini.app.domain.model.CompletionNextDestinationType
import com.typemini.app.domain.model.CourseOverview
import com.typemini.app.domain.model.CourseResumeState
import com.typemini.app.domain.model.HistorySortMode
import com.typemini.app.domain.model.HistorySummary
import com.typemini.app.domain.model.PracticeMode
import com.typemini.app.domain.model.PracticeArticle
import com.typemini.app.domain.model.PracticeResult
import com.typemini.app.domain.model.PracticeResultDraft
import com.typemini.app.domain.model.PracticeUnit
import com.typemini.app.domain.model.UnitProgress
import com.typemini.app.domain.model.UnitSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

class DefaultPracticeRepository(
    private val resultDao: PracticeResultDao,
) : PracticeRepository {
    private val units = defaultPracticeUnits

    override fun getPracticeUnits(): List<PracticeUnit> = units

    override fun getUnit(unitId: String): PracticeUnit? = units.firstOrNull { it.id == unitId }

    override fun getArticle(unitId: String, articleId: String): PracticeArticle? {
        return getUnit(unitId)?.articles?.firstOrNull { it.id == articleId }
    }

    override fun observeCourseOverview(): Flow<CourseOverview> {
        return resultDao.observeResultsNewest().map { entities ->
            buildCourseOverview(units = units, results = entities.map { it.toDomain() })
        }
    }

    override fun observeUnitSummary(unitId: String): Flow<UnitSummary?> {
        return resultDao.observeResultsNewest().map { entities ->
            val unit = getUnit(unitId) ?: return@map null
            buildUnitSummary(unit = unit, results = entities.map { it.toDomain() })
        }
    }

    override suspend fun getUnitSummary(unitId: String): UnitSummary? {
        val unit = getUnit(unitId) ?: return null
        return buildUnitSummary(unit = unit, results = resultDao.getAllResults().map { it.toDomain() })
    }

    override suspend fun getCompletionNextDestination(
        unitId: String,
        articleId: String,
    ): CompletionNextDestination? {
        val summary = getUnitSummary(unitId) ?: return null
        if (summary.isCompleted) {
            return CompletionNextDestination(
                type = CompletionNextDestinationType.UnitSummary,
                unitId = unitId,
            )
        }

        val currentArticle = summary.articleProgress.firstOrNull { it.article.id == articleId }?.article
        val nextIncompleteAfterCurrent = summary.articleProgress
            .asSequence()
            .filter { !it.isCompleted }
            .map { it.article }
            .firstOrNull { article ->
                currentArticle != null && article.order > currentArticle.order
            }
        val firstIncomplete = summary.articleProgress.firstOrNull { !it.isCompleted }?.article
        val nextArticle = nextIncompleteAfterCurrent ?: firstIncomplete ?: return null

        return CompletionNextDestination(
            type = CompletionNextDestinationType.NextArticle,
            unitId = unitId,
            articleId = nextArticle.id,
        )
    }

    override suspend fun savePracticeResult(result: PracticeResultDraft): Long {
        return resultDao.insert(result.toEntity())
    }

    override fun observeHistory(sortMode: HistorySortMode): Flow<List<PracticeResult>> {
        val source = when (sortMode) {
            HistorySortMode.Newest -> resultDao.observeResultsNewest()
            HistorySortMode.Fastest -> resultDao.observeResultsFastest()
        }

        return source.map { results -> results.map { it.toDomain() } }
    }

    override fun observePracticeResult(id: Long): Flow<PracticeResult?> {
        return resultDao.observeResult(id).map { it?.toDomain() }
    }

    override fun observeHistorySummary(): Flow<HistorySummary> {
        val sevenDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)

        return resultDao.observeResultsNewest().map { entities ->
            val recent = entities
                .map { it.toDomain() }
                .filter { it.createdAt >= sevenDaysAgo }

            if (recent.isEmpty()) {
                HistorySummary(
                    averageWpm = 0.0,
                    bestWpm = 0.0,
                    averageAccuracy = 0.0,
                    sessionCount = 0,
                )
            } else {
                HistorySummary(
                    averageWpm = recent.map { it.wpm }.average(),
                    bestWpm = recent.maxOf { it.wpm },
                    averageAccuracy = recent.map { it.accuracy }.average(),
                    sessionCount = recent.size,
                )
            }
        }
    }
}

private fun PracticeResultDraft.toEntity(): PracticeResultEntity {
    return PracticeResultEntity(
        unitId = unitId,
        unitTitle = unitTitle,
        articleId = articleId,
        articleTitle = articleTitle,
        articleOrder = articleOrder,
        mode = mode.name,
        correctKeystrokes = correctKeystrokes,
        errorKeystrokes = errorKeystrokes,
        elapsedSeconds = elapsedSeconds,
        wpm = wpm,
        accuracy = accuracy,
        score = score,
        createdAt = createdAt,
    )
}

private fun PracticeResultEntity.toDomain(): PracticeResult {
    return PracticeResult(
        id = id,
        unitId = unitId,
        unitTitle = unitTitle,
        articleId = articleId,
        articleTitle = articleTitle,
        articleOrder = articleOrder,
        mode = PracticeMode.valueOf(mode),
        correctKeystrokes = correctKeystrokes,
        errorKeystrokes = errorKeystrokes,
        elapsedSeconds = elapsedSeconds,
        wpm = wpm,
        accuracy = accuracy,
        score = score,
        createdAt = createdAt,
    )
}

internal fun buildCourseOverview(
    units: List<PracticeUnit>,
    results: List<PracticeResult>,
): CourseOverview {
    val unitProgress = units.map { unit ->
        val latestByArticle = latestResultByArticle(results.filter { it.unitId == unit.id })
        UnitProgress(
            unit = unit,
            completedArticles = latestByArticle.size,
            totalArticles = unit.articles.size,
            latestCompletedAt = latestByArticle.values.maxOfOrNull { it.createdAt },
        )
    }
    val completedUnits = unitProgress.count { it.isCompleted }
    val resumeUnitProgress = unitProgress.firstOrNull { !it.isCompleted } ?: unitProgress.firstOrNull()
    val resumeSummary = resumeUnitProgress?.let { buildUnitSummary(it.unit, results) }
    val resumeArticle = resumeSummary
        ?.articleProgress
        ?.firstOrNull { !it.isCompleted }
        ?.article
        ?: resumeSummary?.articleProgress?.firstOrNull()?.article
    val resumeState = if (resumeUnitProgress != null && resumeArticle != null) {
        CourseResumeState(
            unitId = resumeUnitProgress.unit.id,
            articleId = resumeArticle.id,
            unitTitle = resumeUnitProgress.unit.title,
            articleTitle = resumeArticle.title,
            completedUnits = completedUnits,
            totalUnits = unitProgress.size,
            isCourseCompleted = completedUnits == unitProgress.size,
        )
    } else {
        null
    }

    return CourseOverview(
        resumeState = resumeState,
        units = unitProgress,
        completedUnits = completedUnits,
        totalUnits = unitProgress.size,
        isCourseCompleted = completedUnits == unitProgress.size && unitProgress.isNotEmpty(),
    )
}

internal fun buildUnitSummary(
    unit: PracticeUnit,
    results: List<PracticeResult>,
): UnitSummary {
    val unitResults = results.filter { it.unitId == unit.id }
    val latestByArticle = latestResultByArticle(unitResults)
    val articleProgress = unit.articles.sortedBy { it.order }.map { article ->
        val articleResults = unitResults.filter { it.articleId == article.id }
        ArticleProgress(
            article = article,
            isCompleted = latestByArticle.containsKey(article.id),
            attemptCount = articleResults.size,
            latestResult = latestByArticle[article.id],
        )
    }
    val latestResults = articleProgress.mapNotNull { it.latestResult }

    return UnitSummary(
        unit = unit,
        articleProgress = articleProgress,
        completedArticles = latestResults.size,
        totalArticles = unit.articles.size,
        averageWpm = latestResults.map { it.wpm }.average().orZero(),
        bestWpm = latestResults.maxOfOrNull { it.wpm } ?: 0.0,
        averageAccuracy = latestResults.map { it.accuracy }.average().orZero(),
        totalAttempts = unitResults.size,
        latestCompletedAt = latestResults.maxOfOrNull { it.createdAt },
    )
}

private fun latestResultByArticle(results: List<PracticeResult>): Map<String, PracticeResult> {
    return results
        .sortedByDescending { it.createdAt }
        .groupBy { it.articleId }
        .mapValues { (_, articleResults) -> articleResults.maxBy { it.createdAt } }
}

private fun Double.orZero(): Double = if (isNaN()) 0.0 else this
