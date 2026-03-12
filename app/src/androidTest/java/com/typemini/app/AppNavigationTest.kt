package com.typemini.app

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class AppNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun app_starts_on_practice_screen() {
        composeRule.onNodeWithText("Practice").assertExists()
        composeRule.onNodeWithText("History").assertExists()
    }
}
