package com.cgens67.gluetune.models

import androidx.compose.runtime.Immutable
import com.cgens67.gluetune.extensions.toMediaItem

@Immutable
data class CreatorPick(
    val id: String,                 // YouTube Video ID
    val title: String,
    val artist: String,
    val durationSeconds: Int,
    val thumbnailUrl: String = "https://i.ytimg.com/vi/$id/hqdefault.jpg",
    val note: String? = null        // Optional note like "All-time Classic", "On Repeat", etc.
) {
    fun toMediaMetadata(): MediaMetadata {
        return MediaMetadata(
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
    }

    fun toMediaItem() = toMediaMetadata().toMediaItem()
}

object CreatorsPicksRepository {
    /**
     * Add or edit your personal selections here!
     * The ID is the YouTube Video ID (e.g. from https://music.youtube.com/watch?v=VIDEO_ID)
     */
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
        )
    )
}
