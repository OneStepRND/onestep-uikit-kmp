package co.onestep.kmp.uikit.testapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.android.core.motionLab.OSTMockIMU
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTRecordingConfiguration

/**
 * A selectable recording preset. [key] doubles as the testTag suffix (`activity.WALK`, …), matching
 * `iosTestApp`'s `MeasurementPreset.key`, so a UI-test can select any activity deterministically.
 */
private data class ActivityPreset(
    val key: String,
    val displayName: String,
    val config: OSTRecordingConfiguration,
)

private fun presetConfigurations(): List<ActivityPreset> = listOf(
    ActivityPreset("WALK", "Walk", OSTRecordingConfiguration.defaultWalk()),
    ActivityPreset("STS", "STS", OSTRecordingConfiguration.sts()),
    ActivityPreset("TUG", "TUG", OSTRecordingConfiguration.tug()),
    ActivityPreset("BALANCE_TEST", "Balance Test", OSTRecordingConfiguration.balanceTest()),
    ActivityPreset("STATIC_BALANCE", "Static Balance", OSTRecordingConfiguration.staticBalance()),
    ActivityPreset("SIX_MINUTE_WALK", "6 Min Walk", OSTRecordingConfiguration.sixMinuteWalk()),
    ActivityPreset("TWO_MINUTE_WALK", "2 Min Walk", OSTRecordingConfiguration.twoMinuteWalk()),
    ActivityPreset(
        "DUAL_TASK",
        "Dual Task",
        OSTRecordingConfiguration.dualTaskSubtract(ttsSpeechText = "Count backwards from 100 by 3"),
    ),
)

/**
 * Configure Flow — the Android mirror of `iosTestApp`'s `ConfigureFlowView`. Same sections and
 * options: Activity (single-select list) · Options (Play voice over, Show permission explanation) ·
 * Mock recording (native [OSTMockIMU], the Android analogue of iOS's bundled mock recordings).
 */
@Composable
fun ConfigureFlowScreen(
    onStartFlow: (config: OSTRecordingConfiguration, mock: OSTMockIMU) -> Unit,
    onBack: () -> Unit,
) {
    val configs = remember { presetConfigurations() }
    var selectedIndex by remember { mutableIntStateOf(0) }
    var config by remember { mutableStateOf(configs[0].config) }
    // Defaults to SUCCESSFUL so the flow completes on an emulator out of the box; the picker mirrors
    // iOS's "Mock recording" section (its options map 1:1 to iOS's bundled mock names).
    var selectedMock by remember { mutableStateOf(OSTMockIMU.SUCCESSFUL) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        TextButton(
            modifier = Modifier
                .align(Alignment.Start)
                .testTag("configure.cancel"),
            onClick = onBack,
        ) {
            Text("< BACK")
        }

        Text(text = "Configure Flow", fontSize = 24.sp)

        Spacer(Modifier.height(24.dp))

        // MARK: Activity
        SectionHeader("Activity")
        configs.forEachIndexed { index, preset ->
            ActivityRow(
                name = preset.displayName,
                testTag = "activity.${preset.key}",
                selected = index == selectedIndex,
                onClick = {
                    selectedIndex = index
                    config = preset.config
                },
            )
        }

        Spacer(Modifier.height(24.dp))

        // MARK: Options
        SectionHeader("Options")
        ToggleRow(
            label = "Play voice over",
            testTag = "toggle.voiceOver",
            checked = config.playVoiceOver,
            onCheckedChange = { config = config.copy(playVoiceOver = it) },
        )
        ToggleRow(
            label = "Show permission explanation",
            testTag = "toggle.permissionExplanation",
            checked = config.showPermissionExplanationScreen,
            onCheckedChange = { config = config.copy(showPermissionExplanationScreen = it) },
        )

        Spacer(Modifier.height(24.dp))

        // MARK: Mock recording
        SectionHeader("Mock recording")
        MockRecordingDropdown(
            selected = selectedMock,
            onSelected = { selectedMock = it },
        )

        Spacer(Modifier.height(32.dp))

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("configure.startLarge"),
            onClick = { onStartFlow(config, selectedMock) },
        ) {
            Text("START FLOW", fontSize = 18.sp)
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        modifier = Modifier.padding(bottom = 8.dp),
        text = text.uppercase(),
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun ActivityRow(
    name: String,
    testTag: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .testTag(testTag)
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = name, fontSize = 17.sp)
            if (selected) {
                Text(text = "✓", fontSize = 17.sp, color = MaterialTheme.colorScheme.primary)
            }
        }
        HorizontalDivider()
    }
}

@Composable
private fun ToggleRow(
    label: String,
    testTag: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = label,
            fontSize = 16.sp,
        )
        Switch(
            modifier = Modifier.testTag(testTag),
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun MockRecordingDropdown(
    selected: OSTMockIMU,
    onSelected: (OSTMockIMU) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxWidth()) {
        OutlinedButton(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("mockRecordingPicker"),
            onClick = { expanded = true },
        ) {
            Text("Mock recording: ${selected.name}")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            OSTMockIMU.entries.forEach { mock ->
                DropdownMenuItem(
                    text = { Text(mock.name) },
                    onClick = {
                        onSelected(mock)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConfigureFlowScreenPreview() {
    ConfigureFlowScreen(
        onStartFlow = { _, _ -> },
        onBack = {},
    )
}
