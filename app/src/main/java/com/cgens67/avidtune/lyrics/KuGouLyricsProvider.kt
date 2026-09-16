package com.cgens67.gluetune.lyrics

import android.content.Context
import com.cgens67.kugou.KuGou
import com.cgens67.gluetune.constants.EnableKugouKey
import com.cgens67.gluetune.utils.dataStore
import com.cgens67.gluetune.utils.get

object KuGouLyricsProvider : LyricsProvider {
    override val name = "Kugou"
    override fun isEnabled(context: Context): Boolean =
        context.dataStore[EnableKugouKey] ?: true

    override suspend fun getLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int
    ): Result<String> =
        KuGou.getLyrics(title, artist, duration)

    override suspend fun getAllLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
        callback: (String) -> Unit
    ) {
        KuGou.getAllPossibleLyricsOptions(title, artist, duration, callback)
    }
}
