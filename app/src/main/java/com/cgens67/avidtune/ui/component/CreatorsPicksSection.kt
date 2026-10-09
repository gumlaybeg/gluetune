@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)

package com.cgens67.gluetune.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.cgens67.gluetune.R
import com.cgens67.gluetune.extensions.toMediaItem
import com.cgens67.gluetune.extensions.togglePlayPause
import com.cgens67.gluetune.models.MediaMetadata
import com.cgens67.gluetune.playback.PlayerConnection
import com.cgens67.gluetune.playback.queues.ListQueue
import com.cgens67.gluetune.ui.menu.YouTubeSongMenu
import com.cgens67.gluetune.ui.utils.resize
import com.cgens67.innertube.YouTube
import com.cgens67.innertube.models.Artist as InnertubeArtist
import com.cgens67.innertube.models.ArtistItem
import com.cgens67.innertube.models.SongItem
import com.cgens67.innertube.models.WatchEndpoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Immutable
data class CreatorPick(
    val id: String,
    val title: String,
    val artist: String,
    val durationSeconds: Int,
    val thumbnailUrl: String = "https://i.ytimg.com/vi/$id/maxresdefault.jpg",
    val note: String? = null
) {
    fun toMediaMetadata(resolvedThumbnail: String? = null): MediaMetadata = MediaMetadata(
        id = id,
        title = title,
        artists = listOf(MediaMetadata.Artist(id = null, name = artist)),
        duration = durationSeconds,
        thumbnailUrl = resolvedThumbnail ?: thumbnailUrl,
        album = null,
        explicit = false,
        liked = false,
        isVideo = false
    )

    fun toMediaItem(resolvedThumbnail: String? = null) = toMediaMetadata(resolvedThumbnail).toMediaItem()

    fun toSongItem(resolvedThumbnail: String? = null) = SongItem(
        id = id,
        title = title,
        artists = listOf(InnertubeArtist(id = null, name = artist)),
        album = null,
        duration = durationSeconds,
        thumbnail = (resolvedThumbnail ?: thumbnailUrl),
        explicit = false,
        endpoint = WatchEndpoint(videoId = id)
    )
}

object CreatorsPicksRepository {
    val picks: List<CreatorPick> = listOf(
        CreatorPick(
            id = "Kr4EQDVETuA",
            title = "Billie Jean",
            artist = "Michael Jackson",
            durationSeconds = 294,
            note = "All-Time Classic"
        ),
        CreatorPick(
            id = "g0ViBH7m4XA",
            title = "Off The Wall",
            artist = "Michael Jackson",
            durationSeconds = 246,
            note = "Groovy Vibe"
        ),
        CreatorPick(
            id = "a4O-abCXsfA",
            title = "Timeless",
            artist = "The Weeknd & Playboi Carti",
            durationSeconds = 256,
            note = "Heavy Rotation"
        ),
        CreatorPick(
            id = "IKlTR6Wlu0o",
            title = "Who's Lovin' You",
            artist = "Jackson 5",
            durationSeconds = 241,
            note = "Soul Classic"
        ),
        CreatorPick(
            id = "ML63tY6uWFk",
            title = "RATHER LIE",
            artist = "Playboi Carti & The Weeknd",
            durationSeconds = 160,
            note = "Trending"
        ),
        CreatorPick(
            id = "pzaNexXFWpA",
            title = "National Treasures",
            artist = "Drake",
            durationSeconds = 174,
            note = "Top Pick"
        )
    )
}

@Composable
fun CreatorsPicksSection(
    currentMediaMetadata: MediaMetadata?,
    isPlaying: Boolean,
    navController: NavController,
    playerConnection: PlayerConnection,
    menuState: MenuState,
    haptic: HapticFeedback,
    modifier: Modifier = Modifier,
    picks: List<CreatorPick> = CreatorsPicksRepository.picks
) {
    if (picks.isEmpty()) return

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Cache clean thumbnails fetched directly from YouTube Music to replace letterboxed ones
    val resolvedThumbnails = remember { mutableStateMapMapOf<String, String>() }

    LaunchedEffect(picks) {
        withContext(Dispatchers.IO) {
            picks.forEach { pick ->
                if (!resolvedThumbnails.containsKey(pick.id)) {
                    YouTube.player(pick.id).onSuccess { response ->
                        val cleanThumb = response.videoDetails?.thumbnail?.thumbnails?.maxByOrNull { it.width }?.url
                        if (cleanThumb != null) {
                            resolvedThumbnails[pick.id] = cleanThumb.resize(800, 800)
                        }
                    }
                }
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.auto_awesome),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Creator's Top Picks",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Handpicked tracks on repeat",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                onClick = {
                    playerConnection.playQueue(
                        ListQueue(
                            title = "Creator's Top Picks",
                            items = picks.shuffled().map { it.toMediaItem(resolvedThumbnails[it.id]) },
                            startIndex = 0
                        )
                    )
                },
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.shuffle),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Play All",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Card List
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(picks, key = { _, item -> item.id }) { index, pick ->
                val isActive = currentMediaMetadata?.id == pick.id
                val currentArt = resolvedThumbnails[pick.id] ?: pick.thumbnailUrl

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) MaterialTheme.colorScheme.secondaryContainer 
                        else MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .width(160.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .combinedClickable(
                            onClick = {
                                if (isActive) {
                                    playerConnection.player.togglePlayPause()
                                } else {
                                    playerConnection.playQueue(
                                        ListQueue(
                                            title = "Creator's Top Picks",
                                            items = picks.map { it.toMediaItem(resolvedThumbnails[it.id]) },
                                            startIndex = index
                                        )
                                    )
                                }
                            },
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                menuState.show {
                                    YouTubeSongMenu(
                                        song = pick.toSongItem(resolvedThumbnails[pick.id]),
                                        navController = navController,
                                        onDismiss = menuState::dismiss
                                    )
                                }
                            }
                        )
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        // Square Aspect-ratio Box with zoom-crop to remove black borders
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        ) {
                            SubcomposeAsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(currentArt)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = pick.title,
                                contentScale = ContentScale.Crop,
                                // Scale up slightly to clip off any hardcoded YouTube black bars
                                modifier = Modifier
                                    .fillMaxSize()
                                    .scale(1.15f),
                                loading = {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularWavyProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            )

                            // Shading gradient
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.Black.copy(alpha = 0.2f),
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.65f)
                                            )
                                        )
                                    )
                            )

                            // Active Playing Indicator
                            if (isActive && isPlaying) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp)
                                        .size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            painter = painterResource(R.drawable.volume_up),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            // Note Tag
                            if (!pick.note.isNullOrBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.Black.copy(alpha = 0.72f),
                                    border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.2f)),
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = pick.note,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Song Title
                        Text(
                            text = pick.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Spacer(Modifier.height(2.dp))

                        // Clickable Artist Navigation
                        Text(
                            text = pick.artist,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    val primaryArtist = pick.artist.split("&", ",", "feat.", "ft.").firstOrNull()?.trim() ?: pick.artist
                                    coroutineScope.launch(Dispatchers.IO) {
                                        YouTube.search(primaryArtist, YouTube.SearchFilter.FILTER_ARTIST).onSuccess { res ->
                                            val artistItem = res.items.filterIsInstance<ArtistItem>().firstOrNull()
                                            if (artistItem != null) {
                                                withContext(Dispatchers.Main) {
                                                    navController.navigate("artist/${artistItem.id}")
                                                }
                                            } else {
                                                withContext(Dispatchers.Main) {
                                                    navController.navigate("search/${java.net.URLEncoder.encode(primaryArtist, "UTF-8")}")
                                                }
                                            }
                                        }.onFailure {
                                            withContext(Dispatchers.Main) {
                                                navController.navigate("search/${java.net.URLEncoder.encode(primaryArtist, "UTF-8")}")
                                            }
                                        }
                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}

private fun <K, V> mutableStateMapMapOf() = mutableStateMapOf<K, V>()
