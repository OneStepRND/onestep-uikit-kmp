import SwiftUI
import OSTUIKit
import OSTUIKitKMP

// MARK: - Measurement presets

/// A selectable recording preset, mirroring `androidTestApp`'s `ConfigureFlowScreen.presetConfigurations()`.
/// `key` doubles as the accessibility identifier suffix so XCUITests can select any activity
/// deterministically (`activity.WALK`, `activity.STATIC_BALANCE`, ...).
struct MeasurementPreset: Identifiable {
    let key: String
    let displayName: String
    let makeConfig: () -> OSTRecordingConfiguration

    var id: String { key }
}

enum MeasurementPresets {
    /// Same ordering and set as the Android test app's Configure Flow dropdown.
    static let all: [MeasurementPreset] = [
        MeasurementPreset(key: "WALK", displayName: "Walk") {
            OSTRecordingConfiguration.companion.defaultWalk(walkInstructions: nil)
        },
        MeasurementPreset(key: "STS", displayName: "STS") {
            OSTRecordingConfiguration.companion.sts(
                instructions: nil,
                preRecordingQuestions: nil,
                postRecordingQuestions: nil,
                didYouUseHandsTitle: "Did you use hands for support?",
                useOfHandsDescription: "Use of hands",
                usedHands: "Used hands",
                didNotUseHands: "Did not use hands"
            )
        },
        MeasurementPreset(key: "TUG", displayName: "TUG") {
            OSTRecordingConfiguration.companion.tug(
                instructions: nil,
                preRecordingQuestions: nil,
                postRecordingQuestions: nil,
                didYouUseHandsTitle: "Did you use hands for support?",
                useOfHandsDescription: "Use of hands",
                usedHands: "Used hands",
                didNotUseHands: "Did not use hands"
            )
        },
        MeasurementPreset(key: "BALANCE_TEST", displayName: "Balance Test") {
            OSTRecordingConfiguration.companion.balanceTest(instructions: nil)
        },
        MeasurementPreset(key: "STATIC_BALANCE", displayName: "Static Balance") {
            OSTRecordingConfiguration.companion.staticBalance(
                instructions: nil,
                balance: MeasurementPresets.defaultBalance()
            )
        },
        MeasurementPreset(key: "SIX_MINUTE_WALK", displayName: "6 Min Walk") {
            OSTRecordingConfiguration.companion.sixMinuteWalk(instructions: nil)
        },
        MeasurementPreset(key: "TWO_MINUTE_WALK", displayName: "2 Min Walk") {
            OSTRecordingConfiguration.companion.twoMinuteWalk(instructions: nil)
        },
        MeasurementPreset(key: "DUAL_TASK", displayName: "Dual Task") {
            OSTRecordingConfiguration.companion.dualTaskSubtract(
                instructions: nil,
                showSummaryScreen: OSTSummaryOptionsFull(),
                showInstructions: true,
                ttsSpeechText: "Count backwards from 100 by 3",
                postRecordingQuestions: nil,
                postTaggingData: nil
            )
        },
    ]

    /// Reconstructs the Kotlin `OSTBalance()` default (its no-arg default constructor is not
    /// bridged to Swift) using the companion factory + default keys, so Static Balance starts
    /// with the same Condition Setup schema the library ships.
    static func defaultBalance() -> OSTBalance {
        OSTBalance(
            categories: OSTBalance.companion.defaultCategories(),
            resultStates: [],
            notesKey: OSTBalance.companion.DEFAULT_NOTES_KEY,
            resultStatesKey: OSTBalance.companion.DEFAULT_RESULT_STATES_KEY
        )
    }
}

// MARK: - Mock recording

/// Drives the SDK's mock-recording path. When `MockRecordingName` is set in `UserDefaults` and a
/// matching `<name>.json.gz` is bundled in `Bundle.main`, the SDK swaps the real recorded data for
/// the mock payload at S3 upload time (`SDKNetworkService.mockFileData()`), so the backend analyzes
/// the mock and returns a real, deterministic measurement — even on a stationary device. This is
/// the iOS analogue of Android's `setMockIMU`, and is UI-agnostic (works with the KMP recording
/// flow). Mirrors the SDK Example app's `UIKitSelectFlowView`.
enum MockRecording {
    /// The `UserDefaults` key the SDK reads at upload time.
    static let userDefaultsKey = "MockRecordingName"

    /// "None" + the bundled mock names (each has a `<name>.json.gz` in `Resources/JSONmocks/`),
    /// same set the SDK Example app exposes. `success*` mocks analyze to a full measurement;
    /// `error_*` mocks analyze to the corresponding error/empty-analysis outcome.
    static let none = "None"
    static let options = [
        none,
        "successWalk", "stsSuccess", "tugSuccess", "dualTaskSuccess", "romSuccess",
        "error_curvy", "error_no_cycle", "error_position", "error_other", "error_short", "error_static",
    ]

    /// Persists (or clears) the selection so the SDK picks it up at the next upload.
    static func apply(_ selection: String) {
        if selection == none {
            UserDefaults.standard.removeObject(forKey: userDefaultsKey)
        } else {
            UserDefaults.standard.set(selection, forKey: userDefaultsKey)
        }
    }

    /// Clears the key so a mock never leaks into a later real recording.
    static func clear() {
        UserDefaults.standard.removeObject(forKey: userDefaultsKey)
    }
}

// MARK: - Toggle application

extension OSTRecordingConfiguration {
    /// Applies the three configurable booleans exposed on the Configure Flow screen. The KMP data
    /// class `copy` is bridged as `doCopy(...)` requiring every field (Kotlin defaults are not
    /// bridged), so we pass all current properties through and override only the toggles.
    func withToggles(
        playVoiceOver: Bool,
        showPermissionExplanationScreen: Bool
    ) -> OSTRecordingConfiguration {
        doCopy(
            uuid: uuid,
            activityType: activityType,
            instructions: instructions,
            duration: duration,
            isCountingDown: isCountingDown,
            prepareScreenData: prepareScreenData,
            playVoiceOver: playVoiceOver,
            preRecordingQuestions: preRecordingQuestions,
            shouldRecordGeoLocation: shouldRecordGeoLocation,
            showSummaryScreen: showSummaryScreen,
            showPermissionExplanationScreen: showPermissionExplanationScreen,
            readyForAnalysisUiAssist: readyForAnalysisUiAssist,
            sensorEnhancedMode: sensorEnhancedMode,
            postTaggingData: postTaggingData,
            showPreRecordingAssistiveDeviceSelection: showPreRecordingAssistiveDeviceSelection,
            showPreRecordingFootwearSelection: showPreRecordingFootwearSelection,
            balance: balance,
            hallwayLengthMeters: hallwayLengthMeters
        )
    }
}

// MARK: - Configure Flow screen

/// Native SwiftUI screen mirroring `androidTestApp`'s ConfigureFlowScreen: pick any activity type,
/// flip the shared toggles, then start the recording flow. Every control carries an accessibility
/// identifier so the XCUITest suite can drive any measurement deterministically.
///
/// The recording flow is presented as this screen's own `fullScreenCover` (over the Configure Flow
/// sheet) rather than being handed back to the host. Presenting it here avoids the dismiss-sheet /
/// present-cover race that a hand-off through the host would introduce, and keeps navigation
/// (start → record → dismiss → back on Configure Flow) deterministic for XCUITest.
struct ConfigureFlowView: View {
    /// Reports a finished/dismissed recording as a short, PHI-free label (e.g. "WALK: <eventName>").
    var onEvent: (String) -> Void = { _ in }

    @Environment(\.dismiss) private var dismiss

    @State private var selectedKey: String = MeasurementPresets.all.first?.key ?? "WALK"
    @State private var playVoiceOver = true
    @State private var showPermissionExplanationScreen = true
    @State private var selectedMock = MockRecording.none
    @State private var pendingConfig: OSTRecordingConfiguration?

    private var selectedPreset: MeasurementPreset {
        MeasurementPresets.all.first { $0.key == selectedKey } ?? MeasurementPresets.all[0]
    }

    var body: some View {
        NavigationStack {
            Form {
                Section("Activity") {
                    ForEach(MeasurementPresets.all) { preset in
                        Button {
                            selectedKey = preset.key
                        } label: {
                            HStack {
                                Text(preset.displayName)
                                    .foregroundStyle(.primary)
                                Spacer()
                                if preset.key == selectedKey {
                                    Image(systemName: "checkmark")
                                        .foregroundStyle(.tint)
                                }
                            }
                        }
                        .accessibilityIdentifier("activity.\(preset.key)")
                    }
                }

                Section("Options") {
                    Toggle("Play voice over", isOn: $playVoiceOver)
                        .accessibilityIdentifier("toggle.voiceOver")
                    Toggle("Show permission explanation", isOn: $showPermissionExplanationScreen)
                        .accessibilityIdentifier("toggle.permissionExplanation")
                }

                // Mock recording: replaces the uploaded data with a bundled analyzed recording, so
                // the flow produces a deterministic result on a stationary device (see MockRecording).
                Section("Mock recording") {
                    Picker("Mock recording", selection: $selectedMock) {
                        ForEach(MockRecording.options, id: \.self) { name in
                            Text(name).tag(name)
                        }
                    }
                    .pickerStyle(.menu)
                    .accessibilityIdentifier("mockRecordingPicker")
                }

                Section {
                    Button {
                        startFlow()
                    } label: {
                        Text("START FLOW")
                            .frame(maxWidth: .infinity)
                            .fontWeight(.semibold)
                    }
                    .accessibilityIdentifier("configure.startLarge")
                }
            }
            .navigationTitle("Configure Flow")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                        .accessibilityIdentifier("configure.cancel")
                }
                // Always-visible Start (the large in-form button can sit below the fold, so it is
                // not guaranteed to be in the accessibility tree for XCUITest).
                ToolbarItem(placement: .confirmationAction) {
                    Button("Start") { startFlow() }
                        .fontWeight(.semibold)
                        .accessibilityIdentifier("configure.start")
                }
            }
            .fullScreenCover(item: $pendingConfig) { config in
                OSTRecordingFlowView(
                    config: config,
                    onResult: { event in
                        // `event.properties` is a Kotlin Map bridged as [AnyHashable: Any]; only
                        // `recording_completed` carries `measurement_id`, so its presence lets the
                        // XCUITest suite tell a real analyzed result from an error/dismiss.
                        let measurementId = event.properties["measurement_id"] as? String
                        let idSuffix = measurementId.map { " (id:\($0.prefix(8)))" } ?? ""
                        onEvent("\(config.activityType.name): \(event.name)\(idSuffix)")
                        MockRecording.clear()
                        pendingConfig = nil
                    },
                    onDismiss: {
                        MockRecording.clear()
                        pendingConfig = nil
                    }
                )
                .ignoresSafeArea()
            }
        }
    }

    /// Applies the selected mock (if any) then presents the recording flow. Setting the mock here —
    /// at the moment recording starts — means it is in place before the SDK's upload swap runs.
    private func startFlow() {
        MockRecording.apply(selectedMock)
        pendingConfig = currentConfig()
    }

    private func currentConfig() -> OSTRecordingConfiguration {
        selectedPreset.makeConfig().withToggles(
            playVoiceOver: playVoiceOver,
            showPermissionExplanationScreen: showPermissionExplanationScreen
        )
    }
}
