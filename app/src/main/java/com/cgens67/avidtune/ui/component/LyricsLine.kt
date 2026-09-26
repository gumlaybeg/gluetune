package com.cgens67.gluetune.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cgens67.gluetune.LocalPlayerConnection
import com.cgens67.gluetune.constants.AppleMusicLyricsBlurKey
import com.cgens67.gluetune.constants.DisableBlurKey
import com.cgens67.gluetune.lyrics.LyricsEntry
import com.cgens67.gluetune.lyrics.WordTimestamp
import com.cgens67.gluetune.playback.PlayerConnection
import com.cgens67.gluetune.ui.screens.settings.LyricsPosition
import com.cgens67.gluetune.utils.rememberPreference
import kotlinx.coroutines.isActive

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LyricsLine(
    entry: LyricsEntry,
    romanizedText: String? = null,
    isSynced: Boolean,
    isActive: Boolean,
    distanceFromCurrent: Int,
    lyricsTextPosition: LyricsPosition,
    textColor: Color,
    textSize: Float,
    lineSpacing: Float,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isSelected: Boolean,
    isSelectionModeActive: Boolean,
    isAutoScrollActive: Boolean,
    animateLyrics: Boolean = true,
    lyricsOffset: Long = 0L,
    currentSkipSegments: List<Pair<Long, Long>> = emptyList(),
    sponsorBlockEnabled: Boolean = false,
    modifier: Modifier = Modifier
) {
    val (appleMusicLyricsBlur) = rememberPreference(AppleMusicLyricsBlurKey, true)
    val (disableBlur) = rememberPreference(DisableBlurKey, false)
    val playerConnection = LocalPlayerConnection.current ?: return

    val blurRadius by animateFloatAsState(
        targetValue = if (disableBlur || !appleMusicLyricsBlur || !isAutoScrollActive || isActive || !isSynced || isSelectionModeActive)
            0f
        else
            6f,
        animationSpec = if (animateLyrics) tween(durationMillis = 600) else snap(),
        label = "blur"
    )

    val animatedScale by animateFloatAsState(
        targetValue = when {
            !isSynced || isActive -> 1.05f
            distanceFromCurrent == 1 -> 1f
            else -> 0.95f
        },
        animationSpec = if (animateLyrics) tween(durationMillis = 400) else snap(),
        label = "scale"
    )

    val animatedAlpha by animateFloatAsState(
        targetValue = when {
            !isSynced || (isSelectionModeActive && isSelected) -> 1f
            isActive -> 1f
            distanceFromCurrent == 1 -> 0.7f
            distanceFromCurrent == 2 -> 0.4f
            else -> 0.2f
        },
        animationSpec = if (animateLyrics) tween(durationMillis = 400) else snap(),
        label = "alpha"
    )

    val agentAlignment = when {
        entry.agent == "v1" -> Alignment.Start
        entry.agent == "v2" -> Alignment.End
        entry.agent == "v1000" -> Alignment.CenterHorizontally
        else -> when (lyricsTextPosition) {
            LyricsPosition.LEFT -> Alignment.Start
            LyricsPosition.CENTER -> Alignment.CenterHorizontally
            LyricsPosition.RIGHT -> Alignment.End
        }
    }

    val agentTextAlign = when {
        entry.agent == "v1" -> TextAlign.Left
        entry.agent == "v2" -> TextAlign.Right
        entry.agent == "v1000" -> TextAlign.Center
        else -> when (lyricsTextPosition) {
            LyricsPosition.LEFT -> TextAlign.Left
            LyricsPosition.CENTER -> TextAlign.Center
            LyricsPosition.RIGHT -> TextAlign.Right
        }
    }

    val itemModifier = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .combinedClickable(
            enabled = true,
            onClick = onClick,
            onLongClick = onLongClick
        )
        .background(
            if (isSelected && isSelectionModeActive)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            else Color.Transparent
        )
        .padding(horizontal = 24.dp, vertical = lineSpacing.dp)
        .graphicsLayer {
            this.alpha = animatedAlpha
            this.scaleX = animatedScale
            this.scaleY = animatedScale
        }
        .then(if (blurRadius > 0.01f) Modifier.blur(blurRadius.dp) else Modifier)

    Column(
        modifier = itemModifier,
        horizontalAlignment = agentAlignment
    ) {
        // BACKING VOCALS FIX:
        // Backing vocal lines (entry.isBackground) now highlight to 1.0f when isActive is true!
        val targetAlpha = when {
            !isSynced -> 1f
            isActive -> 1f
            entry.isBackground -> 0.35f
            isAutoScrollActive -> when (distanceFromCurrent) {
                1 -> 0.35f
                2 -> 0.25f
                3 -> 0.18f
                4 -> 0.12f
                else -> 0.08f
            }
            else -> 0.2f
        }

        val lineAlpha by animateFloatAsState(targetAlpha, if (animateLyrics) tween(250) else snap(), label = "lyricsLineAlpha")
        val lineColor = textColor.copy(alpha = lineAlpha)

        val mainText = if (entry.isBackground && !entry.text.startsWith("(")) "(${entry.text})" else entry.text

        val lyricStyle = TextStyle(
            fontSize = if (entry.isBackground) (textSize * 0.85f).sp else textSize.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = if (entry.isBackground) FontStyle.Italic else FontStyle.Normal,
            lineHeight = if (entry.isBackground) (textSize * 0.85f * 1.3f).sp else (textSize * 1.3f).sp,
            letterSpacing = (-0.5).sp,
            textAlign = agentTextAlign,
            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
            platformStyle = PlatformTextStyle(includeFontPadding = false),
            lineHeightStyle = LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Center,
                trim = LineHeightStyle.Trim.Both
            )
        )

        val effectiveWords = if (entry.words?.isNotEmpty() == true) {
            entry.words
        } else null

        val isTracking = isActive || distanceFromCurrent <= 2

        if (isSynced && effectiveWords != null && isTracking && mainText.isNotBlank()) {
            WordLevelLyrics(
                mainText = mainText,
                words = effectiveWords,
                isTracking = isTracking,
                lyricsOffset = lyricsOffset,
                playerConnection = playerConnection,
                lyricStyle = lyricStyle,
                lineColor = lineColor,
                expressiveAccent = textColor,
                alignment = agentTextAlign,
                entryTime = entry.time,
                currentSkipSegments = currentSkipSegments,
                sponsorBlockEnabled = sponsorBlockEnabled
            )
        } else {
            if (isActive && isSynced) {
                val fillProgress = remember { Animatable(if (animateLyrics) 0f else 1f) }

                LaunchedEffect(entry.time, animateLyrics) {
                    fillProgress.snapTo(if (animateLyrics) 0f else 1f)
                    if (animateLyrics) {
                        fillProgress.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(
                                durationMillis = 800,
                                easing = FastOutSlowInEasing
                            )
                        )
                    }
                }

                val fill = fillProgress.value
                val glowBrush = Brush.horizontalGradient(
                    0.0f to textColor,
                    fill to textColor,
                    (fill + 0.08f).coerceIn(0f, 1f) to lineColor,
                    1.0f to lineColor
                )

                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                brush = glowBrush,
                                shadow = Shadow(
                                    color = textColor.copy(alpha = 0.6f * fill),
                                    offset = Offset(0f, 0f),
                                    blurRadius = 16f
                                )
                            )
                        ) {
                            append(mainText)
                        }
                    },
                    style = lyricStyle,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = mainText,
                    style = lyricStyle.copy(color = lineColor),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (!romanizedText.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = romanizedText,
                style = lyricStyle.copy(
                    fontSize = (textSize * 0.75f).sp,
                    lineHeight = (textSize * 0.75f * 1.3f).sp,
                    fontStyle = FontStyle.Italic
                ),
                color = lineColor.copy(alpha = lineAlpha * 0.8f),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * High-performance, 120 FPS word-level karaoke text drawing.
 * Renders base line once in GPU, then clips and paints the active portion smoothly.
 */
@Composable
private fun WordLevelLyrics(
    mainText: String,
    words: List<WordTimestamp>,
    isTracking: Boolean,
    lyricsOffset: Long,
    playerConnection: PlayerConnection,
    lyricStyle: TextStyle,
    lineColor: Color,
    expressiveAccent: Color,
    alignment: TextAlign,
    entryTime: Long,
    currentSkipSegments: List<Pair<Long, Long>> = emptyList(),
    sponsorBlockEnabled: Boolean = false
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    var smoothPosition by remember { mutableLongStateOf(entryTime + lyricsOffset) }

    LaunchedEffect(isTracking, currentSkipSegments, sponsorBlockEnabled) {
        if (isTracking) {
            var lastPlayerPos = playerConnection.player.currentPosition
            var lastUpdateTime = System.currentTimeMillis()
            while (isActive) {
                withFrameMillis {
                    val now = System.currentTimeMillis()
                    val playerPos = playerConnection.player.currentPosition
                    if (playerPos != lastPlayerPos) {
                        lastPlayerPos = playerPos
                        lastUpdateTime = now
                    }
                    val elapsed = now - lastUpdateTime
                    val currentVideoPos = lastPlayerPos + (if (playerConnection.player.isPlaying) elapsed else 0)

                    var sponsorBlockOffset = 0L
                    if (sponsorBlockEnabled) {
                        for (segment in currentSkipSegments) {
                            if (currentVideoPos >= segment.second) {
                                sponsorBlockOffset += (segment.second - segment.first)
                            } else if (currentVideoPos > segment.first) {
                                sponsorBlockOffset += (currentVideoPos - segment.first)
                            }
                        }
                    }

                    smoothPosition = currentVideoPos - sponsorBlockOffset + lyricsOffset
                }
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val maxWidthPx = constraints.maxWidth
        val layoutResult = remember(mainText, maxWidthPx, lyricStyle) {
            textMeasurer.measure(
                text = mainText,
                style = lyricStyle,
                constraints = Constraints(minWidth = maxWidthPx, maxWidth = maxWidthPx),
                softWrap = true
            )
        }

        // Cache character start & end offsets for words
        val wordCharSpans = remember(mainText, words) {
            var searchStart = 0
            words.map { word ->
                val cleanWord = word.text.trim().removePrefix("(").removeSuffix(")")
                val startIndex = if (cleanWord.isNotEmpty()) mainText.indexOf(cleanWord, searchStart).takeIf { it != -1 } ?: searchStart else searchStart
                val endIndex = (startIndex + cleanWord.length).coerceAtMost(mainText.length)
                if (cleanWord.isNotEmpty() && startIndex != -1) {
                    searchStart = endIndex
                }
                startIndex to endIndex
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height((layoutResult.size.height / density.density).dp)
        ) {
            if (mainText.isEmpty()) return@Canvas

            val currentSmooth = smoothPosition

            // 1. Draw inactive background text in ONE draw call
            drawText(layoutResult, color = lineColor)

            // 2. Draw active sung portions with hardware clipRect
            words.forEachIndexed { idx, word ->
                val startMs = (word.startTime * 1000).toLong()
                val endMs = (word.endTime * 1000).toLong()
                val isCompleted = currentSmooth >= endMs
                val isActiveWord = currentSmooth in startMs..endMs

                if (isCompleted || isActiveWord) {
                    val progress = if (isCompleted) 1f
                    else ((currentSmooth - startMs).toFloat() / (endMs - startMs).coerceAtLeast(1L)).coerceIn(0f, 1f)

                    val (startChar, endChar) = wordCharSpans[idx]
                    if (startChar < endChar && endChar <= mainText.length) {
                        val startBox = layoutResult.getBoundingBox(startChar)
                        val endBox = layoutResult.getBoundingBox((endChar - 1).coerceAtLeast(startChar))

                        if (startBox.top == endBox.top) {
                            val wordLeft = startBox.left
                            val wordRight = endBox.right
                            val currentRight = wordLeft + (wordRight - wordLeft) * progress

                            clipRect(
                                left = wordLeft,
                                top = startBox.top,
                                right = currentRight,
                                bottom = startBox.bottom
                            ) {
                                drawText(layoutResult, color = expressiveAccent)
                            }
                        } else {
                            val firstLineRight = layoutResult.getLineRight(layoutResult.getLineForOffset(startChar))
                            val currentRight = startBox.left + (firstLineRight - startBox.left) * progress
                            clipRect(left = startBox.left, top = startBox.top, right = currentRight, bottom = endBox.bottom) {
                                drawText(layoutResult, color = expressiveAccent)
                            }
                        }
                    }
                }
            }
        }
    }
}
