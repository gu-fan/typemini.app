package com.typemini.app.feature.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.typemini.app.ui.components.EmptyStateCard
import com.typemini.app.ui.components.MetricTile
import com.typemini.app.ui.components.StatSummaryRow
import com.typemini.app.ui.components.TypeMiniSurfaceCard

@Composable
fun ResultRoute(
    resultId: Long,
    repository: PracticeRepository,
    onPracticeAgain: () -> Unit,
    onViewHistory: () -> Unit,
) {
    val result by repository.observePracticeResult(resultId).collectAsStateWithLifecycle(initialValue = null)

    if (result == null) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
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
        onPracticeAgain = onPracticeAgain,
        onViewHistory = onViewHistory,
    )
}

@Composable
fun ResultScreen(
    result: PracticeResult,
    onPracticeAgain: () -> Unit,
    onViewHistory: () -> Unit,
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
                    text = when {
                        result.accuracy >= 98.0 && result.wpm >= 55.0 -> "Elegant and precise."
                        result.accuracy >= 95.0 -> "Clean rhythm."
                        else -> "Another measured pass."
                    },
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = "Saved ${formatTimestamp(result.createdAt)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MetricTile(label = "WPM", value = formatNumber(result.wpm), modifier = Modifier.weight(1f))
                MetricTile(label = "ACC", value = formatAccuracy(result.accuracy), modifier = Modifier.weight(1f))
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MetricTile(label = "TIME", value = formatSeconds(result.elapsedSeconds), modifier = Modifier.weight(1f))
                MetricTile(label = "SCORE", value = result.score.toString(), modifier = Modifier.weight(1f))
            }
        }

        item {
            TypeMiniSurfaceCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                StatSummaryRow(label = "Practice set", value = result.practiceTextTitle)
                StatSummaryRow(label = "Mode", value = result.mode.title)
                StatSummaryRow(label = "Correct keys", value = result.correctKeystrokes.toString())
                StatSummaryRow(label = "Errors", value = result.errorKeystrokes.toString())
            }
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onPracticeAgain,
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
