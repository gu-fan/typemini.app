package com.typemini.app.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.typemini.app.data.repository.PracticeRepository
import com.typemini.app.domain.model.CourseOverview
import com.typemini.app.domain.model.UnitProgress
import com.typemini.app.ui.components.CompactListItem
import com.typemini.app.ui.components.CompactMetricPill
import com.typemini.app.ui.components.CompactSectionHeader
import com.typemini.app.ui.components.EmptyStateCard
import com.typemini.app.ui.components.SectionDivider
import com.typemini.app.ui.components.SectionPill
import com.typemini.app.ui.components.TypeMiniSurfaceCard

@Composable
fun HomeRoute(
    repository: PracticeRepository,
    onContinue: (String, String) -> Unit,
    onOpenUnit: (String) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val overview by repository.observeCourseOverview().collectAsStateWithLifecycle(
        initialValue = CourseOverview(
            resumeState = null,
            units = emptyList(),
            completedUnits = 0,
            totalUnits = 0,
            isCourseCompleted = false,
        ),
    )

    HomeScreen(
        overview = overview,
        onContinue = onContinue,
        onOpenUnit = onOpenUnit,
        onOpenHistory = onOpenHistory,
        onOpenSettings = onOpenSettings,
    )
}

@Composable
fun HomeScreen(
    overview: CourseOverview,
    onContinue: (String, String) -> Unit,
    onOpenUnit: (String) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
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
                title = "English Practice",
                subtitle = "Short lessons, visible progress, and a calm place to keep typing.",
            )
        }

        item {
            ContinueCard(
                overview = overview,
                onContinue = onContinue,
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onOpenHistory,
                ) {
                    Text("History")
                }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onOpenSettings,
                ) {
                    Text("Settings")
                }
            }
        }

        if (overview.units.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No units yet",
                    body = "Add course units to begin the English learning flow.",
                )
            }
        } else {
            item {
                SectionDivider()
            }
            items(overview.units, key = { it.unit.id }) { progress ->
                UnitProgressRow(
                    progress = progress,
                    onClick = { onOpenUnit(progress.unit.id) },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ContinueCard(
    overview: CourseOverview,
    onContinue: (String, String) -> Unit,
) {
    val resume = overview.resumeState

    TypeMiniSurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = if (overview.isCourseCompleted) "Course complete" else "Continue",
            )
            SectionPill("${overview.completedUnits}/${overview.totalUnits}")
        }

        Text(
            text = when {
                resume == null -> "No lesson is available yet."
                overview.isCourseCompleted -> "You have completed every unit. Open any unit to review or restart."
                else -> "${resume.unitTitle} · ${resume.articleTitle}"
            },
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CompactMetricPill(label = "Units", value = "${overview.completedUnits}/${overview.totalUnits}")
            if (resume != null) {
                CompactMetricPill(label = "Next", value = resume.unitTitle)
            }
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = resume != null,
            onClick = {
                resume?.let { onContinue(it.unitId, it.articleId) }
            },
        ) {
            Text("Continue lesson")
        }
    }
}

@Composable
private fun UnitProgressRow(
    progress: UnitProgress,
    onClick: () -> Unit,
) {
    CompactListItem(
        title = progress.unit.title,
        subtitle = progress.unit.difficultyLabel,
        supportingText = progress.unit.description,
        onClick = onClick,
        trailingContent = {
            SectionPill(
                text = if (progress.isCompleted) "Complete" else "${progress.completedArticles}/${progress.totalArticles}",
            )
        },
    )
}
