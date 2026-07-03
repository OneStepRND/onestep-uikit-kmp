package co.onestep.kmp.uikit.features.summary

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
 * @param options Summary display options (Full, Minimal, None).
 * @param onDismiss Callback when the summary is dismissed.
 */
@Composable
fun OSTMeasurementSummary(
    measurement: OSTMotionMeasurement,
    options: OSTSummaryOptions = OSTSummaryOptions.Full,
    origin: OSTSummaryOrigin = OSTSummaryOrigin.CareLog,
    configuration: OSTRecordingConfiguration? = null,
    onDismiss: () -> Unit = {},
) {
    val summaryViewModel = remember {
        SummaryViewModel(
            resourceProvider = UIKitServiceLocator.resourceProvider,
            preferenceManager = UIKitServiceLocator.preferencesBridge,
            recorderBridge = UIKitServiceLocator.recorderBridge,
            motionDataBridge = UIKitServiceLocator.motionDataBridge,
            insightsBridge = UIKitServiceLocator.insightsBridge,
        )
    }

    LaunchedEffect(measurement) {
        summaryViewModel.createSummaryItems(measurement)
    }

    OneStepUiKitTheme {
        SummaryMainFlow(
            modifier = Modifier.fillMaxSize(),
            summaryViewModel = summaryViewModel,
            motionMeasurementId = measurement.id,
            origin = origin,
            configuration = configuration,
            backAction = onDismiss,
        )
    }
}
