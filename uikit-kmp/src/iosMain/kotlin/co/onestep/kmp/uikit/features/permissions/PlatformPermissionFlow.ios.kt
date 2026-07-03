package co.onestep.kmp.uikit.features.permissions

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import co.onestep.kmp.uikit.di.UIKitServiceLocator
import co.onestep.kmp.uikit.features.permissions.ios.IosPermissionChecker
import co.onestep.kmp.uikit.features.permissions.ios.IosPermissionFlowCoordinator
import co.onestep.kmp.uikit.features.permissions.ios.IosPermissionScreen
import co.onestep.kmp.uikit.features.permissions.ios.screens.IosHealthKitPermissionScreen
import co.onestep.kmp.uikit.features.permissions.ios.screens.IosLocationPermissionScreen
import co.onestep.kmp.uikit.features.permissions.ios.screens.IosMotionPermissionScreen
import co.onestep.kmp.uikit.features.permissions.ios.screens.IosRationalizationScreen
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/**
 * iOS implementation of the permission flow.
 *
 * If a host has registered a native permission-flow factory via
 * [co.onestep.kmp.uikit.OSTUIKitIos.registerNativePermissionFlowFactory], the factory's
 * `UIViewController` is presented modally (it owns its own presentation style and dismisses
 * itself); this composable renders only a plain white backdrop underneath. Otherwise the built-in
 * multi-screen Compose flow is used.
 */
@Composable
internal actual fun PlatformPermissionFlow(
    mode: OSTPermissionMode,
    showExplanationScreen: Boolean,
    onComplete: (granted: Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val factory = IosNativePermissionFlowRegistry.factory
    if (factory != null) {
        NativePermissionFlowHost(
            factory = factory,
            mode = mode,
            onComplete = onComplete,
            onDismiss = onDismiss,
        )
    } else {
        ComposePermissionFlow(
            mode = mode,
            showExplanationScreen = showExplanationScreen,
            onComplete = onComplete,
            onDismiss = onDismiss,
        )
    }
}

/**
 * Presents a host-provided native permission-flow view controller modally from the top-most view
 * controller and forwards its result. The native controller dismisses itself; Kotlin only performs
 * a defensive dismiss on disposal if the controller is still presented.
 */
@Composable
private fun NativePermissionFlowHost(
    factory: IosNativePermissionFlowViewControllerFactory,
    mode: OSTPermissionMode,
    onComplete: (granted: Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    // Fire-at-most-once, main-thread callback guard shared by the factory and cleanup path.
    // All access happens on the main thread (LaunchedEffect body, native callbacks dispatched to
    // the main queue, and onDispose), so plain mutable state is sufficient.
    val callbacks = remember { OnceCallbacks(onComplete, onDismiss) }
    val holder = remember { PresentedControllerHolder() }

    LaunchedEffect(Unit) {
        val controller = factory.create(
            mode = mode,
            onComplete = { granted -> callbacks.complete(granted) },
            onDismiss = { callbacks.dismiss() },
        )
        holder.controller = controller
        topMostViewController()?.presentViewController(controller, animated = true, completion = null)
    }

    DisposableEffect(Unit) {
        onDispose {
            // Defensive cleanup: if the native controller is still presented (e.g. the composable
            // was torn down before it dismissed itself), dismiss it. The callback guard prevents a
            // double result. The native side normally dismisses itself, so this is a no-op then.
            val controller = holder.controller
            holder.controller = null
            if (controller != null && controller.presentingViewController != null) {
                dispatch_async(dispatch_get_main_queue()) {
                    controller.dismissViewControllerAnimated(true, completion = null)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    )
}

/** Holds the presented controller across recompositions for defensive disposal. */
private class PresentedControllerHolder {
    var controller: UIViewController? = null
}

/**
 * Ensures each of [onComplete]/[onDismiss] fires at most once (across both), always on the main
 * thread.
 */
private class OnceCallbacks(
    private val onComplete: (Boolean) -> Unit,
    private val onDismiss: () -> Unit,
) {
    private var fired = false

    fun complete(granted: Boolean) {
        if (!fired) {
            fired = true
            dispatch_async(dispatch_get_main_queue()) { onComplete(granted) }
        }
    }

    fun dismiss() {
        if (!fired) {
            fired = true
            dispatch_async(dispatch_get_main_queue()) { onDismiss() }
        }
    }
}

/**
 * Returns the top-most presented view controller from the key window's root, walking the
 * `presentedViewController` chain so the modal is presented above anything already on screen.
 */
@OptIn(ExperimentalForeignApi::class)
private fun topMostViewController(): UIViewController? {
    // shortcut: keyWindow is deprecated on iOS 13+ but remains functional; acceptable until a
    // connectedScenes-based lookup is warranted (single-window app).
    @Suppress("DEPRECATION")
    var controller = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (controller?.presentedViewController != null) {
        controller = controller.presentedViewController
    }
    return controller
}

/**
 * Built-in fallback: a full multi-screen Compose permission flow with rationalization screens,
 * mock system alert previews, settings redirects, and mode-based permission sequencing.
 */
@Composable
private fun ComposePermissionFlow(
    mode: OSTPermissionMode,
    showExplanationScreen: Boolean,
    onComplete: (granted: Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val checker = remember { IosPermissionChecker() }
    val preferencesBridge = UIKitServiceLocator.preferencesBridge
    val analyticsHandler = UIKitServiceLocator.analyticsHandler
    val coordinator = remember {
        IosPermissionFlowCoordinator(mode, checker, onComplete, onDismiss, preferencesBridge, analyticsHandler, showExplanationScreen)
    }

    LaunchedEffect(Unit) {
        coordinator.initialize()
    }

    val screen by coordinator.currentScreen.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        AnimatedContent(
            targetState = screen,
            transitionSpec = {
                (slideInHorizontally { it } + fadeIn())
                    .togetherWith(slideOutHorizontally { -it } + fadeOut())
            },
        ) { currentScreen ->
            when (currentScreen) {
                is IosPermissionScreen.Rationalization ->
                    IosRationalizationScreen(coordinator = coordinator)

                is IosPermissionScreen.Motion ->
                    IosMotionPermissionScreen(
                        coordinator = coordinator,
                        screen = currentScreen,
                        checker = checker,
                    )

                is IosPermissionScreen.Location ->
                    IosLocationPermissionScreen(
                        coordinator = coordinator,
                        screen = currentScreen,
                        checker = checker,
                    )

                is IosPermissionScreen.HealthKit ->
                    IosHealthKitPermissionScreen(
                        coordinator = coordinator,
                        screen = currentScreen,
                        checker = checker,
                    )

                is IosPermissionScreen.Completed -> {
                    // onComplete already called by coordinator
                }
            }
        }
    }
}
