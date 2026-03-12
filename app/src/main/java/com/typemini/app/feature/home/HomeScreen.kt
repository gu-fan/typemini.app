package com.typemini.app.feature.home

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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.typemini.app.data.repository.PracticeRepository
import com.typemini.app.domain.model.CourseOverview
import com.typemini.app.domain.model.UnitProgress
import com.typemini.app.ui.components.EmptyStateCard
import com.typemini.app.ui.components.SectionPill
import com.typemini.app.ui.components.StatSummaryRow
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
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            TypeMiniSurfaceCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "English Practice",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "A clean course flow with units, articles, progress, and a calm typing session.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                StatSummaryRow(
                    label = "Completed units",
                    value = "${overview.completedUnits}/${overview.totalUnits}",
                )
            }
        }

        item {
            TypeMiniSurfaceCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                val resume = overview.resumeState
                Text(
                    text = if (overview.isCourseCompleted) "Course complete" else "Continue",
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = when {
                        resume == null -> "No lesson is available yet."
                        overview.isCourseCompleted -> "You have completed every unit. Review any unit or restart from the first lesson."
                        else -> "${resume.unitTitle} · ${resume.articleTitle}"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
            items(overview.units, key = { it.unit.id }) { progress ->
                UnitProgressCard(
                    progress = progress,
                    onClick = { onOpenUnit(progress.unit.id) },
                )
            }
        }
    }
}

@Composable
private fun UnitProgressCard(
    progress: UnitProgress,
    onClick: () -> Unit,
) {
    TypeMiniSurfaceCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = progress.unit.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            SectionPill(
                text = if (progress.isCompleted) "Complete" else "${progress.completedArticles}/${progress.totalArticles}",
            )
        }
        Text(
            text = progress.unit.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = progress.unit.difficultyLabel,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
