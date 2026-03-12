package com.typemini.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.typemini.app.data.repository.PracticeRepository
import com.typemini.app.feature.history.HistoryRoute
import com.typemini.app.feature.practice.PracticeRoute
import com.typemini.app.feature.practice.PracticeViewModel
import com.typemini.app.feature.result.ResultRoute
import com.typemini.app.feature.settings.SettingsRoute
import com.typemini.app.ui.theme.TypeMiniTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val practiceRoute = "practice"
private const val historyRoute = "history"
private const val settingsRoute = "settings"
private const val resultRoute = "result/{resultId}"

private fun resultRoute(resultId: Long): String = "result/$resultId"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypeMiniApp(
    repository: PracticeRepository,
) {
    TypeMiniTheme {
        val navController = rememberNavController()
        val practiceViewModel: PracticeViewModel = viewModel(
            factory = PracticeViewModel.factory(repository),
        )
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.10f),
                        ),
                    ),
                ),
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    if (currentRoute == historyRoute || currentRoute == settingsRoute || currentRoute?.startsWith("result/") == true) {
                        TopAppBar(
                            navigationIcon = {
                                IconButton(
                                    onClick = {
                                        if (!navController.popBackStack()) {
                                            navController.navigate(practiceRoute) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                        contentDescription = "Back to practice",
                                    )
                                }
                            },
                            title = {
                                Text(
                                    text = when {
                                        currentRoute == historyRoute -> "History"
                                        currentRoute == settingsRoute -> "Settings"
                                        else -> "Session Result"
                                    },
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent,
                                navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                                titleContentColor = MaterialTheme.colorScheme.primary,
                            ),
                        )
                    }
                },
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = practiceRoute,
                    modifier = Modifier.padding(innerPadding),
                ) {
                    composable(practiceRoute) {
                        PracticeRoute(
                            viewModel = practiceViewModel,
                            onResultReady = { resultId ->
                                navController.navigate(resultRoute(resultId))
                            },
                            onOpenHistory = {
                                navController.navigate(historyRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onOpenSettings = {
                                navController.navigate(settingsRoute) {
                                    launchSingleTop = true
                                }
                            },
                        )
                    }
                    composable(historyRoute) {
                        HistoryRoute(
                            repository = repository,
                            onOpenResult = { resultId ->
                                navController.navigate(resultRoute(resultId))
                            },
                        )
                    }
                    composable(settingsRoute) {
                        SettingsRoute(
                            viewModel = practiceViewModel,
                        )
                    }
                    composable(
                        route = resultRoute,
                        arguments = listOf(navArgument("resultId") { type = NavType.LongType }),
                    ) { entry ->
                        val resultId = entry.arguments?.getLong("resultId") ?: return@composable
                        ResultRoute(
                            resultId = resultId,
                            repository = repository,
                            onPracticeAgain = {
                                navController.navigate(practiceRoute) {
                                    popUpTo(practiceRoute) {
                                        inclusive = false
                                    }
                                }
                            },
                            onViewHistory = {
                                navController.navigate(historyRoute) {
                                    popUpTo(practiceRoute) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

fun formatNumber(value: Double): String = String.format(Locale.US, "%.0f", value)

fun formatAccuracy(value: Double): String = String.format(Locale.US, "%.1f%%", value)

fun formatSeconds(value: Double): String = String.format(Locale.US, "%.1fs", value)

fun formatTimestamp(value: Long): String {
    val formatter = DateTimeFormatter.ofPattern("MMM d, HH:mm", Locale.US)
    return formatter.format(
        Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalDateTime(),
    )
}
