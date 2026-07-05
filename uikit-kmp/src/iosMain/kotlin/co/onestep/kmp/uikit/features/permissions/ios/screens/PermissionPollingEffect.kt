package co.onestep.kmp.uikit.features.permissions.ios.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import co.onestep.kmp.uikit.features.permissions.ios.IosPermissionChecker
import co.onestep.kmp.uikit.features.permissions.ios.IosPermissionFlowCoordinator
import co.onestep.kmp.uikit.features.permissions.ios.IosPermissionStatus
import co.onestep.kmp.uikit.features.permissions.ios.IosPermissionType
import kotlinx.coroutines.delay

/**
 * Shared polling effect for post-request permission status detection.
 *
 * After [requested] becomes true, waits 1 second then polls [checker] every 500ms
 * for up to 30 seconds. Calls [coordinator.onPermissionGranted] or
 * [coordinator.onPermissionDenied] as appropriate.
 */
@Composable
internal fun PermissionPollingEffect(
    requested: Boolean,
    permissionType: IosPermissionType,
    checker: IosPermissionChecker,
    coordinator: IosPermissionFlowCoordinator,
) {
    LaunchedEffect(requested) {
        if (requested) {
            // Give the system dialog time to appear and be interacted with
            delay(1000)
            var attempts = 0
            val maxAttempts = 60 // 30 seconds max
            while (attempts < maxAttempts) {
                delay(500)
                attempts++
                val status = checker.checkStatus(permissionType)
                when (status) {
                    IosPermissionStatus.GRANTED -> {
                        coordinator.onPermissionGranted(permissionType)
                        return@LaunchedEffect
                    }
                    IosPermissionStatus.DENIED,
                    IosPermissionStatus.RESTRICTED -> {
                        coordinator.onPermissionDenied(permissionType)
                        return@LaunchedEffect
                    }
                    IosPermissionStatus.NOT_DETERMINED -> {
                        // Still waiting for user response, keep polling
                    }
                }
            }
            // Timed out waiting — treat as denied
            coordinator.onPermissionDenied(permissionType)
        }
    }
}
