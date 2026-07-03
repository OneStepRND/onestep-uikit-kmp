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
///     onResult: { event in handleResult(event) }
/// )
/// ```
public struct OSTRecordingFlowView: UIViewControllerRepresentable {
    private let config: OSTRecordingConfiguration
    private let onResult: (OSTEvent) -> Void
    private let onDismiss: (() -> Void)?

    public init(
        config: OSTRecordingConfiguration,
        onResult: @escaping (OSTEvent) -> Void,
        onDismiss: (() -> Void)? = nil
    ) {
        self.config = config
        self.onResult = onResult
        self.onDismiss = onDismiss
    }

    public func makeUIViewController(context: Context) -> UIViewController {
        if let onDismiss {
            return OSTUIKitIos.shared.createRecordingFlowViewController(
                config: config,
                onResult: onResult,
                onDismiss: onDismiss
            )
        }
        return OSTUIKitIos.shared.createRecordingFlowViewController(
            config: config,
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
