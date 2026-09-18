
package com.cgens67.gluetune.canvas

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.KotlinxSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.text.Normalizer
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

// --- Models ---

@Serializable
data class CanvasArtwork(
    val name: String? = null,
    val artist: String? = null,
    @SerialName("albumId")
    val albumId: String? = null,
    val albumName: String? = null,
    val static: String? = null,
    val animated: String? = null,
    val videoUrl: String? = null,
    val animatedTall: String? = null,
) {
    val preferredAnimationUrl: String?
        get() = animated ?: videoUrl
}

@Serializable
data class ViviMusicCanvasManifest(
    val items: List<ViviMusicCanvasItem> = emptyList()
)

@Serializable
data class ViviMusicCanvasItem(
    val song: String,
    val artist: String,
    val url: String,
    val album: String = "",
)

// --- Utils ---

fun String.normalizeForComparison(): String {
    val decomposed = Normalizer.normalize(this, Normalizer.Form.NFD)
    val withoutDiacritics = Regex("\\p{InCombiningDiacriticalMarks}+").replace(decomposed, "")
    return withoutDiacritics.lowercase(Locale.ROOT)
        .replace(Regex("[^a-z0-9\\s]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
}

fun cleanAlbumTitle(title: String): String {
    return title
        .replace(Regex("\\s*\\([^)]*\\)"), "")
        .replace(Regex("\\s*\\[[^]]*\\]"), "")
        .replace(Regex("\\s*-\\s*(deluxe|remastered|expanded|anniversary|edition|bonus|special).*", RegexOption.IGNORE_CASE), "")
        .trim()
}

// --- Providers ---

object AppleMusicCanvasProvider {

    private const val APPLE_MUSIC_TOKEN =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiIsImtpZCI6IldlYlBsYXlLaWQifQ" +
                ".eyJpc3MiOiJBTVBXZWJQbGF5IiwiaWF0IjoxNzgxMDMyODU1LCJleHAiOjE3ODQw" +
                "NTY4NTUsInJvb3RfaHR0cHNfb3JpZ2luIjpbImFwcGxlLmNvbSJdfQ" +
                ".fiMFcJWkfSlxKP9NVA0UW9CbItD1Rge0SISuepz203XcpU762OqdCpU9M-YkmtKkjRmaIWtjsfGgqZPrlMonpA"

    private var cachedToken: String? = null
    private var tokenExpiryMs: Long = 0L

    private suspend fun getOrFetchToken(): String {
        val now = System.currentTimeMillis()
        if (cachedToken != null && now < tokenExpiryMs - 60_000) {
            return cachedToken!!
        }

        return try {
            val html = client.get("https://music.apple.com/us/browse") {
                header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            }.body<String>()

            val scriptRegex = Regex("""/assets/index(?:-legacy)?[~-][a-zA-Z0-9_-]+\.js""")
            val scripts = scriptRegex.findAll(html).map { it.value }.distinct().toList()

            var fetchedToken: String? = null
            for (scriptPath in scripts) {
                val scriptUrl = "https://music.apple.com$scriptPath"
                val scriptText = client.get(scriptUrl) {
                    header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                }.body<String>()

                val tokenRegex = Regex("""ey[a-zA-Z0-9_-]+\.ey[a-zA-Z0-9_-]+\.[a-zA-Z0-9_-]+""")
                val tokens = tokenRegex.findAll(scriptText).map { it.value }
                for (token in tokens) {
                    try {
                        val body = token.split(".")[1]
                        val decodedBytes = java.util.Base64.getUrlDecoder().decode(body)
                        val decoded = String(decodedBytes, Charsets.UTF_8)
                        if (decoded.contains("iss") && decoded.contains("exp")) {
                            val expIndex = decoded.indexOf("\"exp\":")
                            if (expIndex != -1) {
                                val expValStr = decoded.substring(expIndex + 6).takeWhile { it.isDigit() }
                                val expSeconds = expValStr.toLongOrNull() ?: 0L
                                if (expSeconds * 1000 > now) {
                                    fetchedToken = token
                                    tokenExpiryMs = expSeconds * 1000
                                    break
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // ignore decoding issues
                    }
                }
                if (fetchedToken != null) break
            }

            if (fetchedToken != null) {
                cachedToken = fetchedToken
                fetchedToken
            } else {
                APPLE_MUSIC_TOKEN
            }
        } catch (e: Exception) {
            APPLE_MUSIC_TOKEN
        }
    }

    private const val AMP_BASE_URL = "https://amp-api.music.apple.com"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    private val client by lazy {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(json)
                register(ContentType.Text.JavaScript, KotlinxSerializationConverter(json))
            }
            install(HttpTimeout) {
                connectTimeoutMillis = 15_000
                requestTimeoutMillis = 25_000
                socketTimeoutMillis = 25_000
            }
            install(HttpCache)
            expectSuccess = false
        }
    }

    private data class CacheEntry(
        val value: CanvasArtwork?,
        val expiresAtMs: Long,
    )

    private val cache = ConcurrentHashMap<String, CacheEntry>()
    private const val CACHE_TTL_MS = 1000L * 60 * 60 * 24

    suspend fun getByAlbumArtist(
        album: String,
        artist: String,
        storefront: String = "us",
    ): CanvasArtwork? {
        val key = cacheKey("sa", album, artist, storefront)
        cache[key]?.takeIf { it.expiresAtMs > System.currentTimeMillis() }?.let { return it.value }

        val result = searchAndFetchMotion(album, artist, storefront)
        if (result != null) {
            cache[key] = CacheEntry(result, System.currentTimeMillis() + CACHE_TTL_MS)
        }
        return result
    }

    private suspend fun searchAndFetchMotion(
        album: String,
        artist: String,
        storefront: String,
    ): CanvasArtwork? {
        return runCatching {
            val query = "$artist $album".trim()
            val url = "$AMP_BASE_URL/v1/catalog/$storefront/search"
            val response = client.get(url) {
                header("Authorization", "Bearer ${getOrFetchToken()}")
                header("Origin", "https://music.apple.com")
                header("Referer", "https://music.apple.com/")
                header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                parameter("term", query)
                parameter("types", "albums")
                parameter("limit", "10")
                parameter("extend", "editorialVideo")
            }
            if (response.status != HttpStatusCode.OK) return@runCatching null

            val root = response.body<JsonObject>()
            val results = root["results"]?.jsonObject?.get("albums")?.jsonObject?.get("data")?.jsonArray ?: return@runCatching null

            val normAlbum = cleanAlbumTitle(album).normalizeForComparison()
            val normArtist = artist.normalizeForComparison()

            for (item in results) {
                val obj = item.jsonObject
                val attributes = obj["attributes"]?.jsonObject ?: continue
                val resultArtistName = attributes["artistName"]?.jsonPrimitive?.contentOrNull ?: ""
                val resultAlbumName = attributes["name"]?.jsonPrimitive?.contentOrNull ?: ""

                val normResAlbum = cleanAlbumTitle(resultAlbumName).normalizeForComparison()
                val normResArtist = resultArtistName.normalizeForComparison()

                val artistMatches = normResArtist.contains(normArtist) || normArtist.contains(normResArtist)
                val albumMatches = normResAlbum.contains(normAlbum) || normAlbum.contains(normResAlbum)

                if (!artistMatches || !albumMatches) continue

                val targetAlbumId = obj["id"]?.jsonPrimitive?.contentOrNull ?: continue
                if (targetAlbumId.startsWith("pl.")) continue

                val ev = attributes["editorialVideo"]?.jsonObject
                if (ev != null) {
                    val hlsUrl = extractEditorialVideoUrl(ev, preferTall = false)
                    val tallHlsUrl = extractEditorialVideoUrl(ev, preferTall = true)
                    if (!hlsUrl.isNullOrBlank()) {
                        return@runCatching CanvasArtwork(
                            name = resultAlbumName,
                            artist = resultArtistName,
                            albumId = targetAlbumId,
                            albumName = resultAlbumName,
                            animated = hlsUrl,
                            animatedTall = tallHlsUrl
                        )
                    }
                }

                val fetched = fetchMotionArtwork(targetAlbumId, storefront, resultArtistName)
                if (fetched != null) return@runCatching fetched
            }
            null
        }.onFailure {
            if (it is CancellationException) throw it
        }.getOrNull()
    }

    private suspend fun fetchMotionArtwork(
        albumId: String,
        storefront: String,
        fallbackArtist: String?,
    ): CanvasArtwork? {
        if (albumId.startsWith("pl.")) return null
        return runCatching {
            val url = "$AMP_BASE_URL/v1/catalog/$storefront/albums/$albumId"
            val response = client.get(url) {
                header("Authorization", "Bearer ${getOrFetchToken()}")
                header("Origin", "https://music.apple.com")
                header("Referer", "https://music.apple.com/")
                header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                parameter("extend", "editorialVideo")
            }
            if (response.status != HttpStatusCode.OK) return@runCatching null

            val root = response.body<JsonObject>()
            val data = root["data"]?.jsonArray
            if (data.isNullOrEmpty()) return@runCatching null

            val albumObj = data.firstOrNull()?.jsonObject ?: return@runCatching null
            val attributes = albumObj["attributes"]?.jsonObject
            val albumName = attributes?.get("name")?.jsonPrimitive?.contentOrNull ?: ""
            val artistName = attributes?.get("artistName")?.jsonPrimitive?.contentOrNull ?: fallbackArtist

            val ev = attributes?.get("editorialVideo")?.jsonObject
            if (ev != null) {
                val urlAnim = extractEditorialVideoUrl(ev, preferTall = false)
                val tallUrl = extractEditorialVideoUrl(ev, preferTall = true)
                if (!urlAnim.isNullOrBlank()) {
                    return@runCatching CanvasArtwork(
                        name = albumName,
                        artist = artistName,
                        albumId = albumId,
                        albumName = albumName,
                        animated = urlAnim,
                        animatedTall = tallUrl
                    )
                }
            }
            null
        }.onFailure {
            if (it is CancellationException) throw it
        }.getOrNull()
    }

    private fun extractEditorialVideoUrl(ev: JsonObject, preferTall: Boolean = false): String? {
        val order = if (preferTall) {
            listOf(
                ev["motionDetailTall"]?.jsonObject,
                ev["motionDetailRaw"]?.jsonObject,
                ev["motionDetailSquare"]?.jsonObject,
                ev["motionDetailStatic"]?.jsonObject
            )
        } else {
            listOf(
                ev["motionDetailSquare"]?.jsonObject,
                ev["motionDetailRaw"]?.jsonObject,
                ev["motionDetailTall"]?.jsonObject,
                ev["motionDetailStatic"]?.jsonObject
            )
        }
        val assets = order.filterNotNull()

        for (asset in assets) {
            val video = asset["video"]?.jsonPrimitive?.contentOrNull
                ?: asset["videoUrl"]?.jsonPrimitive?.contentOrNull
                ?: asset["hlsUrl"]?.jsonPrimitive?.contentOrNull
                ?: asset["url"]?.jsonPrimitive?.contentOrNull

            if (!video.isNullOrBlank()) return video
        }
        return null
    }

    private fun cacheKey(prefix: String, vararg parts: String): String {
        return "$prefix|" + parts.joinToString("|") { it.trim().lowercase(Locale.ROOT) }
    }
}

object TidalCanvasProvider {
    private const val BASE_URL = "https://api.tidal.com/v1/"
    private const val TIDAL_TOKEN = "vNVdglQOjFJJGG2U"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    private val client by lazy {
        HttpClient(OkHttp) {
            install(ContentNegotiation) { json(json) }
            install(HttpTimeout) {
                connectTimeoutMillis = 15_000
                requestTimeoutMillis = 30_000
                socketTimeoutMillis = 30_000
            }
            install(HttpCache)
            expectSuccess = false
        }
    }

    private val cache = ConcurrentHashMap<String, CacheEntry>()

    private data class CacheEntry(
        val value: CanvasArtwork?,
        val expiresAtMs: Long
    )

    private const val CACHE_TTL_MS = 1000L * 60 * 60 * 24

    private val countryCode by lazy {
        val country = Locale.getDefault().country
        if (country.length == 2) country.uppercase(Locale.ROOT) else "US"
    }

    suspend fun getByAlbumArtist(
        album: String,
        artist: String
    ): CanvasArtwork? {
        val key = cacheKey("search_album", album, artist)
        cache[key]?.takeIf { it.expiresAtMs > System.currentTimeMillis() }?.let {
            return it.value
        }

        val result = searchOnTidal(
            query = "$album $artist",
            types = "ALBUMS",
            artistValidation = artist,
            albumValidation = album
        )
        if (result != null) {
            cache[key] = CacheEntry(result, System.currentTimeMillis() + CACHE_TTL_MS)
        }
        return result
    }

    private suspend fun searchOnTidal(
        query: String,
        types: String,
        artistValidation: String? = null,
        albumValidation: String? = null
    ): CanvasArtwork? {
        try {
            val response = client.get("${BASE_URL}search") {
                header("X-Tidal-Token", TIDAL_TOKEN)
                parameter("query", query)
                parameter("limit", "10")
                parameter("types", types)
                parameter("countryCode", countryCode)
            }
            if (response.status != HttpStatusCode.OK) return null

            val root = response.body<JsonObject>()
            val key = types.lowercase(Locale.ROOT)
            val section = findSearchSection(root, key) ?: return null
            val items = section.jsonObject["items"]?.jsonArray ?: return null

            val normAlbum = cleanAlbumTitle(albumValidation.orEmpty()).normalizeForComparison()
            val normArtist = artistValidation.orEmpty().normalizeForComparison()

            for (item in items) {
                val obj = item.jsonObject
                val resultTitle = obj["title"]?.jsonPrimitive?.contentOrNull ?: ""
                val artistsArray = obj["artists"]?.jsonArray
                val allArtistNames = artistsArray?.mapNotNull {
                    it.jsonObject["name"]?.jsonPrimitive?.contentOrNull
                } ?: emptyList()

                val combinedArtistStr = if (allArtistNames.isNotEmpty()) {
                    allArtistNames.joinToString(", ")
                } else {
                    obj["artist"]?.jsonObject?.get("name")?.jsonPrimitive?.contentOrNull ?: ""
                }

                val normResAlbum = cleanAlbumTitle(resultTitle).normalizeForComparison()
                val normResArtist = combinedArtistStr.normalizeForComparison()

                val albumMatch = normAlbum.isNotBlank() && (normResAlbum.contains(normAlbum) || normAlbum.contains(normResAlbum))
                val artistMatch = normArtist.isNotBlank() && (normResArtist.contains(normArtist) || normArtist.contains(normResArtist))

                if (!albumMatch || !artistMatch) continue

                val videoCover = obj["videoCover"]?.jsonPrimitive?.contentOrNull
                if (!videoCover.isNullOrBlank()) {
                    val videoUrl = formatVideoUrl(videoCover)
                    if (videoUrl != null) {
                        return CanvasArtwork(
                            name = resultTitle,
                            artist = combinedArtistStr,
                            videoUrl = videoUrl,
                            albumName = resultTitle
                        )
                    }
                }
            }
        } catch (e: Exception) {}
        return null
    }

    private fun findSearchSection(source: JsonElement, key: String): JsonElement? {
        if (source is JsonObject) {
            if (source.containsKey("items") && source["items"] is JsonArray) return source
            if (source.containsKey(key)) {
                val found = findSearchSection(source[key]!!, key)
                if (found != null) return found
            }
            for (value in source.values) {
                val found = findSearchSection(value, key)
                if (found != null) return found
            }
        } else if (source is JsonArray) {
            for (element in source) {
                val found = findSearchSection(element, key)
                if (found != null) return found
            }
        }
        return null
    }

    private fun formatVideoUrl(id: String): String? {
        val parts = id.split("-")
        if (parts.size != 5) return null
        return "https://resources.tidal.com/videos/${parts[0]}/${parts[1]}/${parts[2]}/${parts[3]}/${parts[4]}/1280x1280.mp4"
    }

    private fun cacheKey(prefix: String, vararg parts: String): String {
        return "$prefix|" + parts.joinToString("|") { it.trim().lowercase(Locale.ROOT) }
    }
}

object ViviMusicCanvasProvider {
    private const val BASE_URL = "https://vivimusicanvas.mkmdevilmi.workers.dev/canvas.json"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    private val client by lazy {
        HttpClient(OkHttp) {
            install(ContentNegotiation) { json(json) }
            install(HttpTimeout) {
                connectTimeoutMillis = 12_000
                requestTimeoutMillis = 18_000
                socketTimeoutMillis = 18_000
            }
            install(HttpCache)
            expectSuccess = false
        }
    }

    private data class CacheEntry(
        val value: ViviMusicCanvasManifest?,
        val expiresAtMs: Long,
    )

    private var manifestCache: CacheEntry? = null
    private val ttlMs = 60_000L

    private suspend fun fetchManifest(): ViviMusicCanvasManifest? {
        val currentCache = manifestCache
        if (currentCache != null && currentCache.expiresAtMs > System.currentTimeMillis()) {
            return currentCache.value
        }

        return try {
            val manifest: ViviMusicCanvasManifest = client.get(BASE_URL).body()
            manifestCache = CacheEntry(
                value = manifest,
                expiresAtMs = System.currentTimeMillis() + ttlMs
            )
            manifest
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getBySongArtist(
        song: String,
        artist: String,
        album: String,
    ): CanvasArtwork? {
        if (song.isBlank() || artist.isBlank()) return null
        val manifest = fetchManifest() ?: return null

        val target = manifest.items.firstOrNull { item ->
            val matchSong = song.normalizeForComparison().contains(item.song.normalizeForComparison()) ||
                    item.song.normalizeForComparison().contains(song.normalizeForComparison())
            val matchArtist = artist.normalizeForComparison().contains(item.artist.normalizeForComparison()) ||
                    item.artist.normalizeForComparison().contains(artist.normalizeForComparison())
            val matchAlbum = album.normalizeForComparison() == item.album.normalizeForComparison()
            matchSong && matchArtist && matchAlbum
        }

        return if (target != null) {
            CanvasArtwork(
                name = target.song,
                artist = target.artist,
                albumName = target.album.takeIf { it.isNotBlank() },
                videoUrl = target.url,
                animated = target.url
            )
        } else {
            null
        }
    }
}
