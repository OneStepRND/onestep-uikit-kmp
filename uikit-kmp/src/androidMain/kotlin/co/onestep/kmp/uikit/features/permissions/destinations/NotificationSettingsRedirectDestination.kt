package co.onestep.kmp.uikit.features.permissions.destinations

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import co.onestep.kmp.uikit.features.permissions.OSTPermissionMode
import co.onestep.kmp.uikit.features.permissions.PermissionDataFactory
import co.onestep.kmp.uikit.features.permissions.PermissionRequestScreen
import co.onestep.kmp.uikit.features.permissions.PermissionWizardViewModel
import co.onestep.kmp.uikit.features.recordFlow.screensData.IconData
import co.onestep.kmp.uikit.features.recordFlow.screensData.ToolBarData
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.ic_close
import kotlinx.serialization.Serializable

@Serializable
data class NotificationSettingsRedirectDestination(
    val permissionMode: OSTPermissionMode,
) : PermissionDestinations

internal fun NavGraphBuilder.notificationSettingsRedirect(
    onAskLater: () -> Unit,
    onGoToSystemSettings: () -> Unit,
    viewModel: PermissionWizardViewModel,
) {
    composable<NotificationSettingsRedirectDestination> { backStackEntry ->
        var showInfo by remember { mutableStateOf(false) }
        val args = backStackEntry.toRoute<NotificationSettingsRedirectDestination>()
        val mode = args.permissionMode

        val screenData =
            PermissionDataFactory.notificationSettingsRequest(
                permissionMode = mode,
                onSelection = {
                    viewModel.trackGoToSettingsButtonClick()
                    onGoToSystemSettings()
                },
                toolBarData =
                    ToolBarData(
                        endIcons =
                            listOf(
                                IconData(
                                    icon = Res.drawable.ic_close,
                                    action = {
                                        viewModel.trackCloseButtonClick()
                                        onAskLater()
                                    },
                                ),
                            ),
                    ),
            )

        PermissionRequestScreen(
            screenData =
                screenData.copy(
                    title = screenData.title?.copy(color = LocalOSColors.current.primary_p3_main),
                ),
            onBackPress = {
                viewModel.trackCloseButtonClick()
                onAskLater()
            },
            showInfo = showInfo,
            onInfoToggle = {
                if (!showInfo) viewModel.trackDataUsageInfoClick()
                showInfo = it
            },
        )
    }
}
