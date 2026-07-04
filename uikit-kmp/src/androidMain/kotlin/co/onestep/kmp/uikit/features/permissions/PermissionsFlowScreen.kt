package co.onestep.kmp.uikit.features.permissions

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import co.onestep.kmp.uikit.features.permissions.destinations.PermissionDestinations
import co.onestep.kmp.uikit.features.permissions.destinations.arPermissionRequestScreen
import co.onestep.kmp.uikit.features.permissions.destinations.arSettingsRedirect
import co.onestep.kmp.uikit.features.permissions.destinations.batteryOptimizationPermissionRequestScreen
import co.onestep.kmp.uikit.features.permissions.destinations.notificationPermissionRequestScreen
import co.onestep.kmp.uikit.features.permissions.destinations.notificationSettingsRedirect
import co.onestep.kmp.uikit.features.permissions.destinations.permissionExplanationDestination
import co.onestep.kmp.uikit.navigation.UIktNavDisplay
import co.onestep.kmp.uikit.navigation.pop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PermissionsFlowScreen(
    modifier: Modifier = Modifier,
    viewModel: PermissionWizardViewModel,
    onGoToSystemSettings: () -> Unit,
    onBackPress: () -> Unit,
    onRequestPermissions: () -> Unit,
    startDestination: PermissionDestinations,
) {
    // Android-only flow: the reflection-based rememberNavBackStack overload handles saving the
    // stack without explicit serializer registration (unavailable on iOS, not needed here).
    val backStack = rememberNavBackStack(startDestination)
    val currentDestination by viewModel.currentDestination.collectAsState()

    // The wizard is state-driven: the ViewModel emits the destination to show and the stack
    // always keeps the [start, current] shape the previous popUpTo(start) + launchSingleTop
    // navigation produced.
    LaunchedEffect(currentDestination) {
        currentDestination?.let { destination ->
            if (backStack.lastOrNull() != destination) {
                while (backStack.size > 1) {
                    backStack.removeAt(backStack.size - 1)
                }
                if (destination != backStack.first()) {
                    backStack.add(destination)
                }
            }
        }
    }

    UIktNavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = { backStack.pop() },
        entryProvider = entryProvider {
            arPermissionRequestScreen(
                onRequestPermissions = { onRequestPermissions() },
                onBackPress = onBackPress,
                viewModel = viewModel,
            )
            permissionExplanationDestination(
                viewModel = viewModel,
                onAskLater = onBackPress,
            )
            arSettingsRedirect(
                onAskLater = onBackPress,
                onGoToSystemSettings = onGoToSystemSettings,
                viewModel = viewModel,
            )
            notificationPermissionRequestScreen(
                onRequestPermissions = onRequestPermissions,
                onBackPress = onBackPress,
                viewModel = viewModel,
            )
            notificationSettingsRedirect(
                onAskLater = onBackPress,
                onGoToSystemSettings = onGoToSystemSettings,
                viewModel = viewModel,
            )
            batteryOptimizationPermissionRequestScreen(
                onRequestPermissions = onRequestPermissions,
                onBackPress = onBackPress,
                viewModel = viewModel,
            )
        },
    )
}
