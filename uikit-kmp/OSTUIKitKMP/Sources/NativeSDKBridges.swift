import Foundation
import Combine
import UIKit
import OSTUIKit
import OneStepSDK

// MARK: - Activity-type mapping

/// The KMP `RecorderBridge` hands Swift the activity type as `OSTActivityType.name` — the *Kotlin*
/// enum constant name (e.g. `"WALK"`, `"SIX_MINUTE_WALK"`). Map those to the native
/// `OneStepSDK.OSTActivityType` (a String-raw-value Swift enum).
private func nativeActivityType(fromKmpName name: String) -> OneStepSDK.OSTActivityType {
    switch name.uppercased() {
    case "WALK": return .walk
    case "STS": return .sts
    case "TUG": return .tug
    case "ROM_KNEE_FLEX": return .romKneeFlexionPassive
    case "ROM_KNEE_EXT": return .romKneeExtension
    case "DUAL_TASK_WALK_SUBTRACT": return .dualTaskWalkSubtract
    case "SIX_MINUTE_WALK": return .sixMinWalk
    case "TWO_MINUTE_WALK": return .twoMinWalk
    case "STAIRS": return .stairs
    default: return .walk
    }
}

// MARK: - Native OSTMotionMeasurement -> KMP mapping

/// Builds a KMP `OSTMotionMeasurement` from a native `OneStepSDK.OSTMotionMeasurement` using the
/// iosMain mapper factories (`MeasurementMapperKt`). Grounded on the field shape observed in the
/// native UIKit consumers (`AnalyzingViewModel`, `MeasurementSummmaryDataSource`).
private func toKmp(_ m: OneStepSDK.OSTMotionMeasurement) -> KMPMotionMeasurement {
    // params: native [String: Double]? -> KMP [String: KotlinFloat]
    var kmpParams: [String: KotlinFloat] = [:]
    if let params = m.parameters {
        for (key, value) in params {
            kmpParams[key] = KotlinFloat(float: Float(value))
        }
    }

    // metadata
    // These fields come from the raw SDK struct (OneStepSDK). Note: `.steps` / `.analysisState` /
    // `.measuremetError` are *UIKit* extensions on OSTMotionMeasurement, NOT SDK members — the bridge
    // imports only OneStepSDK, so use the underlying members: metadata.steps, result_state, error.
    let meta = m.metadata
    let kmpMetadata = MeasurementMapperKt.createKmpMetadata(
        locale: meta?.locale,
        seconds: meta?.seconds.map { KotlinInt(int: Int32($0)) },
        steps: meta?.steps.map { KotlinInt(int: Int32($0)) },
        lastModified: nil,
        note: nil, // clinician notes are PHI — never propagate free text into analytics/models
        tags: meta?.tags ?? [],
        assistiveDevice: nil,
        levelOfAssistance: nil,
        walkCourseLength: nil,
        geoLat: meta?.geoLat.map { KotlinDouble(double: $0) },
        geoLng: meta?.geoLng.map { KotlinDouble(double: $0) },
        dataPath: nil,
        audioDataPath: nil
    )

    // error (raw OSTMeasurementError: code / message / details)
    var kmpError: KMPError? = nil
    if let nativeError = m.error {
        kmpError = KMPError(
            code: Int32(nativeError.code),
            message: nativeError.message,
            details: nativeError.details
        )
    }

    // status + resultState strings expected by the mapper
    let statusString: String
    switch m.status {
    case .SYNCED: statusString = "SYNCED"
    case .ANALYZED: statusString = "ANALYZED"
    default: statusString = "NOT_SYNCED"
    }

    let resultStateString: String?
    switch m.result_state {
    case .some(.FULL_ANALYSIS): resultStateString = "FULL_ANALYSIS"
    case .some(.PARTIAL_ANALYSIS): resultStateString = "PARTIAL_ANALYSIS"
    case .some(.EMPTY_ANALYSIS): resultStateString = "EMPTY_ANALYSIS"
    default: resultStateString = nil
    }

    let timestampMs = Int64(m.timestamp.timeIntervalSince1970 * 1000.0)

    // custom_metadata: [String: OSTMixedType]? -> [String: String] (KMP expects string values)
    var kmpCustom: [String: String] = [:]
    if let custom = m.custom_metadata {
        for (key, value) in custom {
            switch value {
            case .string(let s): kmpCustom[key] = s
            case .int(let i): kmpCustom[key] = String(i)
            case .double(let d): kmpCustom[key] = String(d)
            @unknown default: break
            }
        }
    }

    // parameter_arrays: [String: [Double]]? -> [String: [KotlinFloat]]
    var kmpParamArrays: [String: [KotlinFloat]] = [:]
    if let arrays = m.parameter_arrays {
        for (key, values) in arrays {
            kmpParamArrays[key] = values.map { KotlinFloat(float: Float($0)) }
        }
    }

    return MeasurementMapperKt.createKmpMeasurement(
        id: m.id.uuidString,
        timestamp: timestampMs,
        activityType: m.type,
        customMetadata: kmpCustom,
        metadata: kmpMetadata,
        params: kmpParams,
        parameterArrays: kmpParamArrays,
        status: statusString,
        error: kmpError,
        resultState: resultStateString
    )
}

// MARK: - Recorder delegate

/// Drives the native OneStep recorder for the KMP flow, exactly like `WalkRecordingStore`. Owns the
/// Combine subscriptions to `recorderState` / `analyzerState` / `stepsCount` and pushes updates into
/// the KMP `SwiftRecorderBridgeAdapter` (set via `attach(adapter:)` after construction).
final class NativeRecorderDelegate: NSObject, IosRecorderDelegate {

    private let recorder: (any OSTRecorderProtocol)?
    private let motionLab: (any MotionLab)?
    private weak var adapter: SwiftRecorderBridgeAdapter?

    private var recorderStateCancellable: AnyCancellable?
    private var analyzerStateCancellable: AnyCancellable?
    private var stepsCancellable: AnyCancellable?

    /// Latest analyze() result, delivered via the analyzerState publisher.
    private var analyzeContinuation: ((KMPMotionMeasurement?) -> Void)?

    /// Effective recording limit reported to the KMP flow (seconds -> ms). Defaults to the walk
    /// default (60s); updated on each start.
    private var recordingLimitMs: Int64 = 60_000

    override init() {
        if case .success(let onestep) = OneStep.shared(),
           case .success(let lab) = onestep.motionLab() {
            self.motionLab = lab
            self.recorder = lab.recorder
        } else {
            print("OSTUIKitKMP: OneStep SDK not initialized — recorder bridge is inert")
            self.motionLab = nil
            self.recorder = nil
        }
        super.init()
    }

    /// Wire the adapter back-reference and begin observing recorder/analyzer/steps publishers.
    func attach(adapter: SwiftRecorderBridgeAdapter) {
        self.adapter = adapter
        bindRecorderState()
        bindStepsCount()
    }

    // MARK: Publisher bindings

    private func bindRecorderState() {
        recorderStateCancellable = recorder?.recorderState
            .receive(on: RunLoop.main)
            .sink { [weak self] state in
                guard let self else { return }
                // Map native OSTRecorderState -> KMP OSTRecorderState name (toKmpRecorderState).
                // native: idle -> INITIALIZED, recording -> RECORDING, finishedRecording -> DONE.
                let name: String
                switch state {
                case .idle: name = "INITIALIZED"
                case .recording: name = "RECORDING"
                case .finishedRecording: name = "DONE"
                case .error: name = "DONE"
                @unknown default: name = "INITIALIZED"
                }
                self.adapter?.onRecorderStateChanged(stateName: name)
            }
    }

    private func bindStepsCount() {
        stepsCancellable = recorder?.stepsCount
            .receive(on: RunLoop.main)
            .sink { [weak self] steps in
                self?.adapter?.onStepsChanged(steps: Int32(steps))
            }
    }

    private func bindAnalyzerState() {
        analyzerStateCancellable = recorder?.analyzerState
            .receive(on: RunLoop.main)
            .sink { [weak self] state in
                guard let self else { return }
                switch state {
                case .idle:
                    self.adapter?.onAnalyserStateChanged(stateName: "IDLE", errorName: nil, errorMessage: nil)
                case .analyzing:
                    self.adapter?.onAnalyserStateChanged(stateName: "ANALYZING", errorName: nil, errorMessage: nil)
                case .analyzedAndSavedSuccessfully(let measurement):
                    self.adapter?.onAnalyserStateChanged(stateName: "ANALYZED", errorName: nil, errorMessage: nil)
                    self.finishAnalyze(with: toKmp(measurement))
                case .error(let errorInfo):
                    self.adapter?.onAnalyserStateChanged(
                        stateName: "FAILED",
                        errorName: "GENERAL",
                        errorMessage: errorInfo.message
                    )
                    self.finishAnalyze(with: nil)
                @unknown default:
                    break
                }
            }
    }

    private func finishAnalyze(with measurement: KMPMotionMeasurement?) {
        let continuation = analyzeContinuation
        analyzeContinuation = nil
        continuation?(measurement)
    }

    // MARK: IosRecorderDelegate

    func prepareForRecording(activityType: String, completion: @escaping (KotlinBoolean) -> Void) {
        // Native recorder has no explicit prepare step; report ready.
        completion(KotlinBoolean(bool: true))
    }

    func start(
        activityType: String,
        durationMs: Int64,
        sensorEnhancedMode: Bool,
        userInputMetadata: KMPUserInputMetaData?,
        customMetadata: [String: String]?,
        completion: @escaping () -> Void
    ) {
        let seconds = durationMs > 0 ? Int(durationMs / 1000) : nil
        recordingLimitMs = durationMs > 0 ? durationMs : 60_000

        // KMP OSTUserInputMetaData -> native OSTUserInputMetaData (tags only for the walk E2E;
        // assistiveDevice / levelOfAssistance / hallway length are set later via update paths).
        let nativeUserInput = OneStepSDK.OSTUserInputMetaData(
            note: nil, // never carry free-text note (PHI)
            tags: userInputMetadata?.tags,
            assistiveDevice: nil,
            levelOfAssistance: nil
        )

        var nativeCustom: [String: OSTMixedType] = [:]
        if let customMetadata {
            for (key, value) in customMetadata {
                nativeCustom[key] = .string(value)
            }
        }

        recorder?.start(
            activityType: nativeActivityType(fromKmpName: activityType),
            duration: seconds,
            userInputMetadata: nativeUserInput,
            customMetadata: nativeCustom
        )
        completion()
    }

    func stop(completion: @escaping () -> Void) {
        recorder?.stop()
        completion()
    }

    func reset() {
        recorder?.reset()
        analyzeContinuation = nil
    }

    func analyze(uuid: String?, timeoutMs: Int64, completion: @escaping (KMPMotionMeasurement?) -> Void) {
        guard let recorder else { completion(nil); return }
        // Bind to analyzer state (result is delivered through .analyzedAndSavedSuccessfully / .error).
        analyzeContinuation = completion
        bindAnalyzerState()
        Task {
            await recorder.analyze()
        }
    }

    func updateSixMinuteWalkCourseLength(uuid: String, walkCourseLength: KMPWalkCourseLength, completion: @escaping () -> Void) {
        // shortcut: 6MWT hallway update not needed for the walk-recording E2E; no-op.
        // Upgrade path: call motionLab.updateCourseLength(id:length:) with an OSTLength built from value/unit.
        completion()
    }

    func currentRecordingLimit() -> Int64 {
        recordingLimitMs
    }

    func addMarker(marker: String) {
        // shortcut: marker injection not exposed on OSTRecorderProtocol in SDK 1.7.1; no-op.
    }

    func readSingleMotionMeasurement(uuid: String, completion: @escaping (KMPMotionMeasurement?) -> Void) {
        guard let id = UUID(uuidString: uuid), let motionLab else { completion(nil); return }
        Task {
            let measurement = try? motionLab.getMeasurement(id: id)
            completion(measurement.map(toKmp))
        }
    }

    func readMotionMeasurements(
        limit: KotlinInt?,
        order: String?,
        activityType: String?,
        startTimeMs: KotlinLong?,
        endTimeMs: KotlinLong?,
        completion: @escaping ([KMPMotionMeasurement]) -> Void
    ) {
        // shortcut: care-log history not needed for the walk E2E; return empty. Upgrade path: build a
        // TimeRangedDataRequest from start/end and call motionLab.getMeasurements(request:).
        completion([])
    }

    func deleteMotionMeasurement(uuid: String, completion: @escaping () -> Void) {
        guard let id = UUID(uuidString: uuid), let motionLab else { completion(); return }
        Task {
            try? await motionLab.deleteMeasurement(id: id)
            completion()
        }
    }

    func updateMotionMeasurement(uuid: String, metadata: KMPUserInputMetaData, completion: @escaping () -> Void) {
        guard let id = UUID(uuidString: uuid), let motionLab else { completion(); return }
        // KMP -> native metadata: tags only (note is PHI and never forwarded; device/assistance
        // ride the same update path once the KMP flow sets them — extend when needed).
        let nativeMetadata = OneStepSDK.OSTUserInputMetaData(
            note: nil,
            tags: metadata.tags,
            assistiveDevice: nil,
            levelOfAssistance: nil
        )
        Task {
            try? await motionLab.updateMeasurement(id: id, userInputMetadata: nativeMetadata)
            completion()
        }
    }

    func updateBalanceConditionMetadata(uuid: String, conditions: [String: String], completion: @escaping () -> Void) {
        // shortcut: Static Balance condition metadata not part of the walk E2E; no-op.
        completion()
    }

    func selfReportMotionMeasurement(uuid: String, stsRepetitions: Int32, completion: @escaping (KotlinInt) -> Void) {
        // shortcut: STS self-report not part of the walk E2E; report server failure (non-retryable).
        // 2 == SwiftRecorderBridgeAdapter.SELF_REPORT_SERVER_FAILURE (Kotlin companion constant).
        completion(KotlinInt(int: 2))
    }
}

// MARK: - SDK delegate

/// Bridges the KMP `OSTSDKBridge` to the native `OneStep` SDK. Pushes SDK state / events into the
/// `SwiftSDKBridgeAdapter` (set via `attach(adapter:)`).
final class NativeSDKDelegate: NSObject, IosSDKDelegate {

    private weak var adapter: SwiftSDKBridgeAdapter?

    func attach(adapter: SwiftSDKBridgeAdapter) {
        self.adapter = adapter
        // configureOSTUIKitKMPWithNativeSDK() is called AFTER OSTSDKCore.shared.initialize succeeds,
        // so the SDK is identified by the time the KMP flow reads state. Push READY immediately.
        // shortcut: no auth-state publisher is wired (OneStep 1.7.1 auth publisher API is not exercised
        // by the native UIKit and is unconfirmed); the walk E2E only needs a non-Uninitialized state.
        adapter.onSdkStateChanged(stateName: "READY", userId: nil, errorCode: -1, errorMessage: nil)
    }

    func isInitialized() -> Bool {
        // The example app only reaches configure() after OSTSDKCore.shared.initialize succeeds.
        true
    }

    func isMonitoringActive() -> Bool {
        // shortcut: monitoring status not needed for the walk E2E; conservative default false.
        false
    }

    func sendEvent(event: KMPEvent, completion: @escaping () -> Void) {
        // shortcut: SDK ingests its own analytics via OneStep.shared.events; there is no public
        // "inject event" sink in SDK 1.7.1, so forwarding is a no-op here.
        completion()
    }

    func getDailySummaries(completion: @escaping ([KMPDailyBackgroundMeasurement]) -> Void) {
        // shortcut: daily background summaries not needed for the walk E2E; return empty.
        completion([])
    }

    func optInToMonitoring() {
        // shortcut: monitoring opt-in not part of the walk E2E; no-op.
    }
}

// MARK: - Direct bridges (MotionData / Insights)

/// Degraded `MotionDataBridge`: the walk-recording E2E ends in a static/no-movement error whose
/// summary needs no norms or parameter metadata. Norm/metadata access in SDK 1.7.1 requires the
/// async `Insights.getMotionDataService()` product, which the KMP bridge (synchronous) cannot await
/// here — so return nil / empty. Upgrade path: cache an `OSTMotionDataService` at configure time and
/// map its `getNorm` / `getParameterMetadata` / `discreteScore` through the InsightMapperKt factories.
final class NativeMotionDataBridge: NSObject, MotionDataBridge {
    func mainParam(motionMeasurement: KMPMotionMeasurement) -> KotlinPair<KMPParamName, KotlinFloat>? { nil }
    func getAllParametersMetadata() -> [KMPParamName: KMPParameterMetadata] { [:] }
    func getNormByName(name: KMPParamName?) -> KMPNorm? { nil }
    func getParameterMetadata(paramName: KMPParamName) -> KMPParameterMetadata {
        // Non-optional in the protocol; build a minimal placeholder from the InsightMapperKt factory.
        InsightMapperKt.createKmpParameterMetadata(
            activity: "WALK",
            displayName: paramName.name,
            units: nil,
            imperialUnits: nil,
            category: "",
            lowRange: nil,
            sortKey: nil,
            isMainParam: KotlinBoolean(bool: false),
            highRange: nil,
            roundDigits: nil,
            imperialRoundDigits: nil
        )
    }
    func isWithinNorms(param: KMPParamName, value: Float) -> KotlinBoolean? { nil }
    func discreteScore(motionMeasurement: KMPMotionMeasurement, value: Float) -> KMPDiscreteColor? { nil }
    func discreteScore(param: KMPParamName, value: Float) -> KMPDiscreteColor? { nil }
}

/// Degraded `InsightsBridge`: no insights for the static-error walk. Upgrade path: call
/// `Insights.getInsights(for:)` and map through `InsightMapperKt.createKmpInsight(...)`.
final class NativeInsightsBridge: NSObject, InsightsBridge {
    func getInsights(measurement: KMPMotionMeasurement) async throws -> KMPInsights? { nil }
    func getInsightsByUuid(uuid: String) async throws -> KMPInsights? { nil }
}

// MARK: - One-call configuration

/// Wire all KMP bridges to the native OneStep SDK and register the native permission flow. Call once
/// after `OSTSDKCore`/`OneStep` initialization (see the example app).
public func configureOSTUIKitKMPWithNativeSDK() {
    let recorderDelegate = NativeRecorderDelegate()
    let recorderAdapter = SwiftRecorderBridgeAdapter(delegate: recorderDelegate)
    recorderDelegate.attach(adapter: recorderAdapter)

    let sdkDelegate = NativeSDKDelegate()
    let sdkAdapter = SwiftSDKBridgeAdapter(delegate: sdkDelegate)
    sdkDelegate.attach(adapter: sdkAdapter)

    OSTUIKitIos.shared.configure(
        sdkBridge: sdkAdapter,
        recorderBridge: recorderAdapter,
        motionDataBridge: NativeMotionDataBridge(),
        insightsBridge: NativeInsightsBridge(),
        preferencesBridge: IosUserDefaultsPreferencesBridge(),
        featureFlagsBridge: IosUserDefaultsFeatureFlagsBridge(),
        audioPlayer: PlatformAudioPlayer(),
        ttsPlayer: PlatformTTSPlayer(),
        permissionsManager: PlatformPermissionsManager(),
        resourceProvider: ResourceProvider(),
        analyticsHandler: nil
    )

    OSTUIKitKMPNativePermissions.register()
}

// MARK: - Host-app helpers

/// Recent native measurements mapped to KMP models, newest first — for host apps that present
/// `OSTMeasurementSummaryView` outside the recording flow. Requires an initialized, identified SDK.
/// Call from the main thread (native `getMeasurements` is not thread safe).
public func fetchRecentKmpMeasurements(limit: Int = 20) -> [KMPMotionMeasurement] {
    guard case .success(let onestep) = OneStep.shared(),
          case .success(let motionLab) = onestep.motionLab(),
          let native = try? motionLab.getMeasurements(
              request: TimeRangedDataRequest(startTime: nil, endTime: nil)
          )
    else { return [] }
    return native.suffix(limit).reversed().map(toKmp)
}
