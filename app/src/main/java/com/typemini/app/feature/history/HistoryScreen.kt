package com.typemini.app.feature.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.typemini.app.data.repository.PracticeRepository
import com.typemini.app.domain.model.HistorySortMode
import com.typemini.app.domain.model.PracticeResult
import com.typemini.app.ui.components.EmptyStateCard
import com.typemini.app.ui.components.StatSummaryRow
import com.typemini.app.ui.components.TypeMiniSurfaceCard
import com.typemini.app.navigation.formatAccuracy
import com.typemini.app.navigation.formatNumber
import com.typemini.app.navigation.formatTimestamp

@Composable
fun HistoryRoute(
    repository: PracticeRepository,
    onOpenResult: (Long) -> Unit,
) {
    var sortMode by remember { mutableStateOf(HistorySortMode.Newest) }
    val results by repository.observeHistory(sortMode).collectAsStateWithLifecycle(initialValue = emptyList())
    val summary by repository.observeHistorySummary().collectAsStateWithLifecycle(
        initialValue = com.typemini.app.domain.model.HistorySummary(
            averageWpm = 0.0,
            bestWpm = 0.0,
            averageAccuracy = 0.0,
            sessionCount = 0,
        ),
    )

    HistoryScreen(
        sortMode = sortMode,
        onSortModeChange = { sortMode = it },
        results = results,
        averageWpm = formatNumber(summary.averageWpm),
        bestWpm = formatNumber(summary.bestWpm),
        averageAccuracy = formatAccuracy(summary.averageAccuracy),
        sessionCount = summary.sessionCount.toString(),
        onOpenResult = onOpenResult,
    )
}

@Composable
fun HistoryScreen(
    sortMode: HistorySortMode,
    onSortModeChange: (HistorySortMode) -> Unit,
    results: List<PracticeResult>,
    averageWpm: String,
    bestWpm: String,
    averageAccuracy: String,
    sessionCount: String,
    onOpenResult: (Long) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            TypeMiniSurfaceCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Recent rhythm",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "A quiet view of your last seven days: how fast, how clean, and how consistently you returned.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            TypeMiniSurfaceCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                StatSummaryRow(label = "Average WPM", value = averageWpm)
                StatSummaryRow(label = "Best WPM", value = bestWpm)
                StatSummaryRow(label = "Average ACC", value = averageAccuracy)
                StatSummaryRow(label = "Completed sessions", value = sessionCount)
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HistorySortMode.entries.forEach { option ->
                    FilterChip(
                        selected = option == sortMode,
                        onClick = { onSortModeChange(option) },
                        label = { Text(option.title) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    )
                }
            }
        }

        if (results.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No practice saved yet",
                    body = "Complete a session in Practice and your results will appear here automatically.",
                )
            }
        } else {
            items(results, key = { it.id }) { result ->
                HistoryResultCard(
                    result = result,
                    onClick = { onOpenResult(result.id) },
                )
            }
        }
    }
}

@Composable
private fun HistoryResultCard(
    result: PracticeResult,
    onClick: () -> Unit,
) {
    TypeMiniSurfaceCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = result.practiceTextTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = result.mode.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Text(
                text = formatTimestamp(result.createdAt),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ResultTag(label = "WPM ${formatNumber(result.wpm)}")
                ResultTag(label = "ACC ${formatAccuracy(result.accuracy)}")
                ResultTag(label = "SCORE ${result.score}")
            }
        }
    }
}

@Composable
private fun ResultTag(label: String) {
    Text(
        text = label,
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = RoundedCornerShape(999.dp),
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface,
    )
}
