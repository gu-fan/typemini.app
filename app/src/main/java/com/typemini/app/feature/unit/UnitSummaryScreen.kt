package com.typemini.app.feature.unit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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
import com.typemini.app.ui.components.CompactListItem
import com.typemini.app.ui.components.CompactMetricPill
import com.typemini.app.ui.components.CompactSectionHeader
import com.typemini.app.ui.components.EmptyStateCard
import com.typemini.app.ui.components.SectionDivider

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
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
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

@OptIn(ExperimentalLayoutApi::class)
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
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            CompactSectionHeader(
                title = "${summary.unit.title} complete",
                subtitle = "Every article in this unit now has at least one saved completion.",
            )
        }

        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CompactMetricPill(label = "Articles", value = "${summary.completedArticles}/${summary.totalArticles}")
                CompactMetricPill(label = "Attempts", value = summary.totalAttempts.toString())
                CompactMetricPill(label = "AVG WPM", value = formatNumber(summary.averageWpm))
                CompactMetricPill(label = "BEST", value = formatNumber(summary.bestWpm))
                CompactMetricPill(label = "AVG ACC", value = formatAccuracy(summary.averageAccuracy))
            }
        }

        item {
            SectionDivider()
        }

        items(summary.articleProgress, key = { it.article.id }) { article ->
            CompactListItem(
                title = article.article.title,
                subtitle = "Article ${article.article.order}",
                supportingText = "${article.attemptCount} attempts · Latest ${formatNumber(article.latestResult?.wpm ?: 0.0)} WPM",
                trailingText = if (article.isCompleted) "Completed" else "Pending",
            )
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
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
}
