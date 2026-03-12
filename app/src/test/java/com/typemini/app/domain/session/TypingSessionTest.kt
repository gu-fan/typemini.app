package com.typemini.app.domain.session

import com.typemini.app.domain.model.PracticeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TypingSessionTest {
    @Test
    fun `split practice text trims repeated spaces`() {
        assertEquals(
            listOf("hello", "world"),
            splitPracticeText("  hello   world "),
        )
    }

    @Test
    fun `wrong character increments errors and does not advance`() {
        val session = createTypingSession("hello")

        val next = applyCharacterToSession(
            session = session,
            rawInput = 'x',
            mode = PracticeMode.Space,
        )

        assertEquals(0, next.activeCharIndex)
        assertEquals(1, next.errorKeystrokes)
        assertTrue(next.hasError)
    }

    @Test
    fun `space mode waits for space before advancing token`() {
        val session = createTypingSession("go now")
        val afterWord = "go".fold(session) { current, char ->
            applyCharacterToSession(current, char, PracticeMode.Space)
        }

        assertTrue(afterWord.waitingForSpace)
        assertEquals(0, afterWord.activeTokenIndex)

        val next = applyCharacterToSession(afterWord, ' ', PracticeMode.Space)

        assertEquals(1, next.activeTokenIndex)
        assertFalse(next.waitingForSpace)
    }

    @Test
    fun `auto mode advances immediately after a token`() {
        val session = createTypingSession("go now")
        val next = "go".fold(session) { current, char ->
            applyCharacterToSession(current, char, PracticeMode.Auto)
        }

        assertEquals(1, next.activeTokenIndex)
        assertEquals(0, next.activeCharIndex)
    }

    @Test
    fun `build typing metrics returns expected values`() {
        val session = TypingSession(
            activeCharIndex = 0,
            activeTokenIndex = 0,
            correctKeystrokes = 25,
            errorKeystrokes = 5,
            hasError = false,
            isFinished = true,
            tokens = listOf("alpha", "bravo", "charlie", "delta"),
            waitingForSpace = false,
        )

        val metrics = buildTypingMetrics(session, elapsedSeconds = 30.0)

        assertEquals(10.0, metrics.wpm, 0.001)
        assertEquals(83.333, metrics.accuracy, 0.01)
        assertEquals(83, metrics.score)
    }
}
