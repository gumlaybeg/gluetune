package com.cgens67.gluetune.models

import androidx.compose.runtime.Immutable
import com.cgens67.gluetune.extensions.toMediaItem
import com.cgens67.innertube.models.Album as InnertubeAlbum
import com.cgens67.innertube.models.Artist as InnertubeArtist
import com.cgens67.innertube.models.SongItem
import com.cgens67.innertube.models.WatchEndpoint

@Immutable
data class CreatorPick(
    val id: String,
    val title: String,
    val artist: String,
    val album: String? = null,
    val artistId: String? = null,
    val albumId: String? = null,
    val durationSeconds: Int,
    val note: String? = null
) {
    fun toMediaMetadata(
        resolvedThumbnail: String? = null,
        resolvedArtistId: String? = null,
        resolvedAlbumId: String? = null,
        resolvedAlbumName: String? = null,
    ): MediaMetadata = MediaMetadata(
        id = id,
        title = title,
        artists = listOf(
            MediaMetadata.Artist(
                id = resolvedArtistId ?: artistId,
                name = artist
            )
        ),
        duration = durationSeconds,
        thumbnailUrl = resolvedThumbnail ?: "https://i.ytimg.com/vi/$id/maxresdefault.jpg",
        album = (resolvedAlbumId ?: albumId)?.let { albId ->
            MediaMetadata.Album(
                id = albId,
                title = resolvedAlbumName ?: album ?: ""
            )
        },
        explicit = false,
        liked = false,
        isVideo = false
    )

    fun toMediaItem(
        resolvedThumbnail: String? = null,
        resolvedArtistId: String? = null,
        resolvedAlbumId: String? = null,
        resolvedAlbumName: String? = null,
    ) = toMediaMetadata(
        resolvedThumbnail = resolvedThumbnail,
        resolvedArtistId = resolvedArtistId,
        resolvedAlbumId = resolvedAlbumId,
        resolvedAlbumName = resolvedAlbumName
    ).toMediaItem()

    fun toSongItem(
        resolvedThumbnail: String? = null,
        resolvedArtistId: String? = null,
        resolvedAlbumId: String? = null,
        resolvedAlbumName: String? = null,
    ) = SongItem(
        id = id,
        title = title,
        artists = listOf(
            InnertubeArtist(
                id = resolvedArtistId ?: artistId,
                name = artist
            )
        ),
        album = (resolvedAlbumId ?: albumId)?.let { albId ->
            InnertubeAlbum(
                name = resolvedAlbumName ?: album ?: "",
                id = albId
            )
        },
        duration = durationSeconds,
        thumbnail = resolvedThumbnail ?: "https://i.ytimg.com/vi/$id/maxresdefault.jpg",
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
            album = "Thriller",
            durationSeconds = 294,
            note = "All-Time Classic"
        ),
        CreatorPick(
            id = "g0ViBH7m4XA",
            title = "Off The Wall",
            artist = "Michael Jackson",
            album = "Off The Wall",
            durationSeconds = 246,
            note = "Groovy Vibe"
        ),
        CreatorPick(
            id = "a4O-abCXsfA",
            title = "Timeless",
            artist = "The Weeknd & Playboi Carti",
            album = "Hurry Up Tomorrow",
            durationSeconds = 256,
            note = "Heavy Rotation"
        ),
        CreatorPick(
            id = "IKlTR6Wlu0o",
            title = "Who's Lovin' You",
            artist = "Jackson 5",
            album = "Diana Ross Presents The Jackson 5",
            durationSeconds = 241,
            note = "Soul Classic"
        ),
        CreatorPick(
            id = "ML63tY6uWFk",
            title = "RATHER LIE",
            artist = "Playboi Carti & The Weeknd",
            album = "I AM MUSIC",
            durationSeconds = 160,
            note = "Trending"
        ),
        CreatorPick(
            id = "pzaNexXFWpA",
            title = "National Treasures",
            artist = "Drake",
            album = "Single",
            durationSeconds = 174,
            note = "Top Pick"
        )
    )
}
