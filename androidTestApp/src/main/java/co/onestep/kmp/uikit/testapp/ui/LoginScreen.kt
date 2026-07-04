package co.onestep.kmp.uikit.testapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.testapp.TestCredentials

@Composable
fun LoginScreen(
    isConnecting: Boolean,
    errorMessage: String?,
    onConnect: (userId: String) -> Unit,
) {
    var userId by remember { mutableStateOf(TestCredentials.DEFAULT_USER_ID) }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "UIKit KMP Test App",
                fontSize = 28.sp,
                textAlign = TextAlign.Center,
            )

            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = "Organization: onestep-sdk-testing",
                fontSize = 14.sp,
                color = Color.Gray,
            )

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                value = userId,
                onValueChange = { userId = it },
                label = { Text("User ID") },
                singleLine = true,
            )

            if (errorMessage != null) {
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = errorMessage,
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                )
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                enabled = !isConnecting && userId.isNotBlank(),
                onClick = { onConnect(userId.trim()) },
            ) {
                Text("CONNECT", fontSize = 18.sp)
            }
        }

        if (isConnecting) {
            Box(
                Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(Modifier.size(64.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    LoginScreen(
        isConnecting = false,
        errorMessage = null,
        onConnect = {},
    )
}
