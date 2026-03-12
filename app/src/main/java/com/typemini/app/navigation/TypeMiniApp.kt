package com.typemini.app.navigation

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.typemini.app.feature.home.HomeRoute
import com.typemini.app.feature.practice.PracticeRoute
import com.typemini.app.feature.practice.PracticeViewModel
import com.typemini.app.feature.result.ResultRoute
import com.typemini.app.feature.settings.SettingsRoute
import com.typemini.app.feature.unit.UnitRoute
import com.typemini.app.feature.unit.UnitSummaryRoute
import com.typemini.app.domain.model.CompletionNextDestinationType
import com.typemini.app.ui.theme.TypeMiniTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val homeRoute = "home"
private const val unitRoute = "unit/{unitId}"
private const val practiceRoute = "practice/{unitId}/{articleId}"
private const val historyRoute = "history"
private const val settingsRoute = "settings"
private const val unitSummaryRoute = "unit-summary/{unitId}"
private const val resultRoute = "result/{resultId}"

private fun unitRoute(unitId: String): String = "unit/$unitId"
private fun practiceRoute(unitId: String, articleId: String): String = "practice/$unitId/$articleId"
private fun unitSummaryRoute(unitId: String): String = "unit-summary/$unitId"
private fun resultRoute(resultId: Long): String = "result/$resultId"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypeMiniApp(
    repository: PracticeRepository,
    launcherEntryVersion: Int = 0,
) {
    TypeMiniTheme {
        val navController = rememberNavController()
        val practiceViewModel: PracticeViewModel = viewModel(
            factory = PracticeViewModel.factory(repository),
        )
        val practiceState by practiceViewModel.uiState.collectAsStateWithLifecycle()
        val units = repository.getPracticeUnits()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route

        fun navigateHome() {
            practiceViewModel.clearResultAction()
            if (!navController.popBackStack(homeRoute, false)) {
                navController.navigate(homeRoute) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }

        LaunchedEffect(launcherEntryVersion) {
            if (launcherEntryVersion > 0) {
                navController.navigate(homeRoute) {
                    popUpTo(navController.graph.id) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        }

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
                    if (
                        currentRoute == historyRoute ||
                        currentRoute == settingsRoute ||
                        currentRoute?.startsWith("result/") == true ||
                        currentRoute?.startsWith("unit/") == true ||
                        currentRoute?.startsWith("unit-summary/") == true
                    ) {
                        TopAppBar(
                            navigationIcon = {
                                IconButton(
                                    onClick = {
                                        if (currentRoute?.startsWith("unit/") == true) {
                                            navigateHome()
                                        } else if (!navController.popBackStack()) {
                                            navigateHome()
                                        }
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                        contentDescription = if (currentRoute?.startsWith("unit/") == true) {
                                            "Back to home"
                                        } else {
                                            "Back"
                                        },
                                    )
                                }
                            },
                            title = {
                                Text(
                                    text = when {
                                        currentRoute?.startsWith("unit-summary/") == true -> "Unit Summary"
                                        currentRoute?.startsWith("unit/") == true -> "Unit"
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
                    startDestination = homeRoute,
                    modifier = Modifier.padding(innerPadding),
                ) {
                    composable(homeRoute) {
                        HomeRoute(
                            repository = repository,
                            onContinue = { unitId, articleId ->
                                practiceViewModel.clearResultAction()
                                navController.navigate(practiceRoute(unitId, articleId))
                            },
                            onOpenUnit = { unitId ->
                                practiceViewModel.clearResultAction()
                                navController.navigate(unitRoute(unitId))
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
                    composable(
                        route = unitRoute,
                        arguments = listOf(navArgument("unitId") { type = NavType.StringType }),
                    ) { entry ->
                        val unitId = entry.arguments?.getString("unitId") ?: return@composable
                        BackHandler {
                            navigateHome()
                        }
                        UnitRoute(
                            unitId = unitId,
                            repository = repository,
                            onStartArticle = { selectedUnitId, articleId ->
                                practiceViewModel.clearResultAction()
                                navController.navigate(practiceRoute(selectedUnitId, articleId))
                            },
                        )
                    }
                    composable(
                        route = practiceRoute,
                        arguments = listOf(
                            navArgument("unitId") { type = NavType.StringType },
                            navArgument("articleId") { type = NavType.StringType },
                        ),
                    ) { entry ->
                        val unitId = entry.arguments?.getString("unitId") ?: return@composable
                        val articleId = entry.arguments?.getString("articleId") ?: return@composable
                        BackHandler {
                            practiceViewModel.clearResultAction()
                            if (navController.previousBackStackEntry?.destination?.route?.startsWith("unit/") == true) {
                                navController.popBackStack()
                            } else {
                                navController.navigate(unitRoute(unitId)) {
                                    popUpTo(homeRoute) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                        PracticeRoute(
                            viewModel = practiceViewModel,
                            unitId = unitId,
                            articleId = articleId,
                            onResultReady = { resultId ->
                                navController.navigate(resultRoute(resultId))
                            },
                            onBackToUnit = {
                                practiceViewModel.clearResultAction()
                                navController.navigate(unitRoute(unitId)) {
                                    popUpTo(homeRoute) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onOpenHistory = {
                                navController.navigate(historyRoute) {
                                    launchSingleTop = true
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
                        route = unitSummaryRoute,
                        arguments = listOf(navArgument("unitId") { type = NavType.StringType }),
                    ) { entry ->
                        val unitId = entry.arguments?.getString("unitId") ?: return@composable
                        val nextUnitId = units
                            .indexOfFirst { it.id == unitId }
                            .takeIf { it >= 0 }
                            ?.let { index -> units.getOrNull(index + 1)?.id }
                        UnitSummaryRoute(
                            unitId = unitId,
                            repository = repository,
                            hasNextUnit = nextUnitId != null,
                            onOpenNextUnit = {
                                practiceViewModel.clearResultAction()
                                nextUnitId?.let { navController.navigate(unitRoute(it)) }
                            },
                            onBackHome = {
                                practiceViewModel.clearResultAction()
                                navController.navigate(homeRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                        )
                    }
                    composable(
                        route = resultRoute,
                        arguments = listOf(navArgument("resultId") { type = NavType.LongType }),
                    ) { entry ->
                        val resultId = entry.arguments?.getLong("resultId") ?: return@composable
                        val nextDestination = practiceState.nextDestination
                            ?.takeIf { practiceState.resultActionResultId == resultId }
                        val primaryActionLabel = when (nextDestination?.type) {
                            CompletionNextDestinationType.NextArticle -> "Next article"
                            CompletionNextDestinationType.UnitSummary -> "Unit summary"
                            null -> null
                        }
                        ResultRoute(
                            resultId = resultId,
                            repository = repository,
                            primaryActionLabel = primaryActionLabel,
                            onPrimaryAction = if (nextDestination != null) {
                                { result ->
                                    when (nextDestination.type) {
                                        CompletionNextDestinationType.NextArticle -> {
                                            practiceViewModel.clearResultAction()
                                            navController.navigate(
                                                practiceRoute(
                                                    nextDestination.unitId,
                                                    nextDestination.articleId.orEmpty(),
                                                ),
                                            )
                                        }
                                        CompletionNextDestinationType.UnitSummary -> {
                                            practiceViewModel.clearResultAction()
                                            navController.navigate(unitSummaryRoute(result.unitId))
                                        }
                                    }
                                }
                            } else {
                                null
                            },
                            onPracticeAgain = { result ->
                                practiceViewModel.clearResultAction()
                                navController.navigate(practiceRoute(result.unitId, result.articleId))
                            },
                            onViewHistory = {
                                practiceViewModel.clearResultAction()
                                navController.navigate(historyRoute) {
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
