package co.onestep.kmp.uikit.features.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

private const val TAG_FIRST = "UTT_FIRST"
private const val TAG_LAST = "UTT_SEG_LAST"
private const val TAG_SEGMENT = "UTT_SEG_"

internal class TTSPlayerImpl(
    context: Context,
) : TTSPlayer {
    private var tts: TextToSpeech? = null
    private var isEnabled = true
    private var onDoneCallback: (() -> Unit)? = null

    init {
        tts =
            TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale.US
                    tts?.setOnUtteranceProgressListener(
                        object : UtteranceProgressListener() {
                            override fun onStart(utteranceId: String) {}

                            override fun onError(utteranceId: String?) {}

                            override fun onError(
                                utteranceId: String?,
                                errorCode: Int,
                            ) {}

                            override fun onDone(utteranceId: String) {
                                if (utteranceId == TAG_LAST) {
                                    onDoneCallback?.invoke()
                                }
                            }
                        },
                    )
                }
            }
    }

    override fun setOnDoneListener(callback: (() -> Unit)?) {
        onDoneCallback = callback
    }

    override fun enable(enable: Boolean) {
        isEnabled = enable
    }

    override fun speak(text: String) {
        if (!isEnabled || tts == null) return

        val segments = text.split(Regex("\\n\\s*\\n"))
        segments.forEachIndexed { index, segment ->
            val trimmed = segment.trim()
            if (trimmed.isEmpty()) return@forEachIndexed

            if (index == 0) {
                val tag =
                    if (segments.size > 1) {
                        TAG_FIRST
                    } else {
                        TAG_LAST
                    }
                tts?.speak(trimmed, TextToSpeech.QUEUE_FLUSH, null, tag)
            } else {
                tts?.playSilentUtterance(1_000, TextToSpeech.QUEUE_ADD, null)

                val tag =
                    if (segments.last() == segments[index]) {
                        TAG_LAST
                    } else {
                        "$TAG_SEGMENT$index"
                    }
                tts?.speak(trimmed, TextToSpeech.QUEUE_ADD, null, tag)
            }
        }
    }

    override fun stopCurrentSpeech() {
        tts?.stop()
    }

    override fun isSpeaking(): Boolean = tts?.isSpeaking == true

    fun shutdown() {
        tts?.shutdown()
    }
}
