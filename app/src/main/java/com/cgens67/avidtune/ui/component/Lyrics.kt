package com.cgens67.gluetune.ui.component

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.navigation.NavController
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cgens67.gluetune.LocalDatabase
import com.cgens67.gluetune.LocalPlayerConnection
import com.cgens67.gluetune.R
import com.cgens67.gluetune.constants.*
import com.cgens67.gluetune.db.entities.LyricsEntity
import com.cgens67.gluetune.db.entities.LyricsEntity.Companion.LYRICS_NOT_FOUND
import com.cgens67.gluetune.lyrics.LyricsEntry
import com.cgens67.gluetune.lyrics.LyricsResult
import com.cgens67.gluetune.lyrics.LyricsUtils.findCurrentLineIndex
import com.cgens67.gluetune.lyrics.LyricsUtils.parseLyrics
import com.cgens67.gluetune.playback.PlayerConnection
import com.cgens67.gluetune.ui.menu.LyricsMenu
import com.cgens67.gluetune.ui.player.PlayerBackground
import com.cgens67.gluetune.ui.screens.settings.DarkMode
import com.cgens67.gluetune.ui.screens.settings.LyricsPosition
import com.cgens67.gluetune.ui.theme.PlayerColorExtractor
import com.cgens67.gluetune.ui.utils.fadingEdge
import com.cgens67.gluetune.utils.makeTimeString
import com.cgens67.gluetune.utils.rememberEnumPreference
import com.cgens67.gluetune.utils.rememberPreference
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.saket.squiggles.SquigglySlider
import kotlin.math.absoluteValue
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

const val ANIMATE_SCROLL_DURATION = 300L
val LyricsPreviewTime = 2.seconds

@RequiresApi(Build.VERSION_CODES.M)
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun Lyrics(
    sliderPositionProvider: () -> Long?,
    onNavigateBack: (() -> Unit)? = null,
    mediaMetadata: com.cgens67.gluetune.models.MediaMetadata? = null,
    onBackClick: () -> Unit = {},
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
    backgroundAlpha: () -> Float = { 1f },
    navController: NavController? = null,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val menuState = LocalMenuState.current
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val scope = rememberCoroutineScope()
    val database = LocalDatabase.current

    val isFullscreen = onNavigateBack != null
    val currentSkipSegments by playerConnection.currentSkipSegments.collectAsState()
    val sponsorBlockEnabled by playerConnection.sponsorBlockEnabled.collectAsState()

    val sliderStyle by rememberEnumPreference(SliderStyleKey, SliderStyle.DEFAULT)
    val lyricsTextPosition by rememberEnumPreference(LyricsTextPositionKey, LyricsPosition.CENTER)
    val changeLyrics by rememberPreference(LyricsClickKey, true)
    val scrollLyrics by rememberPreference(LyricsScrollKey, true)
    val animateLyrics by rememberPreference(AnimateLyricsKey, true)
    val disableBlur by rememberPreference(DisableBlurKey, false)
    val swipeThumbnail by rememberPreference(SwipeThumbnailKey, true)

    val currentMetadata = mediaMetadata ?: playerConnection.mediaMetadata.collectAsState().value
    val currentSongId = currentMetadata?.id

    var currentLineIndex by remember { mutableIntStateOf(-1) }
    var currentMainLineIndex by remember { mutableIntStateOf(-1) }
    var deferredCurrentMainLineIndex by remember(currentSongId) { mutableIntStateOf(0) }
    var previousMainLineIndex by remember(currentSongId) { mutableIntStateOf(0) }

    var lastPreviewTime by remember(currentSongId) { mutableLongStateOf(0L) }
    var isSeeking by remember { mutableStateOf(false) }
    var initialScrollDone by remember(currentSongId) { mutableStateOf(false) }
    var shouldScrollToFirstLine by remember(currentSongId) { mutableStateOf(true) }
    var isAppMinimized by rememberSaveable { mutableStateOf(false) }
    var sliderPosition by remember { mutableStateOf<Long?>(null) }

    var isAutoScrollEnabled by rememberSaveable { mutableStateOf(true) }

    var isSelectionModeActive by remember(currentSongId) { mutableStateOf(false) }
    val selectedIndices = remember(currentSongId) { mutableStateListOf<Int>() }
    var showMaxSelectionToast by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var shareDialogData by remember { mutableStateOf<Triple<String, String, String>?>(null) }

    val lazyListState = rememberLazyListState()
    var isAnimating by remember { mutableStateOf(false) }
    val maxSelectionLimit = 6

    val canSkipPrevious by playerConnection.canSkipPrevious.collectAsState()
    val canSkipNext by playerConnection.canSkipNext.collectAsState()

    var lyricsCache by remember { mutableStateOf<Map<String, LyricsEntity>>(emptyMap()) }
    var currentLyricsEntity by remember(currentSongId) {
        mutableStateOf<LyricsEntity?>(lyricsCache[currentSongId])
    }
    var isLoadingLyrics by remember(currentSongId) { mutableStateOf(false) }

    val rawLyricsEntity by playerConnection.currentLyrics.collectAsState(initial = null)
    val activeLyricsEntity = rawLyricsEntity ?: currentLyricsEntity

    val originalLyrics = remember(activeLyricsEntity) {
        var text = activeLyricsEntity?.lyrics?.trim()
        if (text != null && text.startsWith("[provider:")) {
            text = text.substringAfter('\n').trim()
        }
        text
    }

    val lyricsOffsetMs = remember(activeLyricsEntity) {
        val raw = activeLyricsEntity?.lyrics.orEmpty()
        Regex("\\[offset:(-?\\d+)\\]").find(raw)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
    }

    val lyricsProviderName = remember(activeLyricsEntity) {
        val text = activeLyricsEntity?.lyrics?.trim()
        if (text != null && text.startsWith("[provider:")) {
            text.substringBefore('\n').trim().removePrefix("[provider:").removeSuffix("]")
        } else null
    }

    val isSynced = remember(originalLyrics) {
        !originalLyrics.isNullOrEmpty() && "\\[\\d\\d:\\d\\d\\.\\d{2,3}\\]".toRegex().containsMatchIn(originalLyrics)
    }

    val lines = remember(originalLyrics) {
        if (originalLyrics.isNullOrEmpty() || originalLyrics == LYRICS_NOT_FOUND) {
            emptyList()
        } else if (isSynced) {
            val parsedLines = parseLyrics(originalLyrics)
            listOf(LyricsEntry.HEAD_LYRICS_ENTRY) + parsedLines
        } else {
            originalLyrics.lines().mapIndexed { index, line ->
                LyricsEntry(index * 100L, line)
            }
        }
    }

    var translatedLines by remember(currentSongId) { mutableStateOf<List<LyricsEntry>?>(null) }
    var showTranslated by remember(currentSongId) { mutableStateOf(false) }
    var isTranslating by remember(currentSongId) { mutableStateOf(false) }

    var showRomanized by remember(currentSongId) { mutableStateOf(false) }
    var romanizedLines by remember(currentSongId) { mutableStateOf<List<String>?>(null) }

    val displayedLines = if (showTranslated && translatedLines != null) translatedLines!! else lines

    val toggleTranslation = {
        if (showTranslated) {
            showTranslated = false
        } else if (translatedLines != null) {
            showTranslated = true
        } else {
            scope.launch {
                isTranslating = true
                val itemsToTranslate = lines.mapNotNull { if (it.text.isNotBlank()) it.text else null }
                val translatedText = com.cgens67.gluetune.utils.TranslationHelper.translate(itemsToTranslate.joinToString("\n"))

                if (translatedText != null) {
                    val split = translatedText.split("\n")
                    var transIdx = 0
                    val newEntries = lines.toMutableList()
                    for (i in newEntries.indices) {
                        val entry = newEntries[i]
                        if (entry.text.isNotBlank()) {
                            newEntries[i] = entry.copy(text = split.getOrElse(transIdx++) { entry.text }, words = null)
                        }
                    }
                    translatedLines = newEntries
                    showTranslated = true
                } else {
                    Toast.makeText(context, R.string.translation_failed, Toast.LENGTH_SHORT).show()
                }
                isTranslating = false
            }
        }
    }

    val toggleRomanization = {
        if (showRomanized) {
            showRomanized = false
        } else if (romanizedLines != null) {
            showRomanized = true
        } else {
            scope.launch {
                val linesToRomanize = lines.map { it.text }
                val result = com.cgens67.gluetune.utils.TranslationHelper.romanize(linesToRomanize)
                romanizedLines = result
                showRomanized = true
            }
        }
    }

    val playbackState by playerConnection.playbackState.collectAsState()
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val currentSong by playerConnection.currentSong.collectAsState(initial = null)

    val playerBackground by rememberEnumPreference(
        key = PlayerBackgroundStyleKey,
        defaultValue = PlayerBackgroundStyle.DEFAULT
    )
    val darkTheme by rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.AUTO)
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val useDarkTheme = remember(darkTheme, isSystemInDarkTheme) {
        if (darkTheme == DarkMode.AUTO) isSystemInDarkTheme else darkTheme == DarkMode.ON
    }

    var position by rememberSaveable(playbackState) { mutableLongStateOf(playerConnection.player.currentPosition) }
    var duration by rememberSaveable(playbackState) { mutableLongStateOf(playerConnection.player.duration) }

    val expressiveAccent = when (playerBackground) {
        PlayerBackgroundStyle.DEFAULT -> MaterialTheme.colorScheme.primary
        else -> Color.White
    }

    val textBackgroundColor = when (playerBackground) {
        PlayerBackgroundStyle.DEFAULT -> MaterialTheme.colorScheme.onBackground
        else -> Color.White
    }

    var gradientColors by remember { mutableStateOf<List<Color>>(emptyList()) }
    val fallbackColorArgb = MaterialTheme.colorScheme.surface.toArgb()

    LaunchedEffect(currentMetadata?.thumbnailUrl, playerBackground, fallbackColorArgb) {
        val thumbUrl = currentMetadata?.thumbnailUrl
        if ((playerBackground == PlayerBackgroundStyle.GRADIENT || playerBackground == PlayerBackgroundStyle.APPLE_MUSIC || playerBackground == PlayerBackgroundStyle.LIVE_MESH) && thumbUrl != null) {
            val cached = PlayerColorExtractor.gradientCache.get(thumbUrl)
            if (cached != null && cached.isNotEmpty()) {
                gradientColors = cached
                return@LaunchedEffect
            }
            withContext(Dispatchers.IO) {
                val result = runCatching {
                    ImageLoader(context).execute(
                        ImageRequest.Builder(context)
                            .data(thumbUrl)
                            .allowHardware(false)
                            .build()
                    ).drawable as? BitmapDrawable
                }.getOrNull()
                result?.bitmap?.let { bitmap ->
                    val palette = Palette.from(bitmap)
                        .maximumColorCount(8)
                        .resizeBitmapArea(100 * 100)
                        .generate()
                    val extracted = PlayerColorExtractor.extractGradientColors(
                        palette = palette,
                        fallbackColor = fallbackColorArgb
                    )
                    PlayerColorExtractor.gradientCache.put(thumbUrl, extracted)
                    withContext(Dispatchers.Main) {
                        gradientColors = extracted
                    }
                }
            }
        } else {
            gradientColors = emptyList()
        }
    }

    // Fetch lyrics
    LaunchedEffect(currentSongId) {
        currentSongId?.let { songId ->
            if (lyricsCache.containsKey(songId)) {
                currentLyricsEntity = lyricsCache[songId]
                return@LaunchedEffect
            }

            isLoadingLyrics = true
            withContext(Dispatchers.IO) {
                try {
                    val existing = database.getLyrics(songId)
                    if (existing != null && existing.lyrics != LYRICS_NOT_FOUND) {
                        lyricsCache = lyricsCache + (songId to existing)
                        currentLyricsEntity = existing
                    } else {
                        val entryPoint = EntryPointAccessors.fromApplication(
                            context.applicationContext,
                            com.cgens67.gluetune.di.LyricsHelperEntryPoint::class.java
                        )
                        val fetchedResult = currentMetadata?.let { entryPoint.lyricsHelper().getLyrics(it) }
                        val fetchedLyrics = fetchedResult?.lyrics
                        val pName = fetchedResult?.providerName

                        val entity = if (!fetchedLyrics.isNullOrBlank() && fetchedLyrics != LYRICS_NOT_FOUND) {
                            val textToSave = if (pName != null) "[provider:$pName]\n$fetchedLyrics" else fetchedLyrics
                            LyricsEntity(songId, textToSave)
                        } else {
                            LyricsEntity(songId, LYRICS_NOT_FOUND)
                        }

                        database.query { upsert(entity) }
                        lyricsCache = lyricsCache + (songId to entity)
                        currentLyricsEntity = entity
                    }
                } catch (e: Exception) {
                    val errorEntity = LyricsEntity(songId, LYRICS_NOT_FOUND)
                    lyricsCache = lyricsCache + (songId to errorEntity)
                    currentLyricsEntity = errorEntity
                } finally {
                    isLoadingLyrics = false
                }
            }
        }
    }

    BackHandler(enabled = isSelectionModeActive || isFullscreen) {
        when {
            isSelectionModeActive -> {
                isSelectionModeActive = false
                selectedIndices.clear()
            }
            isFullscreen -> onNavigateBack?.invoke()
        }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput) {
                    isAutoScrollEnabled = false
                    lastPreviewTime = System.currentTimeMillis()
                }
                return super.onPostScroll(consumed, available, source)
            }
        }
    }

    LaunchedEffect(playbackState) {
        if (isFullscreen && playbackState == Player.STATE_READY) {
            while (isActive) {
                delay(100)
                position = playerConnection.player.currentPosition
                duration = playerConnection.player.duration
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                isAppMinimized = true
            } else if (event == Lifecycle.Event.ON_START) {
                isAppMinimized = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Tracking position & line selection
    LaunchedEffect(originalLyrics, lyricsOffsetMs, currentSkipSegments, sponsorBlockEnabled) {
        if (originalLyrics.isNullOrEmpty() || !isSynced) {
            currentLineIndex = -1
            currentMainLineIndex = -1
            return@LaunchedEffect
        }
        while (isActive) {
            delay(40)
            val sliderPos = sliderPositionProvider()
            isSeeking = sliderPos != null
            val basePosition = sliderPos ?: playerConnection.player.currentPosition

            var sponsorBlockOffset = 0L
            if (sponsorBlockEnabled) {
                for (segment in currentSkipSegments) {
                    if (basePosition >= segment.second) {
                        sponsorBlockOffset += (segment.second - segment.first)
                    } else if (basePosition > segment.first) {
                        sponsorBlockOffset += (basePosition - segment.first)
                    }
                }
            }

            val rawIndex = findCurrentLineIndex(lines, basePosition - sponsorBlockOffset + lyricsOffsetMs)
            currentLineIndex = rawIndex

            var mainIdx = rawIndex
            while (mainIdx >= 0 && lines.getOrNull(mainIdx)?.isBackground == true) {
                mainIdx--
            }
            currentMainLineIndex = mainIdx
        }
    }

    suspend fun performSmoothPageScroll(targetIndex: Int, duration: Int = 1000) {
        if (isAnimating) return
        isAnimating = true
        try {
            val itemInfo = lazyListState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == targetIndex }
            if (itemInfo != null) {
                val viewportHeight = lazyListState.layoutInfo.viewportEndOffset - lazyListState.layoutInfo.viewportStartOffset
                val center = lazyListState.layoutInfo.viewportStartOffset + (viewportHeight / 3)
                val itemCenter = itemInfo.offset + itemInfo.size / 2
                val offset = itemCenter - center
                if (kotlin.math.abs(offset) > 8) {
                    lazyListState.animateScrollBy(
                        value = offset.toFloat(),
                        animationSpec = tween<Float>(
                            durationMillis = if (animateLyrics) duration else 1,
                            easing = FastOutSlowInEasing
                        )
                    )
                }
            } else {
                lazyListState.scrollToItem(targetIndex)
            }
        } finally {
            isAnimating = false
        }
    }

    LaunchedEffect(currentMainLineIndex, isAutoScrollEnabled) {
        if (!isSynced) return@LaunchedEffect
        if (currentMainLineIndex != -1) {
            deferredCurrentMainLineIndex = currentMainLineIndex
        }

        if (isAutoScrollEnabled && currentMainLineIndex != -1 && scrollLyrics) {
            performSmoothPageScroll(currentMainLineIndex, 1000)
        }
        previousMainLineIndex = currentMainLineIndex
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isFullscreen) MaterialTheme.colorScheme.background else Color.Transparent)
    ) {
        // Player Backgrounds
        if (isFullscreen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = backgroundAlpha() }
            ) {
                PlayerBackground(
                    playerBackground = playerBackground,
                    mediaMetadata = currentMetadata,
                    gradientColors = gradientColors,
                    disableBlur = disableBlur
                )
                if (playerBackground != PlayerBackgroundStyle.APPLE_MUSIC) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = if (useDarkTheme) 0.35f else 0.5f))
                    )
                }
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // Subtle Back Button in Fullscreen
            if (isFullscreen) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onNavigateBack?.invoke() }) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back),
                            contentDescription = stringResource(R.string.back),
                            tint = textBackgroundColor
                        )
                    }
                }
            }

            // Lyrics List
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    state = lazyListState,
                    contentPadding = PaddingValues(
                        top = if (isFullscreen) 16.dp else 40.dp,
                        bottom = if (isFullscreen) 180.dp else 70.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .fadingEdge(vertical = 40.dp)
                        .nestedScroll(nestedScrollConnection)
                ) {
                    if (isLoadingLyrics) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 96.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    CircularWavyProgressIndicator(
                                        color = expressiveAccent,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.loading_lyrics),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = expressiveAccent.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    } else if (originalLyrics == LYRICS_NOT_FOUND || displayedLines.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 96.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                                    ),
                                    modifier = Modifier.fillMaxWidth(0.85f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.music_note),
                                            contentDescription = null,
                                            modifier = Modifier.size(36.dp),
                                            tint = expressiveAccent
                                        )
                                        Text(
                                            text = stringResource(R.string.lyrics_not_found),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = stringResource(R.string.lyrics_not_available_desc),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        itemsIndexed(
                            items = displayedLines,
                            key = { index, item -> "$index-${item.time}" }
                        ) { index, item ->
                            val isSelected = selectedIndices.contains(index)

                            // BACKING VOCALS HIGHLIGHT FIX:
                            // A backing vocal highlights whenever it is currently playing (currentLineIndex == index)
                            // OR when the parent main line is playing and the backing line overlaps.
                            val isCurrentlyPlayingLine = (index == currentLineIndex)
                            val isParentMainLineOfPlayingBg = (!item.isBackground && currentLineIndex > index &&
                                    displayedLines.subList(index + 1, currentLineIndex + 1).all { it.isBackground })
                            val isChildBgLineOfPlayingMain = (item.isBackground && currentMainLineIndex >= 0 &&
                                    index > currentMainLineIndex &&
                                    displayedLines.subList(currentMainLineIndex + 1, index + 1).all { it.isBackground } &&
                                    currentLineIndex >= index)

                            val isActiveLine = (isCurrentlyPlayingLine || isParentMainLineOfPlayingBg || isChildBgLineOfPlayingMain) && isSynced
                            val distance = if (isActiveLine) 0 else kotlin.math.abs(index - currentMainLineIndex)
                            val romText = if (showRomanized) romanizedLines?.getOrNull(index) else null

                            LyricsLine(
                                entry = item,
                                romanizedText = romText,
                                isSynced = isSynced,
                                isActive = isActiveLine,
                                distanceFromCurrent = distance,
                                lyricsTextPosition = lyricsTextPosition,
                                textColor = expressiveAccent,
                                textSize = 25f,
                                lineSpacing = 6f,
                                onClick = {
                                    if (isSelectionModeActive) {
                                        if (isSelected) {
                                            selectedIndices.remove(index)
                                            if (selectedIndices.isEmpty()) isSelectionModeActive = false
                                        } else {
                                            if (selectedIndices.size < maxSelectionLimit) selectedIndices.add(index)
                                            else showMaxSelectionToast = true
                                        }
                                    } else if (isSynced && changeLyrics) {
                                        var targetVideoTime = item.time - lyricsOffsetMs
                                        if (sponsorBlockEnabled) {
                                            for (segment in currentSkipSegments) {
                                                if (targetVideoTime >= segment.first) {
                                                    targetVideoTime += (segment.second - segment.first)
                                                }
                                            }
                                        }
                                        playerConnection.player.seekTo(targetVideoTime)
                                        scope.launch { performSmoothPageScroll(index, 1000) }
                                    }
                                },
                                onLongClick = {
                                    if (!isSelectionModeActive) {
                                        isSelectionModeActive = true
                                        selectedIndices.add(index)
                                    }
                                },
                                isSelected = isSelected,
                                isSelectionModeActive = isSelectionModeActive,
                                isAutoScrollActive = isAutoScrollEnabled,
                                animateLyrics = animateLyrics,
                                lyricsOffset = lyricsOffsetMs,
                                currentSkipSegments = currentSkipSegments,
                                sponsorBlockEnabled = sponsorBlockEnabled
                            )

                            // Gap indicator between songs
                            val nextItem = displayedLines.getOrNull(index + 1)
                            if (isSynced && nextItem != null && !item.isBackground && !nextItem.isBackground) {
                                val itemEnd = item.words?.maxOfOrNull { (it.endTime * 1000).toLong() } ?: (item.time + 3000L)
                                val gapDuration = nextItem.time - itemEnd

                                if (gapDuration > 8000L) {
                                    GapIndicator(
                                        gapStart = itemEnd,
                                        gapEnd = nextItem.time,
                                        playerConnection = playerConnection,
                                        lyricsOffset = lyricsOffsetMs,
                                        color = expressiveAccent,
                                        currentSkipSegments = currentSkipSegments,
                                        sponsorBlockEnabled = sponsorBlockEnabled
                                    )
                                }
                            }
                        }

                        if (!lyricsProviderName.isNullOrBlank()) {
                            item(key = "provider_credit") {
                                Text(
                                    text = stringResource(R.string.lyrics_provided_by, lyricsProviderName),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textBackgroundColor.copy(alpha = 0.5f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 28.dp, bottom = 16.dp)
                                )
                            }
                        }
                    }
                }

                // Auto-Scroll Resume Button
                androidx.compose.animation.AnimatedVisibility(
                    visible = !isAutoScrollEnabled && isSynced && !isSelectionModeActive,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = if (isFullscreen) 200.dp else 80.dp)
                ) {
                    Surface(
                        onClick = {
                            scope.launch { performSmoothPageScroll(currentLineIndex, 1000) }
                            isAutoScrollEnabled = true
                        },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        tonalElevation = 6.dp,
                        shadowElevation = 6.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.sync),
                                contentDescription = stringResource(R.string.auto_scroll),
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.auto_scroll),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Selection Mode Actions
                androidx.compose.animation.AnimatedVisibility(
                    visible = isSelectionModeActive,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = if (isFullscreen) 200.dp else 80.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.95f),
                            tonalElevation = 4.dp,
                            modifier = Modifier
                                .size(52.dp)
                                .clickable {
                                    isSelectionModeActive = false
                                    selectedIndices.clear()
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(id = R.drawable.close),
                                    contentDescription = stringResource(R.string.cancel),
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        if (selectedIndices.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(26.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
                                tonalElevation = 4.dp,
                                modifier = Modifier.clickable {
                                    val sortedIndices = selectedIndices.sorted()
                                    val selectedLyricsText = sortedIndices
                                        .mapNotNull { displayedLines.getOrNull(it)?.text }
                                        .joinToString("\n")

                                    if (selectedLyricsText.isNotBlank()) {
                                        shareDialogData = Triple(
                                            selectedLyricsText,
                                            currentMetadata?.title ?: "",
                                            currentMetadata?.artists?.joinToString { it.name } ?: ""
                                        )
                                        showShareDialog = true
                                    }
                                    isSelectionModeActive = false
                                    selectedIndices.clear()
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.media3_icon_share),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "${stringResource(R.string.share)} (${selectedIndices.size})",
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Clean Original Bottom Player Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
            ) {
                val offsetXAnimatable = remember { Animatable(0f) }
                var dragStartTime by remember { mutableLongStateOf(0L) }
                var totalDragDistance by remember { mutableFloatStateOf(0f) }
                val layoutDirection = LocalLayoutDirection.current

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .pointerInput(swipeThumbnail) {
                                if (!swipeThumbnail) return@pointerInput
                                detectHorizontalDragGestures(
                                    onDragStart = {
                                        dragStartTime = System.currentTimeMillis()
                                        totalDragDistance = 0f
                                    },
                                    onDragCancel = {
                                        scope.launch {
                                            offsetXAnimatable.animateTo(
                                                targetValue = 0f,
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                                    stiffness = Spring.StiffnessLow
                                                )
                                            )
                                        }
                                    },
                                    onHorizontalDrag = { _, dragAmount ->
                                        val adjustedDragAmount = if (layoutDirection == LayoutDirection.Rtl) -dragAmount else dragAmount
                                        val allowLeft = adjustedDragAmount < 0 && canSkipNext
                                        val allowRight = adjustedDragAmount > 0 && canSkipPrevious

                                        if (allowLeft || allowRight) {
                                            totalDragDistance += adjustedDragAmount.absoluteValue
                                            scope.launch {
                                                offsetXAnimatable.snapTo(offsetXAnimatable.value + adjustedDragAmount)
                                            }
                                        }
                                    },
                                    onDragEnd = {
                                        val dragDuration = System.currentTimeMillis() - dragStartTime
                                        val velocity = if (dragDuration > 0) totalDragDistance / dragDuration else 0f
                                        val currentOffset = offsetXAnimatable.value

                                        val minDistanceThreshold = 50f
                                        val velocityThreshold = (0.73f * -8.25f) + 8.5f
                                        val autoSwipeThreshold = (600 / (1f + exp(-(-11.44748 * 0.73f + 9.04945)))).roundToInt()

                                        val shouldChangeSong = (
                                                currentOffset.absoluteValue > minDistanceThreshold &&
                                                        velocity > velocityThreshold
                                                ) || (currentOffset.absoluteValue > autoSwipeThreshold)

                                        if (shouldChangeSong) {
                                            val isRightSwipe = currentOffset > 0
                                            if (isRightSwipe && canSkipPrevious) {
                                                playerConnection.seekToPrevious()
                                            } else if (!isRightSwipe && canSkipNext) {
                                                playerConnection.seekToNext()
                                            }
                                        }

                                        scope.launch {
                                            offsetXAnimatable.animateTo(
                                                targetValue = 0f,
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                                    stiffness = Spring.StiffnessLow
                                                )
                                            )
                                        }
                                    }
                                )
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .offset { IntOffset(offsetXAnimatable.value.roundToInt(), 0) }
                                .fillMaxWidth()
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        if (playbackState == Player.STATE_ENDED) {
                                            playerConnection.player.seekTo(0, 0)
                                            playerConnection.player.playWhenReady = true
                                        } else {
                                            if (isPlaying) playerConnection.player.pause() else playerConnection.player.play()
                                        }
                                    }
                            ) {
                                currentMetadata?.let { metadata ->
                                    AsyncImage(
                                        model = metadata.thumbnailUrl,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.4f))
                                )

                                Icon(
                                    painter = painterResource(
                                        if (playbackState == Player.STATE_ENDED) {
                                            R.drawable.replay
                                        } else if (isPlaying) {
                                            R.drawable.pause
                                        } else {
                                            R.drawable.play
                                        }
                                    ),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.weight(1f)
                            ) {
                                currentMetadata?.let { metadata ->
                                    Text(
                                        text = metadata.title,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = textBackgroundColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = if (metadata.artists.isNotEmpty()) {
                                            metadata.artists.joinToString(", ") { it.name }
                                        } else {
                                            stringResource(R.string.unknown)
                                        },
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                        color = textBackgroundColor.copy(alpha = 0.7f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = { playerConnection.toggleLike() }) {
                            Icon(
                                painter = painterResource(
                                    if (currentSong?.song?.liked == true)
                                        R.drawable.favorite
                                    else R.drawable.favorite_border
                                ),
                                contentDescription = null,
                                tint = if (currentSong?.song?.liked == true)
                                    MaterialTheme.colorScheme.error
                                else
                                    textBackgroundColor.copy(alpha = 0.8f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                currentMetadata?.let { metadata ->
                                    menuState.show {
                                        LyricsMenu(
                                            lyricsEntity = activeLyricsEntity,
                                            mediaMetadata = metadata,
                                            onDismiss = menuState::dismiss,
                                            isTranslated = showTranslated,
                                            onTranslateClick = { toggleTranslation() },
                                            isRomanized = showRomanized,
                                            onRomanizeClick = { toggleRomanization() },
                                            navController = navController
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.more_horiz),
                                contentDescription = stringResource(R.string.more_options),
                                tint = textBackgroundColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Slider
                when (sliderStyle) {
                    SliderStyle.DEFAULT -> {
                        Slider(
                            value = (sliderPosition ?: position).toFloat(),
                            valueRange = 0f..(if (duration == C.TIME_UNSET) 0f else duration.toFloat()),
                            onValueChange = { sliderPosition = it.toLong() },
                            onValueChangeFinished = {
                                sliderPosition?.let {
                                    playerConnection.player.seekTo(it)
                                    position = it
                                }
                                sliderPosition = null
                            },
                            colors = SliderDefaults.colors(
                                activeTrackColor = textBackgroundColor,
                                inactiveTrackColor = textBackgroundColor.copy(alpha = 0.3f),
                                thumbColor = textBackgroundColor
                            ),
                        )
                    }
                    SliderStyle.SQUIGGLY -> {
                        SquigglySlider(
                            value = (sliderPosition ?: position).toFloat(),
                            valueRange = 0f..(if (duration == C.TIME_UNSET) 0f else duration.toFloat()),
                            onValueChange = { sliderPosition = it.toLong() },
                            onValueChangeFinished = {
                                sliderPosition?.let {
                                    playerConnection.player.seekTo(it)
                                    position = it
                                }
                                sliderPosition = null
                            },
                            colors = SliderDefaults.colors(
                                activeTrackColor = textBackgroundColor,
                                inactiveTrackColor = textBackgroundColor.copy(alpha = 0.3f),
                                thumbColor = textBackgroundColor
                            ),
                            squigglesSpec = SquigglySlider.SquigglesSpec(
                                amplitude = if (isPlaying && animateLyrics) (4.dp).coerceAtLeast(2.dp) else 0.dp,
                                strokeWidth = 3.dp,
                                wavelength = 36.dp,
                            ),
                        )
                    }
                    SliderStyle.SLIM -> {
                        Slider(
                            value = (sliderPosition ?: position).toFloat(),
                            valueRange = 0f..(if (duration == C.TIME_UNSET) 0f else duration.toFloat()),
                            onValueChange = { sliderPosition = it.toLong() },
                            onValueChangeFinished = {
                                sliderPosition?.let {
                                    playerConnection.player.seekTo(it)
                                    position = it
                                }
                                sliderPosition = null
                            },
                            thumb = { Spacer(modifier = Modifier.size(0.dp)) },
                            colors = SliderDefaults.colors(
                                activeTrackColor = textBackgroundColor,
                                inactiveTrackColor = textBackgroundColor.copy(alpha = 0.3f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = makeTimeString(sliderPosition ?: position),
                        style = MaterialTheme.typography.labelMedium,
                        color = textBackgroundColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Text(
                        text = if (duration != C.TIME_UNSET) makeTimeString(duration) else "",
                        style = MaterialTheme.typography.labelMedium,
                        color = textBackgroundColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }

    if (showShareDialog && shareDialogData != null) {
        ShareLyricsDialog(
            lyricsText = shareDialogData!!.first,
            songTitle = shareDialogData!!.second,
            artists = shareDialogData!!.third,
            mediaMetadata = currentMetadata,
            onDismiss = {
                showShareDialog = false
                shareDialogData = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GapIndicator(
    gapStart: Long,
    gapEnd: Long,
    playerConnection: PlayerConnection,
    lyricsOffset: Long,
    color: Color,
    currentSkipSegments: List<Pair<Long, Long>> = emptyList(),
    sponsorBlockEnabled: Boolean = false
) {
    var smoothPosition by remember { mutableLongStateOf(gapStart) }

    LaunchedEffect(Unit, currentSkipSegments, sponsorBlockEnabled) {
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

    val progress by remember { derivedStateOf { ((smoothPosition - gapStart).toFloat() / (gapEnd - gapStart)).coerceIn(0f, 1f) } }
    val isVisible by remember { derivedStateOf { smoothPosition in gapStart..(gapEnd - 1000L) } }

    androidx.compose.animation.AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(1000)) + expandVertically(tween(1000)),
        exit = fadeOut(tween(1000)) + shrinkVertically(tween(1000))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularWavyProgressIndicator(
                progress = { progress },
                color = color,
                trackColor = color.copy(alpha = 0.2f),
                modifier = Modifier.size(40.dp)
            )
        }
    }
}
