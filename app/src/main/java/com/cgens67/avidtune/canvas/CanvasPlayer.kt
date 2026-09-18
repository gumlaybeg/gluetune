
package com.cgens67.gluetune.canvas

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.Gravity
import android.view.TextureView
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.cgens67.gluetune.utils.rememberPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

val CanvasLoadOnlyWifiKey = booleanPreferencesKey("canvas_load_only_wifi")
val AlbumCanvasEnabledKey = booleanPreferencesKey("album_canvas_enabled")

object CanvasArtworkPlaybackCache {
    private val cache = android.util.LruCache<String, CanvasArtwork>(20)
    fun get(key: String): CanvasArtwork? = cache.get(key)
    fun put(key: String, artwork: CanvasArtwork) {
        cache.put(key, artwork)
    }
}

fun isWifiConnected(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
}

@Composable
fun rememberAlbumCanvas(
    albumTitle: String?,
    artistName: String?,
    firstSongTitle: String? = null,
): CanvasArtwork? {
    val cacheKey = remember(albumTitle, artistName) {
        if (albumTitle != null && artistName != null) "album|$albumTitle|$artistName" else null
    }

    var canvasArtwork by remember(cacheKey) {
        mutableStateOf(cacheKey?.let { CanvasArtworkPlaybackCache.get(it) })
    }
    
    val storefront = remember {
        val country = Locale.getDefault().country
        if (country.length == 2) country.lowercase(Locale.ROOT) else "us"
    }

    val canvasLoadOnlyWifi by rememberPreference(CanvasLoadOnlyWifiKey, defaultValue = false)
    val context = LocalContext.current

    LaunchedEffect(albumTitle, artistName, firstSongTitle) {
        if (canvasArtwork != null || cacheKey == null) return@LaunchedEffect
        if (canvasLoadOnlyWifi && !isWifiConnected(context)) return@LaunchedEffect
        if (albumTitle.isNullOrBlank() || artistName.isNullOrBlank()) {
            canvasArtwork = null
            return@LaunchedEffect
        }

        val fetched = withContext(Dispatchers.IO) {
            val tasks = buildList {
                add(albumTitle to artistName)
                val clean = cleanAlbumTitle(albumTitle)
                if (clean != albumTitle && clean.isNotBlank()) {
                    add(clean to artistName)
                }
            }

            tasks.firstNotNullOfOrNull { (alb, art) ->
                AppleMusicCanvasProvider.getByAlbumArtist(album = alb, artist = art, storefront = storefront)
                    ?.takeIf { !it.preferredAnimationUrl.isNullOrBlank() }
                    ?: (if (storefront != "us") AppleMusicCanvasProvider.getByAlbumArtist(album = alb, artist = art, storefront = "us")?.takeIf { !it.preferredAnimationUrl.isNullOrBlank() } else null)
                    ?: TidalCanvasProvider.getByAlbumArtist(album = alb, artist = art)
                        ?.takeIf { !it.preferredAnimationUrl.isNullOrBlank() }
                    ?: (firstSongTitle?.let { song ->
                        ViviMusicCanvasProvider.getBySongArtist(song = song, artist = art, album = alb)
                            ?.takeIf { !it.preferredAnimationUrl.isNullOrBlank() }
                    })
            }
        }

        if (fetched != null) {
            canvasArtwork = fetched
            CanvasArtworkPlaybackCache.put(cacheKey, fetched)
        }
    }

    return canvasArtwork
}

@Composable
fun CanvasArtworkPlayer(
    primaryUrl: String?,
    fallbackUrl: String?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val urlToPlay = primaryUrl ?: fallbackUrl ?: return
    var isVideoReady by remember { mutableStateOf(false) }
    
    val okHttpClient = remember { okhttp3.OkHttpClient.Builder().build() }
    val mediaSourceFactory = remember(okHttpClient) {
        DefaultMediaSourceFactory(
            DefaultDataSource.Factory(
                context,
                OkHttpDataSource.Factory(okHttpClient),
            ),
        )
    }
    
    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                        .build(),
                    false,
                )
                volume = 0f
                repeatMode = Player.REPEAT_MODE_ONE
                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            }
    }
    
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onRenderedFirstFrame() {
                isVideoReady = true
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }
    
    LaunchedEffect(urlToPlay) {
        val mimeType = if (urlToPlay.contains("m3u8")) MimeTypes.APPLICATION_M3U8 else MimeTypes.VIDEO_MP4
        val mediaItem = MediaItem.Builder()
            .setUri(urlToPlay)
            .setMimeType(mimeType)
            .build()
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
    }
    
    LaunchedEffect(isPlaying) {
        if (isPlaying) exoPlayer.play() else exoPlayer.pause()
    }
    
    val videoAlpha by animateFloatAsState(
        targetValue = if (isVideoReady) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "videoAlpha"
    )
    
    Box(modifier = modifier) {
        AndroidView(
            factory = { viewContext ->
                FrameLayout(viewContext).apply {
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    val textureView = TextureView(viewContext).apply {
                        layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER)
                    }
                    addView(textureView)
                    exoPlayer.setVideoTextureView(textureView)
                }
            },
            modifier = Modifier.matchParentSize().alpha(videoAlpha)
        )
    }
}
