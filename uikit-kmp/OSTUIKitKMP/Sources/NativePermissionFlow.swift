import SwiftUI
import UIKit
import CoreMotion
import CoreLocation
import OSTUIKit

// MARK: - Native permission-flow host

/// Presents the REAL native `OSTPermissionsFlow(mode:)` (SwiftUI) inside a modally-presented
/// `UIHostingController`, mirroring the native `OSTRecordingFlow` `.fullScreenCover` + notification
/// consumption pattern.
///
/// The KMP iOS permission gate (`PlatformPermissionFlow.ios.kt`) presents this controller modally.
/// `OSTPermissionsFlow` self-dismisses (via SwiftUI `dismiss()`) on success, critical-denial, and
/// user-close. It posts `CriticalPermissionsDenied` / `PermissionsFlowDismissedWithoutPermissions`
/// only on the failure paths — the success path just dismisses with no notification. So this host:
///   - observes both notifications (→ failure);
///   - when the inner flow dismisses itself, re-checks the required permissions for the mode
///     (mirroring `PermissionsFlowCoordinator.areRequiredPermissionsMissing`);
///   - calls `onComplete(granted)` exactly once, or `onDismiss()` on the user-close-without-perms path.
final class NativePermissionFlowHostingController: UIHostingController<NativePermissionFlowRootView> {

    private let mode: PermissionFlowMode
    private let onComplete: (Bool) -> Void
    private let onDismiss: () -> Void

    private var didFinish = false
    private var sawFailureNotification = false
    private var sawUserDismissNotification = false

    private let motionManager = CMMotionActivityManager()
    private let locationManager = CLLocationManager()

    init(
        mode: PermissionFlowMode,
        onComplete: @escaping (Bool) -> Void,
        onDismiss: @escaping () -> Void
    ) {
        self.mode = mode
        self.onComplete = onComplete
        self.onDismiss = onDismiss

        // Root view presents OSTPermissionsFlow full-screen and reports when it dismisses.
        var innerDismissed: () -> Void = {}
        let root = NativePermissionFlowRootView(mode: mode) { innerDismissed() }
        super.init(rootView: root)

        innerDismissed = { [weak self] in self?.handleInnerFlowDismissed() }

        modalPresentationStyle = .fullScreen
        overrideUserInterfaceStyle = .light

        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleCriticalDenied),
            name: NSNotification.Name("CriticalPermissionsDenied"),
            object: nil
        )
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleDismissedWithoutPermissions),
            name: NSNotification.Name("PermissionsFlowDismissedWithoutPermissions"),
            object: nil
        )
    }

    @available(*, unavailable)
    @MainActor required dynamic init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    deinit {
        NotificationCenter.default.removeObserver(self)
    }

    @objc private func handleCriticalDenied() {
        sawFailureNotification = true
    }

    @objc private func handleDismissedWithoutPermissions() {
        sawFailureNotification = true
        sawUserDismissNotification = true
    }

    /// Called when the inner `OSTPermissionsFlow` has dismissed itself (success, critical-denial,
    /// or user-close). Resolves the outcome exactly once and dismisses this modal host.
    private func handleInnerFlowDismissed() {
        guard !didFinish else { return }
        didFinish = true

        // A notification may arrive in the same run-loop turn as the dismiss; give it a beat.
        Task { @MainActor in
            let granted = !sawFailureNotification && !requiredPermissionsMissing()
            let userDismissed = sawUserDismissNotification
            self.dismiss(animated: true) { [weak self] in
                guard let self else { return }
                if userDismissed && !granted {
                    self.onDismiss()
                } else {
                    self.onComplete(granted)
                }
            }
        }
    }

    /// Mirrors `PermissionsFlowCoordinator.areRequiredPermissionsMissing()` for the synchronous
    /// (non-HealthKit) portion. HealthKit is treated as satisfied after the flow (per plan): a
    /// `.healthKit`/`.full` flow that completed without a failure notification counts as granted.
    private func requiredPermissionsMissing() -> Bool {
        let motionStatus = CMMotionActivityManager.authorizationStatus()
        let locationStatus = locationManager.authorizationStatus

        switch mode {
        case .inApp:
            let motionMissing = motionStatus != .authorized
            let locationMissing = locationStatus != .authorizedWhenInUse && locationStatus != .authorizedAlways
            return motionMissing || locationMissing
        case .background, .full:
            let motionMissing = motionStatus != .authorized
            let locationMissing = locationStatus != .authorizedAlways
            return motionMissing || locationMissing
        case .healthKit:
            // Treated as granted after the flow (see plan §2); no synchronous status to re-check.
            return false
        @unknown default:
            return false
        }
    }
}

// MARK: - Root SwiftUI wrapper

/// Thin SwiftUI wrapper that immediately presents `OSTPermissionsFlow(mode:)` full-screen and
/// reports back when that cover is dismissed, mirroring the native `OSTRecordingFlow` pattern.
public struct NativePermissionFlowRootView: View {
    private let mode: PermissionFlowMode
    private let onDismissed: () -> Void

    @State private var isPresented = true

    init(mode: PermissionFlowMode, onDismissed: @escaping () -> Void) {
        self.mode = mode
        self.onDismissed = onDismissed
    }

    public var body: some View {
        Color.white
            .ignoresSafeArea()
            .fullScreenCover(isPresented: $isPresented, onDismiss: { onDismissed() }) {
                OSTPermissionsFlow(mode: mode)
            }
    }
}

// MARK: - KMP mode mapping + registration

/// Maps the KMP `OSTPermissionMode` (Swift-visible cases `.inApp/.background/.healthKit/.full`)
/// to the native `PermissionFlowMode`.
private func nativeMode(from mode: OSTPermissionMode) -> PermissionFlowMode {
    switch mode {
    case .inApp: return .inApp
    case .background: return .background
    case .healthKit: return .healthKit
    case .full: return .full
    default: return .inApp
    }
}

/// Factory implementation the KMP registry invokes to build a native permission-flow controller.
/// The Kotlin `fun interface IosNativePermissionFlowViewControllerFactory` exports as an ObjC
/// protocol, so we conform with a small Swift class.
final class NativePermissionFlowFactory: NSObject, IosNativePermissionFlowViewControllerFactory {
    func create(
        mode: OSTPermissionMode,
        onComplete: @escaping (KotlinBoolean) -> Void,
        onDismiss: @escaping () -> Void
    ) -> UIViewController {
        NativePermissionFlowHostingController(
            mode: nativeMode(from: mode),
            onComplete: { granted in onComplete(KotlinBoolean(bool: granted)) },
            onDismiss: onDismiss
        )
    }
}

/// Registers the native permission flow with the KMP UIKit so every permission gate presents the
/// real `OSTPermissionsFlow` instead of the Compose fallback.
public enum OSTUIKitKMPNativePermissions {
    public static func register() {
        OSTUIKitIos.shared.registerNativePermissionFlowFactory(factory: NativePermissionFlowFactory())
    }
}
