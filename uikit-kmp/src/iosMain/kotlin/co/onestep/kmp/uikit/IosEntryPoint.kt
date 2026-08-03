package co.onestep.kmp.uikit

import androidx.compose.ui.window.ComposeUIViewController
import co.onestep.kmp.uikit.bridge.FeatureFlagsBridge
import co.onestep.kmp.uikit.bridge.InsightsBridge
import co.onestep.kmp.uikit.bridge.MotionDataBridge
import co.onestep.kmp.uikit.bridge.OSTSDKBridge
import co.onestep.kmp.uikit.bridge.PatientScopedBridgesFactory
import co.onestep.kmp.uikit.bridge.PlatformAudioPlayer
import co.onestep.kmp.uikit.bridge.PlatformPermissionsManager
import co.onestep.kmp.uikit.bridge.PlatformTTSPlayer
import co.onestep.kmp.uikit.bridge.PreferencesBridge
import co.onestep.kmp.uikit.bridge.RecorderBridge
import co.onestep.kmp.uikit.di.UIKitServiceLocator
import co.onestep.kmp.uikit.features.carlog.OSTCareLog
import co.onestep.kmp.uikit.features.demo.OSTPushPopDemo
import co.onestep.kmp.uikit.features.permissions.IosNativePermissionFlowRegistry
import co.onestep.kmp.uikit.features.permissions.IosNativePermissionFlowViewControllerFactory
import co.onestep.kmp.uikit.features.permissions.OSTPermissionFlow
import co.onestep.kmp.uikit.features.permissions.OSTPermissionMode
import co.onestep.kmp.uikit.features.permissions.ios.IosPermissionChecker
import co.onestep.kmp.uikit.features.permissions.ios.screens.IosMicrophonePermissionScreen
import co.onestep.kmp.uikit.features.recordFlow.OSTRecordingFlow
import co.onestep.kmp.uikit.features.recordFlow.OSTRecordingFlowResult
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTRecordingConfiguration
import co.onestep.kmp.uikit.features.summary.OSTMeasurementSummary
import co.onestep.kmp.uikit.features.summary.models.OSTSummaryOptions
import co.onestep.kmp.uikit.features.web.OSTWebScreen
import co.onestep.kmp.sdk.OSTEvent
import co.onestep.kmp.uikit.models.OSTMotionMeasurement
import co.onestep.kmp.uikit.utils.ResourceProvider
import platform.UIKit.UIColor
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIViewController

/**
 * iOS entry points for the OneStep UIKit library.
 *
 * ## Setup
 * Call [configure] once during app startup before using any factory methods:
 * ```swift
 * OSTUIKitIos.shared.configure(
 *     sdkBridge: mySDKBridge,
 *     recorderBridge: myRecorderBridge,
 *     motionDataBridge: myMotionDataBridge,
 *     insightsBridge: myInsightsBridge,
 *     preferencesBridge: myPreferencesBridge,
 *     featureFlagsBridge: myFeatureFlagsBridge
 * )
 * ```
 *
 * ## Usage
 * ```swift
 * let vc = OSTUIKitIos.shared.createRecordingFlowViewController(
 *     config: config,
 *     onResult: { event in ... }
 * )
 * present(vc, animated: true)
 * ```
 */
object OSTUIKitIos {

    /**
     * Configure all UIKit dependencies. Must be called once before using any factory methods.
     *
     * @param sdkBridge Bridge to the OneStep SDK state/initialization.
     * @param recorderBridge Bridge to the OneStep recorder for recording sessions.
     * @param motionDataBridge Bridge to motion data access (historical measurements).
     * @param insightsBridge Bridge to insights data access.
     * @param preferencesBridge Bridge to user preferences storage.
     * @param featureFlagsBridge Bridge to feature flags.
     * @param audioPlayer Platform audio player (defaults to AVAudioPlayer-based implementation).
     * @param ttsPlayer Platform TTS player (defaults to AVSpeechSynthesizer-based implementation).
     * @param permissionsManager Platform permissions manager (defaults to CoreMotion/Notifications implementation).
     * @param resourceProvider Resource provider for string resolution (defaults to CMP resource resolution).
     */
    fun configure(
        sdkBridge: OSTSDKBridge,
        recorderBridge: RecorderBridge,
        motionDataBridge: MotionDataBridge,
        insightsBridge: InsightsBridge,
        preferencesBridge: PreferencesBridge,
        featureFlagsBridge: FeatureFlagsBridge,
        audioPlayer: PlatformAudioPlayer = PlatformAudioPlayer(),
        ttsPlayer: PlatformTTSPlayer = PlatformTTSPlayer(),
        permissionsManager: PlatformPermissionsManager = PlatformPermissionsManager(),
        resourceProvider: ResourceProvider = ResourceProvider(),
        analyticsHandler: OSTUIKitAnalyticsHandler? = null,
        patientScopedBridgesFactory: PatientScopedBridgesFactory? = null,
    ) {
        UIKitServiceLocator.configure(
            sdkBridge = sdkBridge,
            recorderBridge = recorderBridge,
            motionDataBridge = motionDataBridge,
            insightsBridge = insightsBridge,
            preferencesBridge = preferencesBridge,
            featureFlagsBridge = featureFlagsBridge,
            audioPlayer = audioPlayer,
            ttsPlayer = ttsPlayer,
            permissionsManager = permissionsManager,
            resourceProvider = resourceProvider,
            analyticsHandler = analyticsHandler,
            patientScopedBridgesFactory = patientScopedBridgesFactory,
        )
    }

    /** Whether the UIKit has been configured. */
    val isConfigured: Boolean
        get() = UIKitServiceLocator.isConfigured

    /**
     * Register a host-provided native permission-flow factory.
     *
     * Once registered, [createPermissionFlowViewController] and any in-flow permission gate present
     * the factory's native `UIViewController` (e.g. the real Swift `OSTPermissionsFlow`) modally
     * instead of the built-in Compose fallback. Call once during startup.
     *
     * @param factory Creates the native permission-flow view controller for a given mode.
     */
    fun registerNativePermissionFlowFactory(factory: IosNativePermissionFlowViewControllerFactory) {
        IosNativePermissionFlowRegistry.factory = factory
    }

    /**
     * Remove any previously registered native permission-flow factory, reverting to the built-in
     * Compose permission flow.
     */
    fun unregisterNativePermissionFlowFactory() {
        IosNativePermissionFlowRegistry.factory = null
    }

    /**
     * Create a UIViewController for the recording flow.
     *
     * @param config Recording configuration (activity type, duration, post-tagging, etc.).
     * @param onResult Callback invoked when the recording flow produces an event.
     */
    fun createRecordingFlowViewController(
        config: OSTRecordingConfiguration,
        patientId: String? = null,
        onResult: (OSTEvent) -> Unit,
    ): UIViewController {
        checkConfigured()
        return ComposeUIViewController {
            OSTRecordingFlow(config = config, patientId = patientId, onResult = onResult)
        }.applyDefaultStyle()
    }

    /**
     * Create a UIViewController for the recording flow with dismiss callback.
     *
     * @param config Recording configuration (activity type, duration, post-tagging, etc.).
     * @param onResult Callback invoked when the recording flow produces an event.
     * @param onDismiss Callback invoked when the user dismisses the flow (e.g. X button on permissions).
     */
    fun createRecordingFlowViewController(
        config: OSTRecordingConfiguration,
        patientId: String? = null,
        onResult: (OSTEvent) -> Unit,
        onDismiss: () -> Unit,
    ): UIViewController {
        checkConfigured()
        return ComposeUIViewController {
            OSTRecordingFlow(config = config, patientId = patientId, onResult = onResult, onDismiss = onDismiss)
        }.applyDefaultStyle()
    }

    /**
     * Create a UIViewController for the recording flow that delivers the typed terminal result.
     *
     * This is the iOS counterpart of the Swift uikit's `onDismissResult`: [onFinished] fires with
     * the measurement id and the `summaryUrl` to open in a web view, immediately before
     * [onDismiss]. Prefer this overload over the event-only ones when the host presents the web
     * summary. The summary URL is never routed through [onResult] (HIPAA).
     *
     * @param config Recording configuration (activity type, duration, post-tagging, etc.).
     * @param onResult Callback invoked when the recording flow produces an event.
     * @param onFinished Callback invoked with the terminal [OSTRecordingFlowResult]. Fires only
     *        when the flow produced an analyzed measurement.
     * @param onDismiss Callback invoked when the flow should be dismissed.
     */
    fun createRecordingFlowViewController(
        config: OSTRecordingConfiguration,
        patientId: String? = null,
        onResult: (OSTEvent) -> Unit,
        onFinished: (OSTRecordingFlowResult) -> Unit,
        onDismiss: () -> Unit,
    ): UIViewController {
        checkConfigured()
        return ComposeUIViewController {
            OSTRecordingFlow(
                config = config,
                patientId = patientId,
                onResult = onResult,
                onFinished = onFinished,
                onDismiss = onDismiss,
            )
        }.applyDefaultStyle()
    }

    /**
     * Create a UIViewController for the permission flow.
     *
     * @param mode The permission mode (defaults to IN_APP for backward compatibility).
     * @param onComplete Callback invoked when permissions flow completes.
     */
    fun createPermissionFlowViewController(
        mode: OSTPermissionMode = OSTPermissionMode.IN_APP,
        onComplete: (Boolean) -> Unit,
    ): UIViewController {
        checkConfigured()
        return ComposeUIViewController {
            OSTPermissionFlow(mode = mode, onComplete = onComplete)
        }.applyDefaultStyle()
    }

    /**
     * Create a UIViewController for the standalone microphone permission screen.
     *
     * @param onContinue Callback when microphone permission is granted.
     * @param onSkip Callback when the user skips the permission.
     */
    fun createMicrophonePermissionViewController(
        onContinue: () -> Unit,
        onSkip: () -> Unit,
    ): UIViewController {
        checkConfigured()
        return ComposeUIViewController {
            IosMicrophonePermissionScreen(
                checker = IosPermissionChecker(),
                onContinue = onContinue,
                onSkip = onSkip,
            )
        }.applyDefaultStyle()
    }

    /**
     * Create a UIViewController for the measurement summary screen.
     *
     * @param measurement The measurement to display.
     * @param options Summary display options.
     * @param onDismiss Callback when the summary is dismissed.
     */
    fun createMeasurementSummaryViewController(
        measurement: OSTMotionMeasurement,
        options: OSTSummaryOptions = OSTSummaryOptions.Full,
        onDismiss: () -> Unit = {},
    ): UIViewController {
        checkConfigured()
        return ComposeUIViewController {
            OSTMeasurementSummary(
                measurement = measurement,
                options = options,
                onDismiss = onDismiss,
            )
        }.applyDefaultStyle()
    }

    /**
     * Create a UIViewController for the care log screen.
     *
     * @param onClose Callback when the care log is dismissed.
     */
    fun createCareLogViewController(
        onClose: () -> Unit = {},
    ): UIViewController {
        checkConfigured()
        return ComposeUIViewController {
            OSTCareLog(onClose = onClose)
        }.applyDefaultStyle()
    }

    /**
     * Create a UIViewController hosting a OneStep web mini-app (the web summary, a questionnaire,
     * terms of service) full-screen.
     *
     * The page authenticates with the `auth_token` cookie on `.onestep.co`. On iOS the SDK's
     * `NSHTTPCookieStorage.shared` and the web view's own store are separate, so uikit copies the
     * cookies that apply to [url] across before the first request — a host only has to make sure the
     * cookie is planted (which it already is, for the SDK's own calls to work).
     *
     * ```swift
     * let vc = OSTUIKitIos.shared.createWebViewController(
     *     url: OSTSummaryUrlKt.enhanceOSTSummaryUrl(
     *         url: summaryUrl, origin: "ca_carelog", language: "en", unitSystem: nil, overlayClose: true
     *     ),
     *     onClose: { self.dismiss(animated: true) }
     * )
     * ```
     *
     * @param url an `https` URL; anything else renders the error state.
     * @param onClose invoked by the close button and by a back gesture with no page history left.
     * @param showCloseButton set `false` when the presenting Swift code draws its own chrome.
     */
    fun createWebViewController(
        url: String,
        onClose: () -> Unit = {},
        showCloseButton: Boolean = true,
    ): UIViewController {
        // No checkConfigured(): a web page needs no SDK bridge, only the auth cookie, so this is
        // usable before (or without) configure().
        return ComposeUIViewController {
            OSTWebScreen(
                url = url,
                onClose = onClose,
                showCloseButton = showCloseButton,
            )
        }.applyDefaultStyle()
    }

    /**
     * Creates a view controller demoing the Cupertino push/pop transition and the interactive
     * edge-swipe back gesture (test harnesses only — not consumer API).
     *
     * @param onDismiss Callback when back is invoked on the demo's root screen.
     */
    fun createPushPopDemoViewController(
        onDismiss: () -> Unit = {},
    ): UIViewController {
        // No checkConfigured(): the demo exercises navigation/transitions only, no SDK bridges.
        return ComposeUIViewController {
            OSTPushPopDemo(onDismiss = onDismiss)
        }.applyDefaultStyle()
    }

    private fun checkConfigured() {
        check(UIKitServiceLocator.isConfigured) {
            "OSTUIKitIos.configure() must be called before using any factory methods."
        }
    }
}

/** Applies the standard light-mode white-background style used by all UIKit view controllers. */
private fun UIViewController.applyDefaultStyle(): UIViewController = apply {
    overrideUserInterfaceStyle = UIUserInterfaceStyle.UIUserInterfaceStyleLight
    view.backgroundColor = UIColor.whiteColor
}
