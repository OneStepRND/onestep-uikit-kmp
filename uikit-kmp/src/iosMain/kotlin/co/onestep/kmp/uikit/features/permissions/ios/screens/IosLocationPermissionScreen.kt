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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.TextButton
import co.onestep.designsystem.components.OSButtonSize
import co.onestep.designsystem.components.PrimaryButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import co.onestep.kmp.uikit.features.permissions.ios.LocationPhase
import co.onestep.kmp.uikit.features.permissions.ios.components.DataUsageInfoSheet
import co.onestep.kmp.uikit.features.permissions.ios.components.IosSettingsRedirectContent
import co.onestep.designsystem.components.OSText
import co.onestep.designsystem.theme.LocalOSColors
import co.onestep.designsystem.components.TertiaryButton
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.ic_location_services
import co.onestep.kmp.uikit_kmp.generated.resources.ic_routes_red_stars
import co.onestep.kmp.uikit_kmp.generated.resources.ic_trend_up_stars
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import platform.CoreLocation.CLLocationManager
import platform.Foundation.NSNotificationCenter
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.darwin.NSObjectProtocol

/**
 * Location permission screen with support for both While Using and Always phases.
 *
 * - Phase 1 (WhileUsing): Icon + description + "Allow" button
 * - Phase 2 (Always): Icon + description + "Allow" button
 * - Settings variant: Icon + inline instructions + "Go to Settings" button
 * - Downgraded variant: Red icon + limited access messaging + settings instructions
 *
 * Detects return from Settings via UIApplicationDidBecomeActiveNotification.
 */
@Composable
internal fun IosLocationPermissionScreen(
    coordinator: IosPermissionFlowCoordinator,
    screen: IosPermissionScreen.Location,
    checker: IosPermissionChecker,
) {
    val permissionType = when (screen.phase) {
        LocationPhase.WHILE_USING -> IosPermissionType.LOCATION_WHILE_USING
        LocationPhase.ALWAYS -> IosPermissionType.LOCATION_ALWAYS
    }

    // If location services are globally disabled, go straight to settings
    val locationServicesEnabled = remember { CLLocationManager.locationServicesEnabled() }
    if (!locationServicesEnabled && !screen.showSettings) {
        IosSettingsRedirectContent(
            title = "Location Services Disabled",
            description = "Location Services are turned off on this device. Please enable them in Settings to continue.",
            steps = listOf(
                "Open Settings",
                "Tap Privacy & Security",
                "Tap Location Services",
                "Turn on Location Services",
            ),
            onOpenSettings = { checker.openAppSettings() },
            onSkip = { coordinator.nextScreen() },
        )
        return
    }

    if (screen.showSettings) {
        LocationSettingsContent(
            coordinator = coordinator,
            checker = checker,
            phase = screen.phase,
            permissionType = permissionType,
            isDowngraded = screen.isDowngraded,
        )
    } else {
        LocationRequestContent(
            coordinator = coordinator,
            checker = checker,
            phase = screen.phase,
            permissionType = permissionType,
        )
    }
}

@Composable
private fun LocationSettingsContent(
    coordinator: IosPermissionFlowCoordinator,
    checker: IosPermissionChecker,
    phase: LocationPhase,
    permissionType: IosPermissionType,
    isDowngraded: Boolean,
) {
    var showDataUsage by remember { mutableStateOf(false) }
    val colors = LocalOSColors.current

    // Observe app becoming active (returning from Settings)
    var becameActive by remember { mutableStateOf(false) }
    DisposableEffect(Unit) {
        val observer: NSObjectProtocol = NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIApplicationDidBecomeActiveNotification,
            `object` = null,
            queue = null,
        ) { _ ->
            becameActive = true
        }
        onDispose {
            NSNotificationCenter.defaultCenter.removeObserver(observer)
        }
    }

    LaunchedEffect(becameActive) {
        if (becameActive) {
            becameActive = false
            coordinator.onReturnFromSettings(permissionType)
        }
    }

    // Choose icon, title, description based on phase and downgraded state
    val icon: DrawableResource
    val title: String
    val description: String
    val instructionItems: List<Pair<String, String>> // (icon, label) pairs

    if (isDowngraded) {
        icon = Res.drawable.ic_routes_red_stars
        title = "Location access is currently limited"
        description = "Turn location access back on to analyze your walks, even when the app is closed, for the most accurate, real-life insights."
        instructionItems = listOf(
            "\u2611" to "Select Location",
            "\u2713" to "Tap Always",
        )
    } else {
        when (phase) {
            LocationPhase.WHILE_USING -> {
                icon = Res.drawable.ic_location_services
                title = "In order to monitor your measurements, access to your location is required"
                description = "Please allow access to your location while the app is in use."
                instructionItems = listOf(
                    "\u2611" to "Select Location",
                    "\u2713" to "Tap 'While using'",
                )
            }
            LocationPhase.ALWAYS -> {
                icon = Res.drawable.ic_trend_up_stars
                title = "Get better assessments with real-life walks"
                description = "Please allow access to your location even when the app is closed."
                instructionItems = listOf(
                    "\u2611" to "Select Location",
                    "\u2713" to "Tap Always",
                )
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
        // Close button (X)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = { coordinator.onDismiss() }) {
                OSText(
                    text = "\u2715",
                    fontSize = 20.sp,
                    color = colors.neutral_p1,
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Icon
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(if (isDowngraded) 142.dp else if (phase == LocationPhase.WHILE_USING) 140.dp else 105.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        OSText(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colors.neutral_p3,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(12.dp))

        OSText(
            text = description,
            fontSize = 16.sp,
            color = colors.neutral_p2,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Inline settings instructions
        OSText(
            text = "Go to your device settings and then",
            fontSize = 15.sp,
            color = colors.neutral_p2,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Instruction items
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.padding(horizontal = 32.dp),
        ) {
            instructionItems.forEach { (checkIcon, label) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OSText(
                        text = checkIcon,
                        fontSize = 18.sp,
                        color = if (checkIcon == "\u2611") colors.primary_0 else colors.success_p3,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OSText(
                        text = label,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.neutral_p3,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        PrimaryButton(
            text = "Go to Settings",
            onClick = {
                coordinator.trackGoToSettings(permissionType)
                checker.openAppSettings()
            },
            modifier = Modifier.fillMaxWidth(),
            size = OSButtonSize.Big,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // "How is my data used?" link
        TertiaryButton(
            text = "How is my data used?",
            onClick = { showDataUsage = true },
        )

        if (showDataUsage) {
            DataUsageInfoSheet(
                description = "We use motion-related location data during walks and other standard movement measurements. This helps us analyze your movement, but we never track or store your exact location.",
                onDismiss = { showDataUsage = false },
            )
        }
    }
}

@Composable
private fun LocationRequestContent(
    coordinator: IosPermissionFlowCoordinator,
    checker: IosPermissionChecker,
    phase: LocationPhase,
    permissionType: IosPermissionType,
) {
    var requested by remember { mutableStateOf(false) }
    var showDataUsage by remember { mutableStateOf(false) }
    val colors = LocalOSColors.current

    // Poll for status changes after requesting
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

    val icon: DrawableResource
    val title: String
    val description: String

    when (phase) {
        LocationPhase.WHILE_USING -> {
            icon = Res.drawable.ic_location_services
            title = "In order to monitor your measurements, access to your location is required"
            description = "Please allow access to your location while the app is open. When prompted tap 'Allow While Using App'"
        }
        LocationPhase.ALWAYS -> {
            icon = Res.drawable.ic_trend_up_stars
            title = "Get better assessments with real-life walks"
            description = "Please allow access to your location even when the app is closed. When prompted, select 'Change to Always Allow'"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.neutral_m5)
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Close button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = { coordinator.onDismiss() }) {
                OSText(
                    text = "\u2715",
                    fontSize = 20.sp,
                    color = colors.neutral_p1,
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Icon
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(if (phase == LocationPhase.WHILE_USING) 140.dp else 105.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        OSText(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colors.neutral_p3,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(12.dp))

        OSText(
            text = description,
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
                    coordinator.trackPermissionRequested(permissionType)
                    when (phase) {
                        LocationPhase.WHILE_USING -> checker.requestLocationWhenInUse()
                        LocationPhase.ALWAYS -> checker.requestLocationAlways()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            size = OSButtonSize.Big,
            enabled = !requested,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // "How is my data used?" link
        TertiaryButton(
            text = "How is my data used?",
            onClick = { showDataUsage = true },
        )

        if (showDataUsage) {
            DataUsageInfoSheet(
                description = "We use motion-related location data during walks and other standard movement measurements. This helps us analyze your movement, but we never track or store your exact location.",
                onDismiss = { showDataUsage = false },
            )
        }
    }
}
