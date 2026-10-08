package com.cgens67.gluetune.models

import androidx.compose.runtime.Immutable
import com.cgens67.gluetune.extensions.toMediaItem
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
