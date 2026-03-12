package com.typemini.app.feature.unit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.typemini.app.data.repository.PracticeRepository
import com.typemini.app.domain.model.ArticleProgress
import com.typemini.app.domain.model.UnitSummary
import com.typemini.app.ui.components.CompactListItem
import com.typemini.app.ui.components.CompactMetricPill
import com.typemini.app.ui.components.CompactSectionHeader
import com.typemini.app.ui.components.EmptyStateCard
import com.typemini.app.ui.components.SectionDivider
import com.typemini.app.ui.components.SectionPill
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
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
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
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            CompactSectionHeader(
                title = summary.unit.title,
                subtitle = summary.unit.description,
            )
        }

        item {
            TypeMiniSurfaceCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            ) {
                CompactMetricPill(
                    label = "Progress",
                    value = "${summary.completedArticles}/${summary.totalArticles}",
                )
                CompactMetricPill(
                    label = "Level",
                    value = summary.unit.difficultyLabel,
                )
            }
        }

        item {
            SectionDivider()
        }

        items(summary.articleProgress, key = { it.article.id }) { progress ->
            ArticleProgressRow(
                progress = progress,
                onStartArticle = onStartArticle,
            )
        }
    }
}

@Composable
private fun ArticleProgressRow(
    progress: ArticleProgress,
    onStartArticle: (String, String) -> Unit,
) {
    CompactListItem(
        title = progress.article.title,
        subtitle = "Article ${progress.article.order}",
        supportingText = "${progress.article.tokenCount} words · ${progress.attemptCount} attempts · ${progress.article.difficultyLabel}",
        onClick = { onStartArticle(progress.article.unitId, progress.article.id) },
        trailingContent = {
            SectionPill(if (progress.isCompleted) "Done" else "New")
        },
    )
}
