import SwiftUI
import OSTUIKit
import OSTUIKitKMP

struct ContentView: View {
    @EnvironmentObject var appState: AppState

    @State private var showRecordingWalk = false
    @State private var showRecordingTUG = false
    @State private var showPermissions = false
    @State private var showCareLog = false
    @State private var showSettings = false
    @State private var showMeasurementPicker = false
    @State private var lastEvent: String?

    var body: some View {
        NavigationStack {
            List {
                Section("Recording Flows") {
                    Button {
                        showRecordingWalk = true
                    } label: {
                        Label("Walk Recording", systemImage: "figure.walk")
                    }

                    Button {
                        showRecordingTUG = true
                    } label: {
                        Label("Timed Up & Go", systemImage: "figure.stand")
                    }
                }

                Section("Screens") {
                    Button {
                        showPermissions = true
                    } label: {
                        Label("Permission Flow", systemImage: "lock.shield")
                    }

                    Button {
                        showMeasurementPicker = true
                    } label: {
                        Label("Measurement Summary", systemImage: "chart.bar")
                    }

                    Button {
                        showCareLog = true
                    } label: {
                        Label("Care Log", systemImage: "list.clipboard")
                    }
                }

                if let lastEvent {
                    Section("Last Event") {
                        Text(lastEvent)
                            .font(.caption)
                            .foregroundStyle(.secondary)
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
                }
            }
            .fullScreenCover(isPresented: $showRecordingWalk) {
                OSTRecordingFlowView(
                    config: OSTRecordingConfiguration.companion.defaultWalk(walkInstructions: nil),
                    onResult: { event in
                        lastEvent = "Walk result: \(event.name)"
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
                    }
                )
                .ignoresSafeArea()
            }
            .fullScreenCover(isPresented: $showPermissions) {
                OSTPermissionFlowView(
                    onComplete: { granted in
                        lastEvent = "Permissions: \(granted ? "granted" : "denied")"
                        showPermissions = false
                    }
                )
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
                    }
                }
            }
            .navigationTitle("Select Measurement")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
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
