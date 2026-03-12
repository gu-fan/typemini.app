package com.typemini.app.feature.practice

data class PracticeViewport(
    val allLines: List<List<Int>>,
    val activeLineIndex: Int,
    val visibleStartLine: Int,
    val visibleLines: List<List<Int>>,
)

internal fun buildPracticeLines(
    tokenWidths: List<Int>,
    maxWidth: Int,
    tokenSpacing: Int,
): List<List<Int>> {
    if (tokenWidths.isEmpty()) {
        return emptyList()
    }

    val safeMaxWidth = maxOf(maxWidth, 1)
    val safeSpacing = maxOf(tokenSpacing, 0)
    val lines = mutableListOf<List<Int>>()
    val currentLine = mutableListOf<Int>()
    var currentWidth = 0

    tokenWidths.forEachIndexed { tokenIndex, tokenWidth ->
        val projectedWidth = if (currentLine.isEmpty()) {
            tokenWidth
        } else {
            currentWidth + safeSpacing + tokenWidth
        }

        if (projectedWidth <= safeMaxWidth || currentLine.isEmpty()) {
            currentLine += tokenIndex
            currentWidth = projectedWidth
        } else {
            lines += currentLine.toList()
            currentLine.clear()
            currentLine += tokenIndex
            currentWidth = tokenWidth
        }
    }

    if (currentLine.isNotEmpty()) {
        lines += currentLine.toList()
    }

    return lines
}

internal fun resolvePracticeViewport(
    lines: List<List<Int>>,
    activeTokenIndex: Int,
    visibleLineCount: Int = 4,
    anchorLineIndex: Int = 2,
): PracticeViewport {
    val safeVisibleLineCount = maxOf(visibleLineCount, 1)

    if (lines.isEmpty()) {
        return PracticeViewport(
            allLines = emptyList(),
            activeLineIndex = 0,
            visibleStartLine = 0,
            visibleLines = emptyList(),
        )
    }

    val activeLineIndex = lines.indexOfFirst { activeTokenIndex in it }
        .takeIf { it >= 0 }
        ?: lines.lastIndex

    val maxVisibleStart = maxOf(lines.size - safeVisibleLineCount, 0)
    val requestedStart = activeLineIndex - anchorLineIndex
    val visibleStartLine = requestedStart.coerceIn(0, maxVisibleStart)
    val visibleEndLine = minOf(visibleStartLine + safeVisibleLineCount, lines.size)

    return PracticeViewport(
        allLines = lines,
        activeLineIndex = activeLineIndex,
        visibleStartLine = visibleStartLine,
        visibleLines = lines.subList(visibleStartLine, visibleEndLine),
    )
}
