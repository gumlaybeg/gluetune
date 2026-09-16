package com.cgens67.gluetune.lyrics

import android.content.Context
import com.cgens67.music.betterlyrics.BetterLyrics
import com.cgens67.gluetune.constants.EnableBetterLyricsKey
import com.cgens67.gluetune.utils.dataStore
import com.cgens67.gluetune.utils.get

object BetterLyricsProvider : LyricsProvider {
    override val name = "BetterLyrics"

    override fun isEnabled(context: Context): Boolean = context.dataStore[EnableBetterLyricsKey] ?: true

    override suspend fun getLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
    ): Result<String> = BetterLyrics.getLyrics(title, artist, duration, null)
}