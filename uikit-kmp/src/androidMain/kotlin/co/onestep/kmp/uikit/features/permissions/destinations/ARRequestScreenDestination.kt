package co.onestep.kmp.uikit.features.permissions.destinations

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
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
data object ARRequestScreenDestination : PermissionDestinations

internal fun EntryProviderScope<NavKey>.arPermissionRequestScreen(
    onRequestPermissions: () -> Unit,
    onBackPress: () -> Unit,
    viewModel: PermissionWizardViewModel,
) {
    entry<ARRequestScreenDestination> {
        var showInfo by remember { mutableStateOf(false) }

        val screenData =
            PermissionDataFactory.physicalPermissionRequest(
                onSelection = {
                    viewModel.trackAllowButtonClick()
                    onRequestPermissions()
                },
                toolBarData =
                    ToolBarData(
                        endIcons =
                            listOf(
                                IconData(
                                    icon = Res.drawable.ic_close,
                                    action = {
                                        viewModel.trackCloseButtonClick()
                                        onBackPress()
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
                onBackPress()
            },
            showInfo = showInfo,
            onInfoToggle = {
                if (!showInfo) viewModel.trackDataUsageInfoClick()
                showInfo = it
            },
        )
    }
}
