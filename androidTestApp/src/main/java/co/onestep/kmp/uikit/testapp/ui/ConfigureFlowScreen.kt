package co.onestep.kmp.uikit.testapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTRecordingConfiguration

private fun presetConfigurations(): List<Pair<String, OSTRecordingConfiguration>> = listOf(
    "Walk" to OSTRecordingConfiguration.defaultWalk(),
    "STS" to OSTRecordingConfiguration.sts(),
    "TUG" to OSTRecordingConfiguration.tug(),
    "Balance Test" to OSTRecordingConfiguration.balanceTest(),
    "Static Balance" to OSTRecordingConfiguration.staticBalance(),
    "6 Min Walk" to OSTRecordingConfiguration.sixMinuteWalk(),
    "2 Min Walk" to OSTRecordingConfiguration.twoMinuteWalk(),
    "Dual Task" to OSTRecordingConfiguration.dualTaskSubtract(ttsSpeechText = "Count backwards from 100 by 3"),
)

@Composable
fun ConfigureFlowScreen(
    onStartFlow: (OSTRecordingConfiguration) -> Unit,
    onBack: () -> Unit,
) {
    val configs = remember { presetConfigurations() }
    var selectedIndex by remember { mutableIntStateOf(0) }
    var config by remember { mutableStateOf(configs[0].second) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TextButton(
            modifier = Modifier.align(Alignment.Start),
            onClick = onBack,
        ) {
            Text("< BACK")
        }

        Text(text = "Configure Flow", fontSize = 24.sp)

        Spacer(Modifier.height(24.dp))

        ActivityDropdown(
            configs = configs,
            selectedIndex = selectedIndex,
            onSelected = { index ->
                selectedIndex = index
                config = configs[index].second
            },
        )

        Spacer(Modifier.height(16.dp))

        ToggleRow(
            label = "Play voice over",
            checked = config.playVoiceOver,
            onCheckedChange = { config = config.copy(playVoiceOver = it) },
        )

        ToggleRow(
            label = "Show phone position screen",
            checked = config.showPhonePositionScreen,
            onCheckedChange = { config = config.copy(showPhonePositionScreen = it) },
        )

        ToggleRow(
            label = "Show permission explanation",
            checked = config.showPermissionExplanationScreen,
            onCheckedChange = { config = config.copy(showPermissionExplanationScreen = it) },
        )

        Spacer(Modifier.height(32.dp))

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            onClick = { onStartFlow(config) },
        ) {
            Text("START FLOW", fontSize = 18.sp)
        }
    }
}

@Composable
private fun ActivityDropdown(
    configs: List<Pair<String, OSTRecordingConfiguration>>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxWidth()) {
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { expanded = true },
        ) {
            Text("Activity: ${configs[selectedIndex].first}", fontSize = 18.sp)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            configs.forEachIndexed { index, (name, _) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onSelected(index)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
            text = label,
            fontSize = 16.sp,
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Preview(showBackground = true)
@Composable
private fun ConfigureFlowScreenPreview() {
    ConfigureFlowScreen(
        onStartFlow = {},
        onBack = {},
    )
}
