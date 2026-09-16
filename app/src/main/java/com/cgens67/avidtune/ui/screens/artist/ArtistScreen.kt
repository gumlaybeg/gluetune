package com.cgens67.gluetune.ui.screens.artist

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.cgens67.gluetune.LocalDatabase
import com.cgens67.gluetune.LocalPlayerAwareWindowInsets
import com.cgens67.gluetune.LocalPlayerConnection
import com.cgens67.gluetune.R
import com.cgens67.gluetune.constants.EnableArtistCanvasKey
import com.cgens67.gluetune.db.entities.ArtistEntity
import com.cgens67.gluetune.extensions.toMediaItem
import com.cgens67.gluetune.extensions.togglePlayPause
import com.cgens67.gluetune.models.toMediaMetadata
import com.cgens67.gluetune.playback.queues.ListQueue
import com.cgens67.gluetune.playback.queues.YouTubeQueue
import com.cgens67.gluetune.ui.component.ArtistCanvasHelper
import com.cgens67.gluetune.ui.component.ArtistVideo
import com.cgens67.gluetune.ui.component.LocalMenuState
import com.cgens67.gluetune.ui.component.NavigationTitle
import com.cgens67.gluetune.ui.component.SongListItem
import com.cgens67.gluetune.ui.component.YouTubeGridItem
import com.cgens67.gluetune.ui.component.YouTubeListItem
import com.cgens67.gluetune.ui.component.shimmer.ListItemPlaceHolder
import com.cgens67.gluetune.ui.component.shimmer.ShimmerHost
import com.cgens67.gluetune.ui.component.shimmer.TextPlaceholder
import com.cgens67.gluetune.ui.menu.SongMenu
import com.cgens67.gluetune.ui.menu.YouTubeAlbumMenu
import com.cgens67.gluetune.ui.menu.YouTubeArtistMenu
import com.cgens67.gluetune.ui.menu.YouTubePlaylistMenu
import com.cgens67.gluetune.ui.menu.YouTubeSongMenu
import com.cgens67.gluetune.ui.theme.PlayerColorExtractor
import com.cgens67.gluetune.ui.utils.resize
import com.cgens67.gluetune.utils.rememberPreference
import com.cgens67.gluetune.viewmodels.ArtistViewModel
import com.cgens67.innertube.YouTube
import com.cgens67.innertube.models.AlbumItem
import com.cgens67.innertube.models.ArtistItem
import com.cgens67.innertube.models.EpisodeItem
import com.cgens67.innertube.models.PlaylistItem
import com.cgens67.innertube.models.PodcastItem
import com.cgens67.innertube.models.SongItem
import com.cgens67.innertube.models.WatchEndpoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("ServiceCast")
@OptIn(
    ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun ArtistScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: ArtistViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val database = LocalDatabase.current
    val menuState = LocalMenuState.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val playerConnection = LocalPlayerConnection.current ?: return
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val artistPage = viewModel.artistPage
    val libraryArtist by viewModel.libraryArtist.collectAsState()
    val librarySongs by viewModel.librarySongs.collectAsState()

    val totalPlayCount by remember(viewModel.artistId) { database.artistTotalPlayCount(viewModel.artistId) }.collectAsState(initial = 0)

    val lazyListState = rememberLazyListState()
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Dynamic responsive heights based on orientation
    val headerHeight = if (isLandscape) 220.dp else 420.dp
    val spacerHeight = if (isLandscape) 150.dp else 300.dp
    val headerThresholdDp = if (isLandscape) 180.dp else 380.dp
    val headerHeightPx = with(density) { headerThresholdDp.toPx() }

    // Gradient colors for theme & background
    var gradientColors by remember { mutableStateOf<List<Color>>(emptyList()) }
    val fallbackColor = MaterialTheme.colorScheme.surface.toArgb()
    val surfaceColor = MaterialTheme.colorScheme.surface
    val dominantColor = gradientColors.firstOrNull() ?: surfaceColor

    // Get thumbnail URL
    val thumbnail = artistPage?.artist?.thumbnail ?: libraryArtist?.artist?.thumbnailUrl
    val artistName = artistPage?.artist?.title ?: libraryArtist?.artist?.name ?: stringResource(R.string.unknown)

    val (enableArtistCanvas) = rememberPreference(EnableArtistCanvasKey, defaultValue = true)

    // Artist Canvas
    var artistVideoUrl by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(artistName, enableArtistCanvas) {
        if (enableArtistCanvas && artistName.isNotBlank() && artistName != context.getString(R.string.unknown)) {
            artistVideoUrl = ArtistCanvasHelper.getArtistCanvas(context, artistName)
        } else {
            artistVideoUrl = null
        }
    }

    // Skeleton loading state: don't flash shimmer if data is already available
    var isFetching by rememberSaveable(artistPage != null) {
        mutableStateOf(artistPage == null)
    }
    LaunchedEffect(artistPage) {
        if (artistPage != null) {
            isFetching = false
        }
    }
    LaunchedEffect(Unit) {
        delay(2500)
        isFetching = false
    }

    // Extract gradient colors from artist image
    LaunchedEffect(thumbnail) {
        if (thumbnail != null) {
            val request = ImageRequest.Builder(context)
                .data(thumbnail)
                .size(100, 100)
                .allowHardware(false)
                .build()

            val result = runCatching {
                context.imageLoader.execute(request).drawable
            }.getOrNull()

            if (result != null) {
                val bitmap = (result as? android.graphics.drawable.BitmapDrawable)?.bitmap
                if (bitmap != null) {
                    val palette = withContext(Dispatchers.Default) {
                        Palette.from(bitmap)
                            .maximumColorCount(8)
                            .resizeBitmapArea(100 * 100)
                            .generate()
                    }

                    val extractedColors = PlayerColorExtractor.extractGradientColors(
                        palette = palette,
                        fallbackColor = fallbackColor
                    )
                    gradientColors = extractedColors
                }
            }
        } else {
            gradientColors = emptyList()
        }
    }

    // Reactive scroll progress tracking
    val scrollOffset by remember {
        derivedStateOf {
            if (lazyListState.firstVisibleItemIndex > 0) {
                headerHeightPx
            } else {
                lazyListState.firstVisibleItemScrollOffset.toFloat()
            }
        }
    }

    // Parallax & Fade transitions
    val imageTranslationY by remember {
        derivedStateOf {
            -scrollOffset * 0.4f
        }
    }
    val imageAlpha by remember {
        derivedStateOf {
            (1f - (scrollOffset / (headerHeightPx * 0.85f))).coerceIn(0f, 1f)
        }
    }

    val topBarProgress by remember {
        derivedStateOf {
            if (lazyListState.firstVisibleItemIndex > 0) {
                1f
            } else {
                (lazyListState.firstVisibleItemScrollOffset / (headerHeightPx * 0.65f)).coerceIn(0f, 1f)
            }
        }
    }

    val topBarContainerColor by animateColorAsState(
        targetValue = dominantColor.copy(alpha = topBarProgress * 0.95f),
        animationSpec = tween(250),
        label = "topBarColor"
    )

    val buttonBgAlpha by animateFloatAsState(
        targetValue = if (topBarProgress > 0.8f) 0f else 0.6f,
        animationSpec = tween(250),
        label = "buttonBgAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceColor)
    ) {
        // --- 1. PARALLAX HEADER (BACKGROUND) ---
        if (thumbnail != null || isFetching) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerHeight)
                    .graphicsLayer {
                        translationY = imageTranslationY
                        alpha = imageAlpha
                    }
            ) {
                if (isFetching) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                } else {
                    AsyncImage(
                        model = thumbnail?.resize(1200, 1200),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )

                    artistVideoUrl?.let { url ->
                        ArtistVideo(
                            videoUrl = url,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Smooth gradient overlay to blend into the sheet
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent,
                                    surfaceColor.copy(alpha = 0.4f),
                                    surfaceColor
                                ),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )
            }
        }

        // --- 2. SCROLLABLE CONTENT ---
        LazyColumn(
            state = lazyListState,
            contentPadding = LocalPlayerAwareWindowInsets.current
                .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
                .asPaddingValues(),
            modifier = Modifier.fillMaxSize()
        ) {
            // Transparent spacer to push content below parallax header
            item {
                Spacer(modifier = Modifier.height(spacerHeight))
            }

            if (isFetching && artistPage == null) {
                item(key = "shimmer") {
                    ShimmerHost {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                            color = surfaceColor
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 24.dp)
                            ) {
                                TextPlaceholder(
                                    height = 40.dp,
                                    modifier = Modifier
                                        .fillMaxWidth(0.6f)
                                        .padding(bottom = 16.dp)
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(bottom = 24.dp)
                                ) {
                                    Spacer(modifier = Modifier.height(28.dp).width(80.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface))
                                    Spacer(modifier = Modifier.height(28.dp).width(100.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface))
                                }
                                TextPlaceholder(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                                TextPlaceholder(modifier = Modifier.fillMaxWidth(0.8f).padding(bottom = 24.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Spacer(modifier = Modifier.height(52.dp).weight(1f).clip(RoundedCornerShape(26.dp)).background(MaterialTheme.colorScheme.onSurface))
                                    Spacer(modifier = Modifier.height(52.dp).weight(1f).clip(RoundedCornerShape(26.dp)).background(MaterialTheme.colorScheme.onSurface))
                                    Spacer(modifier = Modifier.height(52.dp).weight(1f).clip(RoundedCornerShape(26.dp)).background(MaterialTheme.colorScheme.onSurface))
                                }
                            }
                        }
                        repeat(6) { ListItemPlaceHolder() }
                    }
                }
            } else {
                // --- ARTIST INFO SHEET ---
                item(key = "header_info") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                        color = surfaceColor
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 24.dp)
                        ) {
                            // Artist Name
                            Text(
                                text = artistName,
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // Stats Badges Row
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 20.dp)
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                artistPage?.subscriberCountText?.let { subscribers ->
                                    StatBadge(icon = R.drawable.person, text = subscribers.split(" ").firstOrNull() ?: subscribers)
                                }
                                artistPage?.monthlyListenerCount?.let { monthlyListeners ->
                                    StatBadge(icon = R.drawable.play, text = monthlyListeners.split(" ").firstOrNull() ?: monthlyListeners)
                                }
                                if (totalPlayCount > 0) {
                                    StatBadge(icon = R.drawable.history, text = totalPlayCount.toString())
                                }
                            }

                            // Description
                            var isDescriptionExpanded by rememberSaveable { mutableStateOf(false) }
                            val fallbackDesc = "$artistName is a music artist."
                            val description = artistPage?.description?.substringBefore("From Wikipedia")?.trim() ?: fallbackDesc

                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                textAlign = TextAlign.Start,
                                modifier = Modifier
                                    .padding(bottom = 24.dp)
                                    .fillMaxWidth()
                                    .animateContentSize()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = { isDescriptionExpanded = !isDescriptionExpanded }
                                    ),
                                maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 3,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 24.sp
                            )

                            // Action Buttons (Proper padding and sizing so text never truncates)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                            ) {
                                // Subscribe
                                val isSubscribed = libraryArtist?.artist?.bookmarkedAt != null
                                ToggleButton(
                                    checked = isSubscribed,
                                    onCheckedChange = {
                                        val isSubscribing = libraryArtist?.artist?.bookmarkedAt == null
                                        database.transaction {
                                            val artist = libraryArtist?.artist
                                            if (artist != null) {
                                                update(artist.localToggleLike())
                                            } else {
                                                artistPage?.artist?.let {
                                                    insert(
                                                        ArtistEntity(
                                                            id = it.id,
                                                            name = it.title,
                                                            channelId = it.channelId,
                                                            thumbnailUrl = it.thumbnail,
                                                        ).localToggleLike()
                                                    )
                                                }
                                            }
                                        }

                                        coroutineScope.launch(Dispatchers.IO) {
                                            try {
                                                val artistId = artistPage?.artist?.id ?: libraryArtist?.artist?.id ?: return@launch
                                                val channelId = artistPage?.artist?.channelId ?: libraryArtist?.artist?.channelId
                                                val targetChannelId = channelId ?: YouTube.getChannelId(artistId)

                                                if (targetChannelId.isNotEmpty()) {
                                                    YouTube.subscribeChannel(targetChannelId, isSubscribing)
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .semantics { role = Role.Button },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                    shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
                                    colors = ToggleButtonDefaults.toggleButtonColors(
                                        containerColor = if (isSubscribed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = if (isSubscribed) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                ) {
                                    Icon(
                                        painter = painterResource(if (isSubscribed) R.drawable.subscribed else R.drawable.subscribe),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = stringResource(if (isSubscribed) R.string.subscribed else R.string.subscribe),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // Radio
                                artistPage?.artist?.radioEndpoint?.let { radioEndpoint ->
                                    ToggleButton(
                                        checked = false,
                                        onCheckedChange = {
                                            playerConnection.playQueue(YouTubeQueue(radioEndpoint))
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .semantics { role = Role.Button },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                        shapes = ButtonGroupDefaults.connectedMiddleButtonShapes(),
                                        colors = ToggleButtonDefaults.toggleButtonColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.radio),
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = stringResource(R.string.radio),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                // Shuffle
                                artistPage?.artist?.shuffleEndpoint?.let { shuffleEndpoint ->
                                    ToggleButton(
                                        checked = false,
                                        onCheckedChange = {
                                            playerConnection.playQueue(YouTubeQueue(shuffleEndpoint))
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .semantics { role = Role.Button },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                        shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
                                        colors = ToggleButtonDefaults.toggleButtonColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.shuffle),
                                            contentDescription = stringResource(R.string.shuffle),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = stringResource(R.string.shuffle),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // --- LOCAL LIBRARY SONGS ---
                if (librarySongs.isNotEmpty()) {
                    item {
                        NavigationTitle(
                            title = stringResource(R.string.filter_library),
                            onClick = { navController.navigate("artist/${viewModel.artistId}/songs") }
                        )
                    }

                    val filteredLibrarySongs = librarySongs

                    itemsIndexed(
                        items = filteredLibrarySongs.take(5),
                        key = { _, item -> "local_song_${item.id}" },
                    ) { index, song ->
                        SongListItem(
                            song = song,
                            showInLibraryIcon = true,
                            isActive = song.id == mediaMetadata?.id,
                            isPlaying = isPlaying,
                            trailingContent = {
                                IconButton(
                                    onClick = {
                                        menuState.show {
                                            SongMenu(
                                                originalSong = song,
                                                navController = navController,
                                                onDismiss = menuState::dismiss,
                                            )
                                        }
                                    },
                                ) {
                                    Icon(painterResource(R.drawable.more_vert), null)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        if (song.id == mediaMetadata?.id) {
                                            playerConnection.player.togglePlayPause()
                                        } else {
                                            playerConnection.playQueue(
                                                ListQueue(
                                                    title = libraryArtist?.artist?.name ?: "Unknown Artist",
                                                    items = librarySongs.map { it.toMediaItem() },
                                                    startIndex = index
                                                )
                                            )
                                        }
                                    },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        menuState.show {
                                            SongMenu(
                                                originalSong = song,
                                                navController = navController,
                                                onDismiss = menuState::dismiss,
                                            )
                                        }
                                    },
                                )
                                .animateItem(),
                        )
                    }

                    if (filteredLibrarySongs.size > 5) {
                        item {
                            Surface(
                                onClick = { navController.navigate("artist/${viewModel.artistId}/songs") },
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.view_all),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(vertical = 16.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // --- YOUTUBE REMOTE SECTIONS ---
                val topSongsSection = artistPage?.sections?.firstOrNull { s -> s.items.all { it is SongItem } }

                artistPage?.sections?.fastForEach { section ->
                    if (section.items.isNotEmpty()) {
                        item(
                            key = "youtube_section_header_${section.title}_${section.items.firstOrNull()?.id.orEmpty()}_${section.moreEndpoint?.browseId.orEmpty()}",
                        ) {
                            NavigationTitle(
                                title = getTranslatedArtistSectionTitle(section.title),
                                onClick = section.moreEndpoint?.let {
                                    { navController.navigate("artist/${viewModel.artistId}/items?browseId=${it.browseId}&params=${it.params}") }
                                },
                            )
                        }
                    }

                    if (section === topSongsSection) {
                        items(
                            items = section.items.distinctBy { it.id },
                            key = { "youtube_song_${it.id}" },
                        ) { song ->
                            YouTubeListItem(
                                item = song as SongItem,
                                isActive = mediaMetadata?.id == song.id,
                                isPlaying = isPlaying,
                                trailingContent = {
                                    IconButton(
                                        onClick = {
                                            menuState.show {
                                                YouTubeSongMenu(
                                                    song = song,
                                                    navController = navController,
                                                    onDismiss = menuState::dismiss,
                                                )
                                            }
                                        },
                                    ) {
                                        Icon(painterResource(R.drawable.more_vert), null)
                                    }
                                },
                                modifier = Modifier
                                    .combinedClickable(
                                        onClick = {
                                            if (song.id == mediaMetadata?.id) {
                                                playerConnection.player.togglePlayPause()
                                            } else {
                                                playerConnection.playQueue(
                                                    YouTubeQueue(WatchEndpoint(videoId = song.id), song.toMediaMetadata())
                                                )
                                            }
                                        },
                                        onLongClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            menuState.show {
                                                YouTubeSongMenu(
                                                    song = song,
                                                    navController = navController,
                                                    onDismiss = menuState::dismiss,
                                                )
                                            }
                                        },
                                    )
                                    .animateItem(),
                            )
                        }
                    } else {
                        item(
                            key = "youtube_section_grid_${section.title}_${section.items.firstOrNull()?.id.orEmpty()}_${section.moreEndpoint?.browseId.orEmpty()}",
                        ) {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(
                                    items = section.items.distinctBy { it.id },
                                    key = {
                                        val type = when (it) {
                                            is SongItem -> "song"
                                            is AlbumItem -> "album"
                                            is ArtistItem -> "artist"
                                            is PlaylistItem -> "playlist"
                                            is EpisodeItem -> "episode"
                                            is PodcastItem -> "podcast"
                                            else -> "item"
                                        }
                                        "youtube_${type}_${it.id}"
                                    },
                                ) { item ->
                                    YouTubeGridItem(
                                        item = item,
                                        isActive = when (item) {
                                            is SongItem -> mediaMetadata?.id == item.id
                                            is AlbumItem -> mediaMetadata?.album?.id == item.id
                                            is EpisodeItem -> mediaMetadata?.id == item.id
                                            else -> false
                                        },
                                        isPlaying = isPlaying,
                                        coroutineScope = coroutineScope,
                                        modifier = Modifier
                                            .combinedClickable(
                                                onClick = {
                                                    when (item) {
                                                        is SongItem -> playerConnection.playQueue(YouTubeQueue(WatchEndpoint(videoId = item.id), item.toMediaMetadata()))
                                                        is EpisodeItem -> playerConnection.playQueue(YouTubeQueue(WatchEndpoint(videoId = item.id), item.asSongItem().toMediaMetadata()))
                                                        is AlbumItem -> navController.navigate("album/${item.id}")
                                                        is ArtistItem -> navController.navigate("artist/${item.id}")
                                                        is PlaylistItem -> navController.navigate("online_playlist/${item.id}")
                                                        is PodcastItem -> navController.navigate("online_playlist/${item.id}")
                                                    }
                                                },
                                                onLongClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    menuState.show {
                                                        when (item) {
                                                            is SongItem -> YouTubeSongMenu(song = item, navController = navController, onDismiss = menuState::dismiss)
                                                            is AlbumItem -> YouTubeAlbumMenu(albumItem = item, navController = navController, onDismiss = menuState::dismiss)
                                                            is ArtistItem -> YouTubeArtistMenu(artist = item, onDismiss = menuState::dismiss)
                                                            is PlaylistItem -> YouTubePlaylistMenu(playlist = item, coroutineScope = coroutineScope, onDismiss = menuState::dismiss)
                                                            is EpisodeItem -> YouTubeSongMenu(song = item.asSongItem(), navController = navController, onDismiss = menuState::dismiss)
                                                            is PodcastItem -> YouTubePlaylistMenu(playlist = item.asPlaylistItem(), coroutineScope = coroutineScope, onDismiss = menuState::dismiss)
                                                        }
                                                    }
                                                },
                                            )
                                            .animateItem(),
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        // --- 3. DYNAMIC TOP APP BAR ---
        TopAppBar(
            title = {
                AnimatedVisibility(
                    visible = topBarProgress > 0.8f,
                    enter = fadeIn(tween(250)),
                    exit = fadeOut(tween(200))
                ) {
                    Text(
                        text = artistName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = navController::navigateUp,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = buttonBgAlpha))
                ) {
                    Icon(
                        painterResource(R.drawable.arrow_back),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        artistPage?.artist?.shareLink?.let { link ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Artist Link", link)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, R.string.link_copied, Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = buttonBgAlpha))
                ) {
                    Icon(
                        painterResource(R.drawable.link),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = topBarContainerColor,
                scrolledContainerColor = topBarContainerColor
            ),
            modifier = Modifier.zIndex(10f)
        )
    }
}

@Composable
private fun StatBadge(icon: Int, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun getTranslatedArtistSectionTitle(title: String): String {
    return when {
        title.equals("top songs", ignoreCase = true) -> stringResource(R.string.artist_top_songs)
        title.equals("albums", ignoreCase = true) -> stringResource(R.string.artist_albums)
        title.equals("singles & eps", ignoreCase = true) || title.equals("singles and eps", ignoreCase = true) -> stringResource(R.string.artist_singles_eps)
        title.equals("videos", ignoreCase = true) -> stringResource(R.string.artist_videos)
        title.equals("live performances", ignoreCase = true) -> stringResource(R.string.artist_live_performances)
        title.equals("featured on", ignoreCase = true) -> stringResource(R.string.artist_featured_on)
        title.equals("fans might also like", ignoreCase = true) || title.equals("fan might also like", ignoreCase = true) -> stringResource(R.string.artist_fans_might_also_like)
        title.startsWith("playlists by", ignoreCase = true) -> {
            val artistName = title.substringAfter("by", "").trim()
            if (artistName.isNotEmpty()) {
                stringResource(R.string.artist_playlists_by, artistName)
            } else {
                stringResource(R.string.artist_playlists)
            }
        }
        else -> title
    }
}
