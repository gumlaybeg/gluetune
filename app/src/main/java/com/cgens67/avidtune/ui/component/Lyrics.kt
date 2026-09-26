package com.cgens67.gluetune.ui.component

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
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
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.navigation.NavController
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
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

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
    val lyricsTextPosition by rememberEnumPreference(LyricsTextPositionKey, LyricsPosition.LEFT)
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

    var gradientColors by remember { mutableStateOf<List<Color>>(emptyList()) }

    // Fetch lyrics logic
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

    suspend fun performSmoothPageScroll(targetIndex: Int, duration: Int = 1200) {
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
                        animationSpec = tween(durationMillis = if (animateLyrics) duration else 1, easing = FastOutSlowInEasing)
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
            performSmoothPageScroll(currentMainLineIndex, 1200)
        }
        previousMainLineIndex = currentMainLineIndex
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isFullscreen) MaterialTheme.colorScheme.background else Color.Transparent)
    ) {
        // Atmospheric Ambient Backgrounds
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
            // --- TOP HEADER BAR ---
            if (isFullscreen) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Back Glass Button
                    Surface(
                        onClick = { onNavigateBack?.invoke() },
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(R.drawable.arrow_back),
                                contentDescription = stringResource(R.string.back),
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Interactive Action Pills (Translate, Romanize, Overflow)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Translation Pill
                        Surface(
                            onClick = { toggleTranslation() },
                            shape = RoundedCornerShape(14.dp),
                            color = if (showTranslated) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, if (showTranslated) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.12f)),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isTranslating) {
                                    CircularProgressIndicator(
                                        color = if (showTranslated) MaterialTheme.colorScheme.onPrimary else Color.White,
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(R.drawable.translate),
                                        contentDescription = null,
                                        tint = if (showTranslated) MaterialTheme.colorScheme.onPrimary else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (showTranslated) stringResource(R.string.show_original) else stringResource(R.string.Translate),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (showTranslated) MaterialTheme.colorScheme.onPrimary else Color.White
                                )
                            }
                        }

                        // Romanize Pill
                        Surface(
                            onClick = { toggleRomanization() },
                            shape = RoundedCornerShape(14.dp),
                            color = if (showRomanized) MaterialTheme.colorScheme.secondary else Color.White.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, if (showRomanized) MaterialTheme.colorScheme.secondary else Color.White.copy(alpha = 0.12f)),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Rom",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (showRomanized) MaterialTheme.colorScheme.onSecondary else Color.White
                                )
                            }
                        }

                        // More Menu Pill
                        Surface(
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
                            },
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(R.drawable.more_horiz),
                                    contentDescription = stringResource(R.string.more_options),
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // --- MAIN LYRICS AREA WITH SMOOTH FADING EDGES ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    state = lazyListState,
                    contentPadding = PaddingValues(
                        top = if (isFullscreen) 24.dp else 48.dp,
                        bottom = if (isFullscreen) 210.dp else 90.dp,
                        start = 12.dp,
                        end = 12.dp
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .fadingEdge(vertical = 48.dp)
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
                                Surface(
                                    shape = RoundedCornerShape(24.dp),
                                    color = Color.White.copy(alpha = 0.1f),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .padding(20.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.lyrics),
                                            contentDescription = null,
                                            modifier = Modifier.size(36.dp),
                                            tint = expressiveAccent
                                        )
                                        Text(
                                            text = stringResource(R.string.lyrics_not_found),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = stringResource(R.string.lyrics_not_available_desc),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White.copy(alpha = 0.7f),
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
                            val isAssociatedBg = item.isBackground &&
                                    currentMainLineIndex >= 0 &&
                                    index > currentMainLineIndex &&
                                    displayedLines.subList(currentMainLineIndex + 1, index + 1).all { it.isBackground }

                            val isActiveLine = (index == currentMainLineIndex || isAssociatedBg) && isSynced
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
                                textSize = 26f,
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
                                        scope.launch { performSmoothPageScroll(index, 1200) }
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

                            // Instrumental / Gap indicator
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
                                    color = Color.White.copy(alpha = 0.45f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 32.dp, bottom = 16.dp)
                                )
                            }
                        }
                    }
                }

                // Auto-Scroll Paused Floating Pill
                AnimatedVisibility(
                    visible = !isAutoScrollEnabled && isSynced && !isSelectionModeActive,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = if (isFullscreen) 195.dp else 75.dp)
                ) {
                    Surface(
                        onClick = {
                            scope.launch { performSmoothPageScroll(currentLineIndex, 1200) }
                            isAutoScrollEnabled = true
                        },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.sync),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.auto_scroll),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Multi-Selection Floating Action Dock
                AnimatedVisibility(
                    visible = isSelectionModeActive,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = if (isFullscreen) 195.dp else 75.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shadowElevation = 12.dp,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TextButton(onClick = {
                                isSelectionModeActive = false
                                selectedIndices.clear()
                            }) {
                                Text(stringResource(R.string.cancel))
                            }

                            Button(
                                onClick = {
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
                                },
                                enabled = selectedIndices.isNotEmpty(),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.share),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("${stringResource(R.string.share)} (${selectedIndices.size})")
                            }
                        }
                    }
                }
            }

            // --- FLOATING GLASS PLAYER CONTROLS (FULLSCREEN) ---
            if (isFullscreen) {
                LyricsBottomPlayerDock(
                    mediaMetadata = currentMetadata,
                    position = position,
                    duration = duration,
                    isPlaying = isPlaying,
                    sliderStyle = sliderStyle,
                    canSkipPrevious = canSkipPrevious,
                    canSkipNext = canSkipNext,
                    playerConnection = playerConnection,
                    sliderPosition = sliderPosition,
                    onSliderPositionChange = { sliderPosition = it },
                    onSliderSeekFinished = {
                        sliderPosition?.let {
                            playerConnection.player.seekTo(it)
                            position = it
                        }
                        sliderPosition = null
                    }
                )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LyricsBottomPlayerDock(
    mediaMetadata: com.cgens67.gluetune.models.MediaMetadata?,
    position: Long,
    duration: Long,
    isPlaying: Boolean,
    sliderStyle: SliderStyle,
    canSkipPrevious: Boolean,
    canSkipNext: Boolean,
    playerConnection: PlayerConnection,
    sliderPosition: Long?,
    onSliderPositionChange: (Long) -> Unit,
    onSliderSeekFinished: () -> Unit
) {
    val currentSong by playerConnection.currentSong.collectAsState(initial = null)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(32.dp),
        color = Color.Black.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
        shadowElevation = 16.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            // Track Info & Like Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = mediaMetadata?.thumbnailUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = mediaMetadata?.title.orEmpty(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = mediaMetadata?.artists?.joinToString(", ") { it.name }.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(onClick = { playerConnection.toggleLike() }) {
                    Icon(
                        painter = painterResource(
                            if (currentSong?.song?.liked == true) R.drawable.favorite else R.drawable.favorite_border
                        ),
                        contentDescription = null,
                        tint = if (currentSong?.song?.liked == true) Color(0xFFFF4B6E) else Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Progress Slider
            when (sliderStyle) {
                SliderStyle.SQUIGGLY -> {
                    SquigglySlider(
                        value = (sliderPosition ?: position).toFloat(),
                        valueRange = 0f..(if (duration == C.TIME_UNSET) 0f else duration.toFloat()),
                        onValueChange = { onSliderPositionChange(it.toLong()) },
                        onValueChangeFinished = onSliderSeekFinished,
                        colors = SliderDefaults.colors(
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color.White.copy(alpha = 0.25f),
                            thumbColor = Color.White
                        ),
                        squigglesSpec = SquigglySlider.SquigglesSpec(
                            amplitude = if (isPlaying) 3.dp else 0.dp,
                            strokeWidth = 3.dp,
                            wavelength = 32.dp
                        )
                    )
                }
                else -> {
                    Slider(
                        value = (sliderPosition ?: position).toFloat(),
                        valueRange = 0f..(if (duration == C.TIME_UNSET) 0f else duration.toFloat()),
                        onValueChange = { onSliderPositionChange(it.toLong()) },
                        onValueChangeFinished = onSliderSeekFinished,
                        colors = SliderDefaults.colors(
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color.White.copy(alpha = 0.25f),
                            thumbColor = Color.White
                        )
                    )
                }
            }

            // Timestamps
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-4).dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = makeTimeString(sliderPosition ?: position),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.65f)
                )
                Text(
                    text = if (duration != C.TIME_UNSET) makeTimeString(duration) else "--:--",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.65f)
                )
            }

            // Transport Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { playerConnection.seekToPrevious() },
                    enabled = canSkipPrevious,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.skip_previous),
                        contentDescription = null,
                        tint = if (canSkipPrevious) Color.White else Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(Modifier.width(24.dp))

                Surface(
                    onClick = { playerConnection.togglePlayPause() },
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play),
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(Modifier.width(24.dp))

                IconButton(
                    onClick = { playerConnection.seekToNext() },
                    enabled = canSkipNext,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.skip_next),
                        contentDescription = null,
                        tint = if (canSkipNext) Color.White else Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
