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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.testapp.AppConstants
import co.onestep.kmp.uikit.testapp.Organization
import co.onestep.kmp.uikit.testapp.Organizations
import co.onestep.kmp.uikit.testapp.SDKEnvironment

/**
 * Settings / Login screen — the Android mirror of `iosTestApp`'s `SettingsView`. Shown as the login
 * screen before identification and (via the Home gear) as a settings sheet afterward.
 *
 * Sections match iOS: Environment · Organization · Identity · Connect as Avatar.
 */
@Composable
fun SettingsScreen(
    initialEnvironment: String,
    initialCustomUrl: String,
    initialOrgName: String,
    initialDistinctId: String,
    isConnecting: Boolean,
    errorMessage: String?,
    onIdentify: (org: Organization, distinctId: String, environment: String, customUrl: String) -> Unit,
    onLogout: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null,
) {
    var environment by remember { mutableStateOf(SDKEnvironment.fromRaw(initialEnvironment)) }
    var customUrl by remember { mutableStateOf(initialCustomUrl) }
    var selectedOrgName by remember {
        mutableStateOf((Organizations.find(initialOrgName) ?: Organizations.default).name)
    }
    var distinctId by remember { mutableStateOf(initialDistinctId) }

    val selectedOrg = remember(selectedOrgName) { Organizations.find(selectedOrgName) ?: Organizations.default }
    val canIdentify = distinctId.trim().isNotEmpty() && !isConnecting

    fun identify(id: String) {
        onIdentify(selectedOrg, id.trim(), environment.rawValue, customUrl)
    }

    Box(
        Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Login", fontSize = 28.sp, modifier = Modifier.weight(1f))
                if (onClose != null) {
                    TextButton(onClick = onClose) { Text("CLOSE") }
                }
            }

            Spacer(Modifier.height(16.dp))

            // MARK: Environment
            // shortcut: UI-only, matching iOS. The environment/custom-URL selection is persisted but
            // not wired into SDK init on either platform. Upgrade path: pass the base URL into
            // OneStep.initialize when the SDK exposes a custom-endpoint override.
            SectionHeader("Environment")
            SegmentedRow(
                options = SDKEnvironment.entries.map { it.rawValue },
                selected = environment.rawValue,
                onSelected = { environment = SDKEnvironment.fromRaw(it) },
            )
            if (environment == SDKEnvironment.CUSTOM) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    value = customUrl,
                    onValueChange = { customUrl = it },
                    label = { Text("Base URL") },
                    singleLine = true,
                )
            }

            Spacer(Modifier.height(24.dp))

            // MARK: Organization
            SectionHeader("Organization")
            OrganizationDropdown(
                selectedOrg = selectedOrg,
                onSelected = { selectedOrgName = it.name },
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("App ID", fontSize = 14.sp)
                Text(
                    text = selectedOrg.appId,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    modifier = Modifier.padding(start = 16.dp),
                )
            }

            Spacer(Modifier.height(24.dp))

            // MARK: Identity
            SectionHeader("Identity")
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings.distinctId"),
                value = distinctId,
                onValueChange = { distinctId = it },
                label = { Text("Distinct ID") },
                singleLine = true,
            )

            if (errorMessage != null) {
                Text(
                    modifier = Modifier.padding(top = 12.dp),
                    text = errorMessage,
                    color = Color.Red,
                )
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .testTag("settings.identify"),
                enabled = canIdentify,
                onClick = { identify(distinctId) },
            ) {
                Text("IDENTIFY", fontSize = 16.sp)
            }

            Spacer(Modifier.height(24.dp))

            // MARK: Connect as Avatar
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "or",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
            )
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .testTag("settings.connectAvatar"),
                enabled = !isConnecting,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9500)),
                onClick = { identify(AppConstants.AVATAR_AANG_DISTINCT_ID) },
            ) {
                Text("CONNECT AS AVATAR", fontSize = 16.sp)
            }

            if (onLogout != null) {
                Spacer(Modifier.height(32.dp))
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isConnecting,
                    onClick = onLogout,
                ) {
                    Text("LOGOUT")
                }
            }
        }

        if (isConnecting) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(Modifier.size(64.dp))
            }
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

/** Two-option segmented selector, the native Material stand-in for iOS's `.pickerStyle(.segmented)`. */
@Composable
private fun SegmentedRow(
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            if (option == selected) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { onSelected(option) },
                ) { Text(option) }
            } else {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = { onSelected(option) },
                ) { Text(option) }
            }
        }
    }
}

@Composable
private fun OrganizationDropdown(
    selectedOrg: Organization,
    onSelected: (Organization) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxWidth()) {
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { expanded = true },
        ) {
            Text(selectedOrg.displayName)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            Organizations.all.forEach { org ->
                DropdownMenuItem(
                    text = { Text(org.displayName) },
                    onClick = {
                        onSelected(org)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    SettingsScreen(
        initialEnvironment = SDKEnvironment.PRODUCTION.rawValue,
        initialCustomUrl = "",
        initialOrgName = Organizations.default.name,
        initialDistinctId = "uikit-kmp-android-test-user",
        isConnecting = false,
        errorMessage = null,
        onIdentify = { _, _, _, _ -> },
    )
}
