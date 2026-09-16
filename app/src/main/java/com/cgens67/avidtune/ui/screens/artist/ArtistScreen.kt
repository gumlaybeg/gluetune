package com.cgens67.gluetune.ui.screens.artist

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
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
import com.cgens67.gluetune.constants.AppBarHeight
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
import com.cgens67.gluetune.ui.utils.backToMain
import com.cgens67.gluetune.ui.utils.fadingEdge
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
import kotlin.math.max

@SuppressLint("ServiceCast")
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class,
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

    // Gradient colors for background
    var gradientColors by remember { mutableStateOf<List<Color>>(emptyList()) }
    val fallbackColor = MaterialTheme.colorScheme.surface.toArgb()
    val surfaceColor = MaterialTheme.colorScheme.surface

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

    // Skeleton loading state
    var isFetching by remember { mutableStateOf(true) }
    LaunchedEffect(artistPage) {
        if (artistPage != null) {
            isFetching = false
        }
    }
    LaunchedEffect(Unit) {
        delay(2500)
        isFetching = false
    }

    // Extract gradient colors
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

    // Dynamic scroll calculations for parallax & top bar
    val firstVisibleIndex by remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }
    val firstVisibleScrollOffset by remember { derivedStateOf { lazyListState.firstVisibleItemScrollOffset } }
    
    val parallaxOffset by remember {
        derivedStateOf {
            if (firstVisibleIndex == 0) firstVisibleScrollOffset.toFloat() * 0.4f else 0f
        }
    }

    val headerAlpha by remember {
        derivedStateOf {
            if (firstVisibleIndex == 0) max(0f, 1f - (firstVisibleScrollOffset / 800f)) else 0f
        }
    }

    // Top App Bar animated states
    val isTopBarScrolled = firstVisibleIndex > 0 || firstVisibleScrollOffset > 250
    val topBarContainerColor by animateColorAsState(
        targetValue = if (isTopBarScrolled) MaterialTheme.colorScheme.surface else Color.Transparent,
        animationSpec = tween(durationMillis = 300),
        label = "TopBarColorAnimation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceColor)
    ) {
        // Subtle ambient background based on gradient palette
        if (gradientColors.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(-2f)
                    .drawBehind {
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    gradientColors.firstOrNull()?.copy(alpha = 0.15f) ?: Color.Transparent,
                                    surfaceColor
                                ),
                                endY = size.height * 0.7f
                            )
                        )
                    }
            )
        }

        // Main Content List
        LazyColumn(
            state = lazyListState,
            contentPadding = LocalPlayerAwareWindowInsets.current
                .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
                .asPaddingValues(),
            modifier = Modifier.fillMaxSize()
        ) {
            if (isFetching && artistPage == null) {
                item(key = "shimmer") {
                    ShimmerHost {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                        Column(modifier = Modifier.padding(16.dp)) {
                            TextPlaceholder(height = 32.dp, modifier = Modifier.fillMaxWidth(0.6f).padding(bottom = 16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                                Spacer(modifier = Modifier.height(32.dp).width(90.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface))
                                Spacer(modifier = Modifier.height(32.dp).width(110.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface))
                            }
                            TextPlaceholder(modifier = Modifier.fillMaxWidth())
                            TextPlaceholder(modifier = Modifier.fillMaxWidth(0.8f))
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Spacer(modifier = Modifier.height(48.dp).weight(1f).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface))
                                Spacer(modifier = Modifier.height(48.dp).weight(1f).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface))
                            }
                        }
                        repeat(6) { ListItemPlaceHolder() }
                    }
                }
            } else {

                // Header Parallax Image
                item(key = "header_image") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .graphicsLayer {
                                translationY = parallaxOffset
                                alpha = headerAlpha
                            }
                    ) {
                        if (thumbnail != null) {
                            AsyncImage(
                                model = thumbnail.resize(1200, 1200),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }

                        artistVideoUrl?.let { url ->
                            ArtistVideo(
                                videoUrl = url,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Gradient Scrim to smoothly fade into surface
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Transparent,
                                            surfaceColor.copy(alpha = 0.6f),
                                            surfaceColor
                                        ),
                                        startY = 0f
                                    )
                                )
                        )
                    }
                }

                // Artist Info & Actions (Overlapping the header image slightly)
                item(key = "header_info") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer { translationY = -80f }
                            .padding(horizontal = 20.dp)
                    ) {
                        // Artist Name
                        Text(
                            text = artistName,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Badges (Subscribers, Monthly Listeners, Plays)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(bottom = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            artistPage?.subscriberCountText?.let { subscribers ->
                                BadgeChip(
                                    icon = R.drawable.person,
                                    text = subscribers.split(" ").firstOrNull() ?: subscribers,
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            artistPage?.monthlyListenerCount?.let { monthlyListeners ->
                                BadgeChip(
                                    icon = R.drawable.play,
                                    text = monthlyListeners.split(" ").firstOrNull() ?: monthlyListeners,
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            if (totalPlayCount > 0) {
                                BadgeChip(
                                    icon = R.drawable.history,
                                    text = totalPlayCount.toString(),
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }

                        // Description
                        var isDescriptionExpanded by rememberSaveable { mutableStateOf(false) }
                        val fallbackDesc = "$artistName is a music artist."
                        val description = artistPage?.description?.substringBefore("From Wikipedia")?.trim() ?: fallbackDesc

                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize(animationSpec = tween(300))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { isDescriptionExpanded = !isDescriptionExpanded }
                                )
                                .padding(bottom = 24.dp),
                            maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 3,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 22.sp
                        )

                        // Modern Action Buttons Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Left side: Subscribe & Radio
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val isSubscribed = libraryArtist?.artist?.bookmarkedAt != null
                                FilledTonalButton(
                                    onClick = {
                                        val isSubscribing = !isSubscribed
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
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = if (isSubscribed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (isSubscribed) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(if (isSubscribed) R.drawable.subscribed else R.drawable.subscribe),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(if (isSubscribed) R.string.subscribed else R.string.subscribe),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                artistPage?.artist?.radioEndpoint?.let { radioEndpoint ->
                                    IconButton(
                                        onClick = { playerConnection.playQueue(YouTubeQueue(radioEndpoint)) },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.radio),
                                            contentDescription = stringResource(R.string.radio),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }

                            // Right side: Prominent Shuffle Button
                            artistPage?.artist?.shuffleEndpoint?.let { shuffleEndpoint ->
                                FloatingActionButton(
                                    onClick = { playerConnection.playQueue(YouTubeQueue(shuffleEndpoint)) },
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                                    shape = CircleShape,
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.shuffle),
                                        contentDescription = stringResource(R.string.shuffle),
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Local Songs Section
                if (librarySongs.isNotEmpty()) {
                    item {
                        NavigationTitle(
                            title = stringResource(R.string.filter_library),
                            onClick = { navController.navigate("artist/${viewModel.artistId}/songs") },
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
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
                                    Icon(painter = painterResource(R.drawable.more_vert), contentDescription = null)
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
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.view_all),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 14.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // YouTube Sections
                val topSongsSection = artistPage?.sections?.firstOrNull { s -> s.items.all { it is SongItem } }

                artistPage?.sections?.fastForEach { section ->
                    if (section.items.isNotEmpty()) {
                        item(
                            key = "youtube_section_header_${section.title}_${section.items.firstOrNull()?.id.orEmpty()}_${section.moreEndpoint?.browseId.orEmpty()}",
                        ) {
                            NavigationTitle(
                                title = getTranslatedArtistSectionTitle(section.title),
                                onClick = section.moreEndpoint?.let {
                                    {
                                        navController.navigate(
                                            "artist/${viewModel.artistId}/items?browseId=${it.browseId}?params=${it.params}",
                                        )
                                    }
                                },
                                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
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
                                        Icon(painter = painterResource(R.drawable.more_vert), contentDescription = null)
                                    }
                                },
                                modifier = Modifier
                                    .combinedClickable(
                                        onClick = {
                                            if (song.id == mediaMetadata?.id) {
                                                playerConnection.player.togglePlayPause()
                                            } else {
                                                playerConnection.playQueue(
                                                    YouTubeQueue(
                                                        WatchEndpoint(videoId = song.id),
                                                        song.toMediaMetadata()
                                                    ),
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
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
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
                                            .width(150.dp) // Slightly wider for a premium feel
                                            .combinedClickable(
                                                onClick = {
                                                    when (item) {
                                                        is SongItem ->
                                                            playerConnection.playQueue(
                                                                YouTubeQueue(
                                                                    WatchEndpoint(videoId = item.id),
                                                                    item.toMediaMetadata()
                                                                ),
                                                            )
                                                        is EpisodeItem ->
                                                            playerConnection.playQueue(
                                                                YouTubeQueue(
                                                                    WatchEndpoint(videoId = item.id),
                                                                    item.asSongItem().toMediaMetadata()
                                                                ),
                                                            )
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
                                                            is SongItem -> YouTubeSongMenu(
                                                                song = item,
                                                                navController = navController,
                                                                onDismiss = menuState::dismiss,
                                                            )
                                                            is AlbumItem -> YouTubeAlbumMenu(
                                                                albumItem = item,
                                                                navController = navController,
                                                                onDismiss = menuState::dismiss,
                                                            )
                                                            is ArtistItem -> YouTubeArtistMenu(
                                                                artist = item,
                                                                onDismiss = menuState::dismiss,
                                                            )
                                                            is PlaylistItem -> YouTubePlaylistMenu(
                                                                playlist = item,
                                                                coroutineScope = coroutineScope,
                                                                onDismiss = menuState::dismiss,
                                                            )
                                                            is EpisodeItem -> YouTubeSongMenu(
                                                                song = item.asSongItem(),
                                                                navController = navController,
                                                                onDismiss = menuState::dismiss,
                                                            )
                                                            is PodcastItem -> YouTubePlaylistMenu(
                                                                playlist = item.asPlaylistItem(),
                                                                coroutineScope = coroutineScope,
                                                                onDismiss = menuState::dismiss,
                                                            )
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

                item { Spacer(modifier = Modifier.height(48.dp)) }
            }
        }

        // Top App Bar overlapping the content
        TopAppBar(
            title = {
                if (isTopBarScrolled) {
                    Text(
                        text = artistName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            modifier = Modifier
                .zIndex(1f)
                .shadow(if (isTopBarScrolled) 4.dp else 0.dp),
            navigationIcon = {
                IconButton(
                    onClick = navController::navigateUp,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clip(CircleShape)
                        .background(if (isTopBarScrolled) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Icon(
                        painterResource(R.drawable.arrow_back),
                        contentDescription = null,
                        tint = if (isTopBarScrolled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
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
                        .background(if (isTopBarScrolled) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Icon(
                        painterResource(R.drawable.link),
                        contentDescription = null,
                        tint = if (isTopBarScrolled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = topBarContainerColor,
                scrolledContainerColor = topBarContainerColor
            )
        )
    }
}

@Composable
private fun BadgeChip(icon: Int, text: String, containerColor: Color, contentColor: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor.copy(alpha = 0.9f))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = contentColor
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            fontWeight = FontWeight.Bold
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
