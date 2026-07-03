package co.onestep.kmp.uikit.features.permissions.ios.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import co.onestep.designsystem.components.OSButtonSize
import co.onestep.designsystem.components.PrimaryButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.features.permissions.ios.IosPermissionChecker
import co.onestep.kmp.uikit.features.permissions.ios.IosPermissionFlowCoordinator
import co.onestep.kmp.uikit.features.permissions.ios.IosPermissionScreen
import co.onestep.kmp.uikit.features.permissions.ios.IosPermissionStatus
import co.onestep.kmp.uikit.features.permissions.ios.IosPermissionType
import co.onestep.kmp.uikit.features.permissions.ios.components.DataUsageInfoSheet
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.designsystem.components.TertiaryButton
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.ic_run_stars
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import platform.Foundation.NSNotificationCenter
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.darwin.NSObjectProtocol

/**
 * Motion/Fitness permission screen.
 *
 * - Request variant: Icon + description + "Allow" button to trigger the request.
 * - Settings variant: Icon + description + inline settings instructions + "Go to Settings" button.
 *
 * Polls CMMotionActivityManager.authorizationStatus() every 500ms to auto-advance.
 */
@Composable
internal fun IosMotionPermissionScreen(
    coordinator: IosPermissionFlowCoordinator,
    screen: IosPermissionScreen.Motion,
    checker: IosPermissionChecker,
) {
    if (screen.showSettings) {
        MotionSettingsContent(coordinator = coordinator, checker = checker)
    } else {
        MotionRequestContent(coordinator = coordinator, checker = checker)
    }
}

@Composable
private fun MotionSettingsContent(
    coordinator: IosPermissionFlowCoordinator,
    checker: IosPermissionChecker,
) {
    var showDataUsage by remember { mutableStateOf(false) }
    val colors = LocalOSColors.current

    // Observe app becoming active (returning from Settings)
    var becameActive by remember { mutableStateOf(false) }
    DisposableEffect(Unit) {
        val observer: NSObjectProtocol = NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIApplicationDidBecomeActiveNotification,
            `object` = null,
            queue = null,
        ) { _ ->
            becameActive = true
        }
        onDispose {
            NSNotificationCenter.defaultCenter.removeObserver(observer)
        }
    }

    LaunchedEffect(becameActive) {
        if (becameActive) {
            becameActive = false
            val status = checker.checkStatus(IosPermissionType.MOTION_FITNESS)
            if (status == IosPermissionStatus.GRANTED) {
                coordinator.onPermissionGranted(IosPermissionType.MOTION_FITNESS)
            }
        }
    }

    // Also poll for status changes
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            val status = checker.checkStatus(IosPermissionType.MOTION_FITNESS)
            if (status == IosPermissionStatus.GRANTED) {
                coordinator.onPermissionGranted(IosPermissionType.MOTION_FITNESS)
                break
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.neutral_m5)
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Close button (X) — dismisses the flow
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = { coordinator.onDismiss() }) {
                OSText(
                    text = "\u2715",
                    fontSize = 20.sp,
                    color = colors.neutral_p1,
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Icon
        Image(
            painter = painterResource(Res.drawable.ic_run_stars),
            contentDescription = null,
            modifier = Modifier.size(105.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        OSText(
            text = "Get deeper insights into your movement",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colors.neutral_p3,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(12.dp))

        OSText(
            text = "Please allow access to your Motion & Fitness data so we can provide the most accurate assessments.",
            fontSize = 16.sp,
            color = colors.neutral_p2,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Inline settings instruction
        OSText(
            text = "Go to your device settings and then toggle on",
            fontSize = 15.sp,
            color = colors.neutral_p2,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Motion & Fitness badge
        Row(
            modifier = Modifier
                .background(
                    color = colors.neutral_m2,
                    shape = RoundedCornerShape(8.dp),
                )
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            OSText(
                text = "\uD83C\uDFC3",
                fontSize = 20.sp,
            )
            Spacer(modifier = Modifier.size(8.dp))
            OSText(
                text = "Motion & Fitness",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = colors.neutral_p3,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        PrimaryButton(
            text = "Go to Settings",
            onClick = {
                coordinator.trackGoToSettings(IosPermissionType.MOTION_FITNESS)
                checker.openAppSettings()
            },
            modifier = Modifier.fillMaxWidth(),
            size = OSButtonSize.Big,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // "How is my data used?" link
        TertiaryButton(
            text = "How is my data used?",
            onClick = { showDataUsage = true },
        )

        if (showDataUsage) {
            DataUsageInfoSheet(
                description = "We access your phone's motion sensors to analyze steps, balance, and how you walk. This helps us provide detailed, clinically relevant insights.",
                onDismiss = { showDataUsage = false },
            )
        }
    }
}

@Composable
private fun MotionRequestContent(
    coordinator: IosPermissionFlowCoordinator,
    checker: IosPermissionChecker,
) {
    var requested by remember { mutableStateOf(false) }
    var showDataUsage by remember { mutableStateOf(false) }
    val colors = LocalOSColors.current

    // Poll for status changes after requesting (fire-and-forget + poll pattern)
    LaunchedEffect(requested) {
        if (requested) {
            // Give the system dialog time to appear and be interacted with
            delay(1000)
            var attempts = 0
            val maxAttempts = 60 // 30 seconds max
            while (attempts < maxAttempts) {
                delay(500)
                attempts++
                val status = checker.checkStatus(IosPermissionType.MOTION_FITNESS)
                when (status) {
                    IosPermissionStatus.GRANTED -> {
                        coordinator.onPermissionGranted(IosPermissionType.MOTION_FITNESS)
                        return@LaunchedEffect
                    }
                    IosPermissionStatus.DENIED,
                    IosPermissionStatus.RESTRICTED -> {
                        coordinator.onPermissionDenied(IosPermissionType.MOTION_FITNESS)
                        return@LaunchedEffect
                    }
                    IosPermissionStatus.NOT_DETERMINED -> {
                        // Still waiting for user response, keep polling
                    }
                }
            }
            // Timed out waiting — treat as denied
            coordinator.onPermissionDenied(IosPermissionType.MOTION_FITNESS)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.neutral_m5)
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Close button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = { coordinator.onDismiss() }) {
                OSText(
                    text = "\u2715",
                    fontSize = 20.sp,
                    color = colors.neutral_p1,
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Icon
        Image(
            painter = painterResource(Res.drawable.ic_run_stars),
            contentDescription = null,
            modifier = Modifier.size(105.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        OSText(
            text = "Get deeper insights into your movement",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colors.neutral_p3,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(12.dp))

        OSText(
            text = "Please allow access to your Motion & Fitness data so we can provide the most accurate assessments.",
            fontSize = 16.sp,
            color = colors.neutral_p2,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.weight(1f))

        PrimaryButton(
            text = if (requested) "Waiting..." else "Allow",
            onClick = {
                if (!requested) {
                    requested = true
                    // Fire-and-forget: trigger the system dialog, polling handles the result
                    checker.requestMotion()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            size = OSButtonSize.Big,
            enabled = !requested,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // "How is my data used?" link
        TertiaryButton(
            text = "How is my data used?",
            onClick = { showDataUsage = true },
        )

        if (showDataUsage) {
            DataUsageInfoSheet(
                description = "We access your phone's motion sensors to analyze steps, balance, and how you walk. This helps us provide detailed, clinically relevant insights.",
                onDismiss = { showDataUsage = false },
            )
        }
    }
}
