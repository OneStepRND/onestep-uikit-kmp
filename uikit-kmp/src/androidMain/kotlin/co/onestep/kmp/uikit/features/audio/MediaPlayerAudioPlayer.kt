package co.onestep.kmp.uikit.features.audio

import android.media.AudioAttributes
import android.media.MediaDataSource
import android.media.MediaPlayer

/**
 * Plays voice-over bytes read from compose resources.
 *
 * The bytes are handed to [MediaPlayer] through a [MediaDataSource] rather than written to a temp
 * file: the clips are small, and the previous `res/raw` lookup they replace was never going to
 * find them (the files ship as compose resources — see `AudioAssets`).
 */
internal class MediaPlayerAudioPlayer {
    private var mediaPlayer: MediaPlayer? = null

    fun play(bytes: ByteArray) {
        if (bytes.isEmpty()) return
        stop()

        try {
            mediaPlayer =
                MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build(),
                    )
                    setDataSource(ByteArrayMediaDataSource(bytes))
                    setOnCompletionListener { mp ->
                        mp.release()
                        mediaPlayer = null
                    }
                    // Synchronous prepare is safe here and elsewhere would not be: the source is
                    // already in memory, so there is no I/O to block the caller on. It also keeps
                    // isPlaying() truthful the moment this returns.
                    prepare()
                    start()
                }
        } catch (e: Exception) {
            // Malformed/unsupported clip — audio is non-critical, but stay loud about why.
            println("MediaPlayerAudioPlayer: cannot play audio: ${e.message}")
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    fun stop() {
        try {
            mediaPlayer?.run {
                if (isPlaying) {
                    stop()
                }
                release()
            }
        } catch (_: IllegalStateException) {
            // MediaPlayer in illegal state — already released
        }
        mediaPlayer = null
    }

    fun isPlaying(): Boolean =
        try {
            mediaPlayer?.isPlaying ?: false
        } catch (_: IllegalStateException) {
            false
        }
}

private class ByteArrayMediaDataSource(
    private val data: ByteArray,
) : MediaDataSource() {
    override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
        if (position >= data.size) return -1
        val start = position.toInt()
        val length = minOf(size, data.size - start)
        data.copyInto(buffer, offset, start, start + length)
        return length
    }

    override fun getSize(): Long = data.size.toLong()

    override fun close() = Unit
}
