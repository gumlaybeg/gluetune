@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)

package com.cgens67.gluetune.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cgens67.gluetune.R
import com.cgens67.gluetune.extensions.toMediaItem
import com.cgens67.gluetune.extensions.togglePlayPause
import com.cgens67.gluetune.models.MediaMetadata
import com.cgens67.gluetune.playback.PlayerConnection
import com.cgens67.gluetune.playback.queues.ListQueue
import com.cgens67.gluetune.ui.menu.YouTubeSongMenu
import com.cgens67.innertube.models.Artist as InnertubeArtist
import com.cgens67.innertube.models.SongItem
import com.cgens67.innertube.models.WatchEndpoint

@Immutable
data class CreatorPick(
    val id: String,
    val title: String,
    val artist: String,
    val durationSeconds: Int,
    val thumbnailUrl: String = "https://i.ytimg.com/vi/$id/hqdefault.jpg",
    val note: String? = null
) {
    fun toMediaMetadata(): MediaMetadata = MediaMetadata(
        id = id,
        title = title,
        artists = listOf(MediaMetadata.Artist(id = null, name = artist)),
        duration = durationSeconds,
        thumbnailUrl = thumbnailUrl,
        album = null,
        explicit = false,
        liked = false,
        isVideo = false
    )

    fun toMediaItem() = toMediaMetadata().toMediaItem()

    fun toSongItem() = SongItem(
        id = id,
        title = title,
        artists = listOf(InnertubeArtist(id = null, name = artist)),
        album = null,
        duration = durationSeconds,
        thumbnail = thumbnailUrl,
        explicit = false,
        endpoint = WatchEndpoint(videoId = id)
    )
}

object CreatorsPicksRepository {
    val picks: List<CreatorPick> = listOf(
        CreatorPick(
            id = "Zi_XLOR8Kw0",
            title = "Billie Jean",
            artist = "Michael Jackson",
            durationSeconds = 294,
            note = "All-Time Classic"
        ),
        CreatorPick(
            id = "JSvT_f_a91I",
            title = "Timeless",
            artist = "The Weeknd & Playboi Carti",
            durationSeconds = 256,
            note = "Heavy Rotation"
        ),
        CreatorPick(
            id = "kPa7bsKwL-8",
            title = "Die With A Smile",
            artist = "Lady Gaga & Bruno Mars",
            durationSeconds = 251,
            note = "Creator's Favorite"
        ),
        CreatorPick(
            id = "d38H45c9x9g",
            title = "Starboy",
            artist = "The Weeknd ft. Daft Punk",
            durationSeconds = 230,
            note = "Essential"
        ),
        CreatorPick(
            id = "h_D3VFfhvs4",
            title = "Smooth Criminal",
            artist = "Michael Jackson",
            durationSeconds = 257,
            note = "Masterpiece"
        ),
        CreatorPick(
            id = "fJ9rUzIMcZQ",
            title = "Bohemian Rhapsody",
            artist = "Queen",
            durationSeconds = 354,
            note = "Legendary"
        ),
        CreatorPick(
            id = "5NV6Rdv1a3w",
            title = "Get Lucky",
            artist = "Daft Punk ft. Pharrell Williams",
            durationSeconds = 248,
            note = "Timeless Vibe"
        ),
        CreatorPick(
            id = "T6eK-2OQtew",
            title = "Not Like Us",
            artist = "Kendrick Lamar",
            durationSeconds = 274,
            note = "Instant Classic"
        ),
        CreatorPick(
            id = "uzS3WG6__G4",
            title = "Pink + White",
            artist = "Frank Ocean",
            durationSeconds = 184,
            note = "Pure Soul"
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
                            items = picks.shuffled().map { it.toMediaItem() },
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
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(picks, key = { _, item -> item.id }) { index, pick ->
                val isActive = currentMediaMetadata?.id == pick.id

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
                                            items = picks.map { it.toMediaItem() },
                                            startIndex = index
                                        )
                                    )
                                }
                            },
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                menuState.show {
                                    YouTubeSongMenu(
                                        song = pick.toSongItem(),
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
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(pick.thumbnailUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = pick.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.Black.copy(alpha = 0.25f),
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
                            text = pick.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = pick.artist,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
