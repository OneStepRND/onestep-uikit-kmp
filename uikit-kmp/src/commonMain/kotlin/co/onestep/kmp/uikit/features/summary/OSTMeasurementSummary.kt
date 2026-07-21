package co.onestep.kmp.uikit.features.summary

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import co.onestep.kmp.uikit.bridge.resolveSessionBridges
import co.onestep.kmp.uikit.di.UIKitServiceLocator
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTRecordingConfiguration
import co.onestep.kmp.uikit.features.summary.models.OSTSummaryOptions
import co.onestep.kmp.uikit.features.summary.models.OSTSummaryOrigin
import co.onestep.kmp.uikit.features.summary.presentation.SummaryViewModel
import co.onestep.kmp.uikit.features.summary.screens.SummaryMainFlow
import co.onestep.kmp.uikit.models.OSTMotionMeasurement
import co.onestep.kmp.uikit.ui.theme.OneStepUiKitTheme

/**
 * Composable entry point for the measurement summary screen.
 *
 * @param measurement The measurement to display.
 * @param patientId Clinician-mode selector, mirroring [co.onestep.kmp.uikit.features.recordFlow.OSTRecordingFlow].
 *        `null` (default) = current-user mode: every SDK touchpoint resolves the auth-bound patient
 *        (single-patient hosts are source-compatible). Non-null = clinician mode: the summary's
 *        recorder/measurement/insights calls run patient-scoped for this id, requiring a
 *        `PatientScopedBridgesFactory` registered at `configure` time (fails fast otherwise). The
 *        id is never attached to analytics, logs, or screen names (HIPAA).
 * @param options Summary display options (Full, Minimal, None).
 * @param onDismiss Callback when the summary is dismissed.
 */
@Composable
fun OSTMeasurementSummary(
    measurement: OSTMotionMeasurement,
    patientId: String? = null,
    options: OSTSummaryOptions = OSTSummaryOptions.Full,
    origin: OSTSummaryOrigin = OSTSummaryOrigin.CareLog,
    configuration: OSTRecordingConfiguration? = null,
    onDismiss: () -> Unit = {},
) {
    // Resolve the patient-bound bridge bundle once per launch. null patientId = current-user mode
    // (auth-bound singletons); non-null delegates to the registered PatientScopedBridgesFactory.
    val bridges = remember(patientId) {
        resolveSessionBridges(
            patientId = patientId,
            currentUserBridges = { UIKitServiceLocator.currentUserBridges() },
            patientScopedBridgesFactory = UIKitServiceLocator.patientScopedBridgesFactory,
        )
    }

    val summaryViewModel = remember(patientId) {
        SummaryViewModel(
            resourceProvider = UIKitServiceLocator.resourceProvider,
            preferenceManager = UIKitServiceLocator.preferencesBridge,
            recorderBridge = bridges.recorderBridge,
            motionDataBridge = bridges.motionDataBridge,
            insightsBridge = bridges.insightsBridge,
        )
    }

    LaunchedEffect(measurement) {
        summaryViewModel.createSummaryItems(measurement)
    }

    OneStepUiKitTheme {
        SummaryMainFlow(
            modifier = Modifier.fillMaxSize(),
            summaryViewModel = summaryViewModel,
            recorderBridge = bridges.recorderBridge,
            motionMeasurementId = measurement.id,
            origin = origin,
            configuration = configuration,
            backAction = onDismiss,
        )
    }
}
