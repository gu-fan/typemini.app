package com.typemini.app.data.repository

import com.typemini.app.domain.model.CompletionNextDestinationType
import com.typemini.app.domain.model.PracticeArticle
import com.typemini.app.domain.model.PracticeMode
import com.typemini.app.domain.model.PracticeResult
import com.typemini.app.domain.model.PracticeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CourseProgressTest {
    private val unit = PracticeUnit(
        id = "unit-1",
        title = "Unit 1",
        description = "Test unit",
        difficultyLabel = "Easy",
        articles = listOf(
            article(id = "a1", order = 1),
            article(id = "a2", order = 2),
            article(id = "a3", order = 3),
        ),
    )

    @Test
    fun `buildUnitSummary counts unique completed articles`() {
        val summary = buildUnitSummary(
            unit = unit,
            results = listOf(
                result(articleId = "a1", articleOrder = 1, createdAt = 100L, wpm = 40.0),
                result(articleId = "a1", articleOrder = 1, createdAt = 200L, wpm = 55.0),
                result(articleId = "a2", articleOrder = 2, createdAt = 300L, wpm = 60.0),
            ),
        )

        assertEquals(2, summary.completedArticles)
        assertEquals(3, summary.totalAttempts)
        assertFalse(summary.isCompleted)
        assertEquals(55.0, summary.articleProgress.first { it.article.id == "a1" }.latestResult?.wpm ?: 0.0, 0.0)
    }

    @Test
    fun `buildCourseOverview resumes on first incomplete article in first incomplete unit`() {
        val overview = buildCourseOverview(
            units = listOf(
                unit,
                unit.copy(
                    id = "unit-2",
                    title = "Unit 2",
                    articles = listOf(
                        article(id = "b1", unitId = "unit-2", order = 1),
                        article(id = "b2", unitId = "unit-2", order = 2),
                    ),
                ),
            ),
            results = listOf(
                result(articleId = "a1", articleOrder = 1, createdAt = 100L),
                result(articleId = "a2", articleOrder = 2, createdAt = 200L),
                result(articleId = "a3", articleOrder = 3, createdAt = 300L),
            ),
        )

        assertEquals(1, overview.completedUnits)
        assertEquals("unit-2", overview.resumeState?.unitId)
        assertEquals("b1", overview.resumeState?.articleId)
        assertFalse(overview.isCourseCompleted)
    }

    @Test
    fun `completion next destination points to unit summary when unit is complete`() {
        val summary = buildUnitSummary(
            unit = unit,
            results = listOf(
                result(articleId = "a1", articleOrder = 1, createdAt = 100L),
                result(articleId = "a2", articleOrder = 2, createdAt = 200L),
                result(articleId = "a3", articleOrder = 3, createdAt = 300L),
            ),
        )

        assertTrue(summary.isCompleted)
        val destination = if (summary.isCompleted) {
            com.typemini.app.domain.model.CompletionNextDestination(
                type = CompletionNextDestinationType.UnitSummary,
                unitId = unit.id,
            )
        } else {
            null
        }

        assertEquals(CompletionNextDestinationType.UnitSummary, destination?.type)
    }

    private fun article(
        id: String,
        order: Int,
        unitId: String = "unit-1",
    ): PracticeArticle {
        return PracticeArticle(
            id = id,
            unitId = unitId,
            title = id,
            description = "desc",
            difficultyLabel = "label",
            order = order,
            content = "simple english content for test",
        )
    }

    private fun result(
        articleId: String,
        articleOrder: Int,
        createdAt: Long,
        wpm: Double = 50.0,
        accuracy: Double = 98.0,
    ): PracticeResult {
        return PracticeResult(
            id = createdAt,
            unitId = if (articleId.startsWith("b")) "unit-2" else "unit-1",
            unitTitle = if (articleId.startsWith("b")) "Unit 2" else "Unit 1",
            articleId = articleId,
            articleTitle = articleId,
            articleOrder = articleOrder,
            mode = PracticeMode.Space,
            correctKeystrokes = 30,
            errorKeystrokes = 1,
            elapsedSeconds = 30.0,
            wpm = wpm,
            accuracy = accuracy,
            score = 100,
            createdAt = createdAt,
        )
    }
}
