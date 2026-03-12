package com.typemini.app.feature.practice

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.typemini.app.R
import com.typemini.app.domain.session.TypingSession

private val LetterKeyboardRows = listOf(
    "qwertyuiop".toList(),
    "asdfghjkl".toList(),
    "zxcvbnm".toList(),
)

private val SymbolKeyboardRows = listOf(
    "1234567890".toList(),
    listOf('@', '#', '$', '&', '-', '+', '(', ')', '/'),
    listOf('%', '"', '\'', ':', ';', '!', '?'),
)

private enum class KeyboardLayer {
    Letters,
    Symbols,
}

@Composable
fun PracticeRoute(
    viewModel: PracticeViewModel,
    unitId: String,
    articleId: String,
    onResultReady: (Long) -> Unit,
    onBackToUnit: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(unitId, articleId) {
        viewModel.loadArticle(unitId, articleId)
    }

    LaunchedEffect(uiState.completedResultId) {
        uiState.completedResultId?.let { resultId ->
            onResultReady(resultId)
            viewModel.consumeCompletedResult()
        }
    }

    PracticeScreen(
        state = uiState,
        onRestart = viewModel::restart,
        onInput = viewModel::onInput,
        onBackspace = viewModel::onBackspace,
        onBackToUnit = onBackToUnit,
        onOpenHistory = onOpenHistory,
        onOpenSettings = onOpenSettings,
    )
}

@Composable
fun PracticeScreen(
    state: PracticeUiState,
    onRestart: () -> Unit,
    onInput: (String) -> PracticeInputFeedback,
    onBackspace: () -> PracticeInputFeedback,
    onBackToUnit: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val activeArticle = state.activeArticle ?: return
    val activeUnit = state.activeUnit ?: return
    val soundPlayer = rememberPracticeSoundPlayer(context)
    var keyboardLayer by remember(state.activeArticleId) { mutableStateOf(KeyboardLayer.Letters) }
    var isShiftEnabled by remember(state.activeArticleId) { mutableStateOf(false) }

    LaunchedEffect(state.activeArticleId, state.mode) {
        keyboardController?.hide()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        PracticeTopBar(
            modifier = Modifier.padding(horizontal = 12.dp),
            title = activeArticle.title,
            subtitle = "${activeUnit.title} · Article ${activeArticle.order}",
            onBackToUnit = onBackToUnit,
            onRestart = onRestart,
            onOpenHistory = onOpenHistory,
            onOpenSettings = onOpenSettings,
        )

        Box(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .fillMaxWidth()
                .weight(1f)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.30f),
                    shape = RoundedCornerShape(28.dp),
                )
                .padding(horizontal = 12.dp, vertical = 14.dp),
        ) {
            TypingViewport(
                state = state,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        PracticeKeyboard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .padding(bottom = 10.dp),
            keyboardLayer = keyboardLayer,
            isShiftEnabled = isShiftEnabled,
            waitingForSpace = state.session.waitingForSpace,
            onLetterPress = { letter ->
                val emittedLetter = if (keyboardLayer == KeyboardLayer.Letters && isShiftEnabled) {
                    letter.uppercaseChar()
                } else {
                    letter
                }
                when (onInput(emittedLetter.toString())) {
                    PracticeInputFeedback.Correct -> soundPlayer.playCorrect()
                    PracticeInputFeedback.Error -> soundPlayer.playError()
                    PracticeInputFeedback.Ignored -> Unit
                }
                if (keyboardLayer == KeyboardLayer.Letters && isShiftEnabled && letter.isLetter()) {
                    isShiftEnabled = false
                }
            },
            onShiftPress = {
                soundPlayer.playCorrect()
                keyboardLayer = KeyboardLayer.Letters
                isShiftEnabled = !isShiftEnabled
            },
            onBackspacePress = {
                when (onBackspace()) {
                    PracticeInputFeedback.Correct -> soundPlayer.playCorrect()
                    PracticeInputFeedback.Error -> soundPlayer.playError()
                    PracticeInputFeedback.Ignored -> Unit
                }
            },
            onSymbolToggle = {
                soundPlayer.playCorrect()
                keyboardLayer = if (keyboardLayer == KeyboardLayer.Letters) {
                    KeyboardLayer.Symbols
                } else {
                    KeyboardLayer.Letters
                }
                isShiftEnabled = false
            },
            onSpacePress = {
                when (onInput(" ")) {
                    PracticeInputFeedback.Correct -> soundPlayer.playCorrect()
                    PracticeInputFeedback.Error -> soundPlayer.playError()
                    PracticeInputFeedback.Ignored -> Unit
                }
            },
        )
    }
}

@Composable
private fun PracticeTopBar(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    onBackToUnit: () -> Unit,
    onRestart: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier
                .width(94.dp)
                .height(44.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            onClick = onBackToUnit,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back to unit",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Unit",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        Box {
            IconButton(
                onClick = { menuExpanded = true },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.88f),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Icon(
                    imageVector = Icons.Outlined.MoreHoriz,
                    contentDescription = "Open practice menu",
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text("重新开始") },
                    onClick = {
                        menuExpanded = false
                        onRestart()
                    },
                )
                DropdownMenuItem(
                    text = { Text("历史") },
                    onClick = {
                        menuExpanded = false
                        onOpenHistory()
                    },
                )
                DropdownMenuItem(
                    text = { Text("设置") },
                    onClick = {
                        menuExpanded = false
                        onOpenSettings()
                    },
                )
            }
        }
    }
}

@Composable
private fun TypingViewport(
    state: PracticeUiState,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val tokenStyle = MaterialTheme.typography.headlineMedium.copy(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 242.dp),
    ) {
        val availableWidthPx = with(density) { maxWidth.roundToPx() }
        val tokenSpacingPx = with(density) { 14.dp.roundToPx() }
        val characterSpacingPx = with(density) { 1.dp.roundToPx() }
        val characterHorizontalPaddingPx = with(density) { 1.dp.roundToPx() }
        val tokenLines = remember(
            state.session.tokens,
            availableWidthPx,
            tokenSpacingPx,
            characterSpacingPx,
            characterHorizontalPaddingPx,
            tokenStyle,
        ) {
            val tokenWidths = state.session.tokens.map { token ->
                token.sumOf { character ->
                    textMeasurer.measure(
                        text = AnnotatedString(character.toString()),
                        style = tokenStyle,
                    ).size.width + characterHorizontalPaddingPx * 2
                } + (token.length - 1).coerceAtLeast(0) * characterSpacingPx
            }

            buildPracticeLines(
                tokenWidths = tokenWidths,
                maxWidth = availableWidthPx,
                tokenSpacing = tokenSpacingPx,
            )
        }
        val viewport = remember(tokenLines, state.session.activeTokenIndex) {
            resolvePracticeViewport(
                lines = tokenLines,
                activeTokenIndex = state.session.activeTokenIndex,
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            repeat(4) { slotIndex ->
                val tokenIndices = viewport.visibleLines.getOrNull(slotIndex)
                if (tokenIndices == null) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                    )
                } else {
                    PracticeLine(
                        tokenIndices = tokenIndices,
                        session = state.session,
                    )
                }
            }
        }
    }
}

@Composable
private fun PracticeLine(
    tokenIndices: List<Int>,
    session: TypingSession,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
        ) {
            tokenIndices.forEachIndexed { position, tokenIndex ->
                PracticeToken(
                    tokenIndex = tokenIndex,
                    session = session,
                )

                val showInlineIndicator = position < tokenIndices.lastIndex
                if (showInlineIndicator) {
                    SpaceAdvanceIndicator(
                        highlighted = tokenIndex == session.activeTokenIndex && session.waitingForSpace,
                    )
                }
            }

            val endsWithWaitingToken = tokenIndices.lastOrNull() == session.activeTokenIndex &&
                session.waitingForSpace &&
                session.activeTokenIndex < session.tokens.lastIndex
            if (endsWithWaitingToken) {
                SpaceAdvanceIndicator(highlighted = true)
            }
        }
    }
}

@Composable
private fun PracticeToken(
    tokenIndex: Int,
    session: TypingSession,
) {
    val token = session.tokens[tokenIndex]
    val isFinishedToken = tokenIndex < session.activeTokenIndex ||
        (session.isFinished && tokenIndex == session.activeTokenIndex)
    val isActiveToken = tokenIndex == session.activeTokenIndex && !session.isFinished

    Row(
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        token.forEachIndexed { charIndex, character ->
            val isCorrect = isFinishedToken || (tokenIndex == session.activeTokenIndex && charIndex < session.activeCharIndex)
            val isCurrent = isActiveToken &&
                charIndex == session.activeCharIndex &&
                !session.waitingForSpace
            val isWrong = isCurrent && session.hasError

            val textColor = when {
                isCorrect -> MaterialTheme.colorScheme.onSurface
                isWrong -> MaterialTheme.colorScheme.error
                isCurrent -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.42f)
            }

            val backgroundColor = when {
                isWrong -> MaterialTheme.colorScheme.errorContainer
                isCurrent -> MaterialTheme.colorScheme.primaryContainer
                else -> Color.Transparent
            }

            Text(
                text = character.toString(),
                modifier = Modifier
                    .background(
                        color = backgroundColor,
                        shape = RoundedCornerShape(10.dp),
                    )
                    .padding(horizontal = 1.dp),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                ),
                color = textColor,
            )
        }
    }
}

@Composable
private fun SpaceAdvanceIndicator(
    highlighted: Boolean,
) {
    Box(
        modifier = Modifier
            .width(14.dp)
            .padding(start = 2.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Box(
            modifier = Modifier
                .width(10.dp)
                .height(2.dp)
                .background(
                    color = if (highlighted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.Transparent
                    },
                    shape = RoundedCornerShape(999.dp),
                ),
        )
    }
}

@Composable
private fun PracticeKeyboard(
    keyboardLayer: KeyboardLayer,
    isShiftEnabled: Boolean,
    waitingForSpace: Boolean,
    onLetterPress: (Char) -> Unit,
    onShiftPress: () -> Unit,
    onBackspacePress: () -> Unit,
    onSymbolToggle: () -> Unit,
    onSpacePress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.26f),
                shape = RoundedCornerShape(28.dp),
            )
            .padding(horizontal = 5.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        val rows = if (keyboardLayer == KeyboardLayer.Letters) LetterKeyboardRows else SymbolKeyboardRows

        rows.take(2).forEachIndexed { rowIndex, keys ->
            KeyboardLetterRow(
                keys = keys,
                isShiftEnabled = keyboardLayer == KeyboardLayer.Letters && isShiftEnabled,
                sidePaddingWeight = when (rowIndex) {
                    1 -> 0.34f
                    else -> 0f
                },
                onLetterPress = onLetterPress,
            )
        }

        KeyboardActionRow(
            keys = rows[2],
            keyboardLayer = keyboardLayer,
            isShiftEnabled = isShiftEnabled,
            onLetterPress = onLetterPress,
            onShiftPress = onShiftPress,
            onBackspacePress = onBackspacePress,
        )

        KeyboardBottomRow(
            keyboardLayer = keyboardLayer,
            waitingForSpace = waitingForSpace,
            onSymbolToggle = onSymbolToggle,
            onLetterPress = onLetterPress,
            onSpacePress = onSpacePress,
        )
    }
}

@Composable
private fun KeyboardLetterRow(
    keys: List<Char>,
    isShiftEnabled: Boolean,
    sidePaddingWeight: Float,
    onLetterPress: (Char) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (sidePaddingWeight > 0f) {
            Spacer(modifier = Modifier.weight(sidePaddingWeight))
        }

        keys.forEach { key ->
            KeyboardKey(
                text = if (isShiftEnabled) key.uppercaseChar().toString() else key.toString(),
                onClick = { onLetterPress(key) },
                modifier = Modifier.weight(1f),
            )
        }

        if (sidePaddingWeight > 0f) {
            Spacer(modifier = Modifier.weight(sidePaddingWeight))
        }
    }
}

@Composable
private fun KeyboardActionRow(
    keys: List<Char>,
    keyboardLayer: KeyboardLayer,
    isShiftEnabled: Boolean,
    onLetterPress: (Char) -> Unit,
    onShiftPress: () -> Unit,
    onBackspacePress: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        KeyboardKey(
            text = if (keyboardLayer == KeyboardLayer.Letters) {
                if (isShiftEnabled) "SHIFT ON" else "SHIFT"
            } else {
                "ABC"
            },
            onClick = onShiftPress,
            modifier = Modifier.weight(1.55f),
            textStyle = MaterialTheme.typography.labelLarge,
            containerColor = if (keyboardLayer == KeyboardLayer.Letters && isShiftEnabled) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            },
            contentColor = if (keyboardLayer == KeyboardLayer.Letters && isShiftEnabled) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )

        keys.forEach { key ->
            KeyboardKey(
                text = if (keyboardLayer == KeyboardLayer.Letters && isShiftEnabled) {
                    key.uppercaseChar().toString()
                } else {
                    key.toString()
                },
                onClick = { onLetterPress(key) },
                modifier = Modifier.weight(1f),
            )
        }

        KeyboardKey(
            text = "BACK",
            onClick = onBackspacePress,
            modifier = Modifier.weight(1.55f),
            textStyle = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun KeyboardBottomRow(
    keyboardLayer: KeyboardLayer,
    waitingForSpace: Boolean,
    onSymbolToggle: () -> Unit,
    onLetterPress: (Char) -> Unit,
    onSpacePress: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        KeyboardKey(
            text = if (keyboardLayer == KeyboardLayer.Letters) "123" else "ABC",
            onClick = onSymbolToggle,
            modifier = Modifier.weight(1.45f),
            textStyle = MaterialTheme.typography.labelLarge,
        )
        KeyboardKey(
            text = "'",
            onClick = { onLetterPress('\'') },
            modifier = Modifier.weight(1f),
        )
        KeyboardKey(
            text = ",",
            onClick = { onLetterPress(',') },
            modifier = Modifier.weight(1f),
        )
        KeyboardKey(
            text = "space",
            onClick = onSpacePress,
            modifier = Modifier.weight(4.2f),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = if (waitingForSpace) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
        KeyboardKey(
            text = ".",
            onClick = { onLetterPress('.') },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun KeyboardKey(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleMedium,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Surface(
        modifier = modifier.height(46.dp),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        onClick = onClick,
    ) {
        Box(
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = textStyle,
                color = contentColor,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun rememberPracticeSoundPlayer(
    context: Context,
): PracticeSoundPlayer {
    val appContext = context.applicationContext
    val soundPlayer = remember(appContext) { PracticeSoundPlayer(appContext) }

    DisposableEffect(soundPlayer) {
        onDispose {
            soundPlayer.release()
        }
    }

    return soundPlayer
}

private class PracticeSoundPlayer(
    context: Context,
) {
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()
    private val correctSoundId = soundPool.load(context, R.raw.correct, 1)
    private val missSoundId = soundPool.load(context, R.raw.miss, 1)

    fun playCorrect() {
        soundPool.play(correctSoundId, 1f, 1f, 0, 0, 1f)
    }

    fun playError() {
        soundPool.play(missSoundId, 1f, 1f, 1, 0, 1f)
    }

    fun release() {
        soundPool.release()
    }
}
