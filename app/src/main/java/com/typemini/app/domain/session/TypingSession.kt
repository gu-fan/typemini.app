package com.typemini.app.domain.session

import com.typemini.app.domain.model.PracticeMode
import kotlin.math.max
import kotlin.math.roundToInt

data class TypingSession(
    val activeCharIndex: Int,
    val activeTokenIndex: Int,
    val correctKeystrokes: Int,
    val errorKeystrokes: Int,
    val hasError: Boolean,
    val isFinished: Boolean,
    val tokens: List<String>,
    val waitingForSpace: Boolean,
    val history: List<TypingSessionSnapshot> = emptyList(),
)

data class TypingSessionSnapshot(
    val activeCharIndex: Int,
    val activeTokenIndex: Int,
    val correctKeystrokes: Int,
    val errorKeystrokes: Int,
    val hasError: Boolean,
    val isFinished: Boolean,
    val waitingForSpace: Boolean,
)

data class TypingMetrics(
    val accuracy: Double,
    val correctCharacters: Int,
    val cpm: Double,
    val elapsedSeconds: Double,
    val mistakes: Int,
    val score: Int,
    val totalCharacters: Int,
    val wpm: Double,
)

fun splitPracticeText(content: String): List<String> {
    return content
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
}

fun createTypingSession(content: String): TypingSession {
    return TypingSession(
        activeCharIndex = 0,
        activeTokenIndex = 0,
        correctKeystrokes = 0,
        errorKeystrokes = 0,
        hasError = false,
        isFinished = false,
        tokens = splitPracticeText(content),
        waitingForSpace = false,
    )
}

private fun normalizeCharacter(input: Char): Char = input.lowercaseChar()

private fun TypingSession.snapshot(): TypingSessionSnapshot {
    return TypingSessionSnapshot(
        activeCharIndex = activeCharIndex,
        activeTokenIndex = activeTokenIndex,
        correctKeystrokes = correctKeystrokes,
        errorKeystrokes = errorKeystrokes,
        hasError = hasError,
        isFinished = isFinished,
        waitingForSpace = waitingForSpace,
    )
}

private fun recordHistory(
    previous: TypingSession,
    next: TypingSession,
): TypingSession {
    if (
        previous.activeCharIndex == next.activeCharIndex &&
        previous.activeTokenIndex == next.activeTokenIndex &&
        previous.correctKeystrokes == next.correctKeystrokes &&
        previous.errorKeystrokes == next.errorKeystrokes &&
        previous.hasError == next.hasError &&
        previous.isFinished == next.isFinished &&
        previous.waitingForSpace == next.waitingForSpace
    ) {
        return previous
    }

    return next.copy(history = previous.history + previous.snapshot())
}

private fun finishSession(session: TypingSession): TypingSession {
    return session.copy(
        hasError = false,
        isFinished = true,
        waitingForSpace = false,
    )
}

private fun advanceToken(session: TypingSession): TypingSession {
    if (session.activeTokenIndex >= session.tokens.lastIndex) {
        return finishSession(session)
    }

    return session.copy(
        activeCharIndex = 0,
        activeTokenIndex = session.activeTokenIndex + 1,
        hasError = false,
        waitingForSpace = false,
    )
}

fun applyCharacterToSession(
    session: TypingSession,
    rawInput: Char,
    mode: PracticeMode,
): TypingSession {
    if (session.isFinished) {
        return session
    }

    val input = normalizeCharacter(rawInput)

    if (session.waitingForSpace) {
        return if (mode == PracticeMode.Space && input == ' ') {
            recordHistory(session, advanceToken(session))
        } else {
            session
        }
    }

    val token = session.tokens.getOrNull(session.activeTokenIndex) ?: return finishSession(session)
    val expected = token.getOrNull(session.activeCharIndex)?.lowercaseChar() ?: return finishSession(session)

    if (input == expected) {
        val next = session.copy(
            activeCharIndex = session.activeCharIndex + 1,
            correctKeystrokes = session.correctKeystrokes + 1,
            hasError = false,
        )

        if (next.activeCharIndex < token.length) {
            return recordHistory(session, next)
        }

        if (next.activeTokenIndex >= next.tokens.lastIndex) {
            return recordHistory(session, finishSession(next))
        }

        val updated = if (mode == PracticeMode.Auto) {
            advanceToken(next)
        } else {
            next.copy(waitingForSpace = true)
        }

        return recordHistory(session, updated)
    }

    if (input == ' ') {
        return session
    }

    return recordHistory(
        session,
        session.copy(
            errorKeystrokes = session.errorKeystrokes + 1,
            hasError = true,
        ),
    )
}

fun removeLastInputFromSession(session: TypingSession): TypingSession {
    val snapshot = session.history.lastOrNull() ?: return session
    return session.copy(
        activeCharIndex = snapshot.activeCharIndex,
        activeTokenIndex = snapshot.activeTokenIndex,
        correctKeystrokes = snapshot.correctKeystrokes,
        errorKeystrokes = snapshot.errorKeystrokes,
        hasError = snapshot.hasError,
        isFinished = snapshot.isFinished,
        waitingForSpace = snapshot.waitingForSpace,
        history = session.history.dropLast(1),
    )
}

private fun sumCharacters(tokens: List<String>): Int = tokens.sumOf { it.length }

fun buildTypingMetrics(session: TypingSession, elapsedSeconds: Double): TypingMetrics {
    val safeElapsed = max(elapsedSeconds, 1.0)
    val totalCharacters = sumCharacters(session.tokens)
    val correctCharacters = session.correctKeystrokes
    val mistakes = session.errorKeystrokes
    val effectiveKeystrokes = correctCharacters + mistakes
    val accuracy = if (effectiveKeystrokes == 0) {
        0.0
    } else {
        correctCharacters.toDouble() / effectiveKeystrokes.toDouble() * 100.0
    }
    val cpm = correctCharacters / safeElapsed * 60.0
    val wpm = cpm / 5.0
    val score = (wpm * (accuracy / 100.0) * 10.0).roundToInt()

    return TypingMetrics(
        accuracy = accuracy,
        correctCharacters = correctCharacters,
        cpm = cpm,
        elapsedSeconds = safeElapsed,
        mistakes = mistakes,
        score = score,
        totalCharacters = totalCharacters,
        wpm = wpm,
    )
}

fun emptyTypingMetrics(tokens: List<String>): TypingMetrics = buildTypingMetrics(
    session = TypingSession(
        activeCharIndex = 0,
        activeTokenIndex = 0,
        correctKeystrokes = 0,
        errorKeystrokes = 0,
        hasError = false,
        isFinished = false,
        tokens = tokens,
        waitingForSpace = false,
        history = emptyList(),
    ),
    elapsedSeconds = 1.0,
)
