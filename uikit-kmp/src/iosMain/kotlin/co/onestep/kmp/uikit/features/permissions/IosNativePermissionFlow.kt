package co.onestep.kmp.uikit.features.permissions

import platform.UIKit.UIViewController

/**
 * Factory for a host-provided native (Swift) permission-flow view controller.
 *
 * When a host app registers a factory (via
 * [co.onestep.kmp.uikit.OSTUIKitIos.registerNativePermissionFlowFactory]), the KMP iOS permission
 * flow presents the factory-created [UIViewController] modally instead of the built-in Compose
 * fallback. This lets an app plug in the real native `OSTPermissionsFlow` (SwiftUI) while keeping
 * the KMP entry points unchanged.
 *
 * The created controller owns its own presentation style and dismisses itself; it must invoke
 * exactly one of the callbacks:
 * - [onComplete] with `true`/`false` once permissions have been resolved, or
 * - [onDismiss] if the user closed the flow without granting the required permissions.
 *
 * @see co.onestep.kmp.uikit.OSTUIKitIos.registerNativePermissionFlowFactory
 */
fun interface IosNativePermissionFlowViewControllerFactory {
    /**
     * @param mode The permission mode requested by the flow.
     * @param onComplete Invoked once with whether the required permissions were granted.
     * @param onDismiss Invoked if the user dismissed the flow without granting permissions.
     */
    fun create(
        mode: OSTPermissionMode,
        onComplete: (Boolean) -> Unit,
        onDismiss: () -> Unit,
    ): UIViewController
}

/**
 * Internal holder for the optionally host-registered native permission-flow factory.
 * `null` means no factory is registered and the built-in Compose flow is used as a fallback.
 */
internal object IosNativePermissionFlowRegistry {
    var factory: IosNativePermissionFlowViewControllerFactory? = null
}
