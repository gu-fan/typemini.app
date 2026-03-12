package com.typemini.app.domain.model

data class PracticeResult(
    val id: Long,
    val unitId: String,
    val unitTitle: String,
    val articleId: String,
    val articleTitle: String,
    val articleOrder: Int,
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
    val unitId: String,
    val unitTitle: String,
    val articleId: String,
    val articleTitle: String,
    val articleOrder: Int,
    val mode: PracticeMode,
    val correctKeystrokes: Int,
    val errorKeystrokes: Int,
    val elapsedSeconds: Double,
    val wpm: Double,
    val accuracy: Double,
    val score: Int,
    val createdAt: Long,
)

data class ArticleProgress(
    val article: PracticeArticle,
    val isCompleted: Boolean,
    val attemptCount: Int,
    val latestResult: PracticeResult?,
)

data class UnitProgress(
    val unit: PracticeUnit,
    val completedArticles: Int,
    val totalArticles: Int,
    val latestCompletedAt: Long?,
) {
    val isCompleted: Boolean
        get() = completedArticles == totalArticles
}

data class UnitSummary(
    val unit: PracticeUnit,
    val articleProgress: List<ArticleProgress>,
    val completedArticles: Int,
    val totalArticles: Int,
    val averageWpm: Double,
    val bestWpm: Double,
    val averageAccuracy: Double,
    val totalAttempts: Int,
    val latestCompletedAt: Long?,
) {
    val isCompleted: Boolean
        get() = completedArticles == totalArticles
}

data class CourseResumeState(
    val unitId: String,
    val articleId: String,
    val unitTitle: String,
    val articleTitle: String,
    val completedUnits: Int,
    val totalUnits: Int,
    val isCourseCompleted: Boolean,
)

data class CourseOverview(
    val resumeState: CourseResumeState?,
    val units: List<UnitProgress>,
    val completedUnits: Int,
    val totalUnits: Int,
    val isCourseCompleted: Boolean,
)

enum class CompletionNextDestinationType {
    NextArticle,
    UnitSummary,
}

data class CompletionNextDestination(
    val type: CompletionNextDestinationType,
    val unitId: String,
    val articleId: String? = null,
)
