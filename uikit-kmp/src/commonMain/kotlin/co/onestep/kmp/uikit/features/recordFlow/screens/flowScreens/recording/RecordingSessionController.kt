package co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.recording

import co.onestep.kmp.sdk.currentTimeMillis
import co.onestep.kmp.uikit.bridge.RecorderBridge
import co.onestep.kmp.uikit.models.OSTActivityType
import co.onestep.kmp.uikit.models.OSTRecorderState
import co.onestep.kmp.uikit.models.OSTUserInputMetaData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * The single stop authority for a recording session (owned by [MotionRecorderViewModel]).
 *
 * The recorder is always started with a grace-padded BACKSTOP duration — the SDK only stops the
 * recording on its own if the app is suspended past the deadline (e.g. iOS backgrounding). In
 * every foreground case this controller stops the recorder itself: when the activity time
 * elapses (wall-clock from "go") or when the caller asks (slide-to-stop / flow exit).
 *
 * TUG and STS start the recorder early, on the Get Ready screen ([startEarly]), so their
 * backstop also covers the prepare countdown; the [GO_MARKER] is written at the instant the
 * activity actually begins ([onGo]) so analysis knows where the countdown ended even if the
 * backstop ever cuts a recording.
 */
internal class RecordingSessionController(
    private val recorderBridge: RecorderBridge,
    private val scope: CoroutineScope,
    private val now: () -> Long = { currentTimeMillis() },
) {
    /** Everything the recorder needs at start; built by the ViewModel just-in-time. */
    data class StartRequest(
        val activityType: OSTActivityType,
        /** Activity time in seconds after "go"; null = unrestricted (SDK internal cap only). */
        val activityDurationSeconds: Int?,
        /** Get Ready countdown seconds; covered by the backstop when starting early. */
        val prepareDurationSeconds: Int = 0,
        val sensorEnhancedMode: Boolean = false,
        val userInputMetadata: OSTUserInputMetaData? = null,
        val customMetadata: Map<String, Any> = emptyMap(),
    )

    private val _elapsedSeconds = MutableStateFlow(0)

    /** Whole seconds since "go". The ViewModel derives the on-screen timer from this. */
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds

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
    private var activityDurationSeconds: Int? = null

    /** TUG/STS: start the recorder while the Get Ready countdown is still on screen. */
    fun startEarly(request: StartRequest) {
        if (startJob != null) return
        startedEarly = true
        startRecorder(request, backstopSeconds(request, includePrepare = true))
    }

    /**
     * The activity begins ("go" — the recording screen appears and the activity timer starts):
     * ensures the recorder is running (starts it now unless [startEarly] already did), writes
     * the [GO_MARKER] for early starts, and runs the recording clock.
     *
     * The clock is wall-clock based, so a delayed coroutine resume can't drift — and when the
     * activity duration elapses the controller stops the recorder itself. [requestProvider] is
     * only invoked if the recorder still needs to be started.
     */
    suspend fun onGo(requestProvider: () -> StartRequest) {
        val earlyStart = startJob
        if (earlyStart != null) {
            earlyStart.join()
            if (startedEarly) recorderBridge.addMarker(GO_MARKER)
        } else {
            val request = requestProvider()
            startRecorder(request, backstopSeconds(request, includePrepare = false))
            startJob?.join()
        }
        goTimeMs = now()
        isRecording = true
        // Reset synchronously so the ViewModel's timer mirror never sees a stale value from a
        // previous recording (e.g. retry after an analysis error).
        _elapsedSeconds.value = 0
        clockJob = scope.launch { runClock() }
    }

    /**
     * Gracefully stops the recording and suspends until the recorder reports DONE (which is
     * what moves the ViewModel's screen-state collector into ANALYZING). Safe to call in any
     * phase — no-ops unless a recorder start was initiated, and never calls `stop()` before
     * the in-flight `start()` completed.
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
     * The recorder reported the recording over on its own (SDK backstop/cap) — drop the session
     * bookkeeping so later teardown ([stopAndAwaitDone]) knows there is nothing left to stop.
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
        _elapsedSeconds.value = 0
    }

    private fun startRecorder(request: StartRequest, backstopDurationSeconds: Int?) {
        activityDurationSeconds = request.activityDurationSeconds
        startJob = scope.launch {
            recorderBridge.start(
                activityType = request.activityType,
                duration = backstopDurationSeconds?.let { it * 1000L },
                sensorEnhancedMode = request.sensorEnhancedMode,
                userInputMetadata = request.userInputMetadata,
                customMetadata = request.customMetadata,
            )
        }
    }

    private fun backstopSeconds(request: StartRequest, includePrepare: Boolean): Int? =
        request.activityDurationSeconds?.let { activity ->
            val prepare = if (includePrepare) request.prepareDurationSeconds else 0
            activity + prepare + BACKSTOP_GRACE_SECONDS
        }

    private suspend fun runClock() {
        _elapsedSeconds.value = 0
        val startedAt = goTimeMs ?: now()
        val duration = activityDurationSeconds
        while (isRecording) {
            delay(1000L)
            val elapsed = ((now() - startedAt) / 1000L).toInt()
            _elapsedSeconds.value = elapsed
            if (duration != null && elapsed >= duration) {
                scope.launch { stopAndAwaitDone() }
                return
            }
        }
    }

    companion object {
        /** Marker written to the recording at the instant the activity actually begins. */
        const val GO_MARKER = "go"

        /**
         * Padding added to the SDK backstop duration so the controller's own wall-clock stop
         * always lands first in foreground; the backstop only fires under process suspension.
         */
        const val BACKSTOP_GRACE_SECONDS = 10
    }
}
