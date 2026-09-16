package co.onestep.kmp.uikit.features.audio

/**
 * Resolves a voice-over key (e.g. `countdown_from_10_iw`) to its path inside the compose
 * resources bundle.
 *
 * The voice files live in `commonMain/composeResources/files/`, which is the only place both
 * platforms can read them from. The Android port originally looked them up in `res/raw` and the
 * iOS port in `NSBundle.mainBundle` — neither of which the files are packaged into — so every
 * voice-over was silently a no-op (OS-17410). Going through compose resources is what keeps the
 * two platforms reading the same bytes from the same place.
 */
internal object AudioAssets {
    /** Assets that are not `.mp3`. Everything else is, so new mp3s need no entry here. */
    private val EXTENSION_OVERRIDES = mapOf(
        "data_is_ready_for_analysis" to "m4a",
        "tap_the_start_button_vo" to "m4a",
    )

    fun pathFor(key: String): String = "files/$key.${EXTENSION_OVERRIDES[key] ?: "mp3"}"
}
