package co.onestep.kmp.uikit

import androidx.compose.ui.window.ComposeUIViewController
import co.onestep.kmp.uikit.bridge.FeatureFlagsBridge
import co.onestep.kmp.uikit.bridge.InsightsBridge
import co.onestep.kmp.uikit.bridge.MotionDataBridge
import co.onestep.kmp.uikit.bridge.OSTSDKBridge
import co.onestep.kmp.uikit.bridge.PlatformAudioPlayer
import co.onestep.kmp.uikit.bridge.PlatformPermissionsManager
import co.onestep.kmp.uikit.bridge.PlatformTTSPlayer
import co.onestep.kmp.uikit.bridge.PreferencesBridge
import co.onestep.kmp.uikit.bridge.RecorderBridge
import co.onestep.kmp.uikit.di.UIKitServiceLocator
import co.onestep.kmp.uikit.features.carlog.OSTCareLog
import co.onestep.kmp.uikit.features.permissions.IosNativePermissionFlowRegistry
import co.onestep.kmp.uikit.features.permissions.IosNativePermissionFlowViewControllerFactory
import co.onestep.kmp.uikit.features.permissions.OSTPermissionFlow
import co.onestep.kmp.uikit.features.permissions.OSTPermissionMode
import co.onestep.kmp.uikit.features.permissions.ios.IosPermissionChecker
import co.onestep.kmp.uikit.features.permissions.ios.screens.IosMicrophonePermissionScreen
import co.onestep.kmp.uikit.features.recordFlow.OSTRecordingFlow
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTRecordingConfiguration
import co.onestep.kmp.uikit.features.summary.OSTMeasurementSummary
import co.onestep.kmp.uikit.features.summary.models.OSTSummaryOptions
import co.onestep.kmp.uikit.models.OSTEvent
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
        onResult: (OSTEvent) -> Unit,
    ): UIViewController {
        checkConfigured()
        return ComposeUIViewController {
            OSTRecordingFlow(config = config, onResult = onResult)
        }.apply {
            overrideUserInterfaceStyle = UIUserInterfaceStyle.UIUserInterfaceStyleLight
            view.backgroundColor = UIColor.whiteColor
        }
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
        onResult: (OSTEvent) -> Unit,
        onDismiss: () -> Unit,
    ): UIViewController {
        checkConfigured()
        return ComposeUIViewController {
            OSTRecordingFlow(config = config, onResult = onResult, onDismiss = onDismiss)
        }.apply {
            overrideUserInterfaceStyle = UIUserInterfaceStyle.UIUserInterfaceStyleLight
            view.backgroundColor = UIColor.whiteColor
        }
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
        }.apply {
            overrideUserInterfaceStyle = UIUserInterfaceStyle.UIUserInterfaceStyleLight
            view.backgroundColor = UIColor.whiteColor
        }
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
        }.apply {
            overrideUserInterfaceStyle = UIUserInterfaceStyle.UIUserInterfaceStyleLight
            view.backgroundColor = UIColor.whiteColor
        }
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
        }.apply {
            overrideUserInterfaceStyle = UIUserInterfaceStyle.UIUserInterfaceStyleLight
            view.backgroundColor = UIColor.whiteColor
        }
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
        }.apply {
            overrideUserInterfaceStyle = UIUserInterfaceStyle.UIUserInterfaceStyleLight
            view.backgroundColor = UIColor.whiteColor
        }
    }

    private fun checkConfigured() {
        check(UIKitServiceLocator.isConfigured) {
            "OSTUIKitIos.configure() must be called before using any factory methods."
        }
    }
}
