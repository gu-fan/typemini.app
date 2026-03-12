package com.typemini.app.domain.model

data class PracticeUnit(
    val id: String,
    val title: String,
    val description: String,
    val difficultyLabel: String,
    val articles: List<PracticeArticle>,
)

data class PracticeArticle(
    val id: String,
    val unitId: String,
    val title: String,
    val description: String,
    val difficultyLabel: String,
    val order: Int,
    val content: String,
) {
    val tokenCount: Int
        get() = content.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
}
