package co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.recording

/**
 * The current recording's perception uuid, step count and length, kept for analytics once the
 * recorder has moved on. Port of Android uikit's `RecordingSnapshot` (51f4553).
 *
 * On a UI timeout the measurement never arrives and the recorder is reset — `analyse()` already
 * resets it as analysis begins — so none of these can be read back from the recorder at the
 * still-analyzing / error screens.
 *
 * Analytics only: the uuid is never used as an API id from here. Not thread-safe; every caller
 * runs on the ViewModel's main-thread scope.
 */
internal class RecordingSnapshot {
    /** Known from the moment the recorder starts; null before the first recording. */
    var perceptionUuid: String? = null
        private set

    /**
     * Steps counted so far in this recording, unfiltered (every activity, unlike the UI's
     * walk-only step count); null before the first recording.
     */
    var steps: Int? = null
        private set

    /** The recorded length in whole seconds, known once the recording stops; null until then. */
    var measurementSeconds: Int? = null
        private set

    fun onRecordingStarted(perceptionUuid: String?) {
        this.perceptionUuid = perceptionUuid
        steps = 0
        measurementSeconds = null
    }

    /**
     * The recorder's counter is cumulative within a recording and drops to 0 when the recorder is
     * reset, so the highest value seen is the recording's step count. Ignored before a recording
     * has started, so a previous recording's count cannot leak into this one.
     */
    fun onStepCount(count: Int) {
        val current = steps ?: return
        if (count > current) steps = count
    }

    fun onRecordingStopped(measurementSeconds: Int) {
        this.measurementSeconds = measurementSeconds
    }

    /** Forgets everything, at the start of each attempt. */
    fun clear() {
        perceptionUuid = null
        steps = null
        measurementSeconds = null
    }
}
