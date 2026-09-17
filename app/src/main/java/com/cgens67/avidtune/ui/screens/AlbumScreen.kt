package com.cgens67.gluetune.ui.screens

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.navigation.NavController
import com.cgens67.gluetune.LocalDatabase
import com.cgens67.gluetune.LocalDownloadUtil
import com.cgens67.gluetune.LocalPlayerAwareWindowInsets
import com.cgens67.gluetune.LocalPlayerConnection
import com.cgens67.gluetune.R
import com.cgens67.gluetune.constants.CoverResolution
import com.cgens67.gluetune.constants.CoverResolutionKey
import com.cgens67.gluetune.constants.HideExplicitKey
import com.cgens67.gluetune.constants.HideMusicVideosKey
import com.cgens67.gluetune.constants.EnableArtistCanvasKey
import com.cgens67.gluetune.db.entities.Album
import com.cgens67.gluetune.extensions.togglePlayPause
import com.cgens67.gluetune.playback.ExoDownloadService
import com.cgens67.gluetune.playback.queues.LocalAlbumRadio
import com.cgens67.gluetune.ui.component.LocalMenuState
import com.cgens67.gluetune.ui.component.NavigationTitle
import com.cgens67.gluetune.ui.component.SongListItem
import com.cgens67.gluetune.ui.component.YouTubeGridItem
import com.cgens67.gluetune.ui.component.shimmer.ListItemPlaceHolder
import com.cgens67.gluetune.ui.component.shimmer.ShimmerHost
import com.cgens67.gluetune.ui.menu.AlbumMenu
import com.cgens67.gluetune.ui.menu.SelectionSongMenu
import com.cgens67.gluetune.ui.menu.SongMenu
import com.cgens67.gluetune.ui.menu.YouTubeAlbumMenu
import com.cgens67.gluetune.ui.utils.ItemWrapper
import com.cgens67.gluetune.ui.utils.resize
import com.cgens67.gluetune.utils.rememberEnumPreference
import com.cgens67.gluetune.utils.rememberPreference
import com.cgens67.gluetune.viewmodels.AlbumViewModel
import com.cgens67.gluetune.ui.component.ArtistVideo
import com.cgens67.gluetune.ui.component.ArtistCanvasHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

@SuppressLint("LocalContextGetResourceValueCall")
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AlbumScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: AlbumViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val menuState = LocalMenuState.current
    val database = LocalDatabase.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val playerConnection = LocalPlayerConnection.current ?: return

    val scope = rememberCoroutineScope()

    val isPlaying by playerConnection.isPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val (coverResolution) = rememberEnumPreference(
        key = CoverResolutionKey,
        defaultValue = CoverResolution.RES_1080
    )

    val playlistId by viewModel.playlistId.collectAsState()
    val albumWithSongs by viewModel.albumWithSongs.collectAsState()
    val otherVersions by viewModel.otherVersions.collectAsState()
    val albumDescription by viewModel.albumDescription.collectAsState()
    val (enableArtistCanvas) = rememberPreference(EnableArtistCanvasKey, defaultValue = true)

    val wrappedSongs = albumWithSongs?.songs?.map { item -> ItemWrapper(item) }?.toMutableList()
    var selection by remember {
        mutableStateOf(false)
    }

    if (selection) {
        BackHandler {
            selection = false
        }
    }

    val downloadUtil = LocalDownloadUtil.current
    var downloadState by remember {
        mutableStateOf(Download.STATE_STOPPED)
    }

    var isDescriptionLoading by remember { mutableStateOf(true) }

    LaunchedEffect(albumWithSongs?.album?.id) {
        if (albumWithSongs != null) {
            if (albumDescription == null) {
                isDescriptionLoading = true
                delay(8000)
                isDescriptionLoading = false
            } else {
                isDescriptionLoading = false
            }
        }
    }

    LaunchedEffect(albumDescription) {
        if (albumDescription != null) {
            isDescriptionLoading = false
        }
    }

    LaunchedEffect(albumWithSongs) {
        val songs = albumWithSongs?.songs?.map { it.id }
        if (songs.isNullOrEmpty()) return@LaunchedEffect
        downloadUtil.downloads.collect { downloads ->
            downloadState =
                if (songs.all { downloads[it]?.state == Download.STATE_COMPLETED }) {
                    Download.STATE_COMPLETED
                } else if (songs.all {
                        downloads[it]?.state == Download.STATE_QUEUED ||
                                downloads[it]?.state == Download.STATE_DOWNLOADING ||
                                downloads[it]?.state == Download.STATE_COMPLETED
                    }
                ) {
                    Download.STATE_DOWNLOADING
                } else {
                    Download.STATE_STOPPED
                }
        }
    }

    // Artist Canvas
    val artistName = albumWithSongs?.artists?.joinToString { it.name }
    var artistVideoUrl by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(artistName, enableArtistCanvas) {
        if (enableArtistCanvas && !artistName.isNullOrBlank() && artistName != context.getString(R.string.unknown)) {
            artistVideoUrl = ArtistCanvasHelper.getArtistCanvas(context, artistName)
        } else {
            artistVideoUrl = null
        }
    }

    val lazyListState = rememberLazyListState()

    val transparentAppBar by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex == 0 && lazyListState.firstVisibleItemScrollOffset < 200
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Blurred immersive background
        AsyncImage(
            model = albumWithSongs?.album?.thumbnailUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(80.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
        )

        CompositionLocalProvider(LocalContentColor provides Color.White) {
            LazyColumn(
                state = lazyListState,
                contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues(),
                modifier = Modifier.fillMaxSize()
            ) {
                val albumWithSongs = albumWithSongs
                if (albumWithSongs != null && albumWithSongs.songs.isNotEmpty()) {
                    item(key = "album_header") {
                        val systemBarsTopPadding = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = systemBarsTopPadding + 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // 1. Centered Square Cover Art
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .aspectRatio(1f)
                                    .shadow(elevation = 32.dp, shape = RoundedCornerShape(16.dp))
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = 0.1f))
                            ) {
                                AsyncImage(
                                    model = albumWithSongs.album.thumbnailUrl?.resize(coverResolution.size, coverResolution.size),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                artistVideoUrl?.let { url ->
                                    ArtistVideo(
                                        videoUrl = url,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            Spacer(Modifier.height(32.dp))

                            // 2. Title and Artist
                            Text(
                                text = albumWithSongs.album.title,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )

                            Spacer(Modifier.height(8.dp))

                            val artistNameLocal = albumWithSongs.artists.joinToString { it.name }
                            Text(
                                text = artistNameLocal,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(horizontal = 24.dp)
                                    .clickable {
                                        if (albumWithSongs.artists.size == 1) {
                                            navController.navigate("artist/${albumWithSongs.artists.first().id}")
                                        }
                                    }
                            )

                            Spacer(Modifier.height(16.dp))

                            // 3. Album Metadata (Year, Tracks, Duration)
                            val albumInfoText = buildString {
                                if (albumWithSongs.album.year != null) {
                                    append("${albumWithSongs.album.year} • ")
                                }
                                append("${albumWithSongs.songs.size} Tracks")
                                val totalDuration = albumWithSongs.songs.sumOf { it.song.duration ?: 0 }
                                val hours = totalDuration / 3600
                                val minutes = (totalDuration % 3600) / 60
                                if (hours > 0) {
                                    append(" • ${hours}h ${minutes}m")
                                } else {
                                    append(" • ${minutes}m")
                                }
                            }
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = albumInfoText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.5f),
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(Modifier.height(24.dp))

                            // 4. Action Buttons matching the Player style
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 32.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Shuffle (Circle)
                                Surface(
                                    onClick = {
                                        playerConnection.service.getAutomix(playlistId)
                                        playerConnection.playQueue(
                                            LocalAlbumRadio(albumWithSongs.copy(songs = albumWithSongs.songs.shuffled())),
                                        )
                                    },
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.1f),
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(painterResource(R.drawable.shuffle), null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }

                                // Download / Remove Download (Circle)
                                Surface(
                                    onClick = {
                                        when (downloadState) {
                                            Download.STATE_COMPLETED, Download.STATE_DOWNLOADING -> {
                                                albumWithSongs.songs.forEach { song ->
                                                    DownloadService.sendRemoveDownload(
                                                        context,
                                                        ExoDownloadService::class.java,
                                                        song.id,
                                                        false,
                                                    )
                                                }
                                            }
                                            else -> {
                                                albumWithSongs.songs.forEach { song ->
                                                    val downloadRequest = DownloadRequest.Builder(song.id, song.id.toUri())
                                                        .setCustomCacheKey(song.id)
                                                        .setData(song.song.title.toByteArray())
                                                        .build()
                                                    DownloadService.sendAddDownload(context, ExoDownloadService::class.java, downloadRequest, false)
                                                }
                                            }
                                        }
                                    },
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.1f),
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        when (downloadState) {
                                            Download.STATE_COMPLETED -> Icon(painterResource(R.drawable.offline), null, tint = Color.White, modifier = Modifier.size(20.dp))
                                            Download.STATE_DOWNLOADING -> CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                                            else -> Icon(painterResource(R.drawable.download), null, tint = Color.White, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }

                                // Play (Large Circle)
                                val isActiveAlbum = mediaMetadata?.album?.id == albumWithSongs.album.id
                                Surface(
                                    onClick = {
                                        if (isPlaying && isActiveAlbum) {
                                            playerConnection.player.pause()
                                        } else if (isActiveAlbum) {
                                            playerConnection.player.play()
                                        } else {
                                            playerConnection.service.getAutomix(playlistId)
                                            playerConnection.playQueue(LocalAlbumRadio(albumWithSongs))
                                        }
                                    },
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.15f),
                                    modifier = Modifier.size(64.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            painter = painterResource(
                                                if (isPlaying && isActiveAlbum)
                                                    R.drawable.pause
                                                else
                                                    R.drawable.play
                                            ),
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }

                                // Share (Circle)
                                Surface(
                                    onClick = {
                                        val intent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, context.getString(R.string.check_out_album_share, albumWithSongs.album.title, albumWithSongs.artists.joinToString { it.name }, "https://music.youtube.com/playlist?list=${albumWithSongs.album.playlistId}"))
                                        }
                                        context.startActivity(Intent.createChooser(intent, null))
                                    },
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.1f),
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(painterResource(R.drawable.share), null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }

                                // Like (Circle)
                                Surface(
                                    onClick = {
                                        database.query { update(albumWithSongs.album.toggleLike()) }
                                    },
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.1f),
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            painter = painterResource(
                                                if (albumWithSongs.album.bookmarkedAt != null) R.drawable.favorite
                                                else R.drawable.favorite_border
                                            ),
                                            contentDescription = null,
                                            tint = if (albumWithSongs.album.bookmarkedAt != null) MaterialTheme.colorScheme.error else Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                            
                            Spacer(Modifier.height(32.dp))
                            
                            // Description (if exists)
                            if (isDescriptionLoading) {
                                ShimmerHost(modifier = Modifier.padding(horizontal = 32.dp)) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Spacer(Modifier.fillMaxWidth().height(14.dp).background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)))
                                        Spacer(Modifier.fillMaxWidth(0.8f).height(14.dp).background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)))
                                    }
                                }
                            } else if (albumDescription != null) {
                                var isDescriptionExpanded by rememberSaveable { mutableStateOf(false) }
                                Text(
                                    text = albumDescription!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier
                                        .padding(horizontal = 32.dp)
                                        .fillMaxWidth()
                                        .animateContentSize()
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = { isDescriptionExpanded = !isDescriptionExpanded }
                                        ),
                                    maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            
                            Spacer(Modifier.height(16.dp))
                        }
                    }

                    // Songs List
                    if (wrappedSongs != null && wrappedSongs.isNotEmpty()) {
                        items(
                            items = wrappedSongs,
                            key = { song -> song.item.id },
                        ) { songWrapper ->
                            Surface(
                                color = Color.Transparent,
                                contentColor = Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 2.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = if (songWrapper.item.id == mediaMetadata?.id) 0.15f else 0.05f))
                            ) {
                                SongListItem(
                                    song = songWrapper.item,
                                    albumIndex = wrappedSongs.indexOf(songWrapper) + 1,
                                    isActive = songWrapper.item.id == mediaMetadata?.id,
                                    isPlaying = isPlaying,
                                    showInLibraryIcon = true,
                                    trailingContent = {
                                        IconButton(
                                            onClick = {
                                                menuState.show {
                                                    SongMenu(
                                                        originalSong = songWrapper.item,
                                                        navController = navController,
                                                        onDismiss = menuState::dismiss,
                                                    )
                                                }
                                            },
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.more_vert),
                                                contentDescription = null,
                                                tint = Color.White
                                            )
                                        }
                                    },
                                    isSelected = songWrapper.isSelected && selection,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(
                                            onClick = {
                                                if (!selection) {
                                                    if (songWrapper.item.id == mediaMetadata?.id) {
                                                        playerConnection.player.togglePlayPause()
                                                    } else {
                                                        playerConnection.service.getAutomix(playlistId)
                                                        playerConnection.playQueue(
                                                            LocalAlbumRadio(
                                                                albumWithSongs,
                                                                startIndex = wrappedSongs.indexOf(songWrapper)
                                                            ),
                                                        )
                                                    }
                                                } else {
                                                    songWrapper.isSelected = !songWrapper.isSelected
                                                }
                                            },
                                            onLongClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                if (!selection) {
                                                    selection = true
                                                }
                                                wrappedSongs.forEach {
                                                    it.isSelected = false
                                                }
                                                songWrapper.isSelected = true
                                            },
                                        ),
                                )
                            }
                        }
                    }

                    // Other Versions Section
                    if (otherVersions.isNotEmpty()) {
                        item(key = "other_versions_title") {
                            NavigationTitle(
                                title = stringResource(R.string.other_versions),
                                modifier = Modifier.animateItem()
                            )
                        }
                        item(key = "other_versions_list") {
                            LazyRow(
                                contentPadding = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal).asPaddingValues(),
                            ) {
                                items(
                                    items = otherVersions.distinctBy { it.id },
                                    key = { it.id },
                                ) { item ->
                                    YouTubeGridItem(
                                        item = item,
                                        isActive = mediaMetadata?.album?.id == item.id,
                                        isPlaying = isPlaying,
                                        coroutineScope = scope,
                                        modifier =
                                        Modifier
                                            .combinedClickable(
                                                onClick = { navController.navigate("album/${item.id}") },
                                                onLongClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    menuState.show {
                                                        YouTubeAlbumMenu(
                                                            albumItem = item,
                                                            navController = navController,
                                                            onDismiss = menuState::dismiss,
                                                        )
                                                    }
                                                },
                                            )
                                            .animateItem(),
                                    )
                                }
                            }
                        }
                    }

                } else {
                    // Loading indicator
                    item(key = "loading_indicator") {
                        Box(
                            modifier = Modifier
                                .fillParentMaxSize()
                                .padding(bottom = 64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ContainedLoadingIndicator()
                        }
                    }
                }
            }
        }

        // Top App Bar
        TopAppBar(
            title = {
                if (selection) {
                    val count = wrappedSongs?.count { it.isSelected } ?: 0
                    Text(
                        text = pluralStringResource(R.plurals.n_song, count, count),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                } else {
                    AnimatedVisibility(visible = !transparentAppBar) {
                        Text(
                            text = albumWithSongs?.album?.title.orEmpty(),
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = Color.White
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = {
                        if (selection) {
                            selection = false
                        } else {
                            navController.navigateUp()
                        }
                    },
                ) {
                    Icon(
                        painter = painterResource(
                            if (selection) R.drawable.close else R.drawable.arrow_back
                        ),
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            },
            actions = {
                if (selection) {
                    val count = wrappedSongs?.count { it.isSelected } ?: 0
                    IconButton(
                        onClick = {
                            if (count == wrappedSongs?.size) {
                                wrappedSongs.forEach { it.isSelected = false }
                            } else {
                                wrappedSongs?.forEach { it.isSelected = true }
                            }
                        },
                    ) {
                        Icon(
                            painter = painterResource(
                                if (count == wrappedSongs?.size) R.drawable.deselect else R.drawable.select_all
                            ),
                            contentDescription = null,
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = {
                            menuState.show {
                                SelectionSongMenu(
                                    songSelection = wrappedSongs?.filter { it.isSelected }!!
                                        .map { it.item },
                                    onDismiss = menuState::dismiss,
                                    clearAction = { selection = false }
                                )
                            }
                        },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.more_vert),
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                } else {
                    albumWithSongs?.let { albumWithSongs ->
                        IconButton(
                            onClick = {
                                menuState.show {
                                    AlbumMenu(
                                        originalAlbum = Album(
                                            albumWithSongs.album,
                                            albumWithSongs.artists
                                        ),
                                        navController = navController,
                                        onDismiss = menuState::dismiss,
                                    )
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.more_vert),
                                contentDescription = stringResource(R.string.more_options),
                                tint = Color.White
                            )
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = if (transparentAppBar && !selection) Color.Transparent else Color.Black.copy(alpha = 0.5f),
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White,
                actionIconContentColor = Color.White
            ),
            scrollBehavior = scrollBehavior
        )
    }
}

suspend fun saveAlbumImageToGallery(context: Context, imageUrl: String, albumTitle: String) {
    try {
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .build()

        val drawable = context.imageLoader.execute(request).drawable

        if (drawable != null) {
            val bitmap = drawable.toBitmap()

            val displayName = "${albumTitle.replace(" ", "_")}.png"
            val mimeType = "image/png"

            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }

            val contentResolver = context.contentResolver
            val uri = contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )

            uri?.let {
                contentResolver.openOutputStream(it)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Save Picture Success",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    } catch (e: IOException) {
        withContext(Dispatchers.Main) {
            Toast.makeText(
                context,
                "X",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
