package com.cgens67.gluetune.musicrecognition.audd

import com.cgens67.gluetune.musicrecognition.shazam.models.RecognitionResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

@Serializable
data class AuddResponse(
    val status: String,
    val result: AuddResult? = null,
    val error: AuddError? = null
)

@Serializable
data class AuddResult(
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val release_date: String? = null,
    val label: String? = null,
    val song_link: String? = null,
    val apple_music: AppleMusicData? = null,
    val spotify: SpotifyData? = null
)

@Serializable
data class AppleMusicData(val artwork: Artwork? = null)

@Serializable
data class Artwork(val url: String? = null)

@Serializable
data class SpotifyData(val external_urls: Map<String, String>? = null)

@Serializable
data class AuddError(val error_code: Int, val error_message: String)

object AuddRecognizer {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
    }

    suspend fun recognize(samples: ShortArray, sampleRate: Int, apiToken: String?): Result<RecognitionResult> {
        return runCatching {
            val wavBytes = pcmToWav(samples, sampleRate)
            val response = client.submitFormWithBinaryData(
                url = "https://api.audd.io/",
                formData = formData {
                    if (!apiToken.isNullOrBlank()) {
                        append("api_token", apiToken)
                    }
                    append("return", "apple_music,spotify")
                    append("file", wavBytes, Headers.build {
                        append(HttpHeaders.ContentType, "audio/wav")
                        append(HttpHeaders.ContentDisposition, "filename=\"sample.wav\"")
                    })
                }
            )

            val auddRes = response.body<AuddResponse>()
            if (auddRes.status == "success" && auddRes.result != null) {
                val res = auddRes.result
                val cover = res.apple_music?.artwork?.url?.replace("{w}", "600")?.replace("{h}", "600")
                RecognitionResult(
                    trackId = res.song_link ?: "",
                    title = res.title ?: "Unknown",
                    artist = res.artist ?: "Unknown",
                    album = res.album,
                    coverArtUrl = cover,
                    coverArtHqUrl = cover,
                    genre = null,
                    releaseDate = res.release_date,
                    label = res.label,
                    lyrics = null,
                    shazamUrl = res.song_link,
                    appleMusicUrl = res.song_link,
                    spotifyUrl = res.spotify?.external_urls?.get("spotify"),
                    isrc = null,
                    youtubeVideoId = null
                )
            } else {
                throw Exception(auddRes.error?.error_message ?: "no match")
            }
        }
    }

    private fun pcmToWav(pcmData: ShortArray, sampleRate: Int): ByteArray {
        val numChannels = 1
        val bitsPerSample = 16
        val byteRate = sampleRate * numChannels * bitsPerSample / 8
        val blockAlign = numChannels * bitsPerSample / 8
        val audioDataLen = pcmData.size * 2
        val totalDataLen = audioDataLen + 36

        val header = ByteArray(44)
        header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte(); header[5] = ((totalDataLen shr 8) and 0xff).toByte(); header[6] = ((totalDataLen shr 16) and 0xff).toByte(); header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0
        header[20] = 1; header[21] = 0
        header[22] = numChannels.toByte(); header[23] = 0
        header[24] = (sampleRate and 0xff).toByte(); header[25] = ((sampleRate shr 8) and 0xff).toByte(); header[26] = ((sampleRate shr 16) and 0xff).toByte(); header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte(); header[29] = ((byteRate shr 8) and 0xff).toByte(); header[30] = ((byteRate shr 16) and 0xff).toByte(); header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = blockAlign.toByte(); header[33] = 0
        header[34] = bitsPerSample.toByte(); header[35] = 0
        header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
        header[40] = (audioDataLen and 0xff).toByte(); header[41] = ((audioDataLen shr 8) and 0xff).toByte(); header[42] = ((audioDataLen shr 16) and 0xff).toByte(); header[43] = ((audioDataLen shr 24) and 0xff).toByte()

        val byteData = ByteArray(audioDataLen)
        ByteBuffer.wrap(byteData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(pcmData)

        val out = ByteArrayOutputStream()
        out.write(header)
        out.write(byteData)
        return out.toByteArray()
    }
}
