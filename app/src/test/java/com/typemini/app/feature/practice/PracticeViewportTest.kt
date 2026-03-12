package com.typemini.app.feature.practice

import org.junit.Assert.assertEquals
import org.junit.Test

class PracticeViewportTest {
    @Test
    fun `buildPracticeLines wraps tokens when width is exceeded`() {
        val lines = buildPracticeLines(
            tokenWidths = listOf(40, 40, 40, 40),
            maxWidth = 100,
            tokenSpacing = 10,
        )

        assertEquals(
            listOf(
                listOf(0, 1),
                listOf(2, 3),
            ),
            lines,
        )
    }

    @Test
    fun `resolvePracticeViewport keeps active token on the third visible line when possible`() {
        val viewport = resolvePracticeViewport(
            lines = listOf(
                listOf(0),
                listOf(1),
                listOf(2),
                listOf(3),
                listOf(4),
                listOf(5),
            ),
            activeTokenIndex = 4,
        )

        assertEquals(4, viewport.activeLineIndex)
        assertEquals(2, viewport.visibleStartLine)
        assertEquals(
            listOf(
                listOf(2),
                listOf(3),
                listOf(4),
                listOf(5),
            ),
            viewport.visibleLines,
        )
    }

    @Test
    fun `resolvePracticeViewport clamps to the first available line near the start`() {
        val viewport = resolvePracticeViewport(
            lines = listOf(
                listOf(0),
                listOf(1),
                listOf(2),
            ),
            activeTokenIndex = 1,
        )

        assertEquals(1, viewport.activeLineIndex)
        assertEquals(0, viewport.visibleStartLine)
        assertEquals(
            listOf(
                listOf(0),
                listOf(1),
                listOf(2),
            ),
            viewport.visibleLines,
        )
    }
}
