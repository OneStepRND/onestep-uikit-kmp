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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    userId: String,
    mockImuName: String,
    mockImuOptions: List<String>,
    onMockImuSelected: (String) -> Unit,
    onClickCareLog: () -> Unit,
    onClickRecordingFlow: () -> Unit,
    onClickPermissionInApp: () -> Unit,
    onClickPermissionBackground: () -> Unit,
    onClickLogout: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(16.dp))

        Text(text = "KMP UI-KIT", fontSize = 40.sp)

        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = "user: $userId",
            fontSize = 14.sp,
            color = Color.Gray,
        )

        Spacer(Modifier.height(24.dp))

        MockImuSelector(
            selected = mockImuName,
            options = mockImuOptions,
            onSelected = onMockImuSelected,
        )

        Spacer(Modifier.height(24.dp))

        HomeButton(text = "CARE LOG", onClick = onClickCareLog)
        HomeButton(text = "RECORDING FLOW", onClick = onClickRecordingFlow)
        HomeButton(text = "IN-APP PERMISSIONS", onClick = onClickPermissionInApp)
        HomeButton(text = "BACKGROUND PERMISSIONS", onClick = onClickPermissionBackground)

        Spacer(Modifier.height(24.dp))

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onClickLogout,
        ) {
            Text("LOGOUT")
        }
    }
}

@Composable
private fun HomeButton(
    text: String,
    onClick: () -> Unit,
) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        onClick = onClick,
    ) {
        Text(text = text, fontSize = 18.sp)
    }
}

@Composable
private fun MockImuSelector(
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxWidth()) {
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { expanded = true },
            colors = ButtonDefaults.outlinedButtonColors(),
        ) {
            Text("Mock IMU: $selected")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    HomeScreen(
        userId = "preview-user",
        mockImuName = "SUCCESSFUL",
        mockImuOptions = listOf("NONE", "SUCCESSFUL"),
        onMockImuSelected = {},
        onClickCareLog = {},
        onClickRecordingFlow = {},
        onClickPermissionInApp = {},
        onClickPermissionBackground = {},
        onClickLogout = {},
    )
}
