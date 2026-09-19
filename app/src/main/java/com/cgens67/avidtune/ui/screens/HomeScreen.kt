@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalFoundationApi::class,
)

package com.cgens67.gluetune.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.carousel.HorizontalCenteredHeroCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.cgens67.innertube.models.AlbumItem
import com.cgens67.innertube.models.ArtistItem
import com.cgens67.innertube.models.EpisodeItem
import com.cgens67.innertube.models.PlaylistItem
import com.cgens67.innertube.models.PodcastItem
import com.cgens67.innertube.models.SongItem
import com.cgens67.innertube.models.WatchEndpoint
import com.cgens67.innertube.models.YTItem
import com.cgens67.innertube.utils.parseCookieString
import com.cgens67.gluetune.LocalDatabase
import com.cgens67.gluetune.LocalPlayerAwareWindowInsets
import com.cgens67.gluetune.LocalPlayerConnection
import com.cgens67.gluetune.R
import com.cgens67.gluetune.constants.AccountNameKey
import com.cgens67.gluetune.constants.CoverResolution
import com.cgens67.gluetune.constants.CoverResolutionKey
import com.cgens67.gluetune.constants.DisableBlurKey
import com.cgens67.gluetune.constants.GridThumbnailHeight
import com.cgens67.gluetune.constants.InnerTubeCookieKey
import com.cgens67.gluetune.constants.ListItemHeight
import com.cgens67.gluetune.constants.ListThumbnailSize
import com.cgens67.gluetune.constants.PlayerBackgroundStyleKey
import com.cgens67.gluetune.constants.PureBlackKey
import com.cgens67.gluetune.constants.SwipeThumbnailKey
import com.cgens67.gluetune.constants.ThumbnailCornerRadius
import com.cgens67.gluetune.db.entities.Album
import com.cgens67.gluetune.db.entities.Artist
import com.cgens67.gluetune.db.entities.LocalItem
import com.cgens67.gluetune.db.entities.Playlist
import com.cgens67.gluetune.db.entities.Song
import com.cgens67.gluetune.extensions.togglePlayPause
import com.cgens67.gluetune.models.MediaMetadata
import com.cgens67.gluetune.models.toMediaMetadata
import com.cgens67.gluetune.playback.PlayerConnection
import com.cgens67.gluetune.playback.queues.LocalAlbumRadio
import com.cgens67.gluetune.playback.queues.YouTubeAlbumRadio
import com.cgens67.gluetune.playback.queues.YouTubeQueue
import com.cgens67.gluetune.ui.component.AlbumGridItem
import com.cgens67.gluetune.ui.component.ArtistGridItem
import com.cgens67.gluetune.ui.component.LocalMenuState
import com.cgens67.gluetune.ui.component.MenuState
import com.cgens67.gluetune.ui.component.NavigationTitle
import com.cgens67.gluetune.ui.component.SongGridItem
import com.cgens67.gluetune.ui.component.SongListItem
import com.cgens67.gluetune.ui.component.YouTubeGridItem
import com.cgens67.gluetune.ui.component.shimmer.GridItemPlaceHolder
import com.cgens67.gluetune.ui.component.shimmer.ShimmerHost
import com.cgens67.gluetune.ui.component.shimmer.TextPlaceholder
import com.cgens67.gluetune.ui.menu.AlbumMenu
import com.cgens67.gluetune.ui.menu.ArtistMenu
import com.cgens67.gluetune.ui.menu.SongMenu
import com.cgens67.gluetune.ui.menu.YouTubeAlbumMenu
import com.cgens67.gluetune.ui.menu.YouTubeArtistMenu
import com.cgens67.gluetune.ui.menu.YouTubePlaylistMenu
import com.cgens67.gluetune.ui.menu.YouTubeSongMenu
import com.cgens67.gluetune.ui.utils.SnapLayoutInfoProvider
import com.cgens67.gluetune.ui.utils.backToMain
import com.cgens67.gluetune.ui.utils.isScrollingUp
import com.cgens67.gluetune.ui.utils.resize
import com.cgens67.gluetune.utils.rememberEnumPreference
import com.cgens67.gluetune.utils.rememberPreference
import com.cgens67.gluetune.viewmodels.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import kotlin.math.min
import kotlin.random.Random

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val menuState = LocalMenuState.current
    val database = LocalDatabase.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val haptic = LocalHapticFeedback.current

    val isPlaying by playerConnection.isPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val quickPicks by viewModel.quickPicks.collectAsState()
    val forgottenFavorites by viewModel.forgottenFavorites.collectAsState()
    val keepListening by viewModel.keepListening.collectAsState()
    val similarRecommendations by viewModel.similarRecommendations.collectAsState()
    val accountPlaylists by viewModel.accountPlaylists.collectAsState()
    val homePage by viewModel.homePage.collectAsState()
    val explorePage by viewModel.explorePage.collectAsState()

    val allLocalItems by viewModel.allLocalItems.collectAsState()
    val allYtItems by viewModel.allYtItems.collectAsState()

    val isLoading: Boolean by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val pullRefreshState = rememberPullToRefreshState()

    val forgottenFavoritesLazyGridState = rememberLazyGridState()

    val accountName by rememberPreference(AccountNameKey, "")
    val accountImageUrl by viewModel.accountImageUrl.collectAsState()
    val innerTubeCookie by rememberPreference(InnerTubeCookieKey, "")
    val isLoggedIn = remember(innerTubeCookie) {
        "SAPISID" in parseCookieString(innerTubeCookie)
    }
    val url = if (isLoggedIn) accountImageUrl else null

    val (disableBlur) = rememberPreference(DisableBlurKey, false)

    val color1 = MaterialTheme.colorScheme.primary
    val color2 = MaterialTheme.colorScheme.secondary
    val color3 = MaterialTheme.colorScheme.tertiary
    val color4 = MaterialTheme.colorScheme.primaryContainer
    val color5 = MaterialTheme.colorScheme.secondaryContainer
    val surfaceColor = MaterialTheme.colorScheme.surface

    val scope = rememberCoroutineScope()
    val lazylistState = rememberLazyListState()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val scrollToTop = backStackEntry?.savedStateHandle?.getStateFlow("scrollToTop", false)?.collectAsState()

    LaunchedEffect(scrollToTop?.value) {
        if (scrollToTop?.value == true) {
            lazylistState.animateScrollToItem(0)
            backStackEntry?.savedStateHandle?.set("scrollToTop", false)
        }
    }

    val localGridItem: @Composable (LocalItem) -> Unit = {
        when (it) {
            is Song -> SongGridItem(
                song = it,
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {
                            if (it.id == mediaMetadata?.id) playerConnection.player.togglePlayPause()
                            else playerConnection.playQueue(YouTubeQueue.radio(it.toMediaMetadata()))
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            menuState.show { SongMenu(originalSong = it, navController = navController, onDismiss = menuState::dismiss) }
                        }
                    ),
                isActive = it.id == mediaMetadata?.id,
                isPlaying = isPlaying,
            )
            is Album -> AlbumGridItem(
                album = it,
                isActive = it.id == mediaMetadata?.album?.id,
                isPlaying = isPlaying,
                coroutineScope = scope,
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { navController.navigate("album/${it.id}") },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            menuState.show { AlbumMenu(originalAlbum = it, navController = navController, onDismiss = menuState::dismiss) }
                        }
                    )
            )
            is Artist -> ArtistGridItem(
                artist = it,
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { navController.navigate("artist/${it.id}") },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            menuState.show { ArtistMenu(originalArtist = it, coroutineScope = scope, onDismiss = menuState::dismiss) }
                        }
                    ),
            )
            is Playlist -> {}
        }
    }

    val ytGridItem: @Composable (YTItem) -> Unit = { item ->
        YouTubeGridItem(
            item = item,
            isActive = item.id in listOf(mediaMetadata?.album?.id, mediaMetadata?.id),
            isPlaying = isPlaying,
            coroutineScope = scope,
            thumbnailRatio = 1f,
            modifier = Modifier
                .combinedClickable(
                    onClick = {
                        when (item) {
                            is SongItem -> playerConnection.playQueue(YouTubeQueue(item.endpoint ?: WatchEndpoint(videoId = item.id), item.toMediaMetadata()))
                            is AlbumItem -> navController.navigate("album/${item.id}")
                            is ArtistItem -> navController.navigate("artist/${item.id}")
                            is PlaylistItem -> navController.navigate("online_playlist/${item.id}")
                            is EpisodeItem -> playerConnection.playQueue(YouTubeQueue(item.endpoint ?: WatchEndpoint(videoId = item.id), item.asSongItem().toMediaMetadata()))
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
                                is PlaylistItem -> YouTubePlaylistMenu(playlist = item, coroutineScope = scope, onDismiss = menuState::dismiss)
                                is EpisodeItem -> YouTubeSongMenu(song = item.asSongItem(), navController = navController, onDismiss = menuState::dismiss)
                                is PodcastItem -> YouTubePlaylistMenu(playlist = item.asPlaylistItem(), coroutineScope = scope, onDismiss = menuState::dismiss)
                            }
                        }
                    }
                )
        )
    }

    LaunchedEffect(forgottenFavorites) {
        forgottenFavoritesLazyGridState.scrollToItem(0)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .pullToRefresh(state = pullRefreshState, isRefreshing = isRefreshing, onRefresh = viewModel::refresh),
        contentAlignment = Alignment.TopStart
    ) {
        if (!disableBlur) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxSize(0.7f)
                    .align(Alignment.TopCenter)
                    .zIndex(-1f)
                    .drawWithCache {
                        val width = this.size.width
                        val height = this.size.height

                        val brush1 = Brush.radialGradient(colors = listOf(color1.copy(0.25f), color1.copy(0.15f), color1.copy(0.08f), color1.copy(0.03f), Color.Transparent), center = Offset(width * 0.15f, height * 0.1f), radius = width * 0.55f)
                        val brush2 = Brush.radialGradient(colors = listOf(color2.copy(0.20f), color2.copy(0.12f), color2.copy(0.06f), color2.copy(0.02f), Color.Transparent), center = Offset(width * 0.85f, height * 0.2f), radius = width * 0.65f)
                        val brush3 = Brush.radialGradient(colors = listOf(color3.copy(0.15f), color3.copy(0.08f), color3.copy(0.04f), color3.copy(0.01f), Color.Transparent), center = Offset(width * 0.3f, height * 0.45f), radius = width * 0.6f)
                        val brush4 = Brush.radialGradient(colors = listOf(color4.copy(0.20f), color4.copy(0.10f), color4.copy(0.05f), color4.copy(0.02f), Color.Transparent), center = Offset(width * 0.7f, height * 0.5f), radius = width * 0.7f)
                        val brush5 = Brush.radialGradient(colors = listOf(color5.copy(0.18f), color5.copy(0.10f), color5.copy(0.05f), color5.copy(0.01f), Color.Transparent), center = Offset(width * 0.5f, height * 0.75f), radius = width * 0.8f)
                        val overlayBrush = Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Transparent, surfaceColor.copy(0.3f), surfaceColor.copy(0.7f), surfaceColor), startY = height * 0.4f, endY = height)

                        onDrawBehind {
                            drawRect(brush1); drawRect(brush2); drawRect(brush3); drawRect(brush4); drawRect(brush5); drawRect(overlayBrush)
                        }
                    }
            )
        }

        val horizontalLazyGridItemWidthFactor = if (maxWidth * 0.475f >= 320.dp) 0.475f else 0.9f
        val horizontalLazyGridItemWidth = maxWidth * horizontalLazyGridItemWidthFactor

        val forgottenFavoritesSnapLayoutInfoProvider = remember(forgottenFavoritesLazyGridState) {
            SnapLayoutInfoProvider(lazyGridState = forgottenFavoritesLazyGridState, positionInLayout = { layoutSize, itemSize -> (layoutSize * horizontalLazyGridItemWidthFactor / 2f - itemSize / 2f) })
        }

        LazyColumn(
            state = lazylistState,
            contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues()
        ) {
            item {
                HomeGreetingHeader(
                    accountName = accountName,
                    accountImageUrl = url,
                    onAccountClick = {
                        if (isLoggedIn) navController.navigate("account")
                        else navController.navigate("settings/account")
                    },
                    modifier = Modifier.animateItem()
                )
            }
            item {
                HomeQuickActionsRow(navController = navController, modifier = Modifier.animateItem())
            }

            quickPicks?.takeIf { it.isNotEmpty() }?.let { quickPicks ->
                item {
                    QuickPicksSection(
                        quickPicks = quickPicks,
                        mediaMetadata = mediaMetadata,
                        isPlaying = isPlaying,
                        navController = navController,
                        playerConnection = playerConnection,
                        menuState = menuState,
                        haptic = haptic,
                        modifier = Modifier.animateItem()
                    )
                }
            }

            keepListening?.takeIf { it.isNotEmpty() }?.let { keepListening ->
                item { 
                    NavigationTitle(
                        title = stringResource(R.string.keep_listening), 
                        modifier = Modifier.animateItem().padding(top = 8.dp)
                    ) 
                }
                item {
                    val rows = min(2, keepListening.size)
                    LazyHorizontalGrid(
                        state = rememberLazyGridState(),
                        rows = GridCells.Fixed(rows),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((GridThumbnailHeight + with(LocalDensity.current) { MaterialTheme.typography.bodyLarge.lineHeight.toDp() * 2 + MaterialTheme.typography.bodyMedium.lineHeight.toDp() * 2 }) * rows)
                            .animateItem()
                    ) { items(keepListening) { localGridItem(it) } }
                }
            }

            accountPlaylists?.takeIf { it.isNotEmpty() }?.let { accountPlaylists ->
                item {
                    NavigationTitle(
                        label = stringResource(R.string.your_ytb_playlists),
                        title = accountName.ifBlank { stringResource(R.string.account) },
                        thumbnail = {
                            if (url != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current).data(url).diskCachePolicy(CachePolicy.ENABLED).diskCacheKey(url).crossfade(true).build(),
                                    placeholder = painterResource(R.drawable.person),
                                    error = painterResource(R.drawable.person),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(ListThumbnailSize).clip(CircleShape)
                                )
                            } else {
                                Icon(painterResource(R.drawable.person), null, modifier = Modifier.size(ListThumbnailSize))
                            }
                        },
                        onClick = { navController.navigate("account") },
                        modifier = Modifier.animateItem().padding(top = 16.dp)
                    )
                }
                item {
                    LazyRow(
                        contentPadding = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal).asPaddingValues(),
                        modifier = Modifier.animateItem()
                    ) {
                        items(accountPlaylists) { item -> ytGridItem(item) }
                    }
                }
            }

            similarRecommendations?.forEach { recommendation ->
                item {
                    val thumbnailUrl = recommendation.title.thumbnailUrl
                    NavigationTitle(
                        label = stringResource(R.string.similar_to_caps),
                        title = recommendation.title.title,
                        thumbnail = if (thumbnailUrl != null) { { AsyncImage(model = thumbnailUrl, contentDescription = null, modifier = Modifier.size(ListThumbnailSize).clip(if (recommendation.title is Artist) CircleShape else RoundedCornerShape(ThumbnailCornerRadius))) } } else null,
                        onClick = {
                            when (val itemTitle = recommendation.title) {
                                is Song -> navController.navigate("album/${itemTitle.album!!.id}")
                                is Album -> navController.navigate("album/${itemTitle.id}")
                                is Artist -> navController.navigate("artist/${itemTitle.id}")
                                is Playlist -> {}
                            }
                        },
                        modifier = Modifier.animateItem().padding(top = 16.dp)
                    )
                }
                item {
                    LazyRow(
                        contentPadding = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal).asPaddingValues(),
                        modifier = Modifier.animateItem()
                    ) {
                        items(recommendation.items) { item -> ytGridItem(item) }
                    }
                }
            }

            homePage?.sections?.filter { !it.title.equals("New releases", ignoreCase = true) }?.forEach { section ->
                item {
                    val thumbnailUrl = section.thumbnail
                    NavigationTitle(
                        title = getTranslatedHomeSectionTitle(section.title),
                        label = section.label?.let { getTranslatedHomeSectionTitle(it) },
                        thumbnail = if (thumbnailUrl != null) { { AsyncImage(model = thumbnailUrl, contentDescription = null, modifier = Modifier.size(ListThumbnailSize).clip(if (section.endpoint?.isArtistEndpoint == true) CircleShape else RoundedCornerShape(ThumbnailCornerRadius))) } } else null,
                        modifier = Modifier.animateItem().padding(top = 16.dp)
                    )
                }
                item {
                    LazyRow(
                        contentPadding = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal).asPaddingValues(),
                        modifier = Modifier.animateItem()
                    ) {
                        items(section.items) { item -> ytGridItem(item) }
                    }
                }
            }

            explorePage?.newReleaseAlbums?.let { newReleaseAlbums ->
                item {
                    NavigationTitle(
                        title = stringResource(R.string.new_release_albums),
                        onClick = { navController.navigate("new_release") },
                        modifier = Modifier.animateItem().padding(top = 16.dp)
                    )
                }
                item {
                    LazyRow(
                        contentPadding = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal).asPaddingValues(),
                        modifier = Modifier.animateItem()
                    ) {
                        items(newReleaseAlbums) { album ->
                            YouTubeGridItem(
                                item = album,
                                isActive = mediaMetadata?.album?.id == album.id,
                                isPlaying = isPlaying,
                                coroutineScope = scope,
                                modifier = Modifier.combinedClickable(
                                    onClick = { navController.navigate("album/${album.id}") },
                                    onLongClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); menuState.show { YouTubeAlbumMenu(albumItem = album, navController = navController, onDismiss = menuState::dismiss) } }
                                ).animateItem()
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                item {
                    ShimmerHost(modifier = Modifier.animateItem()) {
                        TextPlaceholder(height = 36.dp, modifier = Modifier.padding(12.dp).width(250.dp))
                        LazyRow { items(4) { GridItemPlaceHolder() } }
                    }
                }
            }

            forgottenFavorites?.takeIf { it.isNotEmpty() }?.let { forgottenFavorites ->
                item {
                    NavigationTitle(
                        title = stringResource(R.string.forgotten_favorites),
                        modifier = Modifier.animateItem().padding(top = 16.dp)
                    )
                }
                item {
                    val rows = min(4, forgottenFavorites.size)
                    LazyHorizontalGrid(
                        state = forgottenFavoritesLazyGridState,
                        rows = GridCells.Fixed(rows),
                        flingBehavior = rememberSnapFlingBehavior(forgottenFavoritesSnapLayoutInfoProvider),
                        contentPadding = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal).asPaddingValues(),
                        modifier = Modifier.fillMaxWidth().height(ListItemHeight * rows).animateItem()
                    ) {
                        items(forgottenFavorites) { originalSong ->
                            val song by database.song(originalSong.id).collectAsState(initial = originalSong)
                            SongListItem(
                                song = song!!,
                                showInLibraryIcon = true,
                                isActive = song!!.id == mediaMetadata?.id,
                                isPlaying = isPlaying,
                                modifier = Modifier.width(horizontalLazyGridItemWidth).combinedClickable(
                                    onClick = { if (song!!.id == mediaMetadata?.id) playerConnection.player.togglePlayPause() else playerConnection.playQueue(YouTubeQueue.radio(song!!.toMediaMetadata())) },
                                    onLongClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); menuState.show { SongMenu(originalSong = song!!, navController = navController, onDismiss = menuState::dismiss) } }
                                )
                            )
                        }
                    }
                }
            }
        }

        val isFabVisible = (allLocalItems.isNotEmpty() || allYtItems.isNotEmpty()) && lazylistState.isScrollingUp()

        AnimatedVisibility(
            visible = isFabVisible,
            enter = scaleIn(spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)) +
                    fadeIn(tween(300, easing = LinearOutSlowInEasing)) +
                    slideInVertically(spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)) { it },
            exit = scaleOut(tween(400, easing = FastOutSlowInEasing), targetScale = 0.7f) +
                   fadeOut(tween(300, easing = LinearOutSlowInEasing)) +
                   slideOutVertically(tween(400, easing = FastOutSlowInEasing)) { it },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .windowInsetsPadding(
                    LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
                )
        ) {
            FloatingActionButton(
                modifier = Modifier.padding(16.dp),
                onClick = {
                    val local = when {
                        allLocalItems.isNotEmpty() && allYtItems.isNotEmpty() -> Random.nextFloat() < 0.5
                        allLocalItems.isNotEmpty() -> true
                        else -> false
                    }
                    scope.launch(Dispatchers.Main) {
                        if (local) {
                            when (val luckyItem = allLocalItems.random()) {
                                is Song -> playerConnection.playQueue(YouTubeQueue.radio(luckyItem.toMediaMetadata()))
                                is Album -> {
                                    val albumWithSongs = withContext(Dispatchers.IO) { database.albumWithSongs(luckyItem.id).first() }
                                    albumWithSongs?.let { playerConnection.playQueue(LocalAlbumRadio(it)) }
                                }
                                is Artist -> {}
                                is Playlist -> {}
                            }
                        } else {
                            when (val luckyItem = allYtItems.random()) {
                                is SongItem -> playerConnection.playQueue(YouTubeQueue.radio(luckyItem.toMediaMetadata()))
                                is AlbumItem -> playerConnection.playQueue(YouTubeAlbumRadio(luckyItem.playlistId))
                                is ArtistItem -> luckyItem.radioEndpoint?.let { playerConnection.playQueue(YouTubeQueue(it)) }
                                is PlaylistItem -> luckyItem.playEndpoint?.let { playerConnection.playQueue(YouTubeQueue(it)) }
                                is EpisodeItem -> playerConnection.playQueue(YouTubeQueue.radio(luckyItem.asSongItem().toMediaMetadata()))
                                is PodcastItem -> luckyItem.playEndpoint?.let { playerConnection.playQueue(YouTubeQueue(it)) }
                            }
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    painter = painterResource(R.drawable.shuffle),
                    contentDescription = null
                )
            }
        }

        Box(Modifier.align(Alignment.TopCenter)) {
            PullToRefreshDefaults.Indicator(state = pullRefreshState, isRefreshing = isRefreshing, modifier = Modifier.padding(LocalPlayerAwareWindowInsets.current.asPaddingValues()))
        }
    }
}

@Composable
private fun HomeGreetingHeader(
    accountName: String,
    accountImageUrl: String?,
    onAccountClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greetingText = when (hour) {
        in 5..11 -> stringResource(R.string.good_morning)
        in 12..17 -> stringResource(R.string.good_afternoon)
        in 18..21 -> stringResource(R.string.good_evening)
        else -> stringResource(R.string.good_night)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = greetingText,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (accountName.isNotBlank() && accountName != "Guest") {
                Text(
                    text = accountName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .size(48.dp)
                .clickable(onClick = onAccountClick)
        ) {
            if (accountImageUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(accountImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.person),
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private data class QuickActionItem(
    val route: String,
    val icon: Int,
    val label: String,
    val containerColor: Color,
    val contentColor: Color
)

@Composable
private fun HomeQuickActionsRow(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val actions = listOf(
        QuickActionItem("history", R.drawable.history, stringResource(R.string.history), MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer),
        QuickActionItem("stats", R.drawable.equalizer, stringResource(R.string.stats), MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer),
        QuickActionItem("auto_playlist/liked", R.drawable.favorite, stringResource(R.string.liked), MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer),
        QuickActionItem("auto_playlist/downloaded", R.drawable.offline, stringResource(R.string.offline), MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer),
        QuickActionItem("apple_music_trending", R.drawable.apple, stringResource(R.string.trending), MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
    )

    LazyRow(
        modifier = modifier.fillMaxWidth().padding(bottom = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(actions) { action ->
            Surface(
                onClick = {
                    when (action.route) {
                        "history" -> navController.navigate("history")
                        "stats" -> navController.navigate("stats")
                        "auto_playlist/liked" -> navController.navigate("auto_playlist/liked")
                        "auto_playlist/downloaded" -> navController.navigate("auto_playlist/downloaded")
                        "apple_music_trending" -> navController.navigate("apple_music_trending")
                    }
                },
                shape = RoundedCornerShape(16.dp),
                color = action.containerColor,
                modifier = Modifier.width(100.dp).height(80.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(
                        painter = painterResource(action.icon),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = action.contentColor
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = action.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = action.contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun QuickPicksSection(
    quickPicks: List<Song>,
    mediaMetadata: MediaMetadata?,
    isPlaying: Boolean,
    navController: NavController,
    playerConnection: PlayerConnection,
    menuState: MenuState,
    haptic: HapticFeedback,
    modifier: Modifier = Modifier
) {
    val distinctQuickPicks = remember(quickPicks) { quickPicks.distinctBy { it.id } }
    val (coverResolution) = rememberEnumPreference(
        key = CoverResolutionKey,
        defaultValue = CoverResolution.RES_1080
    )

    HorizontalCenteredHeroCarousel(
        state = rememberCarouselState { distinctQuickPicks.size },
        maxItemWidth = 250.dp,
        itemSpacing = 8.dp,
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = modifier.fillMaxWidth().height(290.dp)
    ) { index ->
        val song = distinctQuickPicks[index]
        val isActive = song.id == mediaMetadata?.id

        Box(
            modifier = Modifier
                .fillMaxSize()
                .maskClip(MaterialTheme.shapes.extraLarge)
                .maskBorder(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), MaterialTheme.shapes.extraLarge)
                .combinedClickable(
                    onClick = {
                        if (isActive) playerConnection.player.togglePlayPause()
                        else playerConnection.playQueue(YouTubeQueue.radio(song.toMediaMetadata()))
                    },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        menuState.show { SongMenu(originalSong = song, navController = navController, onDismiss = menuState::dismiss) }
                    }
                )
        ) {
            AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(song.song.thumbnailUrl?.resize(coverResolution.size, coverResolution.size)).crossfade(true).build(), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.7f)))))
            if (isActive && isPlaying) {
                Box(modifier = Modifier.align(Alignment.TopEnd).padding(12.dp).size(32.dp).background(MaterialTheme.colorScheme.primary, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(painterResource(R.drawable.volume_up), null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
                }
            }
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                Text(text = song.song.title, style = MaterialTheme.typography.titleMedium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = song.artists.joinToString { it.name }, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.7f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun getTranslatedHomeSectionTitle(title: String): String {
    return when {
        title.equals("Albums for you", ignoreCase = true) -> stringResource(R.string.albums_for_you)
        title.equals("New releases", ignoreCase = true) -> stringResource(R.string.new_releases)
        title.equals("SIMILAR TO", ignoreCase = true) -> stringResource(R.string.similar_to_caps)
        title.equals("Featured playlists for you", ignoreCase = true) -> stringResource(R.string.featured_playlists_for_you)
        title.equals("Fresh finds, old favorites", ignoreCase = true) -> stringResource(R.string.fresh_finds_old_favorites)
        title.equals("From your library", ignoreCase = true) -> stringResource(R.string.from_your_library)
        title.equals("From the community", ignoreCase = true) -> stringResource(R.string.from_the_community)
        title.equals("Forgotten favorites", ignoreCase = true) -> stringResource(R.string.forgotten_favorites)
        title.equals("Music videos for you", ignoreCase = true) -> stringResource(R.string.music_videos_for_you)
        title.equals("Today's hits", ignoreCase = true) -> stringResource(R.string.todays_hits)
        title.equals("Mixed for you", ignoreCase = true) -> stringResource(R.string.mixed_for_you)
        title.equals("Live performances", ignoreCase = true) -> stringResource(R.string.artist_live_performances)
        title.equals("Your daily discover", ignoreCase = true) -> stringResource(R.string.your_daily_discover)
        else -> title
    }
}
--- START OF FILE app/src/main/java/com/cgens67/avidtune/ui/component/Items.kt ---
@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalFoundationApi::class)

package com.cgens67.gluetune.ui.component

import android.content.Context
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.zIndex
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.Download.STATE_COMPLETED
import androidx.media3.exoplayer.offline.Download.STATE_DOWNLOADING
import androidx.media3.exoplayer.offline.Download.STATE_QUEUED
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.cgens67.gluetune.LocalDatabase
import com.cgens67.gluetune.LocalDownloadUtil
import com.cgens67.gluetune.LocalPlayerConnection
import com.cgens67.gluetune.R
import com.cgens67.gluetune.constants.GridThumbnailHeight
import com.cgens67.gluetune.constants.ListItemHeight
import com.cgens67.gluetune.constants.ListThumbnailSize
import com.cgens67.gluetune.constants.SmallGridThumbnailHeight
import com.cgens67.gluetune.constants.SwipeToSongKey
import com.cgens67.gluetune.constants.ThumbnailCornerRadius
import com.cgens67.gluetune.db.entities.Album
import com.cgens67.gluetune.db.entities.Artist
import com.cgens67.gluetune.db.entities.Playlist
import com.cgens67.gluetune.db.entities.Song
import com.cgens67.gluetune.extensions.toMediaItem
import com.cgens67.innertube.YouTube
import com.cgens67.innertube.models.AlbumItem
import com.cgens67.innertube.models.ArtistItem
import com.cgens67.innertube.models.EpisodeItem
import com.cgens67.innertube.models.PlaylistItem
import com.cgens67.innertube.models.PodcastItem
import com.cgens67.innertube.models.SongItem
import com.cgens67.innertube.models.YTItem
import com.cgens67.gluetune.models.MediaMetadata
import com.cgens67.gluetune.playback.queues.LocalAlbumRadio
import com.cgens67.gluetune.ui.theme.extractThemeColor
import com.cgens67.gluetune.ui.utils.resize
import com.cgens67.gluetune.utils.getPlaylistImageUri
import com.cgens67.gluetune.utils.joinByBullet
import com.cgens67.gluetune.utils.makeTimeString
import com.cgens67.gluetune.utils.rememberPreference
import com.cgens67.gluetune.utils.reportException
import kotlin.math.roundToInt

const val ActiveBoxAlpha = 0.6f
private var cachedItemCornerRadius = 18f

@Composable
fun rememberItemCornerRadius(): Dp {
    val context = LocalContext.current
    var radius by remember { mutableFloatStateOf(cachedItemCornerRadius) }
    LaunchedEffect(Unit) {
        val fetched = AppConfig.getThumbnailCornerRadius(context, 18f)
        radius = fetched
        cachedItemCornerRadius = fetched
    }
    return radius.dp
}

// ─────────────────────────────────────────────────────────────────────────────
// LIST ITEM
// ─────────────────────────────────────────────────────────────────────────────

@Composable
inline fun ListItem(
    modifier: Modifier = Modifier,
    title: String,
    noinline subtitle: (@Composable RowScope.() -> Unit)? = null,
    thumbnailContent: @Composable () -> Unit,
    crossinline trailingContent: @Composable RowScope.() -> Unit = {},
    isActive: Boolean = false,
    isSelected: Boolean = false,
) {
    val defaultContentColor = LocalContentColor.current.takeOrElse { MaterialTheme.colorScheme.onSurface }
    val titleColor = when {
        isActive -> MaterialTheme.colorScheme.primary
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> defaultContentColor
    }
    val subtitleContentColor = when {
        isActive -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        else -> defaultContentColor.copy(alpha = 0.7f)
    }
    val trailingContentColor = when {
        isActive -> MaterialTheme.colorScheme.primary
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> defaultContentColor.copy(alpha = 0.8f)
    }

    val itemCornerRadius = rememberItemCornerRadius()

    val containerModifier = when {
        isActive -> Modifier
            .clip(RoundedCornerShape(itemCornerRadius))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f))
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                RoundedCornerShape(itemCornerRadius)
            )
        isSelected -> Modifier
            .clip(RoundedCornerShape(itemCornerRadius))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                RoundedCornerShape(itemCornerRadius)
            )
        else -> Modifier
            .clip(RoundedCornerShape(itemCornerRadius))
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .focusable()
            .height(ListItemHeight)
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .then(containerModifier)
            .padding(horizontal = 6.dp),
    ) {
        Box(Modifier.padding(4.dp), contentAlignment = Alignment.Center) { thumbnailContent() }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = titleColor,
            )
            if (subtitle != null) {
                CompositionLocalProvider(LocalContentColor provides subtitleContentColor) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) { subtitle() }
                }
            }
        }
        CompositionLocalProvider(LocalContentColor provides trailingContentColor) {
            trailingContent()
        }
    }
}

@Composable
fun ListItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String?,
    badges: @Composable RowScope.() -> Unit = {},
    thumbnailContent: @Composable () -> Unit,
    trailingContent: @Composable RowScope.() -> Unit = {},
    isActive: Boolean = false,
    isSelected: Boolean = false,
) = ListItem(
    title = title,
    modifier = modifier,
    isActive = isActive,
    isSelected = isSelected,
    subtitle = {
        badges()
        if (!subtitle.isNullOrEmpty()) {
            Text(
                text = subtitle,
                color = LocalContentColor.current,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    },
    thumbnailContent = thumbnailContent,
    trailingContent = trailingContent,
)

// ─────────────────────────────────────────────────────────────────────────────
// POSTER CARD GRID ITEM
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun GridItem(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    subtitle: @Composable () -> Unit,
    badges: @Composable RowScope.() -> Unit = {},
    thumbnailContent: @Composable BoxWithConstraintsScope.() -> Unit,
    thumbnailRatio: Float = 1f,
    fillMaxWidth: Boolean = false,
    typeTag: String? = null,
) {
    val itemCornerRadius = rememberItemCornerRadius()

    Card(
        shape = RoundedCornerShape(itemCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = if (fillMaxWidth) {
            modifier
                .focusable()
                .padding(6.dp)
                .fillMaxWidth()
                .aspectRatio(thumbnailRatio)
        } else {
            modifier
                .focusable()
                .padding(6.dp)
                .width(GridThumbnailHeight * 1.35f * thumbnailRatio)
                .aspectRatio(thumbnailRatio)
        }
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            thumbnailContent()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color.Transparent,
                            0.35f to Color.Transparent,
                            0.7f to Color.Black.copy(alpha = 0.65f),
                            1.0f to Color.Black.copy(alpha = 0.94f)
                        )
                    )
            )

            if (!typeTag.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.music_note),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = typeTag,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.5.sp,
                                color = Color.White
                            )
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                ProvideTextStyle(
                    TextStyle(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        lineHeight = 23.sp
                    )
                ) {
                    title()
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    badges()

                    CompositionLocalProvider(LocalContentColor provides Color.White.copy(alpha = 0.8f)) {
                        ProvideTextStyle(
                            TextStyle(
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Normal,
                                lineHeight = 17.sp
                            )
                        ) {
                            subtitle()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GridItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    isActive: Boolean = false,
    badges: @Composable RowScope.() -> Unit = {},
    thumbnailContent: @Composable BoxWithConstraintsScope.() -> Unit,
    thumbnailRatio: Float = 1f,
    fillMaxWidth: Boolean = false,
    typeTag: String? = null,
) = GridItem(
    modifier = modifier,
    title = {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 19.sp,
                lineHeight = 23.sp,
                fontWeight = FontWeight.Bold
            ),
            color = if (isActive) MaterialTheme.colorScheme.primary else Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
        )
    },
    subtitle = {
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Normal
            ),
            color = Color.White.copy(alpha = 0.8f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    },
    badges = badges,
    thumbnailContent = thumbnailContent,
    thumbnailRatio = thumbnailRatio,
    fillMaxWidth = fillMaxWidth,
    typeTag = typeTag,
)

// ─────────────────────────────────────────────────────────────────────────────
// TYPED ITEMS (SONG, ALBUM, ARTIST, PLAYLIST)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SongListItem(
    song: Song,
    modifier: Modifier = Modifier,
    albumIndex: Int? = null,
    viewCountText: String? = null,
    showLikedIcon: Boolean = true,
    showInLibraryIcon: Boolean = false,
    showDownloadIcon: Boolean = true,
    showSongIconPlaceholder: Boolean = false,
    badges: @Composable RowScope.() -> Unit = {
        if (showLikedIcon && song.song.liked) {
            Icon.Favorite()
        }
        if (showInLibraryIcon && song.song.inLibrary != null) {
            Icon.Library()
        }
        if (showDownloadIcon) {
            val download by LocalDownloadUtil.current
                .getDownload(song.id)
                .collectAsState(initial = null)
            Icon.Download(download?.state, percent = download?.percentDownloaded ?: -1f)
        }
    },
    isSelected: Boolean = false,
    isActive: Boolean = false,
    isPlaying: Boolean = false,
    isSwipeable: Boolean = true,
    swipeContentBackgroundColor: Color? = null,
    trailingContent: @Composable RowScope.() -> Unit = {},
) {
    val swipeEnabled by rememberPreference(SwipeToSongKey, defaultValue = false)
    val resolvedSwipeContentBackgroundColor = swipeContentBackgroundColor ?: MaterialTheme.colorScheme.surface
    val itemCornerRadius = rememberItemCornerRadius()

    val content: @Composable () -> Unit = {
        ListItem(
            title = song.song.title,
            subtitle =
                joinByBullet(
                    song.artists.joinToString { it.name },
                    makeTimeString(song.song.duration * 1000L),
                    viewCountText,
                ),
            badges = badges,
            thumbnailContent = {
                ItemThumbnail(
                    thumbnailUrl = song.song.thumbnailUrl?.resize(200, 200),
                    albumIndex = albumIndex,
                    isSelected = isSelected,
                    isActive = isActive,
                    isPlaying = isPlaying,
                    shape = RoundedCornerShape(itemCornerRadius),
                    placeholderIconRes = if (showSongIconPlaceholder) R.drawable.music_note else null,
                    modifier = Modifier.size(ListThumbnailSize),
                )
            },
            trailingContent = trailingContent,
            modifier = modifier,
            isActive = isActive,
            isSelected = isSelected,
        )
    }

    if (isSwipeable && swipeEnabled) {
        SwipeToSongBox(
            mediaItem = song.toMediaItem(),
            modifier = Modifier.fillMaxWidth(),
            contentBackgroundColor = resolvedSwipeContentBackgroundColor,
        ) {
            content()
        }
    } else {
        content()
    }
}

@Composable
fun SongGridItem(
    song: Song,
    modifier: Modifier = Modifier,
    viewCountText: String? = null,
    showLikedIcon: Boolean = true,
    showInLibraryIcon: Boolean = false,
    showDownloadIcon: Boolean = true,
    badges: @Composable RowScope.() -> Unit = {
        if (showLikedIcon && song.song.liked) {
            Icon.Favorite()
        }
        if (showInLibraryIcon && song.song.inLibrary != null) {
            Icon.Library()
        }
        if (showDownloadIcon) {
            val download by LocalDownloadUtil.current.getDownload(song.id).collectAsState(initial = null)
            Icon.Download(download?.state, percent = download?.percentDownloaded ?: -1f)
        }
    },
    isActive: Boolean = false,
    isPlaying: Boolean = false,
    fillMaxWidth: Boolean = false,
) {
    val itemCornerRadius = rememberItemCornerRadius()
    GridItem(
        title = song.song.title,
        subtitle =
            joinByBullet(
                song.artists.joinToString { it.name },
                makeTimeString(song.song.duration * 1000L),
                viewCountText,
            ),
        badges = badges,
        isActive = isActive,
        typeTag = "Song",
        thumbnailContent = {
            ItemThumbnail(
                thumbnailUrl = song.song.thumbnailUrl,
                isActive = isActive,
                isPlaying = isPlaying,
                shape = RoundedCornerShape(itemCornerRadius),
                modifier = Modifier.fillMaxSize(),
            )
            if (!isActive) {
                OverlayPlayButton(visible = true)
            }
        },
        fillMaxWidth = fillMaxWidth,
        modifier = modifier,
    )
}

@Composable
fun ArtistListItem(
    artist: Artist,
    modifier: Modifier = Modifier,
    badges: @Composable RowScope.() -> Unit = {
        if (artist.artist.bookmarkedAt != null) {
            Icon.Favorite()
        }
    },
    trailingContent: @Composable RowScope.() -> Unit = {},
) = ListItem(
    title = artist.artist.name,
    subtitle = pluralStringResource(R.plurals.n_song, artist.songCount, artist.songCount),
    badges = badges,
    thumbnailContent = {
        AsyncImage(
            model = artist.artist.thumbnailUrl?.resize(200, 200),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .size(ListThumbnailSize)
                    .clip(CircleShape),
        )
    },
    trailingContent = trailingContent,
    modifier = modifier,
)

@Composable
fun ArtistGridItem(
    artist: Artist,
    modifier: Modifier = Modifier,
    badges: @Composable RowScope.() -> Unit = {
        if (artist.artist.bookmarkedAt != null) {
            Icon.Favorite()
        }
    },
    fillMaxWidth: Boolean = false,
) {
    val itemCornerRadius = rememberItemCornerRadius()
    GridItem(
        title = artist.artist.name,
        subtitle = pluralStringResource(R.plurals.n_song, artist.songCount, artist.songCount),
        badges = badges,
        typeTag = "Artist",
        thumbnailContent = {
            AsyncImage(
                model = artist.artist.thumbnailUrl?.resize(544, 544),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(itemCornerRadius)),
            )
        },
        fillMaxWidth = fillMaxWidth,
        modifier = modifier,
    )
}

@Composable
fun AlbumListItem(
    album: Album,
    modifier: Modifier = Modifier,
    showLikedIcon: Boolean = true,
    badges: @Composable RowScope.() -> Unit = {
        val database = LocalDatabase.current
        val downloadUtil = LocalDownloadUtil.current
        var songs by remember {
            mutableStateOf(emptyList<Song>())
        }

        LaunchedEffect(Unit) {
            database.albumSongs(album.id).collect {
                songs = it
            }
        }

        var downloadState by remember {
            mutableStateOf(Download.STATE_STOPPED)
        }

        LaunchedEffect(songs) {
            if (songs.isEmpty()) return@LaunchedEffect
            downloadUtil.downloads.collect { downloads ->
                downloadState =
                    when {
                        songs.all { downloads[it.id]?.state == STATE_COMPLETED } -> STATE_COMPLETED
                        songs.all {
                            downloads[it.id]?.state in listOf(
                                STATE_QUEUED,
                                STATE_DOWNLOADING,
                                STATE_COMPLETED,
                            )
                        } -> STATE_DOWNLOADING
                        else -> Download.STATE_STOPPED
                    }
            }
        }

        if (showLikedIcon && album.album.bookmarkedAt != null) {
            Icon.Favorite()
        }
        Icon.Download(downloadState)
    },
    isActive: Boolean = false,
    isPlaying: Boolean = false,
    trailingContent: @Composable RowScope.() -> Unit = {},
) {
    val itemCornerRadius = rememberItemCornerRadius()
    ListItem(
        title = album.album.title,
        subtitle =
            joinByBullet(
                album.artists.joinToString { it.name },
                pluralStringResource(R.plurals.n_song, album.album.songCount, album.album.songCount),
                album.album.year?.toString(),
            ),
        badges = badges,
        thumbnailContent = {
            ItemThumbnail(
                thumbnailUrl = album.album.thumbnailUrl,
                isActive = isActive,
                isPlaying = isPlaying,
                shape = RoundedCornerShape(itemCornerRadius),
                modifier = Modifier.size(ListThumbnailSize),
            )
        },
        trailingContent = trailingContent,
        modifier = modifier,
    )
}

@Composable
fun AlbumGridItem(
    album: Album,
    modifier: Modifier = Modifier,
    coroutineScope: CoroutineScope,
    badges: @Composable RowScope.() -> Unit = {
        val database = LocalDatabase.current
        val downloadUtil = LocalDownloadUtil.current
        var songs by remember { mutableStateOf(emptyList<Song>()) }

        LaunchedEffect(Unit) {
            database.albumSongs(album.id).collect { songs = it }
        }

        var downloadState by remember { mutableStateOf(Download.STATE_STOPPED) }

        LaunchedEffect(songs) {
            if (songs.isEmpty()) return@LaunchedEffect
            downloadUtil.downloads.collect { downloads ->
                downloadState =
                    when {
                        songs.all { downloads[it.id]?.state == STATE_COMPLETED } -> STATE_COMPLETED
                        songs.all {
                            downloads[it.id]?.state in listOf(
                                STATE_QUEUED,
                                STATE_DOWNLOADING,
                                STATE_COMPLETED,
                            )
                        } -> STATE_DOWNLOADING
                        else -> Download.STATE_STOPPED
                    }
            }
        }

        if (album.album.bookmarkedAt != null) {
            Icon.Favorite()
        }
        Icon.Download(downloadState)
    },
    isActive: Boolean = false,
    isPlaying: Boolean = false,
    fillMaxWidth: Boolean = false,
) {
    val itemCornerRadius = rememberItemCornerRadius()
    GridItem(
        title = album.album.title,
        subtitle = album.artists.joinToString { it.name },
        badges = badges,
        isActive = isActive,
        typeTag = stringResource(R.string.album_text),
        thumbnailContent = {
            val database = LocalDatabase.current
            val playerConnection = LocalPlayerConnection.current ?: return@GridItem

            ItemThumbnail(
                thumbnailUrl = album.album.thumbnailUrl,
                isActive = isActive,
                isPlaying = isPlaying,
                shape = RoundedCornerShape(itemCornerRadius),
                modifier = Modifier.fillMaxSize(),
            )

            AlbumPlayButton(
                visible = !isActive,
                onClick = {
                    coroutineScope.launch {
                        database.albumWithSongs(album.id).firstOrNull()?.let { albumWithSongs ->
                            playerConnection.playQueue(LocalAlbumRadio(albumWithSongs))
                        }
                    }
                },
            )
        },
        fillMaxWidth = fillMaxWidth,
        modifier = modifier,
    )
}

@Composable
fun PlaylistListItem(
    playlist: Playlist,
    modifier: Modifier = Modifier,
    autoPlaylist: Boolean = false,
    badges: @Composable RowScope.() -> Unit = {},
    trailingContent: @Composable RowScope.() -> Unit = {},
) {
    val itemCornerRadius = rememberItemCornerRadius()
    ListItem(
        title = playlist.playlist.name,
        subtitle =
            if (autoPlaylist) {
                ""
            } else {
                if (playlist.songCount == 0 && playlist.playlist.remoteSongCount != null) {
                    pluralStringResource(
                        R.plurals.n_song,
                        playlist.playlist.remoteSongCount,
                        playlist.playlist.remoteSongCount,
                    )
                } else {
                    pluralStringResource(
                        R.plurals.n_song,
                        playlist.songCount,
                        playlist.songCount,
                    )
                }
            },
        badges = badges,
        thumbnailContent = {
            PlaylistThumbnail(
                thumbnails = playlist.thumbnails,
                size = ListThumbnailSize,
                placeHolder = {
                    val painter =
                        when (playlist.playlist.name) {
                            stringResource(R.string.liked) -> R.drawable.favorite_border
                            stringResource(R.string.offline) -> R.drawable.offline
                            stringResource(R.string.cached_playlist) -> R.drawable.cached
                            else -> if (autoPlaylist) R.drawable.trending_up else R.drawable.queue_music
                        }
                    Icon(
                        painter = painterResource(painter),
                        contentDescription = null,
                        tint = LocalContentColor.current.copy(alpha = 0.8f),
                        modifier = Modifier.size(ListThumbnailSize / 2),
                    )
                },
                shape = RoundedCornerShape(itemCornerRadius),
            )
        },
        trailingContent = trailingContent,
        modifier = modifier,
    )
}

@Composable
fun PlaylistGridItem(
    playlist: Playlist,
    modifier: Modifier = Modifier,
    autoPlaylist: Boolean = false,
    badges: @Composable RowScope.() -> Unit = {},
    fillMaxWidth: Boolean = false,
    context: Context = LocalContext.current
) {
    val itemCornerRadius = rememberItemCornerRadius()
    val subtitle =
        if (autoPlaylist) {
            ""
        } else {
            if (playlist.songCount == 0 && playlist.playlist.remoteSongCount != null) {
                pluralStringResource(
                    R.plurals.n_song,
                    playlist.playlist.remoteSongCount,
                    playlist.playlist.remoteSongCount,
                )
            } else {
                pluralStringResource(
                    R.plurals.n_song,
                    playlist.songCount,
                    playlist.songCount,
                )
            }
        }

    GridItem(
        title = playlist.playlist.name,
        subtitle = subtitle,
        badges = badges,
        typeTag = "Playlist",
        thumbnailContent = {
            val thumbnailUri = getPlaylistImageUri(context, playlist.playlist.id)
            if (thumbnailUri != null) {
                AsyncImage(
                    model = thumbnailUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(itemCornerRadius))
                )
            } else {
                PlaylistThumbnail(
                    thumbnails = playlist.thumbnails,
                    size = 140.dp,
                    placeHolder = {
                        val painter =
                            when (playlist.playlist.name) {
                                stringResource(R.string.liked) -> R.drawable.favorite_border
                                stringResource(R.string.offline) -> R.drawable.offline
                                stringResource(R.string.cached_playlist) -> R.drawable.cached
                                else -> if (autoPlaylist) R.drawable.trending_up else R.drawable.queue_music
                            }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            Icon(
                                painter = painterResource(painter),
                                contentDescription = null,
                                tint = LocalContentColor.current.copy(alpha = 0.8f),
                                modifier = Modifier.size(48.dp),
                            )
                        }
                    },
                    shape = RoundedCornerShape(itemCornerRadius),
                )
            }
        },
        fillMaxWidth = fillMaxWidth,
        modifier = modifier,
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// YOUTUBE GRID ITEM
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun YouTubeGridItem(
    item: YTItem,
    modifier: Modifier = Modifier,
    viewCountText: String? = null,
    coroutineScope: CoroutineScope? = null,
    badges: @Composable RowScope.() -> Unit = {
        val database = LocalDatabase.current
        val song by database.song(item.id).collectAsState(initial = null)
        val album by database.album(item.id).collectAsState(initial = null)

        if (item is SongItem && song?.song?.liked == true ||
            item is AlbumItem && album?.album?.bookmarkedAt != null
        ) {
            Icon.Favorite()
        }
        if (item.explicit) Icon.Explicit()
        if (item is SongItem && song?.song?.inLibrary != null) Icon.Library()
        if (item is SongItem) {
            val downloads by LocalDownloadUtil.current.downloads.collectAsState()
            val download = downloads[item.id]
            Icon.Download(download?.state, percent = download?.percentDownloaded ?: -1f)
        }
    },
    thumbnailRatio: Float = 1f,
    isActive: Boolean = false,
    isPlaying: Boolean = false,
    fillMaxWidth: Boolean = false,
) {
    val itemCornerRadius = rememberItemCornerRadius()

    val typeTag = when (item) {
        is SongItem -> "Song"
        is AlbumItem -> stringResource(R.string.album_text)
        is ArtistItem -> "Artist"
        is PlaylistItem -> "Playlist"
        is EpisodeItem -> "Episode"
        is PodcastItem -> "Podcast"
    }

    val subtitle = when (item) {
        is SongItem -> {
            val rawViews = viewCountText
                ?: item.views
                ?: (if (item.chartPosition != null) "#${item.chartPosition}" else null)
                ?: item.chartChange
                
            val viewsStr = stringResource(R.string.views)
            val playsStr = stringResource(R.string.plays)
            val views = rawViews
                ?.replace(" views", " $viewsStr", ignoreCase = true)
                ?.replace(" plays", " $playsStr", ignoreCase = true)

            joinByBullet(
                item.artists.joinToString { it.name },
                makeTimeString(item.duration?.times(1000L)),
                views
            )
        }
        is AlbumItem -> joinByBullet(item.artists?.joinToString { it.name }, item.year?.toString())
        is ArtistItem -> "Artist"
        is PlaylistItem -> joinByBullet(item.author?.name, item.songCountText)
        is EpisodeItem -> {
            val rawViews = viewCountText ?: item.views
            val viewsStr = stringResource(R.string.views)
            val playsStr = stringResource(R.string.plays)
            val views = rawViews
                ?.replace(" views", " $viewsStr", ignoreCase = true)
                ?.replace(" plays", " $playsStr", ignoreCase = true)
            joinByBullet(item.author?.name, item.publishDateText, views)
        }
        is PodcastItem -> joinByBullet(item.author?.name, item.episodeCountText)
    }

    GridItem(
        title = item.title,
        subtitle = subtitle,
        badges = badges,
        isActive = isActive,
        typeTag = typeTag,
        thumbnailContent = {
            val database = LocalDatabase.current
            val playerConnection = LocalPlayerConnection.current ?: return@GridItem

            ItemThumbnail(
                thumbnailUrl = item.thumbnail,
                isActive = isActive,
                isPlaying = isPlaying,
                shape = RoundedCornerShape(itemCornerRadius),
                thumbnailRatio = thumbnailRatio,
                modifier = Modifier.fillMaxSize(),
            )

            if ((item is SongItem && !isActive) || (item is EpisodeItem && !isActive)) {
                OverlayPlayButton(visible = true)
            }

            AlbumPlayButton(
                visible = item is AlbumItem && !isActive,
                onClick = {
                    coroutineScope?.launch(Dispatchers.IO) {
                        var albumWithSongs = database.albumWithSongs(item.id).first()
                        var playlistId = ""
                        if (albumWithSongs?.songs.isNullOrEmpty()) {
                            YouTube
                                .album(item.id)
                                .onSuccess { albumPage ->
                                    playlistId = albumPage.album.playlistId
                                    database.transaction { insert(albumPage) }
                                    albumWithSongs = database.albumWithSongs(item.id).first()
                                }.onFailure { reportException(it) }
                        }
                        albumWithSongs?.let {
                            withContext(Dispatchers.Main) {
                                if (playlistId.isNotEmpty()) {
                                    playerConnection.service.getAutomix(playlistId)
                                }
                                playerConnection.playQueue(LocalAlbumRadio(it))
                            }
                        }
                    }
                },
            )
        },
        thumbnailRatio = thumbnailRatio,
        fillMaxWidth = fillMaxWidth,
        modifier = modifier,
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// LOCAL GRIDS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LocalSongsGrid(
    title: String,
    subtitle: String,
    badges: @Composable RowScope.() -> Unit = {},
    thumbnailUrl: String?,
    isActive: Boolean = false,
    isPlaying: Boolean = false,
    fillMaxWidth: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val itemCornerRadius = rememberItemCornerRadius()
    GridItem(
        title = title,
        subtitle = subtitle,
        isActive = isActive,
        badges = badges,
        typeTag = "Song",
        thumbnailContent = {
            LocalThumbnail(
                thumbnailUrl = thumbnailUrl,
                isActive = isActive,
                isPlaying = isPlaying,
                shape = RoundedCornerShape(itemCornerRadius),
                modifier = Modifier.fillMaxSize(),
                showCenterPlay = true,
                playButtonVisible = false,
            )
        },
        fillMaxWidth = fillMaxWidth,
        modifier = modifier,
    )
}

@Composable
fun LocalArtistsGrid(
    title: String,
    subtitle: String,
    badges: @Composable RowScope.() -> Unit = {},
    thumbnailUrl: String?,
    isActive: Boolean = false,
    isPlaying: Boolean = false,
    fillMaxWidth: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val itemCornerRadius = rememberItemCornerRadius()
    GridItem(
        title = title,
        subtitle = subtitle,
        isActive = isActive,
        badges = badges,
        typeTag = "Artist",
        thumbnailContent = {
            LocalThumbnail(
                thumbnailUrl = thumbnailUrl,
                isActive = false,
                isPlaying = false,
                shape = RoundedCornerShape(itemCornerRadius),
                modifier = Modifier.fillMaxSize(),
                showCenterPlay = false,
                playButtonVisible = false,
            )
        },
        fillMaxWidth = fillMaxWidth,
        modifier = modifier,
    )
}

@Composable
fun LocalAlbumsGrid(
    title: String,
    subtitle: String,
    badges: @Composable RowScope.() -> Unit = {},
    thumbnailUrl: String?,
    isActive: Boolean = false,
    isPlaying: Boolean = false,
    fillMaxWidth: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val itemCornerRadius = rememberItemCornerRadius()
    GridItem(
        title = title,
        subtitle = subtitle,
        isActive = isActive,
        badges = badges,
        typeTag = stringResource(R.string.album_text),
        thumbnailContent = {
            LocalThumbnail(
                thumbnailUrl = thumbnailUrl,
                isActive = isActive,
                isPlaying = isPlaying,
                shape = RoundedCornerShape(itemCornerRadius),
                modifier = Modifier.fillMaxSize(),
                showCenterPlay = false,
                playButtonVisible = true,
            )
        },
        fillMaxWidth = fillMaxWidth,
        modifier = modifier,
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// THUMBNAILS & OVERLAYS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ItemThumbnail(
    thumbnailUrl: String?,
    isActive: Boolean,
    isPlaying: Boolean,
    shape: Shape,
    modifier: Modifier = Modifier,
    albumIndex: Int? = null,
    isSelected: Boolean = false,
    shouldLoadImage: Boolean = true,
    @DrawableRes placeholderIconRes: Int? = null,
    thumbnailRatio: Float = 1f,
) {
    val context = LocalContext.current

    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .fillMaxSize()
                .aspectRatio(thumbnailRatio)
                .clip(shape),
    ) {
        if (albumIndex == null) {
            if (placeholderIconRes != null) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(placeholderIconRes),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(ListThumbnailSize * 0.48f),
                    )
                }
            }

            if (shouldLoadImage && !thumbnailUrl.isNullOrBlank()) {
                val request =
                    remember(thumbnailUrl) {
                        ImageRequest
                            .Builder(context)
                            .data(thumbnailUrl.resize(544, 544))
                            .allowHardware(true)
                            .build()
                    }
                AsyncImage(
                    model = request,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else if (placeholderIconRes == null) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                )
            }
        }

        if (albumIndex != null) {
            AnimatedVisibility(
                visible = !isActive,
                enter = fadeIn() + expandIn(expandFrom = Alignment.Center),
                exit = shrinkOut(shrinkTowards = Alignment.Center) + fadeOut(),
            ) {
                Text(
                    text = albumIndex.toString(),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        if (isSelected) {
            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .zIndex(1f)
                        .clip(shape)
                        .background(Color.Black.copy(alpha = 0.45f)),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.done),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        val showCircularPlay = (isActive && !isPlaying && albumIndex == null)

        PlayingIndicatorBox(
            isActive = isActive,
            playWhenReady = isPlaying,
            color =
                if (albumIndex != null) {
                    if (isActive) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                } else {
                    Color.White
                },
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        color =
                            if (albumIndex != null || showCircularPlay) {
                                Color.Transparent
                            } else {
                                Color.Black.copy(alpha = ActiveBoxAlpha)
                            },
                        shape = shape,
                    ),
        )
    }
}

@Composable
fun LocalThumbnail(
    thumbnailUrl: String?,
    isActive: Boolean,
    isPlaying: Boolean,
    shape: Shape,
    modifier: Modifier = Modifier,
    showCenterPlay: Boolean = false,
    playButtonVisible: Boolean = false,
    thumbnailRatio: Float = 1f,
) {
    val context = LocalContext.current

    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .aspectRatio(thumbnailRatio)
                .clip(shape),
    ) {
        val request =
            remember(thumbnailUrl) {
                ImageRequest
                    .Builder(context)
                    .data(thumbnailUrl)
                    .allowHardware(true)
                    .build()
            }
        AsyncImage(
            model = request,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        AnimatedVisibility(
            visible = isActive,
            enter = fadeIn(tween(500)),
            exit = fadeOut(tween(500)),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = if (isPlaying) 0.4f else 0f), shape),
            ) {
                if (isPlaying) {
                    PlayingIndicator(
                        color = Color.White,
                        modifier = Modifier.height(24.dp),
                    )
                } else {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.play),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }
            }
        }

        if (showCenterPlay) {
            AnimatedVisibility(
                visible = !isActive,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.65f),
                    border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            painter = painterResource(R.drawable.play),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }

        if (playButtonVisible) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.65f),
                    border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            painter = painterResource(R.drawable.play),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistThumbnail(
    thumbnails: List<String>,
    size: Dp = 120.dp,
    placeHolder: @Composable () -> Unit,
    shape: Shape,
) {
    val context = LocalContext.current

    when (thumbnails.size) {
        0 -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clip(shape)
                        .background(MaterialTheme.colorScheme.surfaceContainer),
            ) {
                placeHolder()
            }
        }

        1 -> {
            val request =
                remember(thumbnails) {
                    ImageRequest
                        .Builder(context)
                        .data(thumbnails[0].resize(544, 544))
                        .allowHardware(true)
                        .build()
                }
            AsyncImage(
                model = request,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clip(shape),
            )
        }

        else -> {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clip(shape),
            ) {
                listOf(
                    Alignment.TopStart,
                    Alignment.TopEnd,
                    Alignment.BottomStart,
                    Alignment.BottomEnd,
                ).fastForEachIndexed { index, alignment ->
                    val url = thumbnails.getOrNull(index)
                    val request =
                        remember(url) {
                            ImageRequest
                                .Builder(context)
                                .data(url?.resize(256, 256))
                                .allowHardware(true)
                                .build()
                        }
                    AsyncImage(
                        model = request,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier =
                            Modifier
                                .align(alignment)
                                .fillMaxSize(0.5f),
                    )
                }
            }
        }
    }
}

@Composable
fun BoxScope.OverlayPlayButton(visible: Boolean) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier =
            Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
    ) {
        Surface(
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.65f),
            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.2f)),
            modifier = Modifier.size(34.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    painter = painterResource(R.drawable.play),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun BoxScope.AlbumPlayButton(
    visible: Boolean,
    onClick: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier =
            Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
    ) {
        Surface(
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.65f),
            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.2f)),
            onClick = onClick,
            modifier = Modifier.size(34.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    painter = painterResource(R.drawable.play),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun SwipeToSongBox(
    modifier: Modifier = Modifier,
    mediaItem: MediaItem,
    contentBackgroundColor: Color? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val ctx = LocalContext.current
    val player = LocalPlayerConnection.current
    val scope = rememberCoroutineScope()
    val offset = remember { mutableStateOf(0f) }
    val threshold = 300f
    val resolvedContentBackgroundColor = contentBackgroundColor ?: MaterialTheme.colorScheme.surface

    val dragState =
        rememberDraggableState { delta ->
            offset.value = (offset.value + delta).coerceIn(-threshold, threshold)
        }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = dragState,
                    onDragStopped = {
                        when {
                            offset.value >= threshold -> {
                                player?.playNext(listOf(mediaItem))
                                Toast.makeText(ctx, R.string.play_next, Toast.LENGTH_SHORT).show()
                                reset(offset, scope)
                            }

                            offset.value <= -threshold -> {
                                player?.addToQueue(listOf(mediaItem))
                                Toast.makeText(ctx, R.string.add_to_queue, Toast.LENGTH_SHORT).show()
                                reset(offset, scope)
                            }

                            else -> {
                                reset(offset, scope)
                            }
                        }
                    },
                ),
    ) {
        if (offset.value != 0f) {
            val (iconRes, bg, tint, align) =
                if (offset.value > 0) {
                    Quadruple(
                        R.drawable.playlist_play,
                        MaterialTheme.colorScheme.secondary,
                        MaterialTheme.colorScheme.onSecondary,
                        Alignment.CenterStart,
                    )
                } else {
                    Quadruple(
                        R.drawable.queue_music,
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.onPrimary,
                        Alignment.CenterEnd,
                    )
                }

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .align(Alignment.Center)
                        .background(bg),
                contentAlignment = align,
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier =
                        Modifier
                            .padding(horizontal = 24.dp)
                            .size(30.dp)
                            .alpha(0.9f),
                    tint = tint,
                )
            }
        }

        Box(
            modifier =
                Modifier
                    .offset { IntOffset(offset.value.roundToInt(), 0) }
                    .fillMaxWidth()
                    .background(resolvedContentBackgroundColor),
            content = content,
        )
    }
}

private fun reset(
    offset: MutableState<Float>,
    scope: CoroutineScope,
) {
    scope.launch {
        animate(
            initialValue = offset.value,
            targetValue = 0f,
            animationSpec = tween(durationMillis = 300),
        ) { value, _ -> offset.value = value }
    }
}

data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
)

// ─────────────────────────────────────────────────────────────────────────────
// BADGES (REDESIGNED PILL STYLE)
// ─────────────────────────────────────────────────────────────────────────────

private object Icon {
    @Composable
    fun Favorite() {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.16f),
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f)),
            modifier = Modifier.padding(end = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.favorite),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(11.dp),
                )
            }
        }
    }

    @Composable
    fun Library() {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
            modifier = Modifier.padding(end = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.library_add_check),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(11.dp),
                )
            }
        }
    }

    @Composable
    fun Download(
        state: Int?,
        percent: Float = -1f,
    ) {
        when (state) {
            STATE_COMPLETED -> {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)),
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.offline),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(11.dp),
                        )
                    }
                }
            }

            STATE_QUEUED, STATE_DOWNLOADING -> {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        if (percent > 0f) {
                            Text(
                                text = "${percent.toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        } else {
                            CircularWavyProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(11.dp),
                            )
                        }
                    }
                }
            }

            else -> { /* no icon */ }
        }
    }

    @Composable
    fun Explicit() {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            modifier = Modifier.padding(end = 4.dp)
        ) {
            Text(
                text = "E",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                ),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SMALL GRID ITEMS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SmallGridItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    isActive: Boolean = false,
    thumbnailContent: @Composable BoxWithConstraintsScope.() -> Unit,
    thumbnailShape: Shape,
    thumbnailRatio: Float = 1f,
    isArtist: Boolean? = false,
    typeTag: String? = null,
) {
    val itemCornerRadius = rememberItemCornerRadius()

    Card(
        shape = RoundedCornerShape(itemCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .width(SmallGridThumbnailHeight * 1.35f * thumbnailRatio)
            .aspectRatio(thumbnailRatio)
            .padding(4.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            thumbnailContent()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color.Transparent,
                            0.35f to Color.Transparent,
                            0.7f to Color.Black.copy(alpha = 0.65f),
                            1.0f to Color.Black.copy(alpha = 0.94f)
                        )
                    )
            )

            if (!typeTag.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.music_note),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = typeTag,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = Color.White
                            )
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) MaterialTheme.colorScheme.primary else Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.75f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun SongSmallGridItem(
    song: Song,
    modifier: Modifier = Modifier,
    viewCountText: String? = null,
    isActive: Boolean = false,
    isPlaying: Boolean = false,
) {
    val itemCornerRadius = rememberItemCornerRadius()
    val subtitle = joinByBullet(
        song.artists.joinToString { it.name },
        viewCountText
    )
    SmallGridItem(
        title = song.song.title,
        subtitle = subtitle,
        isActive = isActive,
        typeTag = "Song",
        thumbnailContent = {
            AsyncImage(
                model = song.song.thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            if (!isActive) {
                OverlayPlayButton(visible = true)
            }
        },
        thumbnailShape = RoundedCornerShape(itemCornerRadius),
        modifier = modifier,
    )
}

@Composable
fun ArtistSmallGridItem(
    artist: Artist,
    modifier: Modifier = Modifier,
) {
    val itemCornerRadius = rememberItemCornerRadius()
    SmallGridItem(
        title = artist.artist.name,
        typeTag = "Artist",
        thumbnailContent = {
            AsyncImage(
                model = artist.artist.thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        },
        thumbnailShape = RoundedCornerShape(itemCornerRadius),
        modifier = modifier,
        isArtist = true,
    )
}

@Composable
fun AlbumSmallGridItem(
    song: Song,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    isPlaying: Boolean = false,
) {
    val itemCornerRadius = rememberItemCornerRadius()
    song.song.albumName?.let {
        SmallGridItem(
            title = it,
            isActive = isActive,
            typeTag = stringResource(R.string.album_text),
            thumbnailContent = {
                AsyncImage(
                    model = song.song.thumbnailUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            },
            thumbnailShape = RoundedCornerShape(itemCornerRadius),
            modifier = modifier,
        )
    }
}

@Composable
fun YouTubeSmallGridItem(
    item: YTItem,
    modifier: Modifier = Modifier,
    viewCountText: String? = null,
    coroutineScope: CoroutineScope? = null,
    isActive: Boolean = false,
    isPlaying: Boolean = false,
    fillMaxWidth: Boolean = false,
) {
    val itemCornerRadius = rememberItemCornerRadius()

    val typeTag = when (item) {
        is SongItem -> "Song"
        is AlbumItem -> stringResource(R.string.album_text)
        is ArtistItem -> "Artist"
        is PlaylistItem -> "Playlist"
        is EpisodeItem -> "Episode"
        is PodcastItem -> "Podcast"
    }

    val subtitle = when (item) {
        is SongItem -> {
            val rawViews = item.views ?: (if (item.chartPosition != null) "#${item.chartPosition}" else null) ?: item.chartChange ?: viewCountText
            val viewsStr = stringResource(R.string.views)
            val playsStr = stringResource(R.string.plays)
            val views = rawViews
                ?.replace(" views", " $viewsStr", ignoreCase = true)
                ?.replace(" plays", " $playsStr", ignoreCase = true)
            
            joinByBullet(
                item.artists.joinToString { it.name },
                views
            )
        }
        is AlbumItem -> item.artists?.joinToString { it.name }
        is ArtistItem -> null
        is PlaylistItem -> item.songCountText ?: item.author?.name
        is EpisodeItem -> {
            val rawViews = viewCountText ?: item.views
            val viewsStr = stringResource(R.string.views)
            val playsStr = stringResource(R.string.plays)
            val views = rawViews
                ?.replace(" views", " $viewsStr", ignoreCase = true)
                ?.replace(" plays", " $playsStr", ignoreCase = true)
            joinByBullet(item.author?.name, item.publishDateText, views)
        }
        is PodcastItem -> joinByBullet(item.author?.name, item.episodeCountText)
    }

    SmallGridItem(
        title = item.title,
        subtitle = subtitle,
        isActive = isActive,
        typeTag = typeTag,
        thumbnailContent = {
            AsyncImage(
                model = item.thumbnail,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            if (item is SongItem || item is EpisodeItem) {
                if (!isActive) {
                    OverlayPlayButton(visible = true)
                }
            }
        },
        thumbnailShape = RoundedCornerShape(itemCornerRadius),
        modifier = modifier,
        isArtist = item is ArtistItem,
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// FEATURE CARDS (SPOTLIGHTS)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LibraryPinnedCollectionTile(
    title: String,
    @DrawableRes iconRes: Int,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = modifier,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors =
                                listOf(
                                    accentColor.copy(alpha = 0.28f),
                                    MaterialTheme.colorScheme.surfaceContainerHigh,
                                    MaterialTheme.colorScheme.surfaceContainerLow,
                                ),
                        ),
                    ),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.76f),
                    shape = CircleShape,
                ) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.padding(12.dp),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    subtitle?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

private val LibraryCardThumbnailSize = 72.dp
private val LibraryCardGlowElevation = 34.dp
private const val LibraryCardGlowAmbientAlpha = 0.82f
private const val LibraryCardGlowSpotAlpha = 0.96f

private fun playlistCountText(
    playlist: Playlist,
    autoPlaylist: Boolean,
): String =
    if (autoPlaylist) {
        ""
    } else if (playlist.songCount == 0 && playlist.playlist.remoteSongCount != null) {
        playlist.playlist.remoteSongCount.toString()
    } else {
        playlist.songCount.toString()
    }

@Composable
private fun playlistPlaceholderIcon(
    playlist: Playlist,
    autoPlaylist: Boolean,
): Int =
    when (playlist.playlist.name) {
        stringResource(R.string.liked) -> R.drawable.favorite_border
        stringResource(R.string.offline) -> R.drawable.offline
        stringResource(R.string.cached_playlist) -> R.drawable.cached
        else -> if (autoPlaylist) R.drawable.trending_up else R.drawable.queue_music
    }

@Composable
fun LibraryPlaylistFeatureCard(
    playlist: Playlist,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(26.dp),
    autoPlaylist: Boolean = false,
    trailingContent: @Composable RowScope.() -> Unit = {},
) {
    val subtitleText = playlistCountText(playlist = playlist, autoPlaylist = autoPlaylist)
    val thumbnailSize = LibraryCardThumbnailSize
    val itemCornerRadius = rememberItemCornerRadius()
    val thumbnailShape = RoundedCornerShape(itemCornerRadius)
    val context = LocalContext.current
    val primaryThumbnailUrl = playlist.thumbnails.getOrNull(0)
    var extractedGlowColor by remember(primaryThumbnailUrl) { mutableStateOf(Color.Transparent) }
    val glowColor by animateColorAsState(
        targetValue = extractedGlowColor,
        animationSpec = tween(400),
        label = "playlistItemGlow",
    )
    LaunchedEffect(primaryThumbnailUrl) {
        if (primaryThumbnailUrl == null) return@LaunchedEffect
        val bitmap =
            runCatching {
                context.imageLoader
                    .execute(
                        ImageRequest
                            .Builder(context)
                            .data(primaryThumbnailUrl)
                            .size(128)
                            .allowHardware(false)
                            .build(),
                    ).drawable
                    ?.toBitmapOrNull()
            }.getOrNull() ?: return@LaunchedEffect
        extractedGlowColor = withContext(Dispatchers.Default) { bitmap.extractThemeColor() }
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = shape,
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(thumbnailSize)
                        .shadow(
                            elevation = LibraryCardGlowElevation,
                            shape = thumbnailShape,
                            clip = false,
                            ambientColor = glowColor.copy(alpha = LibraryCardGlowAmbientAlpha),
                            spotColor = glowColor.copy(alpha = LibraryCardGlowSpotAlpha),
                        ),
            ) {
                PlaylistThumbnail(
                    thumbnails = playlist.thumbnails,
                    size = thumbnailSize,
                    placeHolder = {
                        Icon(
                            painter = painterResource(playlistPlaceholderIcon(playlist, autoPlaylist)),
                            contentDescription = null,
                            tint = LocalContentColor.current.copy(alpha = 0.8f),
                            modifier = Modifier.size(thumbnailSize / 2),
                        )
                    },
                    shape = thumbnailShape,
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = playlist.playlist.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitleText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.padding(start = 12.dp),
            ) {
                trailingContent()
            }
        }
    }
}

@Composable
fun LibraryAlbumSpotlightCard(
    album: Album,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(26.dp),
    isActive: Boolean = false,
    isPlaying: Boolean = false,
    onPlay: (() -> Unit)? = null,
    trailingContent: @Composable RowScope.() -> Unit = {},
) {
    val itemCornerRadius = rememberItemCornerRadius()
    val subtitle =
        joinByBullet(
            album.artists.joinToString { it.name },
            pluralStringResource(R.plurals.n_song, album.album.songCount, album.album.songCount),
        )
    val context = LocalContext.current
    var extractedGlowColor by remember(album.album.thumbnailUrl) { mutableStateOf(Color.Transparent) }
    val glowColor by animateColorAsState(
        targetValue = extractedGlowColor,
        animationSpec = tween(400),
        label = "albumItemGlow",
    )
    LaunchedEffect(album.album.thumbnailUrl) {
        val url = album.album.thumbnailUrl ?: return@LaunchedEffect
        val bitmap =
            runCatching {
                context.imageLoader
                    .execute(
                        ImageRequest
                            .Builder(context)
                            .data(url)
                            .size(128)
                            .allowHardware(false)
                            .build(),
                    ).drawable
                    ?.toBitmapOrNull()
            }.getOrNull() ?: return@LaunchedEffect
        extractedGlowColor = withContext(Dispatchers.Default) { bitmap.extractThemeColor() }
    }

    Card(
        shape = shape,
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (isActive) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    },
            ),
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(LibraryCardThumbnailSize)
                        .shadow(
                            elevation = LibraryCardGlowElevation,
                            shape = RoundedCornerShape(itemCornerRadius),
                            clip = false,
                            ambientColor = glowColor.copy(alpha = LibraryCardGlowAmbientAlpha),
                            spotColor = glowColor.copy(alpha = LibraryCardGlowSpotAlpha),
                        ),
            ) {
                LocalThumbnail(
                    thumbnailUrl = album.album.thumbnailUrl,
                    isActive = isActive,
                    isPlaying = isPlaying,
                    shape = RoundedCornerShape(itemCornerRadius),
                    modifier = Modifier.fillMaxSize(),
                )
                if (onPlay != null) {
                    AlbumPlayButton(
                        visible = !isActive,
                        onClick = onPlay,
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = album.album.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color =
                        if (isActive) {
                            MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.78f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.padding(start = 12.dp),
            ) {
                trailingContent()
            }
        }
    }
}

@Composable
fun LibraryArtistSpotlightCard(
    artist: Artist,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(26.dp),
    trailingContent: @Composable RowScope.() -> Unit = {},
) {
    val context = LocalContext.current
    var extractedGlowColor by remember(artist.artist.thumbnailUrl) { mutableStateOf(Color.Transparent) }
    val glowColor by animateColorAsState(
        targetValue = extractedGlowColor,
        animationSpec = tween(400),
        label = "artistItemGlow",
    )
    LaunchedEffect(artist.artist.thumbnailUrl) {
        val url = artist.artist.thumbnailUrl ?: return@LaunchedEffect
        val bitmap =
            runCatching {
                context.imageLoader
                    .execute(
                        ImageRequest
                            .Builder(context)
                            .data(url)
                            .size(128)
                            .allowHardware(false)
                            .build(),
                    ).drawable
                    ?.toBitmapOrNull()
            }.getOrNull() ?: return@LaunchedEffect
        extractedGlowColor = withContext(Dispatchers.Default) { bitmap.extractThemeColor() }
    }
    Card(
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(LibraryCardThumbnailSize)
                        .shadow(
                            elevation = LibraryCardGlowElevation,
                            shape = CircleShape,
                            clip = false,
                            ambientColor = glowColor.copy(alpha = LibraryCardGlowAmbientAlpha),
                            spotColor = glowColor.copy(alpha = LibraryCardGlowSpotAlpha),
                        ),
            ) {
                LocalThumbnail(
                    thumbnailUrl = artist.artist.thumbnailUrl,
                    isActive = false,
                    isPlaying = false,
                    shape = CircleShape,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = artist.artist.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = pluralStringResource(R.plurals.n_song, artist.songCount, artist.songCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.padding(start = 12.dp),
            ) {
                trailingContent()
            }
        }
    }
}

@Composable
fun MediaMetadataListItem(
    mediaMetadata: MediaMetadata,
    modifier: Modifier = Modifier,
    viewCountText: String? = null,
    isSelected: Boolean = false,
    isActive: Boolean = false,
    isPlaying: Boolean = false,
    shouldLoadImage: Boolean = true,
    trailingContent: @Composable RowScope.() -> Unit = {},
) {
    val itemCornerRadius = rememberItemCornerRadius()
    ListItem(
        title = mediaMetadata.title,
        subtitle =
            joinByBullet(
                mediaMetadata.artists.joinToString { it.name },
                makeTimeString(mediaMetadata.duration * 1000L),
                viewCountText,
            ),
        thumbnailContent = {
            ItemThumbnail(
                thumbnailUrl = mediaMetadata.thumbnailUrl,
                albumIndex = null,
                isSelected = isSelected,
                isActive = isActive,
                isPlaying = isPlaying,
                shouldLoadImage = shouldLoadImage,
                shape = RoundedCornerShape(itemCornerRadius),
                modifier = Modifier.size(ListThumbnailSize),
            )
        },
        trailingContent = trailingContent,
        modifier = modifier,
        isActive = isActive,
        isSelected = isSelected,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YouTubeListItem(
    item: YTItem,
    modifier: Modifier = Modifier,
    albumIndex: Int? = null,
    viewCountText: String? = null,
    isSelected: Boolean = false,
    isActive: Boolean = false,
    isPlaying: Boolean = false,
    isSwipeable: Boolean = true,
    trailingContent: @Composable RowScope.() -> Unit = {},
    badges: @Composable RowScope.() -> Unit = {
        val database = LocalDatabase.current
        val song by database.song(item.id).collectAsState(initial = null)
        val album by database.album(item.id).collectAsState(initial = null)

        if ((item is SongItem && song?.song?.liked == true) ||
            (item is AlbumItem && album?.album?.bookmarkedAt != null)
        ) {
            Icon.Favorite()
        }
        if (item.explicit) Icon.Explicit()
        if (item is SongItem && song?.song?.inLibrary != null) {
            Icon.Library()
        }
        if (item is SongItem) {
            val downloads by LocalDownloadUtil.current.downloads.collectAsState()
            val download = downloads[item.id]
            Icon.Download(download?.state, percent = download?.percentDownloaded ?: -1f)
        }
    },
) {
    val swipeEnabled by rememberPreference(SwipeToSongKey, defaultValue = false)
    val itemCornerRadius = rememberItemCornerRadius()

    val content: @Composable () -> Unit = {
        ListItem(
            title = item.title,
            subtitle =
                when (item) {
                    is SongItem -> {
                        val rawViews = viewCountText
                            ?: item.views
                            ?: (if (item.chartPosition != null) "#${item.chartPosition}" else null)
                            ?: item.chartChange
                        
                        val viewsStr = stringResource(R.string.views)
                        val playsStr = stringResource(R.string.plays)
                        val views = rawViews
                            ?.replace(" views", " $viewsStr", ignoreCase = true)
                            ?.replace(" plays", " $playsStr", ignoreCase = true)

                        joinByBullet(
                            item.artists.joinToString { it.name },
                            makeTimeString(item.duration?.times(1000L)),
                            views,
                        )
                    }

                    is AlbumItem -> {
                        joinByBullet(item.artists?.joinToString { it.name }, item.year?.toString())
                    }

                    is ArtistItem -> {
                        null
                    }

                    is PlaylistItem -> {
                        joinByBullet(item.author?.name, item.songCountText)
                    }
                    
                    is EpisodeItem -> {
                        val rawViews = viewCountText ?: item.views
                        val viewsStr = stringResource(R.string.views)
                        val playsStr = stringResource(R.string.plays)
                        val views = rawViews
                            ?.replace(" views", " $viewsStr", ignoreCase = true)
                            ?.replace(" plays", " $playsStr", ignoreCase = true)
                        
                        joinByBullet(item.author?.name, item.publishDateText, views)
                    }
                    
                    is PodcastItem -> {
                        joinByBullet(item.author?.name, item.episodeCountText)
                    }
                },
            badges = badges,
            thumbnailContent = {
                ItemThumbnail(
                    thumbnailUrl = item.thumbnail,
                    albumIndex = albumIndex,
                    isSelected = isSelected,
                    isActive = isActive,
                    isPlaying = isPlaying,
                    shape = if (item is ArtistItem) CircleShape else RoundedCornerShape(itemCornerRadius),
                    modifier = Modifier.size(ListThumbnailSize),
                )
            },
            trailingContent = trailingContent,
            modifier = modifier,
            isActive = isActive,
            isSelected = isSelected,
        )
    }

    if (item is SongItem && isSwipeable && swipeEnabled) {
        SwipeToSongBox(
            mediaItem = item.copy(thumbnail = item.thumbnail.resize(1080, 1080)).toMediaItem(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            content()
        }
    } else {
        content()
    }
}
