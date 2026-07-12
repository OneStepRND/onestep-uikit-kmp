package co.onestep.kmp.uikit.testapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.di.UIKitServiceLocator
import co.onestep.kmp.uikit.models.OSTMotionMeasurement
import co.onestep.kmp.uikit.models.OSTOrder
import co.onestep.kmp.uikit.models.OSTTimeRangedDataRequest

/**
 * Measurement Summary picker — the Android mirror of `iosTestApp`'s `MeasurementPickerView`. Loads
 * recent measurements, then routes the chosen one into the summary screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasurementPickerScreen(
    onSelect: (OSTMotionMeasurement) -> Unit,
    onBack: () -> Unit,
) {
    var isLoading by remember { mutableStateOf(true) }
    var measurements by remember { mutableStateOf<List<OSTMotionMeasurement>>(emptyList()) }

    LaunchedEffect(Unit) {
        measurements = UIKitServiceLocator.recorderBridge.readMotionMeasurements(
            OSTTimeRangedDataRequest(limit = 20, order = OSTOrder.DESCENDING),
        )
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Measurement") },
                actions = {
                    TextButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("summary.cancel"),
                    ) { Text("Cancel") }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            when {
                isLoading -> CircularProgressIndicator(Modifier.testTag("summary.loading"))

                measurements.isEmpty() -> Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .testTag("summary.empty"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("No Measurements", fontSize = 18.sp)
                    Text(
                        text = "Record a walk or TUG first to see the summary.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }

                else -> LazyColumn(Modifier.fillMaxSize()) {
                    items(measurements, key = { it.id }) { measurement ->
                        Column {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(measurement) }
                                    .testTag("summary.row")
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                            ) {
                                Text(text = measurement.type.name, fontSize = 17.sp)
                                Text(
                                    text = "ID: ${measurement.id}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            HorizontalDivider(Modifier.padding(start = 16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MeasurementPickerScreenPreview() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}
