package co.onestep.kmp.uikit.features.permissions.ios.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import co.onestep.designsystem.components.OSButtonSize
import co.onestep.designsystem.components.PrimaryButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import co.onestep.kmp.uikit.testing.OSTTestTags
import co.onestep.kmp.uikit.utils.test
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.features.permissions.ios.IosPermissionChecker
import co.onestep.kmp.uikit.features.permissions.ios.IosPermissionType
import co.onestep.kmp.uikit.features.permissions.ios.components.IosMockAlertDialog
import co.onestep.kmp.uikit.features.permissions.ios.components.IosSettingsRedirectContent
import co.onestep.kmp.uikit.features.permissions.ios.components.MockAlertButton
import kotlinx.coroutines.launch
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionRecordPermissionDenied
import platform.AVFAudio.AVAudioSessionRecordPermissionGranted
import platform.AVFAudio.AVAudioSessionRecordPermissionUndetermined
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Standalone microphone permission screen (not part of the main flow modes).
 *
 * - Request variant: Mock microphone alert + "Continue" button
 * - Settings variant: Skip button + "Go to Settings" + instructions
 * - Authorized: Calls onContinue() and exits
 *
 * Exposed via IosEntryPoint as createMicrophonePermissionViewController(onContinue, onSkip).
 */
@Composable
internal fun IosMicrophonePermissionScreen(
    checker: IosPermissionChecker,
    onContinue: () -> Unit,
    onSkip: () -> Unit,
) {
    val audioSession = remember { AVAudioSession.sharedInstance() }
    var showSettings by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var isRequesting by remember { mutableStateOf(false) }

    // Check if already authorized
    val currentPermission = remember { audioSession.recordPermission }
    if (currentPermission == AVAudioSessionRecordPermissionGranted) {
        onContinue()
        return
    }

    if (showSettings) {
        IosSettingsRedirectContent(
            title = "Microphone Access",
            description = "Microphone permission was denied. Please enable it in Settings to record audio during assessments.",
            steps = listOf(
                "Open Settings",
                "Scroll down and tap Privacy & Security",
                "Tap Microphone",
                "Enable the toggle for OneStep",
            ),
            onOpenSettings = { checker.openAppSettings() },
            onSkip = onSkip,
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .test(OSTTestTags.Permissions.MICROPHONE_SCREEN)
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Microphone Access",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "We need microphone access to record audio during walking assessments.",
                fontSize = 16.sp,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Mock alert preview
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                IosMockAlertDialog(
                    title = "\"OneStep\" Would Like to\nAccess the Microphone",
                    message = "Microphone access is used during walking assessments to provide audio-based feedback.",
                    buttons = listOf(
                        MockAlertButton(title = "Don't Allow"),
                        MockAlertButton(title = "OK", isHighlighted = true),
                    ),
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            PrimaryButton(
                text = if (isRequesting) "Requesting..." else "Continue",
                onClick = {
                    if (!isRequesting) {
                        isRequesting = true
                        scope.launch {
                            val granted = suspendCoroutine { continuation ->
                                audioSession.requestRecordPermission { granted ->
                                    continuation.resume(granted)
                                }
                            }
                            isRequesting = false
                            if (granted) {
                                onContinue()
                            } else {
                                showSettings = true
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .test(OSTTestTags.Permissions.PRIMARY_BUTTON),
                size = OSButtonSize.Big,
                enabled = !isRequesting,
            )
        }
    }
}
