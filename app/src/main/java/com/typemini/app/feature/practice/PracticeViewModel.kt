package com.typemini.app.feature.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.typemini.app.data.repository.PracticeRepository
import com.typemini.app.domain.model.CompletionNextDestination
import com.typemini.app.domain.model.PracticeResultDraft
import com.typemini.app.domain.session.applyCharacterToSession
import com.typemini.app.domain.session.buildTypingMetrics
import com.typemini.app.domain.session.createTypingSession
import com.typemini.app.domain.session.removeLastInputFromSession
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max

class PracticeViewModel(
    private val repository: PracticeRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PracticeUiState())
    val uiState: StateFlow<PracticeUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        val units = repository.getPracticeUnits()
        val initialUnit = units.firstOrNull()
        val initialArticle = initialUnit?.articles?.firstOrNull()
        _uiState.value = PracticeUiState(
            units = units,
            activeUnitId = initialUnit?.id.orEmpty(),
            activeArticleId = initialArticle?.id.orEmpty(),
            session = createTypingSession(initialArticle?.content.orEmpty()),
        )
    }

    fun restart() {
        resetSession()
    }

    fun loadArticle(unitId: String, articleId: String) {
        val current = _uiState.value
        if (current.activeUnitId == unitId && current.activeArticleId == articleId) {
            return
        }
        resetSession(nextUnitId = unitId, nextArticleId = articleId)
    }

    fun updateMode(mode: com.typemini.app.domain.model.PracticeMode) {
        resetSession(nextMode = mode)
    }

    fun consumeCompletedResult() {
        _uiState.update { it.copy(completedResultId = null) }
    }

    fun clearResultAction() {
        _uiState.update {
            it.copy(
                resultActionResultId = null,
                nextDestination = null,
            )
        }
    }

    fun onInput(input: String): PracticeInputFeedback {
        var feedback = PracticeInputFeedback.Ignored

        input.forEach { char ->
            feedback = feedback.mergeWith(processCharacter(char))
            if (_uiState.value.session.isFinished) {
                return feedback
            }
        }

        return feedback
    }

    fun onBackspace(): PracticeInputFeedback {
        val current = _uiState.value
        val nextSession = removeLastInputFromSession(current.session)
        if (nextSession == current.session) {
            return PracticeInputFeedback.Ignored
        }

        _uiState.update {
            it.copy(
                session = nextSession,
                finishedAtMillis = if (nextSession.isFinished) it.finishedAtMillis else null,
                isSaving = false,
                completedResultId = null,
                resultActionResultId = null,
                nextDestination = null,
            )
        }

        return PracticeInputFeedback.Correct
    }

    private fun processCharacter(char: Char): PracticeInputFeedback {
        val current = _uiState.value
        if (current.activeArticle == null || current.activeUnit == null) return PracticeInputFeedback.Ignored

        val now = System.currentTimeMillis()
        val nextStartedAt = current.startedAtMillis ?: if (char != ' ') now else null
        val nextSession = applyCharacterToSession(current.session, char, current.mode)

        if (nextSession == current.session) {
            if (current.startedAtMillis == null && nextStartedAt != null) {
                _uiState.update { it.copy(startedAtMillis = nextStartedAt) }
                startTimer(nextStartedAt)
            }
            return PracticeInputFeedback.Ignored
        }

        val elapsedMillis = if (nextStartedAt != null) {
            now - nextStartedAt
        } else {
            current.elapsedMillis
        }

        _uiState.update {
            it.copy(
                session = nextSession,
                startedAtMillis = nextStartedAt,
                finishedAtMillis = if (nextSession.isFinished) now else null,
                elapsedMillis = elapsedMillis,
            )
        }

        if (current.startedAtMillis == null && nextStartedAt != null) {
            startTimer(nextStartedAt)
        }

        if (nextSession.isFinished && !current.session.isFinished) {
            timerJob?.cancel()
            persistCompletion(now)
        }

        return if (nextSession.errorKeystrokes > current.session.errorKeystrokes) {
            PracticeInputFeedback.Error
        } else {
            PracticeInputFeedback.Correct
        }
    }

    private fun persistCompletion(finishedAt: Long) {
        val state = _uiState.value
        val activeUnit = state.activeUnit ?: return
        val activeArticle = state.activeArticle ?: return
        val elapsedSeconds = max(state.elapsedMillis / 1000.0, 1.0)
        val metrics = buildTypingMetrics(state.session, elapsedSeconds)

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val resultId = repository.savePracticeResult(
                PracticeResultDraft(
                    unitId = activeUnit.id,
                    unitTitle = activeUnit.title,
                    articleId = activeArticle.id,
                    articleTitle = activeArticle.title,
                    articleOrder = activeArticle.order,
                    mode = state.mode,
                    correctKeystrokes = state.session.correctKeystrokes,
                    errorKeystrokes = state.session.errorKeystrokes,
                    elapsedSeconds = elapsedSeconds,
                    wpm = metrics.wpm,
                    accuracy = metrics.accuracy,
                    score = metrics.score,
                    createdAt = finishedAt,
                ),
            )
            val nextDestination = repository.getCompletionNextDestination(
                unitId = activeUnit.id,
                articleId = activeArticle.id,
            )
            _uiState.update {
                it.copy(
                    isSaving = false,
                    completedResultId = resultId,
                    resultActionResultId = resultId,
                    nextDestination = nextDestination,
                )
            }
        }
    }

    private fun resetSession(
        nextUnitId: String = _uiState.value.activeUnitId,
        nextArticleId: String = _uiState.value.activeArticleId,
        nextMode: com.typemini.app.domain.model.PracticeMode = _uiState.value.mode,
    ) {
        timerJob?.cancel()

        val units = _uiState.value.units
        val nextUnit = units.firstOrNull { it.id == nextUnitId } ?: units.firstOrNull()
        val nextArticle = nextUnit?.articles?.firstOrNull { it.id == nextArticleId } ?: nextUnit?.articles?.firstOrNull()
        _uiState.update {
            it.copy(
                activeUnitId = nextUnit?.id.orEmpty(),
                activeArticleId = nextArticle?.id.orEmpty(),
                mode = nextMode,
                session = createTypingSession(nextArticle?.content.orEmpty()),
                startedAtMillis = null,
                finishedAtMillis = null,
                elapsedMillis = 0,
                isSaving = false,
                completedResultId = null,
                resultActionResultId = null,
                nextDestination = null,
            )
        }
    }

    private fun startTimer(startedAtMillis: Long) {
        if (timerJob?.isActive == true) return

        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(100)
                _uiState.update { state ->
                    if (state.startedAtMillis != null && state.finishedAtMillis == null) {
                        state.copy(elapsedMillis = System.currentTimeMillis() - startedAtMillis)
                    } else {
                        state
                    }
                }
            }
        }
    }

    companion object {
        fun factory(repository: PracticeRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PracticeViewModel(repository)
            }
        }
    }
}

enum class PracticeInputFeedback {
    Correct,
    Error,
    Ignored,
}

private fun PracticeInputFeedback.mergeWith(other: PracticeInputFeedback): PracticeInputFeedback {
    return when {
        this == PracticeInputFeedback.Error || other == PracticeInputFeedback.Error -> PracticeInputFeedback.Error
        this == PracticeInputFeedback.Correct || other == PracticeInputFeedback.Correct -> PracticeInputFeedback.Correct
        else -> PracticeInputFeedback.Ignored
    }
}
