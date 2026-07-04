package co.onestep.kmp.uikit.features.permissions.destinations

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import co.onestep.kmp.uikit.features.permissions.PermissionWizardViewModel
import co.onestep.kmp.uikit.features.permissions.PermissionsExplanationScreen
import kotlinx.serialization.Serializable

@Serializable
data object ExplanationScreenDestination : PermissionDestinations

internal fun EntryProviderScope<NavKey>.permissionExplanationDestination(
    viewModel: PermissionWizardViewModel,
    onAskLater: () -> Unit,
) {
    entry<ExplanationScreenDestination> {
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
