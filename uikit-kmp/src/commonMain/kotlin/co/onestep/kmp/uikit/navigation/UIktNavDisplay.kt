package co.onestep.kmp.uikit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import co.onestep.kmp.uikit.features.recordFlow.destinations.CustomTagsDestination
import co.onestep.kmp.uikit.features.recordFlow.destinations.HallwayDistanceDestination
import co.onestep.kmp.uikit.features.recordFlow.destinations.NoSummaryNoticeDestination
import co.onestep.kmp.uikit.features.recordFlow.destinations.PreAssistiveDeviceDestination
import co.onestep.kmp.uikit.features.recordFlow.destinations.PreFootwearDestination
import co.onestep.kmp.uikit.features.recordFlow.destinations.SelectWalkDurationDestination
import co.onestep.kmp.uikit.features.recordFlow.destinations.SoundInstructionsDestination
import co.onestep.kmp.uikit.features.recordFlow.destinations.SoundPermissionDeniedAlwaysDestination
import co.onestep.kmp.uikit.features.recordFlow.destinations.SoundPermissionDestination
import co.onestep.kmp.uikit.features.recordFlow.destinations.StartRecordDestination
import co.onestep.kmp.uikit.features.recordFlow.screens.EmptyAnalysisDestination
import co.onestep.kmp.uikit.features.recordFlow.screens.ErrorResultDestination
import co.onestep.kmp.uikit.features.recordFlow.screens.RecordingDestination
import co.onestep.kmp.uikit.features.recordFlow.screens.SummaryResultDestination
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.genericRecording.GenericRecordingNotesDestination
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.staticBalance.ConditionSetupDestination
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.staticBalance.RecordingSavedDestination
import co.onestep.kmp.uikit.features.summary.screens.navigation.CustomQuestionDestination
import co.onestep.kmp.uikit.features.summary.screens.navigation.EditAssistiveDeviceDestination
import co.onestep.kmp.uikit.features.summary.screens.navigation.EditFootwearDestination
import co.onestep.kmp.uikit.features.summary.screens.navigation.EditLevelOfAssistanceDestination
import co.onestep.kmp.uikit.features.summary.screens.navigation.StsManualReportDestination
import co.onestep.kmp.uikit.features.demo.PushPopDemoFirstDestination
import co.onestep.kmp.uikit.features.demo.PushPopDemoSecondDestination
import co.onestep.kmp.uikit.features.demo.PushPopDemoThirdDestination
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.kmp.uikit.features.summary.screens.navigation.SummaryScreenDestination
import co.onestep.kmp.uikit.features.summary.screens.navigation.TaggingScreenDestination
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * SavedState configuration for [androidx.navigation3.runtime.rememberNavBackStack] shared by all
 * uikit flows. Non-JVM targets (iOS) cannot use reflection-based polymorphic serialization, so
 * every [co.onestep.kmp.uikit.utils.UIktDestination] must be registered here explicitly.
 * The destinations live in different packages, so a sealed hierarchy (with automatic subclass
 * registration) is not an option.
 */
internal val UIktNavSavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            // Record flow — pre-recording
            subclass(HallwayDistanceDestination::class)
            subclass(SelectWalkDurationDestination::class)
            subclass(PreAssistiveDeviceDestination::class)
            subclass(PreFootwearDestination::class)
            subclass(CustomTagsDestination::class)
            subclass(SoundPermissionDestination::class)
            subclass(SoundPermissionDeniedAlwaysDestination::class)
            subclass(SoundInstructionsDestination::class)
            subclass(StartRecordDestination::class)
            subclass(NoSummaryNoticeDestination::class)
            // Record flow — static balance
            subclass(ConditionSetupDestination::class)
            subclass(RecordingSavedDestination::class)
            // Record flow — generic recording
            subclass(GenericRecordingNotesDestination::class)
            // Record flow — recording + results
            subclass(RecordingDestination::class)
            subclass(SummaryResultDestination::class)
            subclass(ErrorResultDestination::class)
            subclass(EmptyAnalysisDestination::class)
            // Summary flow
            subclass(SummaryScreenDestination::class)
            subclass(TaggingScreenDestination::class)
            subclass(EditAssistiveDeviceDestination::class)
            subclass(EditLevelOfAssistanceDestination::class)
            subclass(EditFootwearDestination::class)
            subclass(CustomQuestionDestination::class)
            subclass(StsManualReportDestination::class)
            // Cupertino transition demo (test harnesses)
            subclass(PushPopDemoFirstDestination::class)
            subclass(PushPopDemoSecondDestination::class)
            subclass(PushPopDemoThirdDestination::class)
        }
    }
}

/**
 * Pops the top entry unless it is the root (a [NavDisplay] back stack must never be empty).
 *
 * @return `true` if an entry was popped, `false` when already at the root — mirroring
 * Navigation 2's `popBackStack()` return value so call sites can fall back to dismissing.
 */
internal fun NavBackStack<NavKey>.pop(): Boolean =
    if (size > 1) {
        removeAt(size - 1)
        true
    } else {
        false
    }

/**
 * Pops entries from the top until [key] (inclusive) has been removed — the Navigation 3
 * equivalent of `popUpTo(key) { inclusive = true }`. No-op when [key] is not on the stack.
 */
internal fun NavBackStack<NavKey>.popUpToInclusive(key: NavKey) {
    val index = lastIndexOf(key)
    if (index < 0) return
    while (size > index) {
        removeAt(size - 1)
    }
}

/**
 * Shared [CupertinoNavDisplay] wrapper for all uikit flows. Only adds the uikit default backdrop:
 * uikit screens are mostly transparent and rely on a backdrop painted by the flow *outside* the
 * NavDisplay — fine when both screens moved in lockstep, but the Cupertino parallax shows the
 * underlying screen through anything transparent, so each entry must be opaque on its own.
 * Defaults to the design-system screen backdrop (what the flows already paint); pass
 * [Color.Unspecified] to opt out for fully-opaque screens.
 */
@Composable
internal fun UIktNavDisplay(
    backStack: NavBackStack<NavKey>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    interactiveBackGesture: Boolean = !platformProvidesBackGesture,
    screenBackground: Color = LocalOSColors.current.neutral_m5,
    entryProvider: (NavKey) -> NavEntry<NavKey>,
) {
    CupertinoNavDisplay(
        backStack = backStack,
        onBack = onBack,
        modifier = modifier,
        interactiveBackGesture = interactiveBackGesture,
        screenBackground = screenBackground,
        entryProvider = entryProvider,
    )
}
