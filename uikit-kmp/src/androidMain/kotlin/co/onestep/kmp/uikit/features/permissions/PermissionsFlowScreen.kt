package co.onestep.kmp.uikit.features.permissions

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import co.onestep.kmp.uikit.features.permissions.destinations.PermissionDestinations
import co.onestep.kmp.uikit.features.permissions.destinations.arPermissionRequestScreen
import co.onestep.kmp.uikit.features.permissions.destinations.arSettingsRedirect
import co.onestep.kmp.uikit.features.permissions.destinations.batteryOptimizationPermissionRequestScreen
import co.onestep.kmp.uikit.features.permissions.destinations.notificationPermissionRequestScreen
import co.onestep.kmp.uikit.features.permissions.destinations.notificationSettingsRedirect
import co.onestep.kmp.uikit.features.permissions.destinations.permissionExplanationDestination

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
    val navigator = rememberNavController()
    val currentDestination by viewModel.currentDestination.collectAsState()

    LaunchedEffect(currentDestination) {
        currentDestination?.let { destination ->
            if (navigator.currentDestination?.route != destination::class.java.simpleName) {
                navigator.navigate(destination) {
                    popUpTo(navigator.graph.startDestinationId) {
                        inclusive = false
                    }
                    launchSingleTop = true
                }
            }
        }
    }

    NavHost(
        modifier = modifier,
        navController = navigator,
        startDestination = startDestination,
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start) },
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start) },
        popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End) },
        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End) },
    ) {
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
    }
}
