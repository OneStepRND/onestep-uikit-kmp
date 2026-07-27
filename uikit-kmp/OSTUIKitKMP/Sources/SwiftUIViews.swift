import SwiftUI
import OSTUIKit

// MARK: - Recording Flow

/// SwiftUI view for the OneStep recording flow.
///
/// Presents a full-screen recording experience including preparation screens,
/// countdown, recording, and optional post-tagging.
///
/// ```swift
/// OSTRecordingFlowView(
///     config: OSTRecordingConfiguration.companion.defaultWalk(),
///     onResult: { event in handleResult(event) },
///     onFinished: { result in
///         // result.summaryUrl -> present a web view
///     },
///     onDismiss: { dismiss() }
/// )
/// ```
public struct OSTRecordingFlowView: UIViewControllerRepresentable {
    private let config: OSTRecordingConfiguration
    private let patientId: String?
    private let onResult: (OSTEvent) -> Void
    private let onFinished: ((OSTRecordingFlowResult) -> Void)?
    private let onDismiss: (() -> Void)?

    /// - Parameter patientId: Clinician-mode selector. `nil` (default) records for the current
    ///   authenticated user. A non-nil OneStep patient UUID records patient-scoped for that patient
    ///   (requires the native SDK wiring from `configureOSTUIKitKMPWithNativeSDK()`). Never logged.
    /// - Parameter onFinished: Terminal result of the flow — the measurement id plus the
    ///   `summaryUrl` to open in a web view — delivered immediately before `onDismiss`. Fires only
    ///   when the flow produced an analyzed measurement. Requires `onDismiss` to be non-nil.
    public init(
        config: OSTRecordingConfiguration,
        patientId: String? = nil,
        onResult: @escaping (OSTEvent) -> Void,
        onFinished: ((OSTRecordingFlowResult) -> Void)? = nil,
        onDismiss: (() -> Void)? = nil
    ) {
        self.config = config
        self.patientId = patientId
        self.onResult = onResult
        self.onFinished = onFinished
        self.onDismiss = onDismiss
    }

    public func makeUIViewController(context: Context) -> UIViewController {
        if let onFinished, let onDismiss {
            return OSTUIKitIos.shared.createRecordingFlowViewController(
                config: config,
                patientId: patientId,
                onResult: onResult,
                onFinished: onFinished,
                onDismiss: onDismiss
            )
        }
        if let onDismiss {
            return OSTUIKitIos.shared.createRecordingFlowViewController(
                config: config,
                patientId: patientId,
                onResult: onResult,
                onDismiss: onDismiss
            )
        }
        return OSTUIKitIos.shared.createRecordingFlowViewController(
            config: config,
            patientId: patientId,
            onResult: onResult
        )
    }

    public func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

// MARK: - Permission Flow

/// SwiftUI view for the OneStep permission flow.
///
/// Guides the user through requesting required permissions
/// (activity recognition, notifications, and optionally battery optimization).
///
/// ```swift
/// OSTPermissionFlowView { granted in
///     if granted { startRecording() }
/// }
/// ```
public struct OSTPermissionFlowView: UIViewControllerRepresentable {
    private let mode: OSTPermissionMode
    private let onComplete: (Bool) -> Void

    public init(
        mode: OSTPermissionMode = .inApp,
        onComplete: @escaping (Bool) -> Void
    ) {
        self.mode = mode
        self.onComplete = onComplete
    }

    public func makeUIViewController(context: Context) -> UIViewController {
        OSTUIKitIos.shared.createPermissionFlowViewController(
            mode: mode,
            onComplete: { granted in self.onComplete(granted.boolValue) }
        )
    }

    public func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

// MARK: - Measurement Summary

/// SwiftUI view for displaying a measurement summary.
///
/// Shows gait parameters, norms comparison, insights, and activity details
/// for a completed measurement.
///
/// ```swift
/// OSTMeasurementSummaryView(
///     measurement: measurement,
///     onDismiss: { dismiss() }
/// )
/// ```
public struct OSTMeasurementSummaryView: UIViewControllerRepresentable {
    private let measurement: OSTMotionMeasurement
    private let options: any OSTSummaryOptions
    private let onDismiss: () -> Void

    public init(
        measurement: OSTMotionMeasurement,
        options: any OSTSummaryOptions = OSTSummaryOptionsFull(),
        onDismiss: @escaping () -> Void = {}
    ) {
        self.measurement = measurement
        self.options = options
        self.onDismiss = onDismiss
    }

    public func makeUIViewController(context: Context) -> UIViewController {
        OSTUIKitIos.shared.createMeasurementSummaryViewController(
            measurement: measurement,
            options: options,
            onDismiss: onDismiss
        )
    }

    public func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

// MARK: - Care Log

/// SwiftUI view for the OneStep care log.
///
/// Displays historical measurements with tabs for in-app and background data,
/// daily summaries, and navigation to individual measurement details.
///
/// ```swift
/// OSTCareLogView(onClose: { dismiss() })
/// ```
public struct OSTCareLogView: UIViewControllerRepresentable {
    private let onClose: () -> Void

    public init(onClose: @escaping () -> Void = {}) {
        self.onClose = onClose
    }

    public func makeUIViewController(context: Context) -> UIViewController {
        OSTUIKitIos.shared.createCareLogViewController(onClose: onClose)
    }

    public func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

// MARK: - Push/Pop Transition Demo (test harnesses only)

/// SwiftUI view demoing the Compose-implemented Cupertino push/pop transition and the
/// interactive edge-swipe back gesture. Not consumer API.
///
/// ```swift
/// OSTPushPopDemoView(onDismiss: { dismiss() })
/// ```
public struct OSTPushPopDemoView: UIViewControllerRepresentable {
    private let onDismiss: () -> Void

    public init(onDismiss: @escaping () -> Void = {}) {
        self.onDismiss = onDismiss
    }

    public func makeUIViewController(context: Context) -> UIViewController {
        OSTUIKitIos.shared.createPushPopDemoViewController(onDismiss: onDismiss)
    }

    public func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
