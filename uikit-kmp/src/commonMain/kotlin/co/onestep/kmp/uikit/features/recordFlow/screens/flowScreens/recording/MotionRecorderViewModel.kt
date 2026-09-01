package co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.recording

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.onestep.kmp.uikit.bridge.OSTSDKBridge
import co.onestep.kmp.uikit.bridge.PreferencesBridge
import co.onestep.kmp.uikit.bridge.RecorderBridge
import co.onestep.kmp.uikit.data.WalkDuration
import co.onestep.kmp.uikit.features.audio.AudioPlayer
import co.onestep.kmp.uikit.features.audio.TTSPlayer
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTBalance
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTBalanceCondition
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTPrepareData
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTPrepareDuration
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTRecordingConfiguration
import co.onestep.kmp.uikit.features.recordFlow.analytics.RecordFlowAnalyticsTracker
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTRecordingInstruction
import co.onestep.kmp.uikit.features.recordFlow.screensData.IconData
import co.onestep.kmp.uikit.features.recordFlow.screensData.SecondaryButtonData
import co.onestep.kmp.uikit.features.recordFlow.screensData.RecordingScreenData
import co.onestep.kmp.uikit.features.recordFlow.screensData.SlideToStopButtonData
import co.onestep.kmp.uikit.features.recordFlow.screensData.TextData
import co.onestep.kmp.uikit.features.recordFlow.screensData.TimerData
import co.onestep.kmp.uikit.features.recordFlow.screensData.ToolBarData
import co.onestep.kmp.uikit.models.OSTActivityType
import co.onestep.kmp.uikit.features.summary.models.OSTSummaryOptions
import co.onestep.kmp.uikit.models.OSTAnalyserError
import co.onestep.kmp.uikit.models.OSTAnalyserState
import co.onestep.kmp.uikit.models.OSTMotionMeasurement
import co.onestep.kmp.uikit.models.OSTRecorderState
import co.onestep.kmp.uikit.models.OSTAssistiveDevice
import co.onestep.kmp.uikit.models.OSTUserInputMetaData
import co.onestep.kmp.uikit.models.OSTWalkCourseLength.Companion.getWalkCourseLength
import co.onestep.kmp.sdk.currentTimeMillis
import co.onestep.kmp.uikit.utils.Languages
import co.onestep.kmp.uikit.utils.ResourceProvider
import co.onestep.kmp.uikit.utils.toDisplayTime
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.measurement_stopped
import co.onestep.kmp.uikit_kmp.generated.resources.analyzing
import co.onestep.kmp.uikit_kmp.generated.resources.analyzing_in_progress
import co.onestep.kmp.uikit_kmp.generated.resources.dual_task_prepare_instructions
import co.onestep.kmp.uikit_kmp.generated.resources.generating_report
import co.onestep.kmp.uikit_kmp.generated.resources.generic_recording_uploading
import co.onestep.kmp.uikit_kmp.generated.resources.generic_recording_uploading_in_progress
import co.onestep.kmp.uikit_kmp.generated.resources.get_ready
import co.onestep.kmp.uikit_kmp.generated.resources.go
import co.onestep.kmp.uikit_kmp.generated.resources.ic_play_button
import co.onestep.kmp.uikit_kmp.generated.resources.place_the_phone_against_the_thigh
import co.onestep.kmp.uikit_kmp.generated.resources.place_the_phone_in_position
import co.onestep.kmp.uikit_kmp.generated.resources.preparing_results
import co.onestep.kmp.uikit_kmp.generated.resources.recording_in_progress
import co.onestep.kmp.uikit_kmp.generated.resources.slide_to_stop
import co.onestep.kmp.uikit_kmp.generated.resources.start_now
import co.onestep.kmp.uikit_kmp.generated.resources.static_balance_hold_chest
import co.onestep.kmp.uikit_kmp.generated.resources.static_balance_place_pocket_strap
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import org.jetbrains.compose.resources.StringResource
import kotlin.time.Duration.Companion.milliseconds

internal class MotionRecorderViewModel(
    private val resourceProvider: ResourceProvider,
    private val audioPlayer: AudioPlayer,
    private val ttsPlayer: TTSPlayer,
    private val preferenceManager: PreferencesBridge,
    private val recorderBridge: RecorderBridge,
    private val sdkBridge: OSTSDKBridge,
    // True for clinician-mode (patient-scoped) launches. Threaded into HallwayDistanceManager so
    // hallway-length metadata read/write are suppressed for patient sessions. Defaults to false so
    // current-user (patient-app) launches are unchanged.
    private val isPatientSession: Boolean = false,
) : ViewModel() {
    var configuration =
        mutableStateOf(OSTRecordingConfiguration.defaultWalk())

    private val _onInitializationError = Channel<Unit>(Channel.CONFLATED)
    val onInitializationError = _onInitializationError.receiveAsFlow()

    private val _onUiTimeoutExit = Channel<Unit>(Channel.CONFLATED)
    val onUiTimeoutExit = _onUiTimeoutExit.receiveAsFlow()

    // Focused collaborators extracted from this ViewModel (OS God-class decomposition). The
    // ViewModel remains the single entry point the UI sees and delegates to these for their
    // respective concerns; behavior is unchanged.
    private val hallwayManager = HallwayDistanceManager(
        resourceProvider = resourceProvider,
        preferenceManager = preferenceManager,
        sdkBridge = sdkBridge,
        coroutineScope = viewModelScope,
        activityTypeProvider = { configuration.value.activityType },
        hostHallwayLengthMetersProvider = { configuration.value.hallwayLengthMeters },
        isPatientSession = isPatientSession,
    )
    private val toolbar = ToolbarStateHolder(resourceProvider)
    private val balanceManager = BalanceSessionManager()

    // The recorder start/stop lifecycle and the recording clock. The ViewModel is the single
    // stop authority through it: the SDK's start-duration is only a suspended-app backstop.
    private val session = RecordingSessionController(recorderBridge, viewModelScope)

    val recordingLimit: String =
        (recorderBridge.currentRecordingLimit() / 1000 / 60).toInt().toString()

    val stepCount: StateFlow<Int> =
        recorderBridge.stepsCount
            .filter { configuration.value.readyForAnalysisUiAssist && configuration.value.activityType == OSTActivityType.WALK }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = recorderBridge.stepsCount.value,
            )

    var timerValue = mutableStateOf("")
        private set

    var subtitle = mutableStateOf<String?>(null)
        private set

    var recodingScreenState: MutableState<RecordingScreenData> = mutableStateOf(getReadyState())
        private set

    var onMeasurementResult: (OSTMotionMeasurement?) -> Unit = {}

    var onError: (OSTAnalyserError, OSTActivityType?) -> Unit = { _, _ -> }

    /**
     * Analytics tracker, injected by the NavGraph. Null when the host provided no analytics
     * handler, so all tracking is a no-op. Analytics is strictly side-effect-only and never
     * changes flow behavior. Mirrors uikit, which fires the recording-phase measurement
     * events (countdown / analyzing / stop / still-analyzing / start-now) from its VM.
     */
    var analyticsTracker: RecordFlowAnalyticsTracker? = null

    var motionMeasurement = mutableStateOf<OSTMotionMeasurement?>(null)

    val language: String = resourceProvider.getLocaleLanguageTag().substringBefore("-")

    private var timeout = 60

    /**
     * The intended measurement length after "go" — what the timer counts over, supplied by the
     * host as [OSTRecordingConfiguration.duration]. `null` = unrestricted (SDK internal cap only).
     *
     * Excludes the prepare offset (see [prepareOffsetMillis]): the offset extends the recorder's
     * deadline so an early-start activity still gets its full length after "go", but it is not
     * part of the measurement the user is shown.
     */
    private val activityDurationMillis: Long?
        get() = when {
            timeout < 0 -> null // Unrestricted walk
            timeout == 0 -> DEFAULT_RECORDING_DURATION_MS
            else -> timeout * MILLIS_PER_SECOND
        }

    private var note: String? = null

    private var tags: MutableList<String> = mutableListOf()

    private var assistiveDevice: OSTAssistiveDevice? = null

    private val customMetadata: MutableMap<String, Any> = mutableMapOf()

    /**
     * Host-app custom metadata attached to every measurement recorded by this flow
     * (e.g. RTM exclusion, medical devices). Injected by the NavGraph from the
     * [co.onestep.kmp.uikit.features.recordFlow.OSTRecordingFlow] parameter; merged at
     * recorder start before the flow's own keys so internal keys always win.
     */
    var hostCustomMetadata: Map<String, Any> = emptyMap()

    // --- Static Balance session (OS-15960) — state owned by BalanceSessionManager ---------
    // One session per flow launch: the ViewModel is scoped to the recording flow, so a
    // fresh launch gets a fresh session UUID. Every condition recorded in this launch
    // (including via "Record another test") shares it; it is the Engine's grouping key
    // for the session's perceptions.

    /** Groups all Static Balance conditions recorded in this flow launch. */
    val sessionUuid: String get() = balanceManager.sessionUuid

    /**
     * The most recently completed condition's measurement. Used to resume to the web
     * summary when a later condition fails (e.g. "recording too short" → Finish) — the
     * host opens its web summary, which shows all of the day's conditions.
     */
    val lastSavedBalanceMeasurement: OSTMotionMeasurement? get() = balanceManager.lastSavedBalanceMeasurement

    /** The condition configured for the upcoming/current recording, if any. */
    val currentBalanceCondition: OSTBalanceCondition? get() = balanceManager.currentBalanceCondition

    private var readyTimeJob: Job? = null

    private var recordingJob: Job? = null

    private var recordingStateJob: Job? = null

    private var analysingJob: Job? = null

    private var analyseStateJob: Job? = null

    private var uiTimeoutJob: Job? = null

    /**
     * Latches the first terminal error of this analysis attempt so any later one is ignored.
     *
     * The analyser-state collector and the Generic Recording upload path can both observe the same
     * failure — a too-short recording, for instance, is published as
     * [OSTAnalyserState.Failed] **and** returned as a null upload — and the collector's
     * [clearJobs] resets the analyser, so "is the analyser already Failed?" cannot tell a duplicate
     * report from a first one. Without the latch a too-short Generic Recording navigates to its
     * error screen and is then covered by a second error screen pushed on top of it.
     *
     * Reset at the start of every [analyse].
     */
    private var errorReported = false

    private var ttsInstructionsJob: Job? = null

    private var stepMonitorJob: Job? = null

    private var appInForeground = MutableStateFlow(true)

    private var hasPlayedReadyForAnalysisAudio = false

    // Hallway-distance state is owned by HallwayDistanceManager; the ViewModel exposes it
    // unchanged for the UI.
    val hallwayDistanceState get() = hallwayManager.hallwayDistanceState

    // Toolbar state is owned by ToolbarStateHolder; delegated below.
    val showToolbar get() = toolbar.showToolbar
    val toolbarData get() = toolbar.toolbarData

    fun setForegroundState(isForeground: Boolean) {
        appInForeground.value = isForeground
    }

    fun setToolBarData(data: ToolBarData) = toolbar.setToolBarData(data)

    fun showToolbar(show: Boolean) = toolbar.showToolbar(show)

    fun showBackButton(show: Boolean) = toolbar.showBackButton(show)

    fun setToolBarTitle(title: StringResource?) = toolbar.setToolBarTitle(title)

    private fun collectRecordingState() {
        recordingStateJob =
            viewModelScope.launch {
                recorderBridge.recorderState.collect {
                    when (it) {
                        OSTRecorderState.INITIALIZED -> Unit

                        OSTRecorderState.RECORDING -> Unit

                        OSTRecorderState.FINALIZING -> {
                            // This event could be skipped if the RecorderState.DONE is dispatched very quickly
                            if (!recodingScreenState.value.recordScreenStage.isAnalyzing()) {
                                updateState(RecordingScreenData.RecordScreenStage.ANALYZING)
                                startUiTimeout()  // Start timeout when user first sees analyzing UI
                            }
                        }

                        OSTRecorderState.DONE -> {
                            // In case the FINALIZING event is skipped
                            if (!recodingScreenState.value.recordScreenStage.isAnalyzing()) {
                                updateState(RecordingScreenData.RecordScreenStage.ANALYZING)
                                startUiTimeout()  // Also cover case where FINALIZING is skipped
                            }
                            analyse()
                        }
                    }
                }
            }
    }

    private fun collectAnalyseState() {
        analyseStateJob =
            viewModelScope.launch {
                recorderBridge.analyserState.collect {
                    when (it) {
                        OSTAnalyserState.Idle -> Unit

                        OSTAnalyserState.Uploading -> {
                            // A Generic Recording is never analysed, so "analyzing" would promise
                            // a report that never comes. Uploading is all that happens on that
                            // flow, and it is all the copy claims.
                            subtitle.value = resourceProvider.getString(
                                if (skipsAnalysis) {
                                    Res.string.generic_recording_uploading_in_progress
                                } else {
                                    Res.string.analyzing_in_progress
                                },
                            )
                        }

                        OSTAnalyserState.Analyzing -> {
                            // Android never reaches Analyzing for a Generic Recording (the upload
                            // path leaves the analyser at Uploading), but iOS drives both outcomes
                            // through the same native `analyze()` and may pass through it — and
                            // "generating report" would promise a report that is never generated.
                            subtitle.value = resourceProvider.getString(
                                if (skipsAnalysis) {
                                    Res.string.generic_recording_uploading_in_progress
                                } else {
                                    Res.string.generating_report
                                },
                            )
                        }

                        OSTAnalyserState.Analyzed -> {
                            uiTimeoutJob?.cancel()
                            subtitle.value = resourceProvider.getString(Res.string.preparing_results)
                            delay(2000)
                            onMeasurementResult(motionMeasurement.value)
                        }

                        is OSTAnalyserState.Failed -> {
                            uiTimeoutJob?.cancel()
                            clearJobs()
                            reportError(it.error)
                        }
                    }
                }
            }
    }

    fun setConfiguration(configuration: OSTRecordingConfiguration) {
        this.configuration.value = configuration
        audioPlayer.enable(configuration.playVoiceOver)
        ttsPlayer.enable(configuration.prepareScreenData is OSTPrepareData.Tts)
        hallwayManager.loadSavedLength()
    }

    fun initState() {
        subtitle.value = null
        hasPlayedReadyForAnalysisAudio = false

        // Safety net: ensure recorder is in clean state before starting new flow
        val currentState = recorderBridge.recorderState.value
        if (currentState != OSTRecorderState.INITIALIZED) {
            println("MotionRecorderViewModel: initState called with recorder in $currentState state, resetting")
            recorderBridge.reset()
        }

        configuration.value.duration?.let { changeDuration(it) }
        val noPrepareData = configuration.value.prepareScreenData == null
        val durationPrepareData = configuration.value.prepareScreenData as? OSTPrepareData.Duration
        if (noPrepareData || durationPrepareData?.prepareDuration == OSTPrepareDuration.NONE) {
            updateState(RecordingScreenData.RecordScreenStage.RECORDING)
        } else {
            updateState(RecordingScreenData.RecordScreenStage.GET_READY)
        }
        collectRecordingState()
    }

    private fun updateState(stage: RecordingScreenData.RecordScreenStage) {
        when (stage) {
            RecordingScreenData.RecordScreenStage.GET_READY -> handleGetReady()
            RecordingScreenData.RecordScreenStage.RECORDING -> handleRecordingStart()
            RecordingScreenData.RecordScreenStage.ANALYZING -> handleAnalyzingStart()
        }
    }

    private fun handleGetReady() {
        // Prepare foreground service early to avoid crashes when recording starts from background
        viewModelScope.launch {
            recorderBridge.prepareForRecording(configuration.value.activityType)
        }

        configuration.value.prepareScreenData?.let { prepareScreenData ->
            val config = configuration.value
            var instructions =
                resourceProvider.getString(Res.string.dual_task_prepare_instructions)
            when (prepareScreenData) {
                is OSTPrepareData.Tts -> {
                    audioPlayer.stopCurrentAudio()

                    val tts = prepareScreenData.ttsSpeechText.ifEmpty { null }

                    if (prepareScreenData.showInstructions && prepareScreenData.ttsSpeechText.isNotEmpty()) {
                        instructions = prepareScreenData.ttsSpeechText
                    }
                    ttsPlayer.speak(tts ?: instructions)
                    ttsPlayer.setOnDoneListener {
                        updateState(RecordingScreenData.RecordScreenStage.RECORDING)
                        ttsPlayer.setOnDoneListener(null)
                    }
                }

                is OSTPrepareData.Duration -> {
                    if (config.playVoiceOver) {
                        audioPlayer.stopCurrentAudio()
                        when (prepareScreenData.prepareDuration) {
                            // No Russian variant exists for 5-second countdown
                            OSTPrepareDuration.FIVE_SECONDS -> audioPlayer.playAudio(
                                localizedAudioKey("countdown_from_5", ruKey = "countdown_from_5"),
                            )

                            OSTPrepareDuration.TEN_SECONDS -> audioPlayer.playAudio(
                                localizedAudioKey("countdown_from_10"),
                            )

                            OSTPrepareDuration.NONE -> Unit
                        }
                    }
                    // screen: measurement_countdown — the Get Ready countdown screen
                    // is shown (Duration prepare with a non-zero countdown), matching
                    // uikit's countdown screen-view.
                    if (prepareScreenData.prepareDuration != OSTPrepareDuration.NONE) {
                        analyticsTracker?.trackMeasurementCountdownScreen(configuration.value.activityType)
                    }
                    startTimerJob(prepareScreenData)
                    // TUG/STS: the recorder starts already on Get Ready, so the countdown is
                    // part of the recording; the GO marker written at the RECORDING transition
                    // tells analysis where the activity actually began. The recorder's deadline
                    // therefore has to cover the countdown too, or the measurement would end a
                    // whole countdown short of its intended length. The offset is only a
                    // provisional bound — "Start now" can cut the countdown short, so the session
                    // re-anchors the deadline to the real "go" (see RecordingSessionController).
                    if (startsRecorderOnGetReady()) {
                        session.startEarly(
                            buildStartRequest(prepareOffsetMillis = prepareOffsetMillis(prepareScreenData)),
                        )
                    }
                }
            }
            recodingScreenState.value =
                if (config.activityType == OSTActivityType.DUAL_TASK_WALK_SUBTRACT) {
                    getReadyDualTaskState(instructions)
                } else {
                    getReadyState()
                }
        } ?: run {
            updateState(RecordingScreenData.RecordScreenStage.RECORDING)
        }
    }

    private fun handleRecordingStart() {
        readyTimeJob?.cancel()
        audioPlayer.stopCurrentAudio()
        recodingScreenState.value = recordingState()
        startRecording()
    }

    private fun handleAnalyzingStart() {
        // screen: measurement_analyzing — the analyzing UI is first shown.
        // measurement_seconds is the recorded length in whole seconds (wall-clock
        // since recording start, matching uikit's recorded-length semantics).
        val measurementSeconds =
            session.goTimeMs?.let { ((currentTimeMillis() - it) / 1000L).toInt() } ?: 0
        analyticsTracker?.trackAnalyzingScreen(configuration.value.activityType, measurementSeconds)
        recordingJob?.cancel()
        // The recording is over (VM stop, or the SDK backstop/cap fired) — drop the session
        // bookkeeping so a later exit doesn't try to stop an already-stopped recorder.
        session.onRecordingFinished()
        if (configuration.value.playVoiceOver) {
            if (!audioPlayer.isPlaying()) {
                audioPlayer.playAudio(localizedAudioKey("recording_stopped"))
            }
        }
        recodingScreenState.value = analysingState()
    }

    private fun getReadyDualTaskState(instructions: String) = RecordingScreenData(
        recordScreenStage = RecordingScreenData.RecordScreenStage.GET_READY,
        // Nunito Sans ships only regular/medium/bold real weights; W600 has no matching
        // font file and iOS renders it light, so the design's Bold titles/subtitles must
        // use FontWeight.Bold to actually render bold on iOS. (Figma: title/subtitle Bold.)
        title = TextData(resourceProvider.getString(Res.string.get_ready), 60.sp, FontWeight.Bold),
        instructions = TextData(instructions, 28.sp, FontWeight.Bold),
    )

    /** Resolves the Get Ready screen's instruction text based on activity type and balance condition. */
    private fun getReadyInstructions(): String =
        when (configuration.value.activityType) {
            OSTActivityType.ROM_KNEE_EXT -> resourceProvider.getString(Res.string.place_the_phone_against_the_thigh)

            // Static Balance: seated condition holds the phone at the chest; all
            // other stances use pocket/strap placement.
            OSTActivityType.STATIC_BALANCE ->
                if (currentBalanceCondition?.codeFor("stance") == "seated") {
                    resourceProvider.getString(Res.string.static_balance_hold_chest)
                } else {
                    resourceProvider.getString(Res.string.static_balance_place_pocket_strap)
                }

            else -> resourceProvider.getString(Res.string.place_the_phone_in_position)
        }

    private fun getReadyState() = RecordingScreenData(
        recordScreenStage = RecordingScreenData.RecordScreenStage.GET_READY,
        title = TextData(resourceProvider.getString(Res.string.get_ready), 60.sp, FontWeight.Bold),
        instructions = TextData(getReadyInstructions(), 28.sp, FontWeight.Bold),
        timerValue = TimerData(TextData(timerValue.value, 115.sp, FontWeight.Bold), countdown = true),
        bottomButton = SecondaryButtonData(
            text = TextData(resourceProvider.getString(Res.string.start_now), 24.sp, FontWeight.Bold),
            iconData = IconData(icon = Res.drawable.ic_play_button, tintColor = Color.White),
            action = {
                // Clicked: start_measurement_now — user skipped the remaining countdown.
                // time_remaining is the seconds still shown on the countdown at tap time.
                analyticsTracker?.trackStartMeasurementNowClicked(
                    activity = configuration.value.activityType,
                    timeRemaining = timerValue.value.toDoubleOrNull() ?: 0.0,
                )
                timerValue.value = "0"; updateState(RecordingScreenData.RecordScreenStage.RECORDING)
            },
        ),
    )

    private fun recordingState() = RecordingScreenData(
        recordScreenStage = RecordingScreenData.RecordScreenStage.RECORDING,
        title = TextData(resourceProvider.getString(Res.string.go), 60.sp, FontWeight.Bold),
        instructions = TextData(resourceProvider.getString(Res.string.recording_in_progress), 28.sp, FontWeight.Bold),
        timerValue = TimerData(TextData(timerValue.value, 115.sp, FontWeight.Bold), countdown = false),
        slideToStopButton = SlideToStopButtonData(
            textData = TextData(resourceProvider.getString(Res.string.slide_to_stop), 20.sp, FontWeight.Bold),
            action = ::stopRecording,
        ),
    )

    // Generic Recording is never analysed, so the ANALYZING stage says "Uploading" rather than
    // "Analyzing" for it — the stage is the same, only the promise it makes differs.
    private fun analysingState() = RecordingScreenData(
        recordScreenStage = RecordingScreenData.RecordScreenStage.ANALYZING,
        title = TextData(
            resourceProvider.getString(
                if (skipsAnalysis) Res.string.generic_recording_uploading else Res.string.analyzing,
            ),
            60.sp,
            FontWeight.Bold,
        ),
        instructions = TextData(
            resourceProvider.getString(
                if (skipsAnalysis) {
                    Res.string.generic_recording_uploading_in_progress
                } else {
                    Res.string.analyzing_in_progress
                },
            ),
            28.sp,
            FontWeight.Bold,
        ),
    )

    private fun startUiTimeout() {
        if (uiTimeoutJob != null) return  // Already running
        uiTimeoutJob = viewModelScope.launch {
            delay(60_000L)
            println("MotionRecorderViewModel: UI timeout reached after 60 seconds")
            // Cancel all in-flight work (upload continues via NonCancellable in analyze())
            analyseStateJob?.cancel()
            analyseStateJob = null
            analysingJob?.cancel()
            analysingJob = null
            // Reset recorder AND analyser to clean state for re-entry
            recorderBridge.reset()
            when (configuration.value.showSummaryScreen) {
                OSTSummaryOptions.Full,
                OSTSummaryOptions.WEB -> {
                    onError(
                        OSTAnalyserError.Timeout(null, "UI timeout"),
                        configuration.value.activityType
                    )
                }

                OSTSummaryOptions.None -> {
                    viewModelScope.launch {
                        _onUiTimeoutExit.send(Unit)
                    }
                }

                OSTSummaryOptions.MINIMAL -> {
                    onError(
                        OSTAnalyserError.Timeout(null, "UI timeout"),
                        configuration.value.activityType
                    )
                }
            }
        }
    }

    /** TUG/STS begin sensor capture on Get Ready; every other activity starts at "go". */
    private fun startsRecorderOnGetReady(): Boolean =
        configuration.value.activityType == OSTActivityType.TUG ||
            configuration.value.activityType == OSTActivityType.STS

    /**
     * Assembles everything the recorder needs at start (user-input metadata + custom metadata).
     * Called once per recording — either at Get Ready (TUG/STS early start) or at "go".
     */
    private fun buildStartRequest(prepareOffsetMillis: Long = 0L): RecordingSessionController.StartRequest {
        customMetadata.putAll(hostCustomMetadata)
        customMetadata["\$ost_uikit_version"] = ""

        // Static Balance: attach the session grouping key and the per-condition selections
        // (nested under `onestep_balance_conditions`) at recorder start. The recorded
        // duration is the measurement's own `metadata.seconds` (early stop → shorter
        // recording); it is not duplicated into the conditions object. The clinician note is
        // added afterwards on the "Recording saved" screen via [updateBalanceConditionNote].
        if (configuration.value.activityType == OSTActivityType.STATIC_BALANCE) {
            customMetadata[OSTBalanceCondition.KEY_SESSION_UUID] = sessionUuid
            currentBalanceCondition?.let {
                customMetadata[OSTBalanceCondition.KEY_BALANCE_CONDITIONS] =
                    it.toConditionsMetadata(
                        notesKey = configuration.value.balance?.notesKey
                            ?: OSTBalance.DEFAULT_NOTES_KEY,
                    )
            }
        }

        // persist the entered length to the SDK-managed custom-metadata store
        saveHallwayLengthToMetadata()

        return RecordingSessionController.StartRequest(
            activityType = configuration.value.activityType,
            activityDurationMillis = activityDurationMillis,
            prepareOffsetMillis = prepareOffsetMillis,
            sensorEnhancedMode = configuration.value.sensorEnhancedMode,
            userInputMetadata =
                OSTUserInputMetaData(
                    note = note,
                    tags = tags,
                    assistiveDevice = assistiveDevice,
                    walkCourseLength =
                        hallwayManager.hallwayLengthForCurrentTest?.let {
                            getWalkCourseLength(it, isImperialSystem())
                        },
                ),
            customMetadata =
                customMetadata.apply {
                    put(RecorderBridge.READY_FOR_ANALYSIS_KEY, configuration.value.readyForAnalysisUiAssist)
                },
        )
    }

    private fun startRecording() {
        recordingJob =
            viewModelScope.launch {
                session.onGo { buildStartRequest() }
                configuration.value.instructions?.recordingInstructions?.let {
                    // start TTS instructions sequence
                    startRecordingInstructionsJob(it)
                }
                startStepMonitoring()
                mirrorRecordingClock()
            }
    }

    /**
     * Mirrors the session's recording clock into the on-screen timer text.
     *
     * The clock is owned by [RecordingSessionController] and recomputed from absolute anchors, so
     * this is pure formatting — there is no counting here and therefore no drift to accumulate.
     * Both directions read the same elapsed value, which is what makes the early-start activities
     * work with no per-activity branch: TUG's recorder opens during Get Ready, but elapsed is
     * measured from "go", so the timer reads 00:00 there rather than 00:11.
     */
    private suspend fun mirrorRecordingClock() {
        val duration = activityDurationMillis
        val countdown = configuration.value.isCountingDown && duration != null
        session.elapsedMillis.collect { elapsed ->
            val displayMs = if (countdown && duration != null) duration - elapsed else elapsed
            // Count-down rounds up so the full duration shows until the first second has really
            // elapsed and 00:00 appears exactly at the end; count-up floors so it reads 00:00
            // for the first second.
            val seconds = if (countdown) {
                (displayMs + MILLIS_PER_SECOND - 1) / MILLIS_PER_SECOND
            } else {
                displayMs / MILLIS_PER_SECOND
            }
            timerValue.value = seconds.toInt().toDisplayTime()
        }
    }

    fun setWalkDuration(index: Int) {
        changeDuration(WalkDuration.durationByIndex(index).duration)
    }

    /**
     * Stores the pre-recording assistive-device selection so it is attached to the resulting
     * measurement's [OSTUserInputMetaData] at recorder start. Mirrors the Android uikit
     * `setAssistiveDevice`.
     */
    fun setAssistiveDevice(device: OSTAssistiveDevice) {
        this.assistiveDevice = device
    }

    fun addTags(tagsToAdd: List<String>) {
        this.tags.addAll(tagsToAdd)
    }

    fun removeTags(tagsToRemove: List<String>) {
        tags.removeAll(tagsToRemove)
    }

    // --- Static Balance session (OS-15960) — delegated to BalanceSessionManager ----------

    /**
     * Sets the Static Balance condition for the upcoming recording. The condition's
     * per-category selections are attached to the perception as the nested
     * `onestep_balance_conditions` custom metadata at recorder start (see [startRecording]).
     * No note is collected before recording; the single per-condition note is entered
     * afterwards on the "Recording saved" screen and merged into the same nested object via
     * [updateBalanceConditionNote].
     */
    fun setBalanceCondition(condition: OSTBalanceCondition) = balanceManager.setBalanceCondition(condition)

    /** Records a completed condition's measurement in the session. */
    fun onBalanceConditionSaved(measurement: OSTMotionMeasurement) = balanceManager.onBalanceConditionSaved(measurement)

    /** Number of conditions completed in this session so far. */
    fun balanceConditionCount(): Int = balanceManager.balanceConditionCount()

    // The two operations below stay on the ViewModel: they reach into the recorder bridge and
    // the recorder-driven UI state (motionMeasurement, note, tags, customMetadata, timerValue,
    // recodingScreenState), so they cannot move cleanly into BalanceSessionManager. They
    // read/clear the session's condition through the manager.

    /**
     * Attaches the single per-condition note — entered post-recording on the "Recording
     * saved" screen — to the just-analyzed measurement under the nested
     * `onestep_balance_conditions` object, the same channel the per-condition selections
     * travel in (under the server-driven `"note"` tag, [OSTBalance.notesKey]).
     *
     * The note is added after the recording uploaded, so this re-sends the FULL nested
     * object (selections + note) via the update PATCH rather than only the note, so the
     * object stays complete regardless of the server's per-key merge behavior. The
     * measurement's own top-level `note` field is intentionally not used for Static Balance.
     */
    fun updateBalanceConditionNote(newNote: String?) {
        if (newNote.isNullOrBlank()) return
        val measurementId = motionMeasurement.value?.id ?: return
        val condition = currentBalanceCondition ?: return
        val notesKey = configuration.value.balance?.notesKey ?: OSTBalance.DEFAULT_NOTES_KEY
        val conditionsMetadata = condition.copy(notes = newNote)
            .toConditionsMetadata(notesKey = notesKey)
        viewModelScope.launch {
            try {
                recorderBridge.updateBalanceConditionMetadata(
                    uuid = measurementId,
                    conditions = conditionsMetadata,
                )
            } catch (e: Exception) {
                println("MotionRecorderViewModel: Failed to update static balance note for $measurementId: ${e.message}")
            }
        }
    }

    // --- Generic Recording (OS-16861) -----------------------------------------------------

    /**
     * Attaches the single optional free-text note — entered on the Generic Recording's own
     * "Recording saved" screen — to the measurement that was just banked, as the measurement's own
     * top-level `note`.
     *
     * A blank note is a no-op: the note is optional and is never persisted as an empty string.
     *
     * **Suspends until the update completes**, so the caller can await it before finishing the
     * flow. Without that the request is cancelled the moment the host tears the flow down and the
     * note is lost — which on this screen means losing the only thing that describes what was
     * recorded.
     */
    suspend fun updateGenericRecordingNote(newNote: String?) {
        if (newNote.isNullOrBlank()) return
        val measurementId = motionMeasurement.value?.id ?: return
        try {
            recorderBridge.updateMotionMeasurement(
                uuid = measurementId,
                metadata = OSTUserInputMetaData(note = newNote),
            )
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (@Suppress("TooGenericExceptionCaught") failure: Throwable) {
            // Never log the note itself — it is free text a clinician typed about a patient.
            println(
                "MotionRecorderViewModel: Failed to save the generic recording note for " +
                    "$measurementId: ${failure::class.simpleName}",
            )
        }
    }

    /**
     * Resets per-condition state ("Record another test") while keeping the session alive:
     * same [sessionUuid], accumulated measurement ids untouched.
     */
    fun prepareForNextBalanceCondition() {
        balanceManager.clearCurrentCondition()
        note = null
        tags.clear()
        // Drop the per-condition object but keep session_uuid so the next condition stays
        // in the same session.
        customMetadata.remove(OSTBalanceCondition.KEY_BALANCE_CONDITIONS)
        session.resetForNextRecording()
        recorderBridge.reset()
        timerValue.value = ""
        recodingScreenState.value = getReadyState()
    }

    fun stopRecording() {
        if (!session.isRecording) return
        // Clicked: measurement_stop — user slid to stop. elapsed_seconds is the wall-clock
        // recording duration with ms precision, matching uikit.
        session.goTimeMs?.let { startedAt ->
            analyticsTracker?.trackMeasurementStopClicked(
                activity = configuration.value.activityType,
                elapsedSeconds = (currentTimeMillis() - startedAt) / 1000.0,
            )
        }
        viewModelScope.launch {
            stopMeasurementAndAwaitDone()
        }
    }

    private suspend fun stopMeasurementAndAwaitDone(reset: Boolean = false) {
        // Stop the recording coroutine (clock mirror, step monitoring, etc)
        recordingJob?.cancelAndJoin()
        recordingJob = null

        // Stop any TTS instructions tied to the recording
        stopRecordingInstructionsJob()

        // Stop the recorder and wait until it reports DONE (no-op if it never started)
        session.stopAndAwaitDone(reset)
    }

    fun analyse() {
        errorReported = false
        recorderBridge.reset()
        recordingJob?.cancel()
        recordingStateJob?.cancel()
        collectAnalyseState()

        // Ensure timeout is running (covers case where analyse() is called directly)
        startUiTimeout()

        analysingJob =
            viewModelScope.launch {
                appInForeground
                    .filter { it } // only pass when in foreground
                    .filter { recorderBridge.analyserState.value == OSTAnalyserState.Idle } // only pass when idle
                    .first() // suspend until the first `true` is emitted
                if (skipsAnalysis) {
                    // Generic Recording: raw storage only, no pipeline to poll. A successful upload
                    // leaves the analyser at Uploading — it never advances to Analyzed — so the
                    // completion is driven from here rather than from collectAnalyseState().
                    uploadWithoutAnalysing()
                } else {
                    // Then do the analysis exactly once
                    motionMeasurement.value = recorderBridge.analyze()
                }
            }
    }

    /**
     * Banks a Generic Recording and completes the flow with the stored measurement.
     *
     * A failed upload mostly arrives as [OSTAnalyserState.Failed] — the SDK publishes it for the
     * cases the participant can act on, a too-short recording above all — and
     * [collectAnalyseState] already routes those. What is left are the failures the analyser never
     * publishes: no active recording session, the analyser not being Idle, or the uploaded sample
     * not being readable back locally. Without reporting those here the participant would sit on
     * the uploading screen for the full UI timeout and then be told the upload timed out, which is
     * not what happened. [reportError] keeps whichever of the two paths fires first.
     */
    private suspend fun uploadWithoutAnalysing() {
        val uploaded = recorderBridge.uploadWithoutAnalysis()
        motionMeasurement.value = uploaded
        uiTimeoutJob?.cancel()
        if (uploaded != null) {
            onMeasurementResult(uploaded)
        } else {
            reportError(OSTAnalyserError.General(null, "Generic recording upload failed"))
        }
    }

    /** Reports the first terminal error of this analysis attempt and ignores any later one. */
    private fun reportError(error: OSTAnalyserError) {
        if (errorReported) return
        errorReported = true
        onError(error, configuration.value.activityType)
    }

    /**
     * True for measurements whose data is stored raw and never analysed, so the flow must not wait
     * on the analysis pipeline or promise a report the participant will never get.
     */
    private val skipsAnalysis: Boolean
        get() = configuration.value.activityType == OSTActivityType.GENERIC_RECORDING

    private fun changeDuration(newTimeout: Int) {
        timeout = newTimeout
    }

    private fun startStepMonitoring() {
        stepMonitorJob =
            viewModelScope.launch {
                stepCount.collect { count ->
                    if (count >= 20 && !hasPlayedReadyForAnalysisAudio && configuration.value.playVoiceOver) {
                        hasPlayedReadyForAnalysisAudio = true
                        playReadyForAnalysisAudio()
                    }
                }
            }
    }

    /** Get Ready countdown: `N…1` (1 s each), then "GO" for 1.5 s, then the recording begins. */
    private fun startTimerJob(prepareData: OSTPrepareData.Duration) {
        readyTimeJob?.cancel()
        val prepareSeconds = prepareData.prepareDuration.seconds
        timerValue.value = prepareSeconds.toString()
        readyTimeJob =
            viewModelScope.launch {
                for (time in prepareSeconds downTo 1) {
                    timerValue.value = time.toString()
                    delay(COUNTDOWN_TICK_MS)
                }
                timerValue.value = resourceProvider.getString(Res.string.go)
                delay(GO_FRAME_MS)
                updateState(RecordingScreenData.RecordScreenStage.RECORDING)
            }
    }

    /**
     * Wall time the Get Ready countdown occupies before "go" — what an early-start activity has to
     * add to the recorder's deadline so the measurement still gets its full length after "go".
     *
     * Reconstructed from [startTimerJob]'s own schedule: one [COUNTDOWN_TICK_MS] frame per counted
     * second **plus the longer final "GO" frame**, so a ten-second countdown occupies 11 500 ms,
     * not 10 000 ms. Both derive from the same constants; keep them in lock-step.
     *
     * Only a [OSTPrepareData.Duration] prepare has a length that is knowable up front. A
     * [OSTPrepareData.Tts] prepare ends on a speech-synthesis callback, so no offset can be
     * predicted for it — do not extend the early start to a TTS prepare without re-anchoring the
     * recorder's deadline at "go" instead of predicting it.
     */
    private fun prepareOffsetMillis(prepareData: OSTPrepareData.Duration): Long {
        val seconds = prepareData.prepareDuration.seconds
        if (seconds <= 0) return 0L
        return seconds * COUNTDOWN_TICK_MS + GO_FRAME_MS
    }

    fun clearJobs() {
        // Cancel state collectors FIRST (before stop flow) to prevent
        // the DONE state from triggering analyse() during cancellation
        recordingStateJob?.cancel()
        recordingStateJob = null
        analysingJob?.cancel()
        analysingJob = null
        analyseStateJob?.cancel()
        analyseStateJob = null
        uiTimeoutJob?.cancel()
        uiTimeoutJob = null

        viewModelScope.launch {
            // Gracefully stop the recorder and wait for DONE, if a start was initiated
            // (covers a TUG/STS early start during Get Ready too)
            stopMeasurementAndAwaitDone(reset = true)

            // Cancel remaining jobs
            readyTimeJob?.cancel()
            audioPlayer.stopCurrentAudio()
            stepMonitorJob?.cancel()
            ttsPlayer.stopCurrentSpeech()
        }

        // Ensure reset happens even if viewModelScope is cancelled.
        // stopMeasurementAndAwaitDone no-ops when no recorder start is in flight
        // (analysis phase or pre-recording), so reset() never gets called. Do it directly here.
        if (!session.recorderStartInitiated) {
            recorderBridge.reset()
        }
    }

    private fun startRecordingInstructionsJob(instructionsQueue: List<OSTRecordingInstruction>) {
        ttsInstructionsJob =
            viewModelScope.launch {
                try {
                    val startTime = currentTimeMillis()

                    for (instruction in instructionsQueue) {
                        val delayTime =
                            instruction.startTimeMillis - (currentTimeMillis() - startTime)
                        if (delayTime > 0) delay(delayTime.milliseconds) // Wait until the correct timestamp
                        ttsPlayer.speak(instruction.text)
                        instruction.marker?.let {
                            recorderBridge.addMarker(it)
                        }
                    }
                } catch (e: CancellationException) {
                    e.printStackTrace()
                    ttsPlayer.speak(resourceProvider.getString(Res.string.measurement_stopped))
                }
            }
    }

    fun stopRecordingInstructionsJob() {
        ttsInstructionsJob?.cancel()
        ttsPlayer.stopCurrentSpeech()
    }

    /**
     * Returns the localized audio file key for [base] by appending the language suffix.
     * [ruKey] overrides the Russian/Ukrainian/Romanian key (default: "${base}_ru").
     * [iwKey] overrides the Hebrew key (default: "${base}_iw").
     */
    private fun localizedAudioKey(
        base: String,
        ruKey: String = "${base}_ru",
        iwKey: String = "${base}_iw",
    ): String = when (language) {
        Languages.RUSSIAN, Languages.UKRAINIAN, Languages.ROMANIAN -> ruKey
        Languages.HEBREW, Languages.HEBREW_LEGACY -> iwKey
        else -> base
    }

    fun playReadyForAnalysisAudio() {
        // Note: the Russian asset is intentionally named "data_is_read_for_analysis_ru"
        // (matching the actual mp3 filename); Hebrew uses "_heb" instead of "_iw".
        audioPlayer.playAudio(
            localizedAudioKey(
                "data_is_ready_for_analysis",
                ruKey = "data_is_read_for_analysis_ru",
                iwKey = "data_is_ready_for_analysis_heb",
            ),
        )
    }

    // --- Hallway distance — delegated to HallwayDistanceManager --------------------------

    /** The committed hallway length in meters, for the host-facing flow result. Null when skipped. */
    val committedHallwayLengthMeters: Float? get() = hallwayManager.committedHallwayLengthMeters

    fun saveHallwayLengthToMetadata() = hallwayManager.saveHallwayLengthToMetadata()

    fun onHallwayInputChanged(rawValue: String) = hallwayManager.onHallwayInputChanged(rawValue)

    fun onHallwayContinue(): Boolean = hallwayManager.onHallwayContinue()

    fun onShortHallwayStartTest(): Boolean = hallwayManager.onShortHallwayStartTest()

    fun onHallwaySkip() = hallwayManager.onHallwaySkip()

    fun dismissShortHallwayDialog() = hallwayManager.dismissShortHallwayDialog()

    fun onSuppressShortHallwayWarningChanged(suppress: Boolean) =
        hallwayManager.onSuppressShortHallwayWarningChanged(suppress)

    fun isImperialSystem(): Boolean = hallwayManager.isImperialSystem()

    private companion object {
        // Get Ready countdown frame durations. prepareOffsetMillis() reconstructs the countdown's
        // total wall time from these, so startTimerJob and the offset must share them.
        const val COUNTDOWN_TICK_MS = 1_000L

        /** The "GO" frame is held longer than a counted second. */
        const val GO_FRAME_MS = 1_500L

        const val MILLIS_PER_SECOND = 1_000L

        /** Measurement length used when the host supplies none. */
        const val DEFAULT_RECORDING_DURATION_MS = 60 * MILLIS_PER_SECOND
    }
}
