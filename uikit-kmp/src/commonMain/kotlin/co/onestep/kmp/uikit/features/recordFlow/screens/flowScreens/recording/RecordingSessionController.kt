package co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.recording

import co.onestep.kmp.sdk.currentTimeMillis
import co.onestep.kmp.uikit.bridge.RecorderBridge
import co.onestep.kmp.uikit.models.OSTActivityType
import co.onestep.kmp.uikit.models.OSTRecorderState
import co.onestep.kmp.uikit.models.OSTUserInputMetaData
import co.onestep.kmp.uikit.utils.monotonicNowMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Owns one recording session for [MotionRecorderViewModel]: recorder lifecycle, the recording
 * clock, and the stop.
 *
 * ## Who owns recording time
 *
 * The **SDK** does. It is started with the exact deadline the measurement should end at, and it
 * enforces that deadline independently of the UI — capture keeps running to it after the recording
 * screen is gone. The UI never counts seconds of its own: every tick recomputes from absolute
 * anchors, so the display cannot accumulate drift and is correct immediately after a dropped tick,
 * a slow frame, backgrounding, or screen-off. See
 * [co.onestep.kmp.uikit.models.OSTRecordingWindow].
 *
 * The one thing the SDK cannot know is **when the activity actually begins**. TUG and STS start
 * capture early, on the Get Ready screen ([startEarly]), so the deadline handed to the recorder
 * has to *predict* how long the countdown will take ([StartRequest.prepareOffsetMillis]). That
 * prediction is exact when the countdown runs to completion and too long when the user taps
 * "Start now". So "go" is anchored here, at the UI transition ([onGo], which also writes the
 * [GO_MARKER]), and the clock takes whichever end comes first — the SDK's published deadline or
 * `go + activity duration`. On the normal path those coincide to within a frame.
 *
 * @param monotonicNow Monotonic clock used for all recording-duration math. Never wall clock:
 *   an NTP sync mid-recording would otherwise change the measured length.
 * @param now Wall clock, used only for [goTimeMs] (an analytics timestamp).
 */
internal class RecordingSessionController(
    private val recorderBridge: RecorderBridge,
    private val scope: CoroutineScope,
    private val now: () -> Long = { currentTimeMillis() },
    private val monotonicNow: () -> Long = { monotonicNowMillis() },
) {
    /** Everything the recorder needs at start; built by the ViewModel just-in-time. */
    data class StartRequest(
        val activityType: OSTActivityType,
        /** Intended measurement length after "go"; null = unrestricted (SDK internal cap only). */
        val activityDurationMillis: Long?,
        /**
         * Wall time the Get Ready countdown occupies before "go". Added to the recorder's
         * deadline when capture starts early ([startEarly]) so the deadline lands at
         * `go + activityDuration` rather than a whole countdown earlier. 0 for any prepare mode
         * whose length is not knowable up front (e.g. a TTS prepare, which ends on a
         * speech-synthesis callback).
         */
        val prepareOffsetMillis: Long = 0,
        val sensorEnhancedMode: Boolean = false,
        val userInputMetadata: OSTUserInputMetaData? = null,
        val customMetadata: Map<String, Any> = emptyMap(),
    )

    private val _elapsedMillis = MutableStateFlow(0L)

    /**
     * Milliseconds of the *measurement* elapsed — from "go", not from sensor start. The ViewModel
     * derives the on-screen timer from this in both directions. For a timed activity it stops at
     * the activity duration, so a count-down derived as `duration - elapsed` never goes negative.
     */
    val elapsedMillis: StateFlow<Long> = _elapsedMillis

    /** True from "go" until the recording is stopped (by deadline, user, or teardown). */
    var isRecording: Boolean = false
        private set

    /** Wall-clock ms at "go" — the activity start, used for analytics elapsed time. */
    var goTimeMs: Long? = null
        private set

    /**
     * True once a recorder start was initiated and not yet stopped/finished. This — not
     * [isRecording] — is the "does the recorder need a graceful stop" guard, because TUG/STS
     * start the recorder during Get Ready, before [isRecording] flips on at "go".
     */
    val recorderStartInitiated: Boolean get() = startJob != null

    private var startJob: Job? = null
    private var clockJob: Job? = null
    private var startedEarly = false
    private var activityDurationMillis: Long? = null

    /** TUG/STS: start the recorder while the Get Ready countdown is still on screen. */
    fun startEarly(request: StartRequest) {
        if (startJob != null) return
        startedEarly = true
        startRecorder(request, includePrepareOffset = true)
    }

    /**
     * The activity begins ("go" — the recording screen appears and the activity timer starts):
     * ensures the recorder is running (starts it now unless [startEarly] already did), writes the
     * [GO_MARKER] for early starts, and runs the recording clock from this instant.
     *
     * [requestProvider] is only invoked if the recorder still needs to be started.
     */
    suspend fun onGo(requestProvider: () -> StartRequest) {
        val earlyStart = startJob
        if (earlyStart != null) {
            earlyStart.join()
            if (startedEarly) recorderBridge.addMarker(GO_MARKER)
        } else {
            startRecorder(requestProvider(), includePrepareOffset = false)
            startJob?.join()
        }
        goTimeMs = now()
        isRecording = true
        // Reset synchronously so the ViewModel's timer mirror never sees a stale value from a
        // previous recording (e.g. retry after an analysis error).
        _elapsedMillis.value = 0
        val goMonotonicMs = monotonicNow()
        clockJob = scope.launch { runClock(goMonotonicMs) }
    }

    /**
     * Gracefully stops the recording and suspends until the recorder reports DONE (which is what
     * moves the ViewModel's screen-state collector into ANALYZING). Safe to call in any phase — it
     * no-ops unless a recorder start was initiated, and never calls `stop()` before the in-flight
     * `start()` completed.
     */
    suspend fun stopAndAwaitDone(reset: Boolean = false) {
        val pendingStart = startJob ?: return
        startJob = null
        isRecording = false
        clockJob?.cancelAndJoin()
        clockJob = null
        pendingStart.join()
        recorderBridge.stop()
        recorderBridge.recorderState.first { it == OSTRecorderState.DONE }
        if (reset) {
            recorderBridge.reset()
        }
    }

    /**
     * The recorder reported the recording over on its own (it reached the deadline it was started
     * with) — drop the session bookkeeping so later teardown ([stopAndAwaitDone]) knows there is
     * nothing left to stop.
     */
    fun onRecordingFinished() {
        isRecording = false
        startJob = null
        clockJob?.cancel()
        clockJob = null
    }

    /** Full per-recording reset (Static Balance "Record another test"). */
    fun resetForNextRecording() {
        onRecordingFinished()
        startedEarly = false
        goTimeMs = null
        _elapsedMillis.value = 0
    }

    private fun startRecorder(request: StartRequest, includePrepareOffset: Boolean) {
        activityDurationMillis = request.activityDurationMillis
        val duration = recorderDurationMillis(request, includePrepareOffset)
        startJob = scope.launch {
            recorderBridge.start(
                activityType = request.activityType,
                duration = duration,
                sensorEnhancedMode = request.sensorEnhancedMode,
                userInputMetadata = request.userInputMetadata,
                customMetadata = request.customMetadata,
            )
        }
    }

    /**
     * The deadline handed to the recorder: exactly how long capture should run, so the SDK's own
     * auto-stop is the measurement's end even if the app is suspended and this controller never
     * gets to run.
     *
     * Clamped to the recorder's own limit because `start` rejects anything above it, and that
     * rejection would surface as an uncaught crash out of a `launch` rather than an error state —
     * an unrestricted duration already sits at the ceiling, so any offset would trip it.
     * `null` (unrestricted) is passed through untouched; the SDK applies its internal cap.
     */
    private fun recorderDurationMillis(request: StartRequest, includePrepareOffset: Boolean): Long? {
        val activity = request.activityDurationMillis ?: return null
        val offset = if (includePrepareOffset) request.prepareOffsetMillis else 0L
        return (activity + offset).coerceAtMost(recorderBridge.currentRecordingLimit())
    }

    /**
     * Recomputes the measurement clock from absolute anchors on every tick — it never increments,
     * so a late resume or a dropped tick self-corrects on the next one.
     *
     * The recording ends at whichever comes first: the SDK's published deadline, or
     * `go + activityDuration`. The second term only wins when "go" arrived earlier than the
     * predicted prepare offset (the user tapped "Start now" during an early-start countdown), a
     * thing the SDK cannot know about; on every other path the SDK's deadline lands first and
     * stops the recording itself. It is also the whole clock on a platform whose SDK does not
     * publish a window yet (see [RecorderBridge.currentRecordingWindow]).
     */
    private suspend fun runClock(goMonotonicMs: Long) {
        val duration = activityDurationMillis
        while (isRecording) {
            val now = monotonicNow()
            val sinceGo = now - goMonotonicMs
            if (duration == null) {
                _elapsedMillis.value = sinceGo.coerceAtLeast(0)
            } else {
                val window = recorderBridge.currentRecordingWindow.value
                val untilSdkDeadline = window?.remainingMillisAt(now) ?: Long.MAX_VALUE
                val remaining = minOf(duration - sinceGo, untilSdkDeadline)
                _elapsedMillis.value = (duration - remaining).coerceIn(0, duration)
                if (remaining <= 0) {
                    scope.launch { stopAndAwaitDone() }
                    return
                }
            }
            delay(CLOCK_TICK_MILLIS)
        }
    }

    companion object {
        /** Marker written to the recording at the instant the activity actually begins. */
        const val GO_MARKER = "go"

        /**
         * How often the clock re-reads its anchors. Sub-second so the final second of a count-down
         * is responsive; it has no bearing on accuracy, since every tick is recomputed rather than
         * incremented.
         */
        const val CLOCK_TICK_MILLIS = 250L
    }
}
