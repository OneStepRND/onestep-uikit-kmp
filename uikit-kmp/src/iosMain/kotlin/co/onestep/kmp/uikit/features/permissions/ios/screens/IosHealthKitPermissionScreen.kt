package co.onestep.kmp.uikit.features.permissions.ios.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.features.permissions.ios.IosPermissionChecker
import co.onestep.kmp.uikit.features.permissions.ios.IosPermissionFlowCoordinator
import co.onestep.kmp.uikit.features.permissions.ios.IosPermissionScreen
import co.onestep.kmp.uikit.features.permissions.ios.IosPermissionType
import co.onestep.kmp.uikit.features.permissions.ios.components.IosSettingsRedirectContent
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.theme.LocalOSColors
import kotlinx.coroutines.launch

/**
 * HealthKit permission screen.
 *
 * - Request variant: HealthKit icon + description + "Continue" button.
 * - Settings variant: Instructions for navigating Health app settings.
 *
 * Requests: step count, walking step length, walking speed.
 */
@Composable
internal fun IosHealthKitPermissionScreen(
    coordinator: IosPermissionFlowCoordinator,
    screen: IosPermissionScreen.HealthKit,
    checker: IosPermissionChecker,
) {
    if (screen.showSettings) {
        IosSettingsRedirectContent(
            title = "HealthKit Access",
            description = "HealthKit permission was not fully granted. Please enable it in the Health app settings.",
            steps = listOf(
                "Open the Health app",
                "Tap your profile icon in the top right",
                "Tap \"Apps\" under Privacy",
                "Find and tap OneStep",
                "Enable the data types you'd like to share",
            ),
            onOpenSettings = { checker.openAppSettings() },
            onSkip = { coordinator.nextScreen() },
        )
    } else {
        HealthKitRequestContent(coordinator = coordinator, checker = checker)
    }
}

@Composable
private fun HealthKitRequestContent(
    coordinator: IosPermissionFlowCoordinator,
    checker: IosPermissionChecker,
) {
    val scope = rememberCoroutineScope()
    var isRequesting by remember { mutableStateOf(false) }
    val colors = LocalOSColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.neutral_m5)
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PermissionCloseButton { coordinator.onDismiss() }

        Spacer(modifier = Modifier.height(40.dp))

        // HealthKit heart icon
        OSText(
            text = "\u2764\uFE0F",
            fontSize = 64.sp,
        )

        Spacer(modifier = Modifier.height(24.dp))

        OSText(
            text = "HealthKit Access",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = colors.neutral_p3,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(12.dp))

        OSText(
            text = "We'd like to read your health data to provide comprehensive walking and mobility insights.",
            fontSize = 16.sp,
            color = colors.neutral_p2,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Data types we'll request
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = colors.neutral_m2,
                    shape = RoundedCornerShape(12.dp),
                )
                .padding(16.dp),
        ) {
            OSText(
                text = "Data we'll access:",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.neutral_p3,
            )
            Spacer(modifier = Modifier.height(8.dp))
            listOf(
                "Step Count",
                "Walking Step Length",
                "Walking Speed",
            ).forEach { item ->
                OSText(
                    text = "• $item",
                    fontSize = 15.sp,
                    color = colors.neutral_p3,
                    modifier = Modifier.padding(vertical = 2.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // "How is my data used?" link (PRD requirement)
        DataUsageFooter(
            description = "HealthKit data including step count, walking speed, and step length is used to " +
                "provide comprehensive walking and mobility insights. This data stays on your " +
                "device and is only shared with your healthcare provider when you choose to.",
        )

        Spacer(modifier = Modifier.weight(1f))

        PrimaryButton(
            text = if (isRequesting) "Requesting..." else "Continue",
            onClick = {
                if (!isRequesting) {
                    isRequesting = true
                    scope.launch {
                        val granted = checker.requestHealthKit()
                        isRequesting = false
                        if (granted) {
                            coordinator.onPermissionGranted(IosPermissionType.HEALTH_KIT)
                        } else {
                            coordinator.onPermissionDenied(IosPermissionType.HEALTH_KIT)
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            size = OSButtonSize.Big,
            enabled = !isRequesting,
        )
    }
}
