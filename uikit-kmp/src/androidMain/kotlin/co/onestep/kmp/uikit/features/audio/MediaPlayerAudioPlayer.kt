package co.onestep.kmp.uikit.features.audio

import android.content.Context
import android.media.MediaPlayer

internal class MediaPlayerAudioPlayer(
    private val context: Context,
) : AudioPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private var enable = true

    override fun enable(enable: Boolean) {
        this.enable = enable
    }

    override fun playAudio(resourceKey: String) {
        if (!enable) return
        stopCurrentAudio()

        val resId = context.resources.getIdentifier(resourceKey, "raw", context.packageName)
        if (resId == 0) return

        mediaPlayer =
            MediaPlayer.create(context, resId).apply {
                setOnCompletionListener { mp ->
                    if (mp.isPlaying) {
                        mp.stop()
                    }
                    mp.release()
                    mediaPlayer = null
                }
                start()
            }
    }

    override fun stopCurrentAudio() {
        if (!enable) return
        try {
            mediaPlayer?.run {
                if (isPlaying) {
                    stop()
                }
                release()
            }
            mediaPlayer = null
        } catch (_: IllegalStateException) {
            // MediaPlayer in illegal state — already released
        }
    }

    override fun isPlaying(): Boolean {
        if (!enable) return false
        return try {
            mediaPlayer?.isPlaying ?: false
        } catch (_: IllegalStateException) {
            false
        }
    }
}
