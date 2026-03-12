package com.typemini.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = MistPrimary,
    onPrimary = MistSurface,
    primaryContainer = MistPrimaryContainer,
    onPrimaryContainer = MistInk,
    tertiary = MistTertiary,
    background = MistBackground,
    onBackground = MistInk,
    surface = MistSurface,
    onSurface = MistInk,
    surfaceContainer = MistSurfaceHigh,
    surfaceContainerHigh = MistSurface,
    surfaceContainerHighest = MistPrimaryContainer.copy(alpha = 0.62f),
    onSurfaceVariant = MistMuted,
    outline = MistMuted.copy(alpha = 0.35f),
    error = MistError,
    errorContainer = MistErrorContainer,
    onErrorContainer = MistError,
)

private val DarkColors = darkColorScheme(
    primary = NightPrimary,
    onPrimary = NightBackground,
    primaryContainer = NightPrimaryContainer,
    onPrimaryContainer = NightInk,
    tertiary = NightTertiary,
    background = NightBackground,
    onBackground = NightInk,
    surface = NightSurface,
    onSurface = NightInk,
    surfaceContainer = NightSurfaceHigh,
    surfaceContainerHigh = NightSurfaceHigh,
    surfaceContainerHighest = NightPrimaryContainer.copy(alpha = 0.86f),
    onSurfaceVariant = NightMuted,
    outline = NightMuted.copy(alpha = 0.32f),
    error = NightError,
    errorContainer = NightErrorContainer,
    onErrorContainer = NightError,
)

@Composable
fun TypeMiniTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = TypeMiniTypography,
        content = content,
    )
}
