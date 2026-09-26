package com.cgens67.gluetune.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
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
import java.text.BreakIterator

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

    // Depth-of-field progressive atmospheric blur
    val blurRadius by animateFloatAsState(
        targetValue = if (disableBlur || !appleMusicLyricsBlur || !isAutoScrollActive || isActive || !isSynced || isSelectionModeActive) {
            0f
        } else {
            when (distanceFromCurrent) {
                1 -> 2.5f
                2 -> 4.5f
                else -> 7f
            }
        },
        animationSpec = if (animateLyrics) tween(durationMillis = 500) else snap(),
        label = "blur"
    )

    val animatedScale by animateFloatAsState(
        targetValue = when {
            !isSynced || isActive -> 1.06f
            distanceFromCurrent == 1 -> 0.98f
            else -> 0.92f
        },
        animationSpec = if (animateLyrics) spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow) else snap(),
        label = "scale"
    )

    val animatedAlpha by animateFloatAsState(
        targetValue = when {
            !isSynced -> 1f
            isSelectionModeActive && isSelected -> 1f
            isActive -> 1f
            distanceFromCurrent == 1 -> 0.65f
            distanceFromCurrent == 2 -> 0.38f
            else -> 0.18f
        },
        animationSpec = if (animateLyrics) tween(durationMillis = 350) else snap(),
        label = "alpha"
    )

    val agentAlignment = when {
        entry.agent == "v1" -> Alignment.Start
        entry.agent == "v2" -> Alignment.End
        entry.agent == "v1000" -> Alignment.CenterHorizontally
        entry.isBackground -> Alignment.End
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
        entry.isBackground -> TextAlign.Right
        else -> when (lyricsTextPosition) {
            LyricsPosition.LEFT -> TextAlign.Left
            LyricsPosition.CENTER -> TextAlign.Center
            LyricsPosition.RIGHT -> TextAlign.Right
        }
    }

    val itemModifier = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .combinedClickable(
            enabled = true,
            onClick = onClick,
            onLongClick = onLongClick
        )
        .background(
            if (isSelected && isSelectionModeActive) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
            } else Color.Transparent
        )
        .padding(horizontal = 16.dp, vertical = lineSpacing.dp)
        .graphicsLayer {
            this.alpha = animatedAlpha
            this.scaleX = animatedScale
            this.scaleY = animatedScale
        }
        .then(if (blurRadius > 0.1f) Modifier.blur(blurRadius.dp) else Modifier)

    Column(
        modifier = itemModifier,
        horizontalAlignment = agentAlignment
    ) {
        val mainText = if (entry.isBackground) entry.text.removePrefix("(").removeSuffix(")") else entry.text

        // Tag label for background vocal lines
        if (entry.isBackground) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = textColor.copy(alpha = 0.15f),
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Text(
                    text = "Backing",
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.75f),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        val lyricStyle = TextStyle(
            fontSize = if (entry.isBackground) (textSize * 0.78f).sp else textSize.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = if (entry.isBackground) FontStyle.Italic else FontStyle.Normal,
            lineHeight = if (entry.isBackground) (textSize * 0.78f * 1.35f).sp else (textSize * 1.35f).sp,
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
        } else if (mainText.isNotBlank()) {
            remember(mainText, entry.time) {
                val splitWords = mainText.split(Regex("\\s+")).filter { it.isNotBlank() }
                val wordDurationSec = 0.22
                val wordStaggerSec = 0.04
                val startTimeSec = entry.time / 1000.0
                splitWords.mapIndexed { idx, wordText ->
                    WordTimestamp(
                        text = wordText,
                        startTime = startTimeSec + (idx * wordStaggerSec),
                        endTime = startTimeSec + (idx * wordStaggerSec) + wordDurationSec,
                        hasTrailingSpace = idx < splitWords.size - 1
                    )
                }
            }
        } else null

        val isTracking = isActive || distanceFromCurrent <= 2

        if (isSynced && effectiveWords != null && isTracking && mainText.isNotBlank()) {
            FluidWordKaraoke(
                mainText = mainText,
                words = effectiveWords,
                isTracking = isTracking,
                lyricsOffset = lyricsOffset,
                playerConnection = playerConnection,
                lyricStyle = lyricStyle,
                expressiveAccent = textColor,
                alignment = agentTextAlign,
                entryTime = entry.time,
                animateLyrics = animateLyrics,
                currentSkipSegments = currentSkipSegments,
                sponsorBlockEnabled = sponsorBlockEnabled
            )
        } else {
            Text(
                text = mainText,
                style = lyricStyle.copy(color = textColor.copy(alpha = if (isActive) 1f else 0.45f)),
                modifier = Modifier.fillMaxWidth()
            )
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
                color = textColor.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun FluidWordKaraoke(
    mainText: String,
    words: List<WordTimestamp>,
    isTracking: Boolean,
    lyricsOffset: Long,
    playerConnection: PlayerConnection,
    lyricStyle: TextStyle,
    expressiveAccent: Color,
    alignment: TextAlign,
    entryTime: Long,
    animateLyrics: Boolean = true,
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

    val graphemeClusters = remember(mainText) { mainText.toGraphemeClusters() }
    val clusterCount = graphemeClusters.size
    val clusterCharOffsets = remember(mainText) {
        IntArray(clusterCount).also { offsets ->
            var charOffset = 0
            graphemeClusters.forEachIndexed { i, cluster ->
                offsets[i] = charOffset
                charOffset += cluster.length
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

        val charBoundsList = remember(layoutResult, clusterCharOffsets) {
            Array(clusterCount) { i -> layoutResult.getBoundingBox(clusterCharOffsets[i]) }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height((layoutResult.size.height / density.density).dp)
                .graphicsLayer(clip = false)
        ) {
            if (mainText.isEmpty()) return@Canvas

            // 1. Draw Inactive / Ghost Base Text
            drawText(layoutResult, color = expressiveAccent.copy(alpha = 0.32f))

            // 2. Active Karaoke Progressive Reveal with Fluid Gradient Sweep
            val currentSmooth = smoothPosition

            words.forEach { word ->
                val startMs = (word.startTime * 1000).toLong()
                val endMs = (word.endTime * 1000).toLong()
                val isCompleted = currentSmooth >= endMs
                val isActive = currentSmooth in startMs..endMs

                if (isCompleted || isActive) {
                    val progress = if (isCompleted) 1f else ((currentSmooth - startMs).toFloat() / (endMs - startMs).coerceAtLeast(1L)).coerceIn(0f, 1f)

                    // Find text bounds corresponding to this word in mainText
                    val cleanText = word.text.trim()
                    val wordIndex = mainText.indexOf(cleanText)
                    if (wordIndex != -1) {
                        val wordEnd = (wordIndex + cleanText.length).coerceAtMost(mainText.length)
                        val startCluster = clusterCharOffsets.indexOfFirst { it >= wordIndex }.coerceAtLeast(0)
                        val endCluster = clusterCharOffsets.indexOfLast { it < wordEnd }.coerceAtLeast(startCluster)

                        if (startCluster in charBoundsList.indices && endCluster in charBoundsList.indices) {
                            val left = charBoundsList[startCluster].left
                            val right = charBoundsList[endCluster].right
                            val top = charBoundsList[startCluster].top
                            val bottom = charBoundsList[endCluster].bottom
                            val currentRight = left + (right - left) * progress

                            clipRect(left = left, top = top, right = currentRight, bottom = bottom) {
                                drawText(layoutResult, color = expressiveAccent)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun String.toGraphemeClusters(): List<String> {
    if (isEmpty()) return emptyList()
    val result = mutableListOf<String>()
    val it = BreakIterator.getCharacterInstance()
    it.setText(this)
    var start = it.first()
    var end = it.next()
    while (end != BreakIterator.DONE) {
        result.add(substring(start, end))
        start = end
        end = it.next()
    }
    return result
}
