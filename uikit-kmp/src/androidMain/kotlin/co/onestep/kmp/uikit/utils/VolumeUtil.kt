package co.onestep.kmp.uikit.utils

import android.content.Context
import android.media.AudioManager

internal object VolumeUtil {
    /**
     * Determines if sound instructions should be shown to the user.
     *
     * @param context The application context.
     * @return True if sound instructions should be shown, false otherwise.
     */
    internal fun shouldShowSoundInstructions(context: Context): Boolean = isVolumeLowOrSilent(context)

    /**
     * Checks if the volume is lower than 30% of the maximum volume or if the device is in silent mode.
     */
    private fun isVolumeLowOrSilent(context: Context): Boolean {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val volumePercentage = currentVolume / maxVolume.toFloat()

        val isVolumeLow = volumePercentage < 0.3
        val isSilent = audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL

        return isVolumeLow || isSilent
    }
}
