package com.typemini.app.domain.model

data class PracticeText(
    val id: String,
    val title: String,
    val difficultyLabel: String,
    val content: String,
) {
    val tokenCount: Int
        get() = content.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
}
