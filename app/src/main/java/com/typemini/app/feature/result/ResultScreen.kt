package com.typemini.app.feature.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.typemini.app.data.repository.PracticeRepository
import com.typemini.app.domain.model.PracticeResult
import com.typemini.app.navigation.formatAccuracy
import com.typemini.app.navigation.formatNumber
import com.typemini.app.navigation.formatSeconds
import com.typemini.app.navigation.formatTimestamp
import com.typemini.app.ui.components.CompactListItem
import com.typemini.app.ui.components.CompactMetricPill
import com.typemini.app.ui.components.CompactSectionHeader
import com.typemini.app.ui.components.EmptyStateCard
import com.typemini.app.ui.components.SectionDivider
import com.typemini.app.ui.components.TypeMiniSurfaceCard

@Composable
fun ResultRoute(
    resultId: Long,
    repository: PracticeRepository,
    primaryActionLabel: String?,
    onPrimaryAction: ((PracticeResult) -> Unit)?,
    onPracticeAgain: (PracticeResult) -> Unit,
    onViewHistory: () -> Unit,
) {
    val result by repository.observePracticeResult(resultId).collectAsStateWithLifecycle(initialValue = null)

    if (result == null) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        ) {
            item {
                EmptyStateCard(
                    title = "Result unavailable",
                    body = "This session could not be loaded. Return to Practice and start a fresh round.",
                )
            }
        }
        return
    }

    ResultScreen(
        result = result!!,
        primaryActionLabel = primaryActionLabel,
        onPrimaryAction = onPrimaryAction,
        onPracticeAgain = onPracticeAgain,
        onViewHistory = onViewHistory,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResultScreen(
    result: PracticeResult,
    primaryActionLabel: String?,
    onPrimaryAction: ((PracticeResult) -> Unit)?,
    onPracticeAgain: (PracticeResult) -> Unit,
    onViewHistory: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            CompactSectionHeader(
                title = result.articleTitle,
                subtitle = "${result.unitTitle} · Saved ${formatTimestamp(result.createdAt)}",
            )
        }

        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CompactMetricPill(label = "WPM", value = formatNumber(result.wpm))
                CompactMetricPill(label = "ACC", value = formatAccuracy(result.accuracy))
                CompactMetricPill(label = "TIME", value = formatSeconds(result.elapsedSeconds))
                CompactMetricPill(label = "SCORE", value = result.score.toString())
            }
        }

        item {
            TypeMiniSurfaceCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            ) {
                Text(
                    text = when {
                        result.accuracy >= 98.0 && result.wpm >= 55.0 -> "Elegant and precise."
                        result.accuracy >= 95.0 -> "Clean rhythm."
                        else -> "Another measured pass."
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Use this page as a quick checkpoint, then move to the next article or repeat the same one.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            SectionDivider()
        }

        item {
            CompactListItem(
                title = result.unitTitle,
                subtitle = "Unit",
                trailingText = result.mode.title,
            )
        }

        item {
            CompactListItem(
                title = "Article ${result.articleOrder} · ${result.articleTitle}",
                subtitle = "Article",
                trailingText = "${result.correctKeystrokes} correct",
                supportingText = "${result.errorKeystrokes} errors",
            )
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (primaryActionLabel != null && onPrimaryAction != null) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onPrimaryAction(result) },
                    ) {
                        Text(primaryActionLabel)
                    }
                }
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onPracticeAgain(result) },
                ) {
                    Text("Practice again")
                }
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onViewHistory,
                ) {
                    Text("View history")
                }
            }
        }
    }
}
