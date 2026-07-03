package co.onestep.kmp.uikit.features.permissions.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import co.onestep.kmp.uikit.features.permissions.PermissionWizardViewModel
import co.onestep.kmp.uikit.features.permissions.PermissionsExplanationScreen
import kotlinx.serialization.Serializable

@Serializable
data object ExplanationScreenDestination : PermissionDestinations

internal fun NavGraphBuilder.permissionExplanationDestination(
    viewModel: PermissionWizardViewModel,
    onAskLater: () -> Unit,
) {
    composable<ExplanationScreenDestination> {
        PermissionsExplanationScreen(
            onClose = {
                viewModel.trackCloseButtonClick()
                onAskLater()
            },
            onContinue = {
                viewModel.setExplanationShown()
            },
        )
    }
}
