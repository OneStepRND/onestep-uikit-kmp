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
import androidx.compose.runtime.Composable
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
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.ic_run_stars
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

/**
 * Motion/Fitness permission screen.
 *
 * - Request variant: Icon + description + "Allow" button to trigger the request.
 * - Settings variant: Icon + description + inline settings instructions + "Go to Settings" button.
 *
 * Return-from-Settings detection is handled by [ObserveReturnFromSettings].
 * Post-request polling is handled by [PermissionPollingEffect].
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
    val colors = LocalOSColors.current

    // Detect return from Settings and re-check motion permission
    ObserveReturnFromSettings {
        val status = checker.checkStatus(IosPermissionType.MOTION_FITNESS)
        if (status == IosPermissionStatus.GRANTED) {
            coordinator.onPermissionGranted(IosPermissionType.MOTION_FITNESS)
        }
    }

    // The CMMotionActivityManager system prompt can resolve in-process without firing
    // UIApplicationDidBecomeActiveNotification, so the observer alone can miss the grant —
    // keep polling while this screen is visible.
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            if (checker.checkStatus(IosPermissionType.MOTION_FITNESS) == IosPermissionStatus.GRANTED) {
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
        PermissionCloseButton { coordinator.onDismiss() }

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
                text = "🏃",
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

        DataUsageFooter(
            description = "We access your phone's motion sensors to analyze steps, balance, and how you walk. This helps us provide detailed, clinically relevant insights.",
        )
    }
}

@Composable
private fun MotionRequestContent(
    coordinator: IosPermissionFlowCoordinator,
    checker: IosPermissionChecker,
) {
    var requested by remember { mutableStateOf(false) }
    val colors = LocalOSColors.current

    // Poll for status changes after requesting (fire-and-forget + poll pattern)
    PermissionPollingEffect(
        requested = requested,
        permissionType = IosPermissionType.MOTION_FITNESS,
        checker = checker,
        coordinator = coordinator,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.neutral_m5)
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PermissionCloseButton { coordinator.onDismiss() }

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

        DataUsageFooter(
            description = "We access your phone's motion sensors to analyze steps, balance, and how you walk. This helps us provide detailed, clinically relevant insights.",
        )
    }
}
