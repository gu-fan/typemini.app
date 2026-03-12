package com.typemini.app.feature.practice

import com.typemini.app.domain.model.CompletionNextDestination
import com.typemini.app.domain.model.PracticeMode
import com.typemini.app.domain.model.PracticeArticle
import com.typemini.app.domain.model.PracticeUnit
import com.typemini.app.domain.session.TypingSession
import kotlin.math.min

data class PracticeUiState(
    val units: List<PracticeUnit> = emptyList(),
    val activeUnitId: String = "",
    val activeArticleId: String = "",
    val mode: PracticeMode = PracticeMode.Space,
    val session: TypingSession = TypingSession(
        activeCharIndex = 0,
        activeTokenIndex = 0,
        correctKeystrokes = 0,
        errorKeystrokes = 0,
        hasError = false,
        isFinished = false,
        tokens = emptyList(),
        waitingForSpace = false,
    ),
    val startedAtMillis: Long? = null,
    val finishedAtMillis: Long? = null,
    val elapsedMillis: Long = 0,
    val isSaving: Boolean = false,
    val completedResultId: Long? = null,
    val resultActionResultId: Long? = null,
    val nextDestination: CompletionNextDestination? = null,
) {
    val activeUnit: PracticeUnit?
        get() = units.firstOrNull { it.id == activeUnitId } ?: units.firstOrNull()

    val activeArticle: PracticeArticle?
        get() = activeUnit?.articles?.firstOrNull { it.id == activeArticleId } ?: activeUnit?.articles?.firstOrNull()

    val progressCount: Int
        get() = min(
            session.activeTokenIndex + if (session.isFinished && session.tokens.isNotEmpty()) 1 else 0,
            session.tokens.size,
        )
}
