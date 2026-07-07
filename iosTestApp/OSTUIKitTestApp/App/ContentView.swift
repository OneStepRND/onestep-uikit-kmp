import SwiftUI
import OSTUIKit
import OSTUIKitKMP

struct ContentView: View {
    @EnvironmentObject var appState: AppState

    @State private var showConfigureFlow = false
    @State private var showRecordingWalk = false
    @State private var showRecordingTUG = false
    @State private var showPermissionsInApp = false
    @State private var showPermissionsBackground = false
    @State private var showCareLog = false
    @State private var showSettings = false
    @State private var showMeasurementPicker = false
    @State private var lastEvent: String?
    // Mirrors the library's global theme mode; the flows presented below read it when opened.
    @State private var themeMode: ThemeMode = OneStepUiKit.shared.themeMode

    var body: some View {
        NavigationStack {
            List {
                Section("Theme") {
                    Picker("Theme", selection: $themeMode) {
                        Text("Light").tag(ThemeMode.light)
                        Text("Dark").tag(ThemeMode.dark)
                        Text("System").tag(ThemeMode.system)
                    }
                    .pickerStyle(.segmented)
                    .accessibilityIdentifier("home.theme")
                    .onChange(of: themeMode) { newValue in
                        OneStepUiKit.shared.setThemeMode(mode: newValue)
                    }
                }

                Section("Recording Flows") {
                    Button {
                        showConfigureFlow = true
                    } label: {
                        Label("Configure & Record", systemImage: "slider.horizontal.3")
                    }
                    .accessibilityIdentifier("home.configureAndRecord")

                    Button {
                        showRecordingWalk = true
                    } label: {
                        Label("Walk Recording", systemImage: "figure.walk")
                    }
                    .accessibilityIdentifier("home.walkRecording")

                    Button {
                        showRecordingTUG = true
                    } label: {
                        Label("Timed Up & Go", systemImage: "figure.stand")
                    }
                    .accessibilityIdentifier("home.tug")
                }

                Section("Screens") {
                    Button {
                        showPermissionsInApp = true
                    } label: {
                        Label("Permission Flow (In-App)", systemImage: "lock.shield")
                    }
                    .accessibilityIdentifier("home.permissionInApp")

                    Button {
                        showPermissionsBackground = true
                    } label: {
                        Label("Permission Flow (Background)", systemImage: "lock.rotation")
                    }
                    .accessibilityIdentifier("home.permissionBackground")

                    Button {
                        showMeasurementPicker = true
                    } label: {
                        Label("Measurement Summary", systemImage: "chart.bar")
                    }
                    .accessibilityIdentifier("home.measurementSummary")

                    Button {
                        showCareLog = true
                    } label: {
                        Label("Care Log", systemImage: "list.clipboard")
                    }
                    .accessibilityIdentifier("home.careLog")
                }

                if let lastEvent {
                    Section("Last Event") {
                        Text(lastEvent)
                            .font(.caption)
                            .foregroundStyle(.secondary)
                            .accessibilityIdentifier("home.lastEvent")
                    }
                }
            }
            .navigationTitle("UIKit KMP Test")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button {
                        showSettings = true
                    } label: {
                        Image(systemName: "gear")
                    }
                    .accessibilityIdentifier("home.settings")
                }
            }
            // Configure Flow presents the recording flow itself (fullScreenCover over the sheet).
            .sheet(isPresented: $showConfigureFlow) {
                ConfigureFlowView { label in
                    lastEvent = label
                }
            }
            .fullScreenCover(isPresented: $showRecordingWalk) {
                OSTRecordingFlowView(
                    config: OSTRecordingConfiguration.companion.defaultWalk(walkInstructions: nil),
                    onResult: { event in
                        lastEvent = "Walk result: \(event.name)"
                        showRecordingWalk = false
                    },
                    onDismiss: {
                        showRecordingWalk = false
                    }
                )
                .ignoresSafeArea()
            }
            .fullScreenCover(isPresented: $showRecordingTUG) {
                OSTRecordingFlowView(
                    config: OSTRecordingConfiguration.companion.tug(
                        instructions: nil,
                        preRecordingQuestions: nil,
                        postRecordingQuestions: nil,
                        didYouUseHandsTitle: "Did you use hands for support?",
                        useOfHandsDescription: "Use of hands",
                        usedHands: "Used hands",
                        didNotUseHands: "Did not use hands"
                    ),
                    onResult: { event in
                        lastEvent = "TUG result: \(event.name)"
                        showRecordingTUG = false
                    },
                    onDismiss: {
                        showRecordingTUG = false
                    }
                )
                .ignoresSafeArea()
            }
            .fullScreenCover(isPresented: $showPermissionsInApp) {
                OSTPermissionFlowView(mode: .inApp) { granted in
                    lastEvent = "Permissions (in-app): \(granted ? "granted" : "denied")"
                    showPermissionsInApp = false
                }
                .ignoresSafeArea()
            }
            .fullScreenCover(isPresented: $showPermissionsBackground) {
                OSTPermissionFlowView(mode: .background) { granted in
                    lastEvent = "Permissions (background): \(granted ? "granted" : "denied")"
                    showPermissionsBackground = false
                }
                .ignoresSafeArea()
            }
            .fullScreenCover(isPresented: $showCareLog) {
                OSTCareLogView(
                    onClose: {
                        showCareLog = false
                    }
                )
                .ignoresSafeArea()
            }
            .sheet(isPresented: $showSettings) {
                SettingsView(appState: appState)
            }
            .sheet(isPresented: $showMeasurementPicker) {
                MeasurementPickerView { measurement in
                    showMeasurementPicker = false
                }
            }
        }
    }
}

// Allow OSTRecordingConfiguration to drive a `.fullScreenCover(item:)`.
extension OSTRecordingConfiguration: @retroactive Identifiable {
    public var id: String { uuid }
}

// MARK: - Measurement Picker (loads recent measurements for summary view)

struct MeasurementPickerView: View {
    let onSelect: (OSTMotionMeasurement) -> Void

    @Environment(\.dismiss) private var dismiss
    @State private var measurements: [OSTMotionMeasurement] = []
    @State private var isLoading = true
    @State private var selectedMeasurement: OSTMotionMeasurement?

    var body: some View {
        NavigationStack {
            Group {
                if isLoading {
                    ProgressView("Loading measurements...")
                        .accessibilityIdentifier("summary.loading")
                } else if measurements.isEmpty {
                    VStack(spacing: 12) {
                        Image(systemName: "chart.bar.xaxis")
                            .font(.system(size: 48))
                            .foregroundStyle(.secondary)
                        Text("No Measurements")
                            .font(.headline)
                        Text("Record a walk or TUG first to see the summary.")
                            .font(.subheadline)
                            .foregroundStyle(.secondary)
                            .multilineTextAlignment(.center)
                    }
                    .padding()
                    .accessibilityIdentifier("summary.empty")
                } else {
                    List(measurements, id: \.id) { measurement in
                        Button {
                            selectedMeasurement = measurement
                        } label: {
                            VStack(alignment: .leading) {
                                Text(measurement.type.name)
                                    .font(.headline)
                                Text("ID: \(measurement.id)")
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                            }
                        }
                        .accessibilityIdentifier("summary.row")
                    }
                }
            }
            .navigationTitle("Select Measurement")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                        .accessibilityIdentifier("summary.cancel")
                }
            }
            .fullScreenCover(item: $selectedMeasurement) { measurement in
                OSTMeasurementSummaryView(
                    measurement: measurement,
                    onDismiss: {
                        selectedMeasurement = nil
                        dismiss()
                    }
                )
                .ignoresSafeArea()
            }
            .task {
                await loadMeasurements()
            }
        }
    }

    @MainActor
    private func loadMeasurements() async {
        measurements = fetchRecentKmpMeasurements(limit: 20)
        isLoading = false
    }
}

// Make OSTMotionMeasurement identifiable for SwiftUI List
extension OSTMotionMeasurement: @retroactive Identifiable {}
