package co.onestep.kmp.uikit.features.recordFlow.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import co.onestep.kmp.uikit.bridge.Permission
import co.onestep.kmp.uikit.bridge.PermissionStatus
import co.onestep.kmp.uikit.bridge.resolveSessionBridges
import co.onestep.kmp.uikit.di.UIKitServiceLocator
import co.onestep.kmp.uikit.features.audio.PlatformAudioPlayerAdapter
import co.onestep.kmp.uikit.features.audio.PlatformTTSPlayerAdapter
import co.onestep.kmp.uikit.features.recordFlow.RecordFlowDataFactory
import co.onestep.kmp.uikit.features.recordFlow.RecordFlowError
import co.onestep.kmp.uikit.features.recordFlow.OSTRecordingFlowResult
import co.onestep.kmp.uikit.features.recordFlow.RecordFlowOutcome
import co.onestep.kmp.uikit.features.recordFlow.ResultHandler
import co.onestep.kmp.uikit.features.recordFlow.components.Toolbar
import co.onestep.kmp.uikit.features.recordFlow.components.ToolBarColors
import co.onestep.kmp.uikit.features.recordFlow.components.ToolBarHeight
import co.onestep.kmp.uikit.features.recordFlow.analytics.RecordFlowAnalyticsEvents
import co.onestep.kmp.uikit.features.recordFlow.analytics.RecordFlowAnalyticsTracker
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTRecordingConfiguration
import co.onestep.kmp.uikit.features.recordFlow.configurations.collectsPostRecordingNote
import co.onestep.kmp.uikit.features.recordFlow.configurations.defaultInstructions
import co.onestep.kmp.uikit.features.recordFlow.destinations.CustomTagsDestination
import co.onestep.kmp.uikit.features.recordFlow.destinations.HallwayDistanceDestination
import co.onestep.kmp.uikit.features.recordFlow.destinations.PreAssistiveDeviceDestination
import co.onestep.kmp.uikit.features.recordFlow.destinations.PreFootwearDestination
import co.onestep.kmp.uikit.features.recordFlow.destinations.SelectWalkDurationDestination
import co.onestep.kmp.uikit.features.recordFlow.destinations.SoundInstructionsDestination
import co.onestep.kmp.uikit.features.recordFlow.destinations.SoundPermissionDeniedAlwaysDestination
import co.onestep.kmp.uikit.features.recordFlow.destinations.SoundPermissionDestination
import co.onestep.kmp.uikit.features.recordFlow.destinations.StartRecordDestination
import co.onestep.kmp.uikit.features.recordFlow.destinations.customTagsScreen
import co.onestep.kmp.uikit.features.recordFlow.destinations.hallwayDistanceScreen
import co.onestep.kmp.uikit.features.recordFlow.destinations.preAssistiveDeviceScreen
import co.onestep.kmp.uikit.features.recordFlow.destinations.preFootwearScreen
import co.onestep.kmp.uikit.features.recordFlow.destinations.selectWalkDurationScreen
import co.onestep.kmp.uikit.features.recordFlow.destinations.soundInstructionsScreen
import co.onestep.kmp.uikit.features.recordFlow.destinations.soundPermissionScreen
import co.onestep.kmp.uikit.features.recordFlow.destinations.startRecordScreen
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTBalance
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.errors.ErrorScreen
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.staticBalance.ConditionSetupDestination
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.genericRecording.GenericRecordingNotesDestination
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.genericRecording.genericRecordingNotesScreen
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.staticBalance.RecordingSavedDestination
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.staticBalance.conditionSetupScreen
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.staticBalance.recordingSavedScreen
import co.onestep.kmp.uikit.features.recordFlow.screens.instructions.InstructionsContent
import co.onestep.kmp.uikit.features.recordFlow.screensData.EmptyAnalysisScreenData
import co.onestep.kmp.uikit.features.recordFlow.screensData.IconData
import co.onestep.kmp.uikit.features.recordFlow.screensData.PrimaryButtonData
import co.onestep.kmp.uikit.features.recordFlow.screensData.TextData
import co.onestep.kmp.uikit.features.recordFlow.screensData.ToolBarData
import co.onestep.kmp.uikit.navigation.UIktNavDisplay
import co.onestep.kmp.uikit.navigation.UIktNavSavedStateConfiguration
import co.onestep.kmp.uikit.navigation.pop
import co.onestep.kmp.uikit.navigation.popUpToInclusive
import co.onestep.kmp.uikit.ui.components.BottomSheet
import co.onestep.kmp.uikit.testing.OSTTestTags
import co.onestep.kmp.uikit.utils.test
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.recording.MotionRecorderViewModel
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.recording.RecordingScreenContent
import co.onestep.kmp.uikit.features.recordFlow.screensData.RecordingScreenData
import co.onestep.kmp.uikit.features.recordFlow.screensData.isSixOrTwoMinWalk
import co.onestep.kmp.uikit.features.tagging.models.Footwear
import co.onestep.kmp.uikit.features.summary.OSTMeasurementSummary
import co.onestep.kmp.uikit.features.summary.screens.navigation.StsManualReportDestination
import co.onestep.kmp.uikit.features.summary.screens.navigation.stsManualReportScreen
import co.onestep.kmp.uikit.features.summary.models.OSTSummaryOptions
import co.onestep.kmp.uikit.features.summary.models.OSTSummaryOrigin
import co.onestep.kmp.uikit.models.FeatureFlag
import co.onestep.kmp.uikit.models.OSTActivityType
import co.onestep.kmp.sdk.OSTEvent
import co.onestep.kmp.uikit.models.OSTMotionMeasurement
import co.onestep.kmp.sdk.currentTimeMillis
import co.onestep.kmp.uikit.models.displayNameRes
import co.onestep.kmp.uikit.utils.UIktDestination
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.continue_camel_case
import co.onestep.kmp.uikit_kmp.generated.resources.great_job_on_completing_a_walk
import co.onestep.kmp.uikit_kmp.generated.resources.ic_chevron_left
import co.onestep.kmp.uikit_kmp.generated.resources.ic_close
import co.onestep.kmp.uikit_kmp.generated.resources.ic_warning
import co.onestep.kmp.uikit_kmp.generated.resources.no
import co.onestep.kmp.uikit_kmp.generated.resources.steps_measured
import co.onestep.kmp.uikit_kmp.generated.resources.short_hallway_dont_show_again
import co.onestep.kmp.uikit_kmp.generated.resources.short_hallway_edit_hallway_length
import co.onestep.kmp.uikit_kmp.generated.resources.short_hallway_length_message
import co.onestep.kmp.uikit_kmp.generated.resources.short_hallway_length_title
import co.onestep.kmp.uikit_kmp.generated.resources.short_hallway_start_test
import co.onestep.kmp.uikit_kmp.generated.resources.stop_recording_dialog_text
import co.onestep.kmp.uikit_kmp.generated.resources.yes
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import co.onestep.designsystem.components.ButtonVariant
import co.onestep.designsystem.components.OSPopup
import co.onestep.designsystem.theme.LocalOSColors
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

// Post-recording navigation destinations (remain local to NavGraph)
@Serializable
internal data object RecordingDestination : UIktDestination

@Serializable
internal data object SummaryResultDestination : UIktDestination

@Serializable
internal data object ErrorResultDestination : UIktDestination

@Serializable
internal data object EmptyAnalysisDestination : UIktDestination

/**
 * The destination the flow records from — the one every "start over" path returns to.
 *
 * [StartRecordDestination] for most activities: a big "Start" button beside a "View instructions"
 * link. **Two activities skip it and record from [RecordingDestination]**, each because that screen
 * would be a tap that asks for nothing:
 *
 * - **Generic Recording** — OneStep does not know what is being recorded, so the configuration
 *   carries no instructions (`OSTRecordingConfiguration.genericRecording()`), and the recording
 *   screen's own "Get ready" countdown already gives the clinician a beat before it starts.
 * - **Dual Task** — its Get Ready screen *is* a start screen. It shows the whole spoken protocol
 *   (`MotionRecorderViewModel.getReadyDualTaskState`, fed the TTS utterance when the host sets
 *   `OSTPrepareData.Tts(showInstructions = true)`) and waits on its own "Start now" button, so a
 *   Start screen in front of it asks the clinician to confirm twice — once before the instructions
 *   are read out, and again after. The instructions link is no loss either: this is the one
 *   activity whose full instructions are read aloud and printed on the next screen.
 *
 * ⚠️ Abandoning a recording behaves differently for these two: there is no Start screen to step
 * back to, and stepping "back" to [RecordingDestination] would restart the recording just
 * abandoned, so the flow exits instead. That branch keys off this function's result rather than
 * naming activities, so it already covers both.
 */
internal fun recordEntryDestinationFor(activityType: OSTActivityType): UIktDestination =
    when (activityType) {
        OSTActivityType.GENERIC_RECORDING,
        OSTActivityType.DUAL_TASK_WALK_SUBTRACT,
        -> RecordingDestination

        else -> StartRecordDestination
    }

/**
 * The ordered pre-recording screens for [config], ending in the destination it records from
 * ([recordEntryDestinationFor]). Each screen advances to its successor in this list.
 *
 * Pure, and separate from the composable, so the sequence is assertable without a Compose runtime.
 * [micStatus] stays a lambda rather than a value because it is a permission query, and only
 * dual-task makes it.
 */
internal fun buildPreRecordDestinations(
    config: OSTRecordingConfiguration,
    micStatus: () -> PermissionStatus,
    showSoundInstructions: () -> Boolean,
): List<UIktDestination> = buildList {
    // Static Balance (OS-15960): each condition begins on the Condition Setup screen,
    // then flows StartRecord -> Recording -> "Recording saved". "Record another test"
    // loops back to the start of this sequence (Condition Setup).
    val isStaticBalance = config.activityType == OSTActivityType.STATIC_BALANCE
    if (isStaticBalance) {
        add(ConditionSetupDestination)
        // The length is chosen per condition, right after the condition is confirmed
        // (OS-17175). Unconditional: `staticBalance()` carries a non-null default duration,
        // so the generic gate below would never fire, and the session loop must re-ask on
        // every condition.
        add(SelectWalkDurationDestination)
    }

    // a) Hallway distance for 6min/2min walks
    if (isSixOrTwoMinWalk(config.activityType)) {
        add(HallwayDistanceDestination)
    }

    // b) Walk duration picker if duration isn't set
    if (!isStaticBalance && (config.duration == null || config.duration == 0)) {
        add(SelectWalkDurationDestination)
    }

    // c.1) Optional pre-recording assistive-device selection
    if (config.showPreRecordingAssistiveDeviceSelection) {
        add(PreAssistiveDeviceDestination)
    }

    // c.2) Optional pre-recording footwear selection
    if (config.showPreRecordingFootwearSelection) {
        add(PreFootwearDestination)
    }

    // d) Optional pre-recording questions (custom tags)
    config.preRecordingQuestions?.let {
        add(CustomTagsDestination)
    }

    // e) Microphone permission for dual-task
    if (config.activityType == OSTActivityType.DUAL_TASK_WALK_SUBTRACT) {
        val status = micStatus()
        when {
            status == PermissionStatus.GRANTED -> Unit
            status != PermissionStatus.DENIED -> add(SoundPermissionDestination)
            else -> add(SoundPermissionDeniedAlwaysDestination)
        }
    }

    // f) Sound instructions if volume is low
    if (config.playVoiceOver && showSoundInstructions()) {
        add(SoundInstructionsDestination)
    }

    // g) End on the screen the flow records from.
    add(recordEntryDestinationFor(config.activityType))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RecordFlowNavGraph(
    config: OSTRecordingConfiguration,
    patientId: String? = null,
    onResult: (OSTEvent) -> Unit,
    onFinished: (OSTRecordingFlowResult) -> Unit = {},
    onDismiss: () -> Unit,
    shouldShowSoundInstructions: () -> Boolean = { false },
    onAskMicrophonePermission: () -> Unit = {},
    onGoToSettings: () -> Unit = {},
    customMetadata: Map<String, Any> = emptyMap(),
) {
    // Resolve the patient-bound bridge bundle once per launch. null patientId = current-user mode
    // (today's auth-bound singletons). Non-null = clinician mode: build a patient-scoped bundle via
    // the registered factory, failing fast if none was configured — silently falling back to the
    // singleton would attribute a patient's recording to the wrong identity.
    val bridges = remember(patientId) {
        resolveSessionBridges(
            patientId = patientId,
            currentUserBridges = { UIKitServiceLocator.currentUserBridges() },
            patientScopedBridgesFactory = UIKitServiceLocator.patientScopedBridgesFactory,
        )
    }

    val resourceProvider = UIKitServiceLocator.resourceProvider
    val featureFlags = UIKitServiceLocator.featureFlagsBridge
    val permissionsManager = UIKitServiceLocator.permissionsManager
    val scope = rememberCoroutineScope()

    // Record-flow analytics tracker (null when the host provided no analytics handler → all
    // tracking is a no-op). Analytics is side-effect-only and never changes flow behavior.
    val recordFlowTracker = UIKitServiceLocator.recordFlowAnalyticsTracker
    val activity = config.activityType

    // Build the ordered pre-recording destination sequence based on config
    val preRecordDestinations = remember(config) {
        buildPreRecordDestinations(
            config = config,
            micStatus = { permissionsManager.checkPermissionStatus(Permission.MICROPHONE) },
            showSoundInstructions = shouldShowSoundInstructions,
        )
    }

    // Build navigation map: each destination -> its successor
    val navigationMap = remember(preRecordDestinations) {
        preRecordDestinations.zipWithNext().toMap()
    }

    // Where the flow records from, and so where every "back to the beginning" lands. Generic
    // Recording has no Start screen (see [buildPreRecordDestinations]), so for it that is the
    // recording itself.
    val recordEntryDestination: UIktDestination = recordEntryDestinationFor(config.activityType)

    val startDestination: NavKey = preRecordDestinations.firstOrNull() ?: recordEntryDestination

    // Navigation 3 back stack owned by this flow. The uikit serializers module makes it
    // saveable across config changes and process death on every platform (iOS has no
    // reflection-based fallback).
    val backStack = rememberNavBackStack(UIktNavSavedStateConfiguration, startDestination)
    val currentKey: NavKey? = backStack.lastOrNull()

    // Helper to navigate to the next destination in the pre-recording sequence
    fun navigateToNext(current: UIktDestination) {
        val next = navigationMap[current] ?: return
        backStack.add(next)
    }

    val viewModel = remember(patientId) {
        MotionRecorderViewModel(
            // Patient-scoped in clinician mode; the auth-bound singleton in current-user mode.
            recorderBridge = bridges.recorderBridge,
            audioPlayer = PlatformAudioPlayerAdapter(UIKitServiceLocator.audioPlayer),
            ttsPlayer = PlatformTTSPlayerAdapter(UIKitServiceLocator.ttsPlayer),
            preferenceManager = UIKitServiceLocator.preferencesBridge,
            resourceProvider = resourceProvider,
            // sdkBridge stays the singleton: sdkState/events are not patient-scoped, and the only
            // patient-touching use (hallway-length metadata) is suppressed via isPatientSession.
            sdkBridge = UIKitServiceLocator.sdkBridge,
            isPatientSession = patientId != null,
        ).apply {
            setConfiguration(config)
            hostCustomMetadata = customMetadata
            // Inject the tracker so the VM can fire the recording-phase measurement events
            // (countdown / analyzing / stop / start-now), matching uikit's VM-side tracking.
            analyticsTracker = recordFlowTracker
        }
    }

    var resultMeasurement by remember { mutableStateOf<OSTMotionMeasurement?>(null) }
    var resultError by remember { mutableStateOf<RecordFlowError?>(null) }
    // The on-screen error identity (canonical code + localized title) while an error screen is
    // shown, so the toolbar exit_button can report error_code + error_title_string — mirroring
    // uikit's LocalRecordErrorSink / viewModel.currentError. Set when navigating to
    // [ErrorResultDestination]; cleared when the error screen leaves the composition.
    var currentErrorCode by remember { mutableStateOf<String?>(null) }
    var currentErrorTitle by remember { mutableStateOf<String?>(null) }
    var showInstructionsSheet by remember { mutableStateOf(false) }
    var showExitConfirmationDialog by remember { mutableStateOf(false) }
    var showRecordingExitDialog by remember { mutableStateOf(false) }

    // Toolbar state
    val showToolbar = viewModel.showToolbar
    val toolbarData = viewModel.toolbarData
    val tagBackRequests = remember { MutableSharedFlow<Unit>() }
    val currentScreenIndex = remember { mutableIntStateOf(0) }

    // Wire app foreground state to viewModel so analyse() can proceed
    // Mirrors original Fragment onStart/onStop lifecycle wiring
    LifecycleStartEffect(Unit) {
        viewModel.setForegroundState(true)
        onStopOrDispose {
            viewModel.setForegroundState(false)
        }
    }

    // Set up toolbar data (back + close icons)
    LaunchedEffect(Unit) {
        viewModel.setToolBarData(
            ToolBarData(
                startIcon =
                    IconData(Res.drawable.ic_chevron_left) {
                        val current = backStack.lastOrNull()
                        // Clicked: back_button — toolbar back within the measurement flow.
                        // screen_name is derived per-destination from the current key
                        // (simple class name, lowercased), matching uikit MainFlowScreen.
                        recordFlowTracker?.trackBackClicked(
                            screenName = current?.let { it::class.simpleName?.lowercase() }
                                ?: "",
                            activity = activity,
                        )
                        if (current == CustomTagsDestination) {
                            scope.launch { tagBackRequests.emit(Unit) }
                        } else {
                            viewModel.clearJobs()
                            if (!backStack.pop()) {
                                onDismiss()
                            }
                        }
                    },
                endIcons =
                    listOf(
                        IconData(Res.drawable.ic_close) {
                            val current = backStack.lastOrNull()
                            // Clicked: exit_button — toolbar close within the measurement flow.
                            // screen_name is the destination's simple class name with the trailing
                            // "Destination" stripped (e.g. "ErrorStsShort"), matching uikit.
                            // error_code/error_title_string are populated only when exiting from an
                            // error screen (currentErrorCode/Title are null otherwise).
                            recordFlowTracker?.trackExitClicked(
                                screenName = current?.let {
                                    it::class.simpleName?.removeSuffix("Destination")
                                } ?: "",
                                activity = activity,
                                errorCode = currentErrorCode,
                                errorTitle = currentErrorTitle,
                            )
                            viewModel.clearJobs()
                            onDismiss()
                        },
                    ),
            ),
        )
        viewModel.showToolbar(true)
        viewModel.showBackButton(true)
    }

    // Adjust toolbar per destination
    LaunchedEffect(currentKey) {
        adjustToolBar(currentKey, viewModel, config)
    }

    // Screen-view analytics: fire the per-destination `screen:` events as each pre-recording
    // / start destination becomes current, mirroring uikit's per-screen LaunchedEffect
    // screen-views. Static Balance's condition-setup screen-view is fired via that screen's
    // own onScreenView callback below (it needs the 1-based condition number + session uuid).
    LaunchedEffect(currentKey) {
        when (currentKey) {
            SelectWalkDurationDestination ->
                recordFlowTracker?.trackWalkSelectDurationScreen(activity)
            PreAssistiveDeviceDestination ->
                recordFlowTracker?.trackPreRecordingAssistiveDeviceScreen(activity)
            PreFootwearDestination ->
                recordFlowTracker?.trackPreRecordingFootwearScreen(activity)
            CustomTagsDestination ->
                recordFlowTracker?.trackPreTagScreen(activity, RecordFlowAnalyticsEvents.TagSource.PRE_TAG)
            SoundInstructionsDestination ->
                recordFlowTracker?.trackIncreaseVolumeScreen(activity)
            StartRecordDestination ->
                recordFlowTracker?.trackMeasurementStartScreen(activity)
        }
    }

    Box(Modifier.fillMaxSize().background(LocalOSColors.current.neutral_m5)) {
        // The toolbar overlays a fixed top inset instead of sitting in a Column above the
        // NavDisplay (mirrors the original uikit MainFlowScreen). The NavDisplay is always
        // fillMaxSize() with a constant per-route top inset, so hiding the toolbar — e.g. on the
        // Get Ready / recording screen — no longer collapses a Column slot and stretches the
        // content; the toolbar just fades away over the reserved space. Because the NavDisplay is
        // inset below the toolbar (not overlapping it) there is no iOS touch conflict.
        //
        // Screens that intentionally render with no top chrome (Recording saved, Summary)
        // reclaim the inset so they keep their full-height layout.
        val collapseToolbarGap = when (currentKey) {
            RecordingSavedDestination,
            GenericRecordingNotesDestination,
            SummaryResultDestination -> true
            else -> false
        }

        UIktNavDisplay(
            modifier = Modifier
                .fillMaxSize()
                // Reserve the toolbar's full height (status bar + ToolBarHeight) so the
                // overlaid toolbar never covers content and toggling it never resizes the
                // screen. Collapsed routes render full-bleed (no top chrome).
                .then(
                    if (collapseToolbarGap) {
                        Modifier
                    } else {
                        Modifier
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .padding(top = ToolBarHeight.dp)
                    },
                ),
            backStack = backStack,
            onBack = { backStack.pop() },
            entryProvider = entryProvider {
        // --- Pre-recording screens ---

        // Hallway distance (for 6min/2min walks)
        hallwayDistanceScreen(
            stateProvider = { viewModel.hallwayDistanceState.value },
            onValueChange = { value ->
                viewModel.onHallwayInputChanged(value)
            },
            onContinue = {
                if (viewModel.onHallwayContinue()) {
                    // hallway_length_submitted — a length was entered. value/unit come from the
                    // committed hallway state; unit is the spec-canonical m/ft.
                    recordFlowTracker?.trackHallwayLengthSubmitted(
                        activity = activity,
                        entered = true,
                        value = viewModel.hallwayDistanceState.value.inputValue.toIntOrNull(),
                        unit = hallwayUnit(viewModel.isImperialSystem()),
                    )
                    navigateToNext(HallwayDistanceDestination)
                }
            },
            onContinueWithoutLength = {
                // hallway_length_submitted — the clinician skipped entering a length.
                recordFlowTracker?.trackHallwayLengthSubmitted(
                    activity = activity,
                    entered = false,
                    value = null,
                    unit = null,
                )
                viewModel.onHallwaySkip()
                navigateToNext(HallwayDistanceDestination)
            },
        )

        // Walk duration selection
        selectWalkDurationScreen(
            activityType = activity,
            recordingLimit = viewModel.recordingLimit,
            onPrimaryAction = { index ->
                // Clicked: walk_duration_selected — the tapped option's index. Which option set
                // it indexes depends on the activity (walk: 0=1min …; Static Balance: 0=10s …).
                recordFlowTracker?.trackWalkDurationSelected(activity, index)
                viewModel.setWalkDuration(index)
                // Static Balance confirms the condition one screen earlier, but the PRD's
                // static_balance_condition_confirmed carries the recording length — only known
                // here (OS-17175). Fired after setWalkDuration so the `duration` prop is the
                // clinician's choice, not the configured default. It emits the full condition
                // config (canonical category codes) + duration + condition_number +
                // session_uuid; the optional free-text note is NOT sent (HIPAA).
                viewModel.currentBalanceCondition?.let { condition ->
                    recordFlowTracker?.trackStaticBalanceConditionConfirmed(
                        condition = condition,
                        durationSeconds = viewModel.configuration.value.duration ?: 0,
                        conditionNumber = viewModel.balanceConditionCount() + 1,
                        sessionUuid = viewModel.sessionUuid,
                    )
                }
                navigateToNext(SelectWalkDurationDestination)
            },
        )

        // Pre-recording assistive-device selection (optional, gated by config)
        preAssistiveDeviceScreen(
            onDeviceSelected = { device ->
                // Clicked: pre_recording_assistive_device_selected — the typed device
                // selection (enum name), never PII.
                recordFlowTracker?.trackPreRecordingAssistiveDeviceSelected(activity, device)
                viewModel.setAssistiveDevice(device)
                navigateToNext(PreAssistiveDeviceDestination)
            },
        )

        // Pre-recording footwear selection (optional, gated by config). NONE adds no tag,
        // matching uikit; other selections are attached as a tag by their display name.
        preFootwearScreen(
            onFootwearSelected = { footwear, displayName ->
                // Clicked: pre_recording_footwear_selected — the typed footwear selection
                // (enum name), never PII.
                recordFlowTracker?.trackPreRecordingFootwearSelected(activity, footwear)
                if (footwear != Footwear.NONE) {
                    viewModel.addTags(listOf(displayName))
                }
                navigateToNext(PreFootwearDestination)
            },
        )

        // Pre-recording questions (custom tags)
        customTagsScreen(
            preRecordingQuestions = config.preRecordingQuestions,
            onAddTags = { viewModel.addTags(it) },
            onRemoveTags = { viewModel.removeTags(it) },
            onToolbarBackRequest = tagBackRequests,
            currentIndex = currentScreenIndex,
            onBack = { backStack.pop() },
            onDone = {
                navigateToNext(CustomTagsDestination)
            },
        )

        // Sound permission (microphone) — either SoundPermission or SoundPermissionDeniedAlways
        // is in the list, never both. Skip/Allow from either screen navigates to the same next.
        val nextAfterSoundPermission = navigationMap[SoundPermissionDestination]
            ?: navigationMap[SoundPermissionDeniedAlwaysDestination]

        soundPermissionScreen(
            resourceProvider = resourceProvider,
            onAskMicrophonePermission = {
                onAskMicrophonePermission()
                nextAfterSoundPermission?.let { backStack.add(it) }
            },
            onSkip = {
                nextAfterSoundPermission?.let { backStack.add(it) }
            },
            onGoToSettings = onGoToSettings,
        )

        // Sound instructions (volume low)
        soundInstructionsScreen(
            primaryAction = {
                navigateToNext(SoundInstructionsDestination)
            },
        )

        // Start record screen (big "Start" button + "View instructions")
        startRecordScreen(
            activityType = activity,
            primaryAction = {
                // Clicked: start_measurement — user tapped Start on the StartRecord screen.
                recordFlowTracker?.trackStartMeasurementClicked(activity)
                backStack.popUpToInclusive(StartRecordDestination)
                backStack.add(RecordingDestination)
            },
            secondaryAction = {
                // screen: measurement_instructions — opened from the StartRecord ("GO") screen.
                recordFlowTracker?.trackMeasurementInstructionsScreen(
                    activity,
                    RecordFlowAnalyticsTracker.PRIOR_SCREEN_MEASUREMENT_START,
                )
                showInstructionsSheet = true
            },
            onBackPress = {
                showExitConfirmationDialog = true
            },
        )

        // --- Static Balance screens (OS-15960) ---

        // Condition setup — start of each condition. On Continue, stores the condition and
        // advances to the next pre-record destination (StartRecord).
        conditionSetupScreen(
            balance = config.balance ?: OSTBalance(),
            onScreenView = {
                // screen: static_balance_condition_setup — condition_number is 1-based within
                // the session (completed count + 1); session_uuid groups the session.
                recordFlowTracker?.trackStaticBalanceConditionSetupScreen(
                    conditionNumber = viewModel.balanceConditionCount() + 1,
                    sessionUuid = viewModel.sessionUuid,
                )
            },
            onContinue = { condition ->
                // static_balance_condition_confirmed is fired from the duration screen that
                // follows, where the chosen recording length is known (OS-17175).
                viewModel.setBalanceCondition(condition)
                navigateToNext(ConditionSetupDestination)
            },
        )

        // Recording saved — shown after a condition uploads. "Record another" loops back to
        // Condition Setup (same session); "Go to summary" finishes with the web summary.
        recordingSavedScreen(
            conditionLine = { viewModel.currentBalanceCondition?.displayLine().orEmpty() },
            durationSeconds = {
                viewModel.motionMeasurement.value?.metadata?.seconds
                    ?: viewModel.configuration.value.duration
                    ?: 0
            },
            onRecordAnother = { note ->
                // static_balance_note_added — only the session uuid is sent, NEVER the
                // free-text note (HIPAA). static_balance_another_test carries condition_count.
                if (!note.isNullOrBlank()) {
                    recordFlowTracker?.trackStaticBalanceNoteAdded(viewModel.sessionUuid)
                }
                recordFlowTracker?.trackStaticBalanceConditionChoice(
                    recordAnother = true,
                    conditionCount = viewModel.balanceConditionCount(),
                    sessionUuid = viewModel.sessionUuid,
                )
                viewModel.updateBalanceConditionNote(note)
                viewModel.prepareForNextBalanceCondition()
                // Nav2 popped to the start destination (inclusive) and re-launched Condition
                // Setup as a fresh single-top entry; in Nav3 that is simply "reset the stack".
                backStack.clear()
                backStack.add(ConditionSetupDestination)
            },
            onGoToSummary = { note ->
                // static_balance_note_added — only the session uuid (never the note text).
                // static_balance_go_to_summary carries condition_count.
                if (!note.isNullOrBlank()) {
                    recordFlowTracker?.trackStaticBalanceNoteAdded(viewModel.sessionUuid)
                }
                recordFlowTracker?.trackStaticBalanceConditionChoice(
                    recordAnother = false,
                    conditionCount = viewModel.balanceConditionCount(),
                    sessionUuid = viewModel.sessionUuid,
                )
                viewModel.updateBalanceConditionNote(note)
                finishStaticBalance(
                    viewModel.motionMeasurement.value,
                    viewModel.sessionUuid,
                    onResult,
                    onFinished,
                    onDismiss,
                )
            },
            // The host's post-tagging decision. A blinded (research) workspace passes
            // `OSTPostTaggingData.None`, which drops the note field: the clinician there can never
            // see the note again, so the field was a dead end (OS-16914).
            showNote = { config.collectsPostRecordingNote() },
        )

        // Generic Recording notes (OS-16861) — shown once the raw recording is banked. Continue is
        // awaited so the note update lands before the flow finishes and the host tears it down.
        genericRecordingNotesScreen(
            durationSeconds = {
                viewModel.motionMeasurement.value?.metadata?.seconds
                    ?: viewModel.configuration.value.duration
                    ?: 0
            },
            onContinue = { note ->
                viewModel.updateGenericRecordingNote(note)
                val measurement = viewModel.motionMeasurement.value
                if (measurement != null) {
                    finishWithMeasurement(
                        measurement = measurement,
                        sessionUuid = null,
                        // Generic Recording has no hallway screen, so there is no length to report.
                        hallwayLengthMeters = null,
                        onResult = onResult,
                        onFinished = onFinished,
                        onDismiss = onDismiss,
                    )
                } else {
                    onDismiss()
                }
            },
        )

        // --- Recording and post-recording screens ---

        // Recording screen. RecordingScreenContent is purely presentational (stable data +
        // lambdas); all ViewModel interaction lives here in the destination entry.
        entry<RecordingDestination> {
            val screenData = viewModel.recodingScreenState.value
            val stepCount by viewModel.stepCount.collectAsStateWithLifecycle(0)

            // Set the recording lifecycle callbacks once when this destination enters the
            // composition — same lifecycle as the old RecordingScreenContent LaunchedEffect(Unit),
            // so the initState / onMeasurementResult / onError timing is unchanged.
            LaunchedEffect(Unit) {
                viewModel.initState()
                viewModel.onMeasurementResult = { measurement ->
                    resultMeasurement = measurement
                    // Route via ResultHandler exactly like the Android uikit flow:
                    // FULL_ANALYSIS -> summary; EMPTY/PARTIAL -> the specific analysis-error
                    // screen (or EmptyAnalysisWithSteps for walk/dual-task with steps);
                    // null measurement -> general error.
                    if (config.activityType == OSTActivityType.GENERIC_RECORDING) {
                        // Generic Recording is never analysed, so ResultHandler cannot route it:
                        // it dispatches on `resultState`, which only the analysis pipeline ever
                        // sets, and a banked recording has none — it would land on the general
                        // error screen. A stored recording IS the success case here, so go straight
                        // to the notes screen. This also has to take precedence over
                        // shouldSkipNativeSummary(), which would otherwise finish the flow
                        // immediately on `showSummaryScreen = None` and never collect the note.
                        // A null measurement means the upload failed; the VM has already reported
                        // that through onError, so there is nothing to route here.
                        if (measurement != null) {
                            backStack.popUpToInclusive(RecordingDestination)
                            backStack.add(GenericRecordingNotesDestination)
                        }
                    } else when (val outcome = ResultHandler.handleMeasurementResult(measurement)) {
                        is RecordFlowOutcome.Summary -> {
                            if (config.activityType == OSTActivityType.STATIC_BALANCE) {
                                // Session loop: a successful condition lands on the "Recording
                                // saved" screen instead of finishing the flow. Static Balance
                                // uses a web-only summary, so no native summary screen is shown.
                                viewModel.onBalanceConditionSaved(outcome.measurement)
                                backStack.popUpToInclusive(RecordingDestination)
                                backStack.add(RecordingSavedDestination)
                            } else if (shouldSkipNativeSummary(
                                    config.showSummaryScreen,
                                    outcome.measurement,
                                )
                            ) {
                                // None / WEB-with-a-url: the flow is over here. Finish with the
                                // measurement id + summaryUrl so the host opens the web summary,
                                // exactly like uikit's RecordFlowFragment.onMeasurementResult.
                                // WEB without a summaryUrl (server bug) falls through to the
                                // native summary rather than dead-ending the user.
                                finishWithMeasurement(
                                    measurement = outcome.measurement,
                                    sessionUuid = null,
                                    hallwayLengthMeters = viewModel.committedHallwayLengthMeters,
                                    onResult = onResult,
                                    onFinished = onFinished,
                                    onDismiss = onDismiss,
                                )
                            } else {
                                backStack.popUpToInclusive(RecordingDestination)
                                backStack.add(SummaryResultDestination)
                            }
                        }

                        is RecordFlowOutcome.EmptyAnalysisWithSteps -> {
                            backStack.popUpToInclusive(RecordingDestination)
                            backStack.add(EmptyAnalysisDestination)
                        }

                        is RecordFlowOutcome.Error -> {
                            resultError = outcome.error
                            backStack.popUpToInclusive(RecordingDestination)
                            backStack.add(ErrorResultDestination)
                        }
                    }
                }
                viewModel.onError = { error, activityType ->
                    // Technical analyser errors ("no result from the lab"). No live network
                    // probe exists in KMP yet, so networkStatus is assumed true — the
                    // connectivity screen still shows for OSTAnalyserError.NetworkError.
                    resultError = ResultHandler.onAnalyseError(
                        analyserError = error,
                        activityType = activityType,
                        networkStatus = true,
                    )
                    backStack.popUpToInclusive(RecordingDestination)
                    backStack.add(ErrorResultDestination)
                }
            }

            // Hide the toolbar once the flow leaves GET_READY (recording/analyzing), mirroring
            // the old RecordingScreenContent onRecording callback (keyed on the screen state).
            LaunchedEffect(screenData) {
                if (screenData.recordScreenStage != RecordingScreenData.RecordScreenStage.GET_READY) {
                    viewModel.showToolbar(false)
                }
            }

            RecordingScreenContent(
                modifier = Modifier.fillMaxSize(),
                screenData = screenData,
                subtitleOverride = viewModel.subtitle.value,
                stepCount = stepCount,
                timerValue = viewModel.timerValue.value,
                onStopped = { viewModel.stopRecording() },
                onBackPress = { showRecordingExitDialog = true },
            )
        }

        // Summary result
        entry<SummaryResultDestination> {
            val measurement = resultMeasurement
            if (measurement != null) {
                OSTMeasurementSummary(
                    measurement = measurement,
                    patientId = patientId,
                    options = OSTSummaryOptions.Full,
                    origin = OSTSummaryOrigin.Recording,
                    configuration = config,
                    // Dismissing the native summary still delivers the full result (id +
                    // summaryUrl) — uikit's SummaryFragment does the same, so a host can offer
                    // "open the web summary" after the in-app one.
                    onDismiss = {
                        finishWithMeasurement(
                            measurement = measurement,
                            sessionUuid = null,
                            hallwayLengthMeters = viewModel.committedHallwayLengthMeters,
                            onResult = onResult,
                            onFinished = onFinished,
                            onDismiss = onDismiss,
                        )
                    },
                )
            }
        }

        // Empty analysis result — the "steps measured" no-score screen reached from the
        // walk / dual-task empty-analysis path. Ports uikit's emptyAnalysisWithStepsScreen:
        // Continue -> onDone; no retry (uikit's screen has none).
        entry<EmptyAnalysisDestination> {
            EmptyAnalysisDestinationContent(
                resultMeasurement = resultMeasurement,
                activity = activity,
                recordFlowTracker = recordFlowTracker,
                onDismiss = onDismiss,
            )
        }

        // Error result — the specific screen is selected by [resultError] (set by ResultHandler).
        entry<ErrorResultDestination> {
            val error = resultError ?: RecordFlowError.General
            val errorMeasurement = viewModel.motionMeasurement.value
            // STS "Enter results manually" secondary action for the STS Short/Static/Position
            // error screens. Non-null only when the init-time flag is on AND this is an STS
            // error with a known measurement uuid. Fires Clicked: enter_results_manually
            // (screen_origin = error) then navigates to the manual-report destination.
            val onEnterStsResultsManually: (() -> Unit)? =
                if (featureFlags.isEnabled(FeatureFlag.STS_MANUAL_REPORT) &&
                    (error == RecordFlowError.StsShort ||
                        error == RecordFlowError.StaticSts ||
                        error == RecordFlowError.StsPosition) &&
                    errorMeasurement?.id != null
                ) {
                    {
                        recordFlowTracker?.trackEnterResultsManuallyClicked(
                            activity = activity,
                            screenOrigin = RecordFlowAnalyticsTracker.SCREEN_ORIGIN_ERROR,
                        )
                        backStack.add(
                            StsManualReportDestination(
                                uuid = errorMeasurement.id,
                                initialValue = null,
                            ),
                        )
                    }
                } else {
                    null
                }
            // Build the screen data once so the analytics title/subtitle strings exactly match
            // the localized strings shown on the error screen (uikit sends the same strings).
            val errorScreenData = RecordFlowDataFactory.errorScreenData(
                error = error,
                resourceProvider = resourceProvider,
                onRetry = {},
                onSecondaryAction = {},
            )

            // Publish this error's identity (canonical code + localized title) so the toolbar
            // exit_button can report error_code + error_title_string while the error screen is
            // shown; clear it on leave. Mirrors uikit's LocalRecordErrorSink / currentError.
            DisposableEffect(error, errorScreenData.title?.text) {
                currentErrorCode = RecordFlowAnalyticsTracker.canonicalErrorCode(error.errorType)
                currentErrorTitle = errorScreenData.title?.text
                onDispose {
                    currentErrorCode = null
                    currentErrorTitle = null
                }
            }

            // screen: measurement_error — emitted once when the error screen is shown. Sends
            // the canonical error_code (derived from error.errorType), localized title/subtitle
            // strings and the recorded measurement metadata (each omitted when null).
            LaunchedEffect(error) {
                recordFlowTracker?.trackErrorScreen(
                    activity = activity,
                    errorType = error.errorType,
                    measurementSeconds = errorMeasurement?.metadata?.seconds,
                    steps = errorMeasurement?.metadata?.steps,
                    perceptionUuid = errorMeasurement?.id,
                    titleString = errorScreenData.title?.text,
                    subtitleString = errorScreenData.subtitle?.text,
                    appSection = RecordFlowAnalyticsTracker.APP_SECTION_DEFAULT,
                )
            }
            ErrorScreen(
                onBackPress = {
                    // uikit fires the same try_again / error_continue event on the error screen's
                    // back-press as on its primary CTA (ErrorScreen.kt:78, ErrorTimeOut.kt:51).
                    // Timeout uses error_continue; every other error uses try_again.
                    when (error) {
                        RecordFlowError.Timeout ->
                            recordFlowTracker?.trackErrorContinueClicked(activity, error.errorType)

                        else ->
                            recordFlowTracker?.trackTryAgainClicked(
                                activity = activity,
                                errorType = error.errorType,
                                appSection = RecordFlowAnalyticsTracker.APP_SECTION_DEFAULT,
                                // uikit passes the per-error screen name (ERROR_TYPE), not a
                                // constant — each error destination sets screenName = ERROR_TYPE
                                // (e.g. "curvy"), which equals RecordFlowError.errorType here.
                                screenName = error.errorType,
                            )
                    }
                    onDismiss()
                },
                onSelection = {
                    // The primary CTA differs per error, matching uikit's per-screen event:
                    //  - Connectivity ("Reload")  -> Clicked: measurement_reload
                    //  - Timeout ("Continue")      -> Clicked: measurement_error_continue
                    //  - all other errors ("Try again") -> Clicked: measurement_try_again
                    when (error) {
                        RecordFlowError.Connectivity ->
                            recordFlowTracker?.trackReloadClicked(activity)

                        RecordFlowError.Timeout ->
                            recordFlowTracker?.trackErrorContinueClicked(activity, error.errorType)

                        else ->
                            recordFlowTracker?.trackTryAgainClicked(
                                activity = activity,
                                errorType = error.errorType,
                                appSection = RecordFlowAnalyticsTracker.APP_SECTION_DEFAULT,
                                // uikit passes the per-error screen name (ERROR_TYPE), not a
                                // constant — each error destination sets screenName = ERROR_TYPE
                                // (e.g. "curvy"), which equals RecordFlowError.errorType here.
                                screenName = error.errorType,
                            )
                    }
                    viewModel.clearJobs()
                    backStack.popUpToInclusive(ErrorResultDestination)
                    // Back to wherever this flow records from — the Start screen, or the recording
                    // itself for Generic Recording, which has none.
                    backStack.add(recordEntryDestination)
                },
                // Secondary CTA: "View instructions" opens the instructions sheet on analysis
                // errors; for the Static Balance short error it is "Finish" — resume to the
                // web summary if a prior condition completed this session, else exit.
                onSecondaryAction =
                    if (error == RecordFlowError.StaticBalanceShort) {
                        {
                            finishStaticBalance(
                                viewModel.lastSavedBalanceMeasurement,
                                viewModel.sessionUuid,
                                onResult,
                                onFinished,
                                onDismiss,
                            )
                        }
                    } else {
                        {
                            // screen: measurement_instructions — opened from an error screen.
                            recordFlowTracker?.trackMeasurementInstructionsScreen(
                                activity,
                                RecordFlowAnalyticsTracker.PRIOR_SCREEN_MEASUREMENT_ERROR,
                            )
                            showInstructionsSheet = true
                        }
                    },
                screenDataFactory = { retry, secondary ->
                    RecordFlowDataFactory.errorScreenData(
                        error = error,
                        resourceProvider = resourceProvider,
                        onRetry = retry,
                        onSecondaryAction = secondary,
                        onEnterResultsManually = onEnterStsResultsManually,
                    )
                },
            )
        }

        // STS manual self-report (entry from the STS error screens). Registered unconditionally;
        // it is only reachable when [onEnterStsResultsManually] navigated here (flag on).
        stsManualReportScreen(
            applyTopToolBarPadding = false,
            onSubmitted = { uuid ->
                // The user manually provided a value, so by definition there is a result. The
                // server-side resultState may still be EMPTY/PARTIAL (self-report does not update
                // it), which would route back to the same error screen — so bypass the state
                // check and surface the refreshed measurement directly to the summary.
                scope.launch {
                    // Re-read through the same (patient-scoped in clinician mode) recorder the flow
                    // recorded with, not the singleton, so the refreshed measurement resolves in scope.
                    val refreshed = bridges.recorderBridge.readSingleMotionMeasurement(uuid)
                    if (refreshed != null) {
                        resultMeasurement = refreshed
                        backStack.popUpToInclusive(ErrorResultDestination)
                        backStack.add(SummaryResultDestination)
                    } else {
                        // Override saved but local re-read failed — don't strand the user; exit
                        // the flow (the saved value syncs on next fetch).
                        onDismiss()
                    }
                }
            },
            onClose = { backStack.pop() },
            // Failure on the connectivity-error screen exits the recording flow rather than
            // dropping the user back on the STS error screen they came from.
            onExitOnFailure = onDismiss,
            // screen: measurement_enter_results_manually — entered from an STS error screen.
            onScreenView = {
                recordFlowTracker?.trackEnterResultsManuallyScreen(
                    activity = activity,
                    screenOrigin = RecordFlowAnalyticsTracker.SCREEN_ORIGIN_ERROR,
                )
            },
            // Clicked: enter_results_manually_save — HIPAA: only the entered count value.
            onSaveClicked = { value ->
                recordFlowTracker?.trackEnterResultsManuallySaveClicked(
                    activity = activity,
                    value = value.toString(),
                )
            },
        )
            }, // end entryProvider
        ) // end UIktNavDisplay

        // Toolbar overlays the reserved top inset (see note above). Drawn on top of the
        // NavDisplay's inset region — not overlapping the content — so toggling its visibility
        // never resizes the screen.
        RecordFlowToolbar(
            visible = showToolbar.value,
            toolbarData = toolbarData.value,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        // Short hallway warning dialog (shown when user enters a value below recommended)
        val hallwayState = viewModel.hallwayDistanceState.value
        if (hallwayState.showShortHallwayDialog) {
            HallwayWarningDialog(
                recommendedValue = hallwayState.recommendedValue,
                unitText = hallwayState.unitText,
                dontShowAgainChecked = hallwayState.suppressShortHallwayWarning,
                onShown = { recordFlowTracker?.trackShortHallwayPopupShown(activity) },
                onDismiss = viewModel::dismissShortHallwayDialog,
                onSuppressChange = viewModel::onSuppressShortHallwayWarningChanged,
                onStartTest = {
                    // clicked_short_hallway_start_test — proceed with the short length.
                    recordFlowTracker?.trackShortHallwayStartTestClicked(activity)
                    if (viewModel.onShortHallwayStartTest()) {
                        navigateToNext(HallwayDistanceDestination)
                    }
                },
                onEdit = {
                    // clicked_short_hallway_edit — dismiss to edit the length.
                    recordFlowTracker?.trackShortHallwayEditClicked(activity)
                    viewModel.dismissShortHallwayDialog()
                },
            )
        }

        // Exit confirmation dialog shown when user presses back on StartRecord screen
        if (showExitConfirmationDialog) {
            ExitConfirmationDialog(
                onDismissRequest = { showExitConfirmationDialog = false },
                onConfirm = {
                    viewModel.clearJobs()
                    onDismiss()
                },
            )
        }

        // Exit confirmation dialog shown when user presses back during active recording
        if (showRecordingExitDialog) {
            ExitConfirmationDialog(
                onDismissRequest = { showRecordingExitDialog = false },
                onConfirm = {
                    viewModel.clearJobs()
                    // Abandoning a recording steps back to the Start screen — except for Generic
                    // Recording, which starts *at* the recording: stepping "back" to it there would
                    // restart the very recording the clinician just chose to abandon, so the flow
                    // exits instead, exactly as backing out of a Start screen does.
                    if (recordEntryDestination == RecordingDestination) {
                        onDismiss()
                    } else {
                        backStack.popUpToInclusive(RecordingDestination)
                        backStack.add(recordEntryDestination)
                    }
                },
            )
        }
    } // end Box

    if (showInstructionsSheet) {
        val instructions = config.instructions ?: config.defaultInstructions()
        BottomSheet(
            // Open straight to full height — the instructions (media + list) need the space,
            // and the partially-expanded half-sheet clipped the content on iOS.
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onDismissRequest = { showInstructionsSheet = false },
            testTag = OSTTestTags.RecordFlow.INSTRUCTIONS_SHEET,
        ) {
            InstructionsContent(instructionsData = instructions)
        }
    }
}

@Composable
private fun RecordFlowToolbar(
    visible: Boolean,
    toolbarData: ToolBarData,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut(),
    ) {
        Toolbar(
            toolbarData = toolbarData,
            toolBarColor = ToolBarColors(
                containerColor = Color.Transparent,
                contentColor = LocalOSColors.current.neutral_p3,
            ),
        )
    }
}

@Composable
private fun HallwayWarningDialog(
    recommendedValue: Int,
    unitText: String,
    dontShowAgainChecked: Boolean,
    onShown: () -> Unit,
    onDismiss: () -> Unit,
    onSuppressChange: (Boolean) -> Unit,
    onStartTest: () -> Unit,
    onEdit: () -> Unit,
) {
    LaunchedEffect(Unit) { onShown() }
    OSPopup(
        // A dialog composes in its own window, so the tag goes on the popup itself.
        modifier = Modifier.test(OSTTestTags.RecordFlow.HALLWAY_WARNING_DIALOG),
        onDismissRequest = onDismiss,
        title = stringResource(Res.string.short_hallway_length_title),
        description = stringResource(
            Res.string.short_hallway_length_message,
            recommendedValue,
            unitText,
        ),
        closeIcon = vectorResource(Res.drawable.ic_close),
        // Start Test proceeds with the short length (secondary/outline per design).
        confirmButtonText = stringResource(Res.string.short_hallway_start_test),
        confirmButtonVariant = ButtonVariant.Secondary,
        onConfirm = onStartTest,
        // Edit Hallway Length is the primary (filled) action.
        cancelButtonText = stringResource(Res.string.short_hallway_edit_hallway_length),
        cancelButtonVariant = ButtonVariant.Primary,
        onCancel = onEdit,
        checkboxText = stringResource(Res.string.short_hallway_dont_show_again),
        checkboxChecked = dontShowAgainChecked,
        onCheckboxCheckedChange = onSuppressChange,
    )
}

@Composable
private fun ExitConfirmationDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
) {
    OSPopup(
        modifier = Modifier.test(OSTTestTags.RecordFlow.EXIT_DIALOG),
        onDismissRequest = onDismissRequest,
        title = stringResource(Res.string.stop_recording_dialog_text),
        confirmButtonText = stringResource(Res.string.yes),
        confirmButtonVariant = ButtonVariant.Primary,
        onConfirm = {
            onDismissRequest()
            onConfirm()
        },
        cancelButtonText = stringResource(Res.string.no),
        cancelButtonVariant = ButtonVariant.Secondary,
        onCancel = onDismissRequest,
    )
}

@Composable
private fun EmptyAnalysisDestinationContent(
    resultMeasurement: OSTMotionMeasurement?,
    activity: OSTActivityType,
    recordFlowTracker: RecordFlowAnalyticsTracker?,
    onDismiss: () -> Unit,
) {
    val steps = resultMeasurement?.metadata?.steps
    LaunchedEffect(Unit) {
        // This manual-entry screen is reached from the no-score / error path.
        recordFlowTracker?.trackEnterResultsManuallyScreen(
            activity = activity,
            screenOrigin = RecordFlowAnalyticsTracker.SCREEN_ORIGIN_ERROR,
        )
    }
    EmptyAnalysisScreen(
        screenData = EmptyAnalysisScreenData(
            timeStampMillis = resultMeasurement?.timestamp ?: currentTimeMillis(),
            title = stringResource(
                Res.string.steps_measured,
                steps ?: stringResource(Res.string.no),
            ),
            steps = steps,
            icon = IconData(
                icon = Res.drawable.ic_warning,
                tintColor = LocalOSColors.current.error_p2,
            ),
            subtitle = stringResource(Res.string.great_job_on_completing_a_walk),
            brandButtonData = PrimaryButtonData(
                text = TextData(
                    stringResource(Res.string.continue_camel_case),
                    24.sp,
                    FontWeight.W600,
                ),
                action = {
                    recordFlowTracker?.trackEnterResultsManuallySaveClicked(
                        activity = activity,
                        value = steps?.toString() ?: "",
                    )
                    onDismiss()
                },
            ),
        ),
    )
}

/** Spec-canonical hallway unit token for analytics: "ft" (imperial) or "m" (metric). */
private fun hallwayUnit(isImperial: Boolean): String =
    if (isImperial) RecordFlowAnalyticsEvents.Units.FEET else RecordFlowAnalyticsEvents.Units.METERS

private fun adjustToolBar(
    key: NavKey?,
    viewModel: MotionRecorderViewModel,
    config: OSTRecordingConfiguration,
) {
    if (key == null) return
    when (key) {
        // Get Ready (countdown) keeps a transparent toolbar (chevron + close) per design. The
        // toolbar is hidden later, once recording actually starts, via
        // RecordingScreenContent.onRecording -> showToolbar(false).
        RecordingDestination -> {
            viewModel.showToolbar(true)
            viewModel.setToolBarTitle(null)
            viewModel.showBackButton(true)
        }

        StartRecordDestination -> {
            viewModel.showToolbar(true)
            viewModel.setToolBarTitle(config.activityType.displayNameRes)
            viewModel.showBackButton(true)
        }

        ConditionSetupDestination -> {
            viewModel.showToolbar(true)
            viewModel.setToolBarTitle(config.activityType.displayNameRes)
            viewModel.showBackButton(true)
        }

        // No toolbar on the "Recording saved" screen — its own "Go to summary" /
        // "Record another test" buttons are the only actions. Same for the Generic Recording
        // notes screen, whose only action is Continue.
        RecordingSavedDestination,
        GenericRecordingNotesDestination -> {
            viewModel.showToolbar(false)
        }

        SummaryResultDestination -> {
            viewModel.showToolbar(false)
        }

        ErrorResultDestination,
        EmptyAnalysisDestination -> {
            viewModel.showToolbar(true)
            viewModel.setToolBarTitle(null)
            viewModel.showBackButton(false)
        }

        else -> {
            viewModel.showToolbar(true)
            viewModel.setToolBarTitle(null)
            viewModel.showBackButton(true)
        }
    }
}

/**
 * True when the analyzed measurement should bypass the native summary screen and finish the
 * flow immediately, handing the result to the host — uikit's `webShortCircuit` rule:
 *
 *  - [OSTSummaryOptions.None]: never show a summary.
 *  - [OSTSummaryOptions.WEB]: show no summary *provided* the server returned a `summaryUrl`;
 *    without one there is nothing for the host to open, so fall back to the native summary.
 */
private fun shouldSkipNativeSummary(
    option: OSTSummaryOptions,
    measurement: OSTMotionMeasurement,
): Boolean = option == OSTSummaryOptions.None ||
    (option == OSTSummaryOptions.WEB && !measurement.summaryUrl.isNullOrEmpty())

/**
 * Single terminal exit for a flow that produced a measurement.
 *
 * Emits the `recording_completed` [OSTEvent] (PHI-free: ids only — the summary link never goes
 * into the analytics stream), then hands the host the typed [OSTRecordingFlowResult] carrying
 * [OSTMotionMeasurement.summaryUrl] so it can open the web summary, then dismisses.
 */
private fun finishWithMeasurement(
    measurement: OSTMotionMeasurement,
    sessionUuid: String?,
    hallwayLengthMeters: Float?,
    onResult: (OSTEvent) -> Unit,
    onFinished: (OSTRecordingFlowResult) -> Unit,
    onDismiss: () -> Unit,
) {
    onResult(
        OSTEvent(
            name = "recording_completed",
            properties = buildMap {
                put("measurement_id", measurement.id)
                sessionUuid?.let { put("session_uuid", it) }
            },
        ),
    )
    onFinished(
        OSTRecordingFlowResult(
            measurementId = measurement.id,
            summaryUrl = measurement.summaryUrl,
            sessionUuid = sessionUuid,
            hallwayLengthMeters = hallwayLengthMeters,
        ),
    )
    onDismiss()
}

/**
 * Finishes the Static Balance flow with the web summary (OS-15960). Static Balance has no native
 * summary: the flow finishes with the last completed condition's measurement id, its
 * [OSTMotionMeasurement.summaryUrl] and the session grouping key, so the host app opens the web
 * summary showing every condition of the session.
 *
 * When no condition completed this session (no [measurement]), it just dismisses — matching
 * uikit's fallback of exiting cleanly rather than looping on an error screen.
 */
private fun finishStaticBalance(
    measurement: OSTMotionMeasurement?,
    sessionUuid: String,
    onResult: (OSTEvent) -> Unit,
    onFinished: (OSTRecordingFlowResult) -> Unit,
    onDismiss: () -> Unit,
) {
    if (measurement != null) {
        finishWithMeasurement(
            measurement = measurement,
            sessionUuid = sessionUuid,
            // Static Balance has no hallway screen, so there is no length to report.
            hallwayLengthMeters = null,
            onResult = onResult,
            onFinished = onFinished,
            onDismiss = onDismiss,
        )
    } else {
        onDismiss()
    }
}
