package com.typemini.app.feature.unit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.typemini.app.data.repository.PracticeRepository
import com.typemini.app.domain.model.ArticleProgress
import com.typemini.app.domain.model.UnitSummary
import com.typemini.app.ui.components.EmptyStateCard
import com.typemini.app.ui.components.SectionPill
import com.typemini.app.ui.components.StatSummaryRow
import com.typemini.app.ui.components.TypeMiniSurfaceCard

@Composable
fun UnitRoute(
    unitId: String,
    repository: PracticeRepository,
    onStartArticle: (String, String) -> Unit,
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
                    title = "Unit unavailable",
                    body = "This unit could not be loaded.",
                )
            }
        }
        return
    }

    UnitScreen(
        summary = summary!!,
        onStartArticle = onStartArticle,
    )
}

@Composable
fun UnitScreen(
    summary: UnitSummary,
    onStartArticle: (String, String) -> Unit,
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
                    text = summary.unit.title,
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = summary.unit.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                StatSummaryRow(
                    label = "Progress",
                    value = "${summary.completedArticles}/${summary.totalArticles}",
                )
                StatSummaryRow(
                    label = "Difficulty",
                    value = summary.unit.difficultyLabel,
                )
            }
        }

        items(summary.articleProgress, key = { it.article.id }) { progress ->
            ArticleProgressCard(
                progress = progress,
                onStartArticle = onStartArticle,
            )
        }
    }
}

@Composable
private fun ArticleProgressCard(
    progress: ArticleProgress,
    onStartArticle: (String, String) -> Unit,
) {
    TypeMiniSurfaceCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onStartArticle(progress.article.unitId, progress.article.id) },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Article ${progress.article.order}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            SectionPill(
                text = if (progress.isCompleted) "Done" else "New",
            )
        }
        Text(
            text = progress.article.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = progress.article.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        StatSummaryRow(
            label = "Attempts",
            value = progress.attemptCount.toString(),
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onStartArticle(progress.article.unitId, progress.article.id) },
        ) {
            Text(if (progress.isCompleted) "Practice again" else "Start article")
        }
    }
}
