package com.typemini.app.domain.model

enum class PracticeMode(
    val title: String,
    val helper: String,
) {
    Space(
        title = "Space",
        helper = "Finish a word, then press space to advance.",
    ),
    Auto(
        title = "Auto",
        helper = "Advance automatically after the last correct letter.",
    ),
}
