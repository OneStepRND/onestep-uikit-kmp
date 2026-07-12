package co.onestep.kmp.uikit.di

import co.onestep.kmp.uikit.OSTUIKitAnalyticsHandler
import co.onestep.kmp.uikit.features.recordFlow.analytics.RecordFlowAnalyticsTracker
import co.onestep.kmp.uikit.features.summary.analytics.SummaryAnalyticsTracker
import co.onestep.kmp.uikit.bridge.FeatureFlagsBridge
import co.onestep.kmp.uikit.bridge.InsightsBridge
import co.onestep.kmp.uikit.bridge.MotionDataBridge
import co.onestep.kmp.uikit.bridge.OSTSDKBridge
import co.onestep.kmp.uikit.bridge.PatientScopedBridges
import co.onestep.kmp.uikit.bridge.PatientScopedBridgesFactory
import co.onestep.kmp.uikit.bridge.PlatformAudioPlayer
import co.onestep.kmp.uikit.bridge.PlatformPermissionsManager
import co.onestep.kmp.uikit.bridge.PlatformTTSPlayer
import co.onestep.kmp.uikit.bridge.PreferencesBridge
import co.onestep.kmp.uikit.bridge.RecorderBridge
import co.onestep.kmp.uikit.utils.ResourceProvider

/**
 * Multiplatform service locator for UIKit dependencies.
 * Platform-specific implementations are provided via [configure].
 */
object UIKitServiceLocator {

    private var _sdkBridge: OSTSDKBridge? = null
    private var _recorderBridge: RecorderBridge? = null
    private var _motionDataBridge: MotionDataBridge? = null
    private var _insightsBridge: InsightsBridge? = null
    private var _preferencesBridge: PreferencesBridge? = null
    private var _featureFlagsBridge: FeatureFlagsBridge? = null
    private var _audioPlayer: PlatformAudioPlayer? = null
    private var _ttsPlayer: PlatformTTSPlayer? = null
    private var _permissionsManager: PlatformPermissionsManager? = null
    private var _resourceProvider: ResourceProvider? = null
    private var _analyticsHandler: OSTUIKitAnalyticsHandler? = null

    // App-lifetime clinician-mode config: the *factory*, not a scope. Null in single-patient hosts.
    // The per-launch patient scope is never stored here — it lives on the flow's composition only.
    private var _patientScopedBridgesFactory: PatientScopedBridgesFactory? = null

    val sdkBridge: OSTSDKBridge
        get() = _sdkBridge ?: error("UIKitServiceLocator not configured: sdkBridge is null")
    val recorderBridge: RecorderBridge
        get() = _recorderBridge ?: error("UIKitServiceLocator not configured: recorderBridge is null")
    val motionDataBridge: MotionDataBridge
        get() = _motionDataBridge ?: error("UIKitServiceLocator not configured: motionDataBridge is null")
    val insightsBridge: InsightsBridge
        get() = _insightsBridge ?: error("UIKitServiceLocator not configured: insightsBridge is null")
    val preferencesBridge: PreferencesBridge
        get() = _preferencesBridge ?: error("UIKitServiceLocator not configured: preferencesBridge is null")
    val featureFlagsBridge: FeatureFlagsBridge
        get() = _featureFlagsBridge ?: error("UIKitServiceLocator not configured: featureFlagsBridge is null")
    val audioPlayer: PlatformAudioPlayer
        get() = _audioPlayer ?: error("UIKitServiceLocator not configured: audioPlayer is null")
    val ttsPlayer: PlatformTTSPlayer
        get() = _ttsPlayer ?: error("UIKitServiceLocator not configured: ttsPlayer is null")
    val permissionsManager: PlatformPermissionsManager
        get() = _permissionsManager ?: error("UIKitServiceLocator not configured: permissionsManager is null")
    val resourceProvider: ResourceProvider
        get() = _resourceProvider ?: error("UIKitServiceLocator not configured: resourceProvider is null")
    val analyticsHandler: OSTUIKitAnalyticsHandler?
        get() = _analyticsHandler

    /**
     * Clinician-mode bridge factory, or null when the host configured single-patient (current-user)
     * mode only. When a flow is launched with a non-null `patientId` but this is null, the flow
     * fails fast rather than silently attributing the recording to the auth-bound singleton.
     */
    val patientScopedBridgesFactory: PatientScopedBridgesFactory?
        get() = _patientScopedBridgesFactory

    // Analytics trackers are rebuilt whenever the handler changes (configure/reset). They
    // are null when no handler was provided, so screens/VMs no-op instead of crashing.
    private var _recordFlowAnalyticsTracker: RecordFlowAnalyticsTracker? = null
    private var _summaryAnalyticsTracker: SummaryAnalyticsTracker? = null

    /**
     * Record-flow analytics tracker, or null when no analytics handler was configured.
     * Mirrors uikit's `UiKitServiceLocator.recordFlowAnalyticsTracker`.
     */
    internal val recordFlowAnalyticsTracker: RecordFlowAnalyticsTracker?
        get() = _recordFlowAnalyticsTracker

    /**
     * Summary analytics tracker, or null when no analytics handler was configured.
     * Mirrors uikit's `UiKitServiceLocator.summaryAnalyticsTracker`.
     */
    internal val summaryAnalyticsTracker: SummaryAnalyticsTracker?
        get() = _summaryAnalyticsTracker

    val isConfigured: Boolean
        get() = _sdkBridge != null

    /**
     * Configure all dependencies. Call once during app startup.
     */
    fun configure(
        sdkBridge: OSTSDKBridge,
        recorderBridge: RecorderBridge,
        motionDataBridge: MotionDataBridge,
        insightsBridge: InsightsBridge,
        preferencesBridge: PreferencesBridge,
        featureFlagsBridge: FeatureFlagsBridge,
        audioPlayer: PlatformAudioPlayer,
        ttsPlayer: PlatformTTSPlayer,
        permissionsManager: PlatformPermissionsManager,
        resourceProvider: ResourceProvider? = null,
        analyticsHandler: OSTUIKitAnalyticsHandler? = null,
        patientScopedBridgesFactory: PatientScopedBridgesFactory? = null,
    ) {
        _sdkBridge = sdkBridge
        _recorderBridge = recorderBridge
        _motionDataBridge = motionDataBridge
        _insightsBridge = insightsBridge
        _preferencesBridge = preferencesBridge
        _featureFlagsBridge = featureFlagsBridge
        _audioPlayer = audioPlayer
        _ttsPlayer = ttsPlayer
        _permissionsManager = permissionsManager
        _resourceProvider = resourceProvider
        _analyticsHandler = analyticsHandler
        _recordFlowAnalyticsTracker = analyticsHandler?.let { RecordFlowAnalyticsTracker(it) }
        _summaryAnalyticsTracker = analyticsHandler?.let { SummaryAnalyticsTracker(it) }
        _patientScopedBridgesFactory = patientScopedBridgesFactory
    }

    /**
     * The auth-bound (current-user) bridge bundle used by patient-app mode. Reads the singletons
     * configured at startup — the same instances the flow resolved directly before clinician mode
     * existed, so single-patient hosts are unaffected.
     */
    fun currentUserBridges(): PatientScopedBridges =
        PatientScopedBridges(
            recorderBridge = recorderBridge,
            insightsBridge = insightsBridge,
            motionDataBridge = motionDataBridge,
        )

    /**
     * Reset all dependencies. Useful for testing.
     */
    fun reset() {
        _sdkBridge = null
        _recorderBridge = null
        _motionDataBridge = null
        _insightsBridge = null
        _preferencesBridge = null
        _featureFlagsBridge = null
        _audioPlayer = null
        _ttsPlayer = null
        _permissionsManager = null
        _resourceProvider = null
        _analyticsHandler = null
        _recordFlowAnalyticsTracker = null
        _summaryAnalyticsTracker = null
        _patientScopedBridgesFactory = null
    }
}
