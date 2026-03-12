package com.typemini.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class AppNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun app_starts_on_home_screen() {
        composeRule.onNodeWithText("English Practice").assertIsDisplayed()
        composeRule.onNodeWithText("Continue lesson").assertIsDisplayed()
        composeRule.onNodeWithText("History").assertIsDisplayed()
    }

    @Test
    fun home_can_open_unit_and_start_article() {
        composeRule.onNodeWithText("Daily Basics").performClick()
        composeRule.onNodeWithText("Morning Routine").assertIsDisplayed()
        composeRule.onNodeWithText("Morning Routine").performClick()
        composeRule.onNodeWithText("Daily Basics · Article 1").assertIsDisplayed()
        composeRule.onNodeWithText("Morning Routine").assertIsDisplayed()
    }
}
