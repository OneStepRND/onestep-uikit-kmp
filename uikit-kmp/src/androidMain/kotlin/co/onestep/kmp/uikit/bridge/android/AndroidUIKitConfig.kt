package co.onestep.kmp.uikit.bridge.android

import android.content.Context
import co.onestep.android.core.OneStep
import co.onestep.kmp.uikit.bridge.PatientScopedBridgesFactory
import co.onestep.kmp.uikit.bridge.PlatformAudioPlayer
import co.onestep.kmp.uikit.bridge.PlatformPermissionsManager
import co.onestep.kmp.uikit.bridge.PlatformTTSPlayer
import co.onestep.kmp.uikit.di.UIKitServiceLocator
import co.onestep.kmp.uikit.utils.ResourceProvider

/**
 * One-call convenience function that configures UIKitServiceLocator
 * with Android bridge implementations backed by the real OneStep SDK.
 *
 * Call once in your Activity or Application after the SDK is initialized:
 * ```
 * UIKitServiceLocator.configureWithAndroidSDK(applicationContext, oneStep)
 * ```
 *
 * @param patientScopedBridgesFactory Resolves patient-scoped SDK products for flows launched with a
 *        `patientId` (OSTRecordingFlow / OSTMeasurementSummary). Defaults to the stock
 *        [AndroidPatientScopedBridgesFactory], which is stateless and only ever invoked when a
 *        `patientId` is supplied — so single-patient (patient-app) hosts, which always launch with
 *        `patientId == null`, never touch it and are unaffected. Override only to inject a custom
 *        factory (e.g. test fakes or an alternate scope-resolution strategy).
 */
fun UIKitServiceLocator.configureWithAndroidSDK(
    context: Context,
    oneStep: OneStep,
    patientScopedBridgesFactory: PatientScopedBridgesFactory = AndroidPatientScopedBridgesFactory(),
) {
    val preferencesBridge = AndroidPreferencesBridge(context)
    configure(
        sdkBridge = AndroidSDKBridge(oneStep),
        recorderBridge = AndroidRecorderBridge(oneStep),
        motionDataBridge = AndroidMotionDataBridge(oneStep),
        insightsBridge = AndroidInsightsBridge(oneStep),
        preferencesBridge = preferencesBridge,
        featureFlagsBridge = AndroidFeatureFlagsBridge(),
        audioPlayer = PlatformAudioPlayer(context),
        ttsPlayer = PlatformTTSPlayer(context),
        permissionsManager = PlatformPermissionsManager(context, preferencesBridge),
        resourceProvider = ResourceProvider(context),
        patientScopedBridgesFactory = patientScopedBridgesFactory,
    )
}
