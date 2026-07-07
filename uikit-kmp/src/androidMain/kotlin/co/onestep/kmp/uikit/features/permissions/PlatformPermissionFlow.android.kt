package co.onestep.kmp.uikit.features.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import co.onestep.designsystem.theme.LocalOSColors
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import co.onestep.kmp.uikit.OSTUIKitAnalyticsHandler
import co.onestep.kmp.uikit.bridge.SystemBarEffect
import co.onestep.kmp.uikit.di.UIKitServiceLocator
import co.onestep.kmp.uikit.features.permissions.analytics.PermissionAnalyticsTracker
import co.onestep.kmp.uikit.models.OSTEvent

/**
 * Android implementation of the permission flow.
 *
 * Uses Compose APIs to fully replicate the behavior that was previously
 * managed by OSTPermissionFlowActivity:
 * - [rememberLauncherForActivityResult] for runtime permission requests
 * - [rememberLauncherForActivityResult] for battery optimization intent
 * - [LifecycleEventObserver] for detecting return from Settings
 * - [LocalActivity] to obtain the Activity reference
 */
@Composable
internal actual fun PlatformPermissionFlow(
    mode: OSTPermissionMode,
    showExplanationScreen: Boolean,
    onComplete: (granted: Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val activity = LocalActivity.current ?: return
    val context = LocalContext.current

    // Force light mode: dark icons on light background
    SystemBarEffect(darkIcons = true)

    // Dependencies
    val prefs = UIKitServiceLocator.preferencesBridge
    val permissionsManager = remember { PermissionsManagerImpl(context, prefs) }
    val analyticsTracker = remember {
        val handler = UIKitServiceLocator.analyticsHandler
            ?: object : OSTUIKitAnalyticsHandler {
                override fun onEvent(event: OSTEvent) { /* no-op */ }
            }
        PermissionAnalyticsTracker(handler)
    }

    // ViewModel (scoped to this composable, not the Activity)
    val viewModel = remember {
        PermissionWizardViewModel(
            prefs = prefs,
            permissionsManager = permissionsManager,
            analyticsTracker = analyticsTracker,
            showPermissionExplanationScreen = showExplanationScreen,
            mode = mode,
        ).also { it.init(activity) }
    }

    // Permission launcher for runtime permissions (AR, notification, audio)
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        viewModel.processPermissionResult(
            activity = activity,
            granted = result.values.all { it },
        )
    }

    // Battery optimization launcher
    val batteryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isGranted = pm.isIgnoringBatteryOptimizations(context.packageName)
        viewModel.processPermissionResult(activity = activity, granted = isGranted)
    }

    // Lifecycle observer for returning from system Settings
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (viewModel.returningFromSettings.value) {
                    viewModel.checkPermissionsAfterSettings(activity)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Exit handling
    val shouldExit by viewModel.shouldExit.collectAsState()
    LaunchedEffect(shouldExit) {
        if (shouldExit) {
            val allGranted = viewModel.permissionStates.value.values.all { it.isGranted }
            onComplete(allGranted)
        }
    }

    // Callback: request the current permission via the appropriate launcher
    val onRequestPermissions: () -> Unit = {
        when (viewModel.currentPermissionType.value) {
            PermissionType.ACTIVITY_RECOGNITION -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    permissionLauncher.launch(arrayOf(Manifest.permission.ACTIVITY_RECOGNITION))
                }
            }
            PermissionType.POST_NOTIFICATIONS -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
                }
            }
            PermissionType.BATTERY_OPTIMIZATION -> {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                batteryLauncher.launch(intent)
            }
            PermissionType.RECORD_AUDIO -> {
                permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
            }
            else -> {}
        }
    }

    // Callback: open system app settings (for permanently denied permissions)
    val onGoToSystemSettings: () -> Unit = {
        viewModel.markGoingToSettings()
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }

    // UI - themed surface extends behind system bars to avoid gaps (white in light, dark in dark)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalOSColors.current.neutral_m5)
            .systemBarsPadding(),
    ) {
        PermissionsFlowScreen(
            modifier = Modifier.fillMaxSize(),
            viewModel = viewModel,
            onGoToSystemSettings = onGoToSystemSettings,
            onBackPress = { onDismiss() },
            onRequestPermissions = onRequestPermissions,
            startDestination = viewModel.determineStartDestination(activity),
        )
    }
}
