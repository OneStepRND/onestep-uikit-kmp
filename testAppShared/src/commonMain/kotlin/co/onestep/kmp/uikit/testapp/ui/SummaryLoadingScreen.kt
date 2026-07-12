package co.onestep.kmp.uikit.testapp.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import co.onestep.kmp.uikit.di.UIKitServiceLocator
import co.onestep.kmp.uikit.models.OSTMotionMeasurement
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun SummaryLoadingScreen(
    measurementId: String,
    onLoaded: (OSTMotionMeasurement) -> Unit,
    onError: () -> Unit,
) {
    LaunchedEffect(measurementId) {
        val measurement = UIKitServiceLocator.recorderBridge.readSingleMotionMeasurement(measurementId)
        if (measurement != null) {
            onLoaded(measurement)
        } else {
            println("SummaryLoadingScreen: measurement not found")
            onError()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Preview(showBackground = true)
@Composable
private fun SummaryLoadingScreenPreview() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}
