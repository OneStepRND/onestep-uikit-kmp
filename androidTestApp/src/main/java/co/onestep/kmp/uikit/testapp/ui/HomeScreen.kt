package co.onestep.kmp.uikit.testapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.ui.theme.OneStepUiKit
import co.onestep.kmp.uikit.ui.theme.ThemeMode

/**
 * Home screen — the Android mirror of `iosTestApp`'s `ContentView`. Same sections and options:
 * Recording Flows · Screens · (optional) Last Event, with a gear that opens Settings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userId: String,
    lastEvent: String?,
    onClickConfigureAndRecord: () -> Unit,
    onClickWalkRecording: () -> Unit,
    onClickTug: () -> Unit,
    onClickPermissionInApp: () -> Unit,
    onClickPermissionBackground: () -> Unit,
    onClickMeasurementSummary: () -> Unit,
    onClickCareLog: () -> Unit,
    onClickPushPopDemo: () -> Unit,
    onClickSettings: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("UIKit KMP Test") },
                actions = {
                    TextButton(
                        onClick = onClickSettings,
                        modifier = Modifier.testTag("home.settings"),
                    ) { Text("Settings") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                modifier = Modifier.padding(start = 16.dp, top = 12.dp),
                text = "user: $userId",
                fontSize = 12.sp,
                color = Color.Gray,
            )

            SectionHeader("Theme")
            ThemeModeSelector()

            SectionHeader("Recording Flows")
            NavRow("Configure & Record", "home.configureAndRecord", onClickConfigureAndRecord)
            NavRow("Walk Recording", "home.walkRecording", onClickWalkRecording)
            NavRow("Timed Up & Go", "home.tug", onClickTug)

            SectionHeader("Screens")
            NavRow("Permission Flow (In-App)", "home.permissionInApp", onClickPermissionInApp)
            NavRow("Permission Flow (Background)", "home.permissionBackground", onClickPermissionBackground)
            NavRow("Measurement Summary", "home.measurementSummary", onClickMeasurementSummary)
            NavRow("Care Log", "home.careLog", onClickCareLog)
            NavRow("iOS Push/Pop Demo", "home.pushPopDemo", onClickPushPopDemo)

            if (lastEvent != null) {
                SectionHeader("Last Event")
                Text(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("home.lastEvent"),
                    text = lastEvent,
                    fontSize = 13.sp,
                    color = Color.Gray,
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

/**
 * Live Light / Dark / System selector. Calls the library's global
 * [OneStepUiKit.setThemeMode]; every `OneStepUiKitTheme`-wrapped flow (Recording,
 * Summary, Care Log, Permissions) recomposes to the chosen mode.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeModeSelector() {
    // OneStepUiKit.themeMode is backed by Compose state, so reading it here makes the
    // chips recompose (and re-highlight) the moment setThemeMode is called.
    val current = OneStepUiKit.themeMode
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ThemeMode.entries.forEach { mode ->
            FilterChip(
                selected = current == mode,
                onClick = { OneStepUiKit.setThemeMode(mode) },
                label = { Text(mode.name) },
                modifier = Modifier
                    .padding(end = 8.dp)
                    .testTag("home.theme.${mode.name}"),
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 4.dp),
        text = text.uppercase(),
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun NavRow(
    label: String,
    testTag: String,
    onClick: () -> Unit,
) {
    Column {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .testTag(testTag)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            text = label,
            fontSize = 17.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        HorizontalDivider(Modifier.padding(start = 16.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    HomeScreen(
        userId = "preview-user",
        lastEvent = "WALK: recording_completed (id:1a2b3c4d)",
        onClickConfigureAndRecord = {},
        onClickWalkRecording = {},
        onClickTug = {},
        onClickPermissionInApp = {},
        onClickPermissionBackground = {},
        onClickMeasurementSummary = {},
        onClickCareLog = {},
        onClickPushPopDemo = {},
        onClickSettings = {},
    )
}
