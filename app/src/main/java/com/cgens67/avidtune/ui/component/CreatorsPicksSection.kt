@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)

package com.cgens67.gluetune.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import com.cgens67.gluetune.LocalDatabase
import com.cgens67.gluetune.R
import com.cgens67.gluetune.extensions.toMediaItem
import com.cgens67.gluetune.extensions.togglePlayPause
import com.cgens67.gluetune.models.CreatorPick
import com.cgens67.gluetune.models.CreatorsPicksRepository
import com.cgens67.gluetune.models.MediaMetadata
import com.cgens67.gluetune.playback.PlayerConnection
import com.cgens67.gluetune.playback.queues.ListQueue
import com.cgens67.gluetune.ui.menu.YouTubeSongMenu
import com.cgens67.gluetune.ui.utils.resize
import com.cgens67.innertube.YouTube
import com.cgens67.innertube.models.AlbumItem
import com.cgens67.innertube.models.ArtistItem
import com.cgens67.innertube.models.SongItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder

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
    val database = LocalDatabase.current

    // Caches official clean metadata (clean square cover, artist ID, album ID)
    val resolvedSongItems = remember { mutableStateMapOf<String, SongItem>() }

    LaunchedEffect(picks) {
        withContext(Dispatchers.IO) {
            picks.forEach { pick ->
                if (!resolvedSongItems.containsKey(pick.id)) {
                    // Check local database first
                    val dbSong = database.song(pick.id).firstOrNull()
                    if (dbSong != null && !dbSong.song.thumbnailUrl.isNullOrBlank()) {
                        resolvedSongItems[pick.id] = SongItem(
                            id = dbSong.song.id,
                            title = dbSong.song.title,
                            artists = dbSong.artists.map { com.cgens67.innertube.models.Artist(id = it.id, name = it.name) },
                            album = dbSong.album?.let { com.cgens67.innertube.models.Album(name = it.title, id = it.id) },
                            duration = dbSong.song.duration,
                            thumbnail = dbSong.song.thumbnailUrl ?: "",
                            explicit = false
                        )
                    } else {
                        // Fetch official YouTube Music metadata
                        YouTube.queue(listOf(pick.id)).onSuccess { songs ->
                            songs.firstOrNull()?.let { songItem ->
                                resolvedSongItems[pick.id] = songItem
                            }
                        }
                    }
                }
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
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
                            items = picks.shuffled().map { pick ->
                                val song = resolvedSongItems[pick.id]
                                pick.toMediaItem(
                                    resolvedThumbnail = song?.thumbnail,
                                    resolvedArtistId = song?.artists?.firstOrNull()?.id,
                                    resolvedAlbumId = song?.album?.id,
                                    resolvedAlbumName = song?.album?.name
                                )
                            },
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

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(picks, key = { _, item -> item.id }) { index, pick ->
                val isActive = currentMediaMetadata?.id == pick.id
                val resolvedSong = resolvedSongItems[pick.id]
                val hasCleanThumbnail = resolvedSong?.thumbnail != null
                val currentArt = resolvedSong?.thumbnail?.resize(800, 800)
                    ?: "https://i.ytimg.com/vi/${pick.id}/mqdefault.jpg"

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
                                            items = picks.map { p ->
                                                val s = resolvedSongItems[p.id]
                                                p.toMediaItem(
                                                    resolvedThumbnail = s?.thumbnail,
                                                    resolvedArtistId = s?.artists?.firstOrNull()?.id,
                                                    resolvedAlbumId = s?.album?.id,
                                                    resolvedAlbumName = s?.album?.name
                                                )
                                            },
                                            startIndex = index
                                        )
                                    )
                                }
                            },
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                menuState.show {
                                    val songItem = resolvedSong ?: pick.toSongItem()
                                    YouTubeSongMenu(
                                        song = songItem,
                                        navController = navController,
                                        onDismiss = menuState::dismiss
                                    )
                                }
                            }
                        )
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
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
                                modifier = Modifier
                                    .fillMaxSize()
                                    .scale(if (hasCleanThumbnail) 1f else 1.45f), // Crops out fallback YouTube letterboxes
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

                        Text(
                            text = resolvedSong?.title ?: pick.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(Modifier.height(2.dp))

                        // Clickable Artist Navigation
                        val artistName = resolvedSong?.artists?.joinToString(", ") { it.name } ?: pick.artist
                        val artistId = resolvedSong?.artists?.firstOrNull()?.id ?: pick.artistId

                        Text(
                            text = artistName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    if (!artistId.isNullOrBlank()) {
                                        navController.navigate("artist/$artistId")
                                    } else {
                                        val primaryArtist = artistName.split("&", ",", "feat.", "ft.").firstOrNull()?.trim() ?: artistName
                                        coroutineScope.launch(Dispatchers.IO) {
                                            YouTube.search(primaryArtist, YouTube.SearchFilter.FILTER_ARTIST).onSuccess { res ->
                                                val foundArtist = res.items.filterIsInstance<ArtistItem>().firstOrNull()
                                                withContext(Dispatchers.Main) {
                                                    if (foundArtist != null) {
                                                        navController.navigate("artist/${foundArtist.id}")
                                                    } else {
                                                        navController.navigate("search/${URLEncoder.encode(primaryArtist, "UTF-8")}")
                                                    }
                                                }
                                            }.onFailure {
                                                withContext(Dispatchers.Main) {
                                                    navController.navigate("search/${URLEncoder.encode(primaryArtist, "UTF-8")}")
                                                }
                                            }
                                        }
                                    }
                                }
                        )

                        // Clickable Album Navigation
                        val albumName = resolvedSong?.album?.name ?: pick.album
                        val albumId = resolvedSong?.album?.id ?: pick.albumId

                        if (!albumName.isNullOrBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = albumName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable {
                                        if (!albumId.isNullOrBlank()) {
                                            navController.navigate("album/$albumId")
                                        } else {
                                            coroutineScope.launch(Dispatchers.IO) {
                                                YouTube.search("$albumName $artistName", YouTube.SearchFilter.FILTER_ALBUM).onSuccess { res ->
                                                    val foundAlbum = res.items.filterIsInstance<AlbumItem>().firstOrNull()
                                                    withContext(Dispatchers.Main) {
                                                        if (foundAlbum != null) {
                                                            navController.navigate("album/${foundAlbum.id}")
                                                        } else {
                                                            navController.navigate("search/${URLEncoder.encode(albumName, "UTF-8")}")
                                                        }
                                                    }
                                                }.onFailure {
                                                    withContext(Dispatchers.Main) {
                                                        navController.navigate("search/${URLEncoder.encode(albumName, "UTF-8")}")
                                                    }
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
}
