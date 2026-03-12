package com.typemini.app.feature.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.typemini.app.navigation.formatAccuracy
import com.typemini.app.navigation.formatNumber
import com.typemini.app.navigation.formatTimestamp
import com.typemini.app.ui.components.CompactListItem
import com.typemini.app.ui.components.CompactMetricPill
import com.typemini.app.ui.components.CompactSectionHeader
import com.typemini.app.ui.components.EmptyStateCard
import com.typemini.app.ui.components.SectionDivider

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

@OptIn(ExperimentalLayoutApi::class)
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
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            CompactSectionHeader(
                title = "History",
                subtitle = "A compact log of recent sessions, sorted the way you want to review them.",
            )
        }

        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CompactMetricPill(label = "AVG", value = averageWpm)
                CompactMetricPill(label = "BEST", value = bestWpm)
                CompactMetricPill(label = "ACC", value = averageAccuracy)
                CompactMetricPill(label = "SESSIONS", value = sessionCount)
            }
        }

        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
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
                    body = "Complete a session and your results will appear here automatically.",
                )
            }
        } else {
            item {
                SectionDivider()
            }
            items(results, key = { it.id }) { result ->
                HistoryResultRow(
                    result = result,
                    onClick = { onOpenResult(result.id) },
                )
            }
        }
    }
}

@Composable
private fun HistoryResultRow(
    result: PracticeResult,
    onClick: () -> Unit,
) {
    CompactListItem(
        title = result.articleTitle,
        subtitle = result.unitTitle,
        supportingText = "${formatTimestamp(result.createdAt)} · ${result.mode.title}",
        trailingText = "${formatNumber(result.wpm)} WPM · ${formatAccuracy(result.accuracy)}",
        onClick = onClick,
    )
}
