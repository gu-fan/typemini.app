package com.typemini.app.feature.unit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.typemini.app.domain.model.UnitSummary
import com.typemini.app.navigation.formatAccuracy
import com.typemini.app.navigation.formatNumber
import com.typemini.app.ui.components.EmptyStateCard
import com.typemini.app.ui.components.MetricTile
import com.typemini.app.ui.components.StatSummaryRow
import com.typemini.app.ui.components.TypeMiniSurfaceCard

@Composable
fun UnitSummaryRoute(
    unitId: String,
    repository: PracticeRepository,
    hasNextUnit: Boolean,
    onOpenNextUnit: () -> Unit,
    onBackHome: () -> Unit,
) {
    val summary by repository.observeUnitSummary(unitId).collectAsStateWithLifecycle(initialValue = null)

    if (summary == null) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
        ) {
            item {
                EmptyStateCard(
                    title = "Summary unavailable",
                    body = "This unit summary could not be loaded.",
                )
            }
        }
        return
    }

    UnitSummaryScreen(
        summary = summary!!,
        hasNextUnit = hasNextUnit,
        onOpenNextUnit = onOpenNextUnit,
        onBackHome = onBackHome,
    )
}

@Composable
fun UnitSummaryScreen(
    summary: UnitSummary,
    hasNextUnit: Boolean,
    onOpenNextUnit: () -> Unit,
    onBackHome: () -> Unit,
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
                    text = "${summary.unit.title} complete",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = "Every article in this unit has at least one saved completion.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                StatSummaryRow(
                    label = "Articles",
                    value = "${summary.completedArticles}/${summary.totalArticles}",
                )
                StatSummaryRow(
                    label = "Attempts",
                    value = summary.totalAttempts.toString(),
                )
            }
        }

        item {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MetricTile(
                    label = "AVG WPM",
                    value = formatNumber(summary.averageWpm),
                    modifier = Modifier.weight(1f),
                )
                MetricTile(
                    label = "BEST WPM",
                    value = formatNumber(summary.bestWpm),
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            MetricTile(
                label = "AVG ACC",
                value = formatAccuracy(summary.averageAccuracy),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        items(summary.articleProgress, key = { it.article.id }) { article ->
            TypeMiniSurfaceCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Article ${article.article.order} · ${article.article.title}",
                    style = MaterialTheme.typography.titleMedium,
                )
                StatSummaryRow(
                    label = "Status",
                    value = if (article.isCompleted) "Completed" else "Pending",
                )
                StatSummaryRow(
                    label = "Attempts",
                    value = article.attemptCount.toString(),
                )
                StatSummaryRow(
                    label = "Latest WPM",
                    value = formatNumber(article.latestResult?.wpm ?: 0.0),
                )
            }
        }

        item {
            if (hasNextUnit) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onOpenNextUnit,
                ) {
                    Text("Next unit")
                }
            }
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onBackHome,
            ) {
                Text("Back home")
            }
        }
    }
}
