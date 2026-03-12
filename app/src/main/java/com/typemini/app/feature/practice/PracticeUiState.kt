package com.typemini.app.feature.practice

import com.typemini.app.domain.model.PracticeMode
import com.typemini.app.domain.model.PracticeText
import com.typemini.app.domain.session.TypingSession
import kotlin.math.min

data class PracticeUiState(
    val texts: List<PracticeText> = emptyList(),
    val selectedTextId: String = "",
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
) {
    val activeText: PracticeText?
        get() = texts.firstOrNull { it.id == selectedTextId } ?: texts.firstOrNull()

    val progressCount: Int
        get() = min(
            session.activeTokenIndex + if (session.isFinished && session.tokens.isNotEmpty()) 1 else 0,
            session.tokens.size,
        )
}
