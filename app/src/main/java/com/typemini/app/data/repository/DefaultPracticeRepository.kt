package com.typemini.app.data.repository

import com.typemini.app.data.db.PracticeResultDao
import com.typemini.app.data.db.PracticeResultEntity
import com.typemini.app.data.seed.defaultPracticeTexts
import com.typemini.app.domain.model.HistorySortMode
import com.typemini.app.domain.model.HistorySummary
import com.typemini.app.domain.model.PracticeMode
import com.typemini.app.domain.model.PracticeResult
import com.typemini.app.domain.model.PracticeResultDraft
import com.typemini.app.domain.model.PracticeText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

class DefaultPracticeRepository(
    private val resultDao: PracticeResultDao,
) : PracticeRepository {
    override fun getPracticeTexts(): List<PracticeText> = defaultPracticeTexts

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
        practiceTextId = practiceTextId,
        practiceTextTitle = practiceTextTitle,
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
        practiceTextId = practiceTextId,
        practiceTextTitle = practiceTextTitle,
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
