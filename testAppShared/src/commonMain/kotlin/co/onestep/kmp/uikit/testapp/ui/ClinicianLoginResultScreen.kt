package co.onestep.kmp.uikit.testapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.testapp.ClinicianLoginUiState
import co.onestep.kmp.uikit.testapp.ClinicianSession

/**
 * Renders the clinician web-login flow result. The returned JWT is displayed on-screen (this is an
 * internal test harness); the token is never logged and no clinician PII/PHI is echoed (HIPAA).
 */
@Composable
fun ClinicianLoginResultScreen(
    state: ClinicianLoginUiState,
    onDone: () -> Unit,
    onEnterApp: (ClinicianSession) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .testTag("clinicianLogin.result"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = "Clinician Web Login", fontSize = 28.sp)

        when (state) {
            is ClinicianLoginUiState.InProgress -> Progress(
                "Waiting for browser sign-in… complete the login in the browser, then you'll be sent back here.",
            )

            is ClinicianLoginUiState.Exchanging -> Progress("Exchanging one-time code for a session token…")

            is ClinicianLoginUiState.Success -> SuccessBody(state.session)

            is ClinicianLoginUiState.Error -> Text(
                modifier = Modifier.testTag("clinicianLogin.error"),
                text = "Login failed: ${state.message}",
                color = Color.Red,
            )

            ClinicianLoginUiState.Idle -> Unit
        }

        Spacer(Modifier.height(8.dp))

        val isBusy = state is ClinicianLoginUiState.InProgress || state is ClinicianLoginUiState.Exchanging
        when {
            isBusy -> TextButton(
                modifier = Modifier.fillMaxWidth().testTag("clinicianLogin.cancel"),
                onClick = onDone,
            ) { Text("CANCEL") }

            state is ClinicianLoginUiState.Success -> {
                Button(
                    modifier = Modifier.fillMaxWidth().testTag("clinicianLogin.continue"),
                    onClick = { onEnterApp(state.session) },
                ) { Text("CONTINUE TO APP") }
                TextButton(
                    modifier = Modifier.fillMaxWidth().testTag("clinicianLogin.done"),
                    onClick = onDone,
                ) { Text("DONE") }
            }

            else -> Button(
                modifier = Modifier.fillMaxWidth().testTag("clinicianLogin.done"),
                onClick = onDone,
            ) { Text("DONE") }
        }
    }
}

@Composable
private fun Progress(message: String) {
    CircularProgressIndicator()
    Text(text = message, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun SuccessBody(session: ClinicianSession) {
    Text(text = "Signed in ✓", color = Color(0xFF2E7D32), fontSize = 18.sp)

    Text(text = "JWT", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
    Text(
        modifier = Modifier.fillMaxWidth().testTag("clinicianLogin.token"),
        text = session.token,
        fontFamily = FontFamily.Monospace,
        fontSize = 12.sp,
    )

    if (session.userUuid != null) {
        Spacer(Modifier.height(4.dp))
        Text(text = "User UUID", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
        Text(
            text = session.userUuid,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ClinicianLoginSuccessPreview() {
    ClinicianLoginResultScreen(
        state = ClinicianLoginUiState.Success(
            ClinicianSession(token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.payload.sig", userUuid = "abc-123"),
        ),
        onDone = {},
        onEnterApp = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun ClinicianLoginErrorPreview() {
    ClinicianLoginResultScreen(
        state = ClinicianLoginUiState.Error("HTTP 401"),
        onDone = {},
        onEnterApp = {},
    )
}
