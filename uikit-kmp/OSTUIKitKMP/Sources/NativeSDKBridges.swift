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

/// Resolves and caches the native `OSTMotionDataService`. The product is obtained asynchronously
/// (`Insights.getMotionDataService()`), but the KMP `MotionDataBridge` protocol is synchronous —
/// so this provider supports both a non-blocking async accessor (for the `InsightsBridge`, which is
/// itself `suspend`) and a bounded blocking accessor (for the synchronous `MotionDataBridge`).
///
/// The blocking accessor is only ever invoked from KMP `Dispatchers.Default` (a Kotlin/Native worker
/// thread, never the main thread and never Swift's cooperative pool), so parking that thread on a
/// semaphore while a detached `Task` resolves the service is safe — this mirrors Android's
/// `by lazy { runBlocking { … } }` in `AndroidMotionDataBridge`.
final class MotionDataServiceProvider: @unchecked Sendable {
    private let lock = NSLock()
    private var cached: (any OSTMotionDataService)?

    /// Kick off service resolution early (at configure time) so the summary rarely has to block.
    func warmUp() {
        Task.detached { [weak self] in
            let service = await MotionDataServiceProvider.fetch()
            self?.store(service)
        }
    }

    /// Non-blocking accessor for the async `InsightsBridge`.
    func serviceAsync() async -> (any OSTMotionDataService)? {
        if let cached = current() { return cached }
        let service = await MotionDataServiceProvider.fetch()
        store(service)
        return service
    }

    /// Bounded blocking accessor for the synchronous `MotionDataBridge`. MUST be called off the main
    /// thread (KMP `Dispatchers.Default` guarantees this). The timeout ensures a wedged SDK can never
    /// strand the summary on its loading shimmer.
    func serviceBlocking() -> (any OSTMotionDataService)? {
        if let cached = current() { return cached }
        let semaphore = DispatchSemaphore(value: 0)
        Task.detached { [self] in
            store(await MotionDataServiceProvider.fetch())
            semaphore.signal()
        }
        _ = semaphore.wait(timeout: .now() + 8)
        return current()
    }

    private func current() -> (any OSTMotionDataService)? {
        lock.lock(); defer { lock.unlock() }
        return cached
    }

    private func store(_ service: (any OSTMotionDataService)?) {
        guard let service else { return }
        lock.lock(); defer { lock.unlock() }
        cached = service
    }

    private static func fetch() async -> (any OSTMotionDataService)? {
        guard case .success(let onestep) = OneStep.shared(),
              case .success(let insights) = onestep.insights() else { return nil }
        return await insights.getMotionDataService()
    }
}

// MARK: Param-name / activity / color mapping (native OneStepSDK <-> KMP)

/// KMP `OSTParamName.columnName` for each native `OSTParamName`. Native rawValues are camelCase case
/// names while KMP keys measurement parameters by snake-case `columnName`; this explicit table makes
/// the mapping robust to either representation instead of relying on string-format guesses.
private func kmpColumnName(forNative param: OneStepSDK.OSTParamName) -> String? {
    switch param {
    case .walkingCadence: return "cadence"
    case .walkingVelocity: return "velocity"
    case .walkingDoubleSupport: return "double_support"
    case .walkingStance: return "stance"
    case .walkingStanceAsymmetry: return "stance_asymmetry"
    case .walkingStrideLength: return "stride_length"
    case .walkingStepLength: return "step_length"
    case .walkingStepLengthLeft: return "step_length_left"
    case .walkingStepLengthRight: return "step_length_right"
    case .walkingStepLengthDiff: return "step_length_diff"
    case .walkingStepLengthAsymmetry: return "step_length_asymmetry"
    case .walkingConsistency: return "consistency"
    case .walkingHipRange: return "hip_range"
    case .walkingBaseWidth: return "base_width"
    case .walkingDoubleSupportAsymmetry: return "double_support_asymmetry"
    case .walkingSingleSupportRight: return "single_support_right"
    case .walkingSingleSupportLeft: return "single_support_left"
    case .walkingStanceRight: return "stance_right"
    case .walkingStanceLeft: return "stance_left"
    case .walkingWalkScore: return "walk_score"
    case .walkingDistance: return "distance"
    case .walkingCadenceVariability: return "cadence_variability"
    case .walkingVelocityVariability: return "velocity_variability"
    case .tugDurationSeconds: return "tug_duration_seconds"
    case .tugForwardSeconds: return "tug_forward_seconds"
    case .tugBackwardSeconds: return "tug_backward_seconds"
    case .tugSittingSeconds: return "tug_sitting_seconds"
    case .tugStandingSeconds: return "tug_standing_seconds"
    case .tugTurningSeconds: return "tug_turning_seconds"
    case .tugTurningToChairSeconds: return "tug_turning_to_chair_seconds"
    case .tugDistanceMeters: return "tug_distance_meters"
    case .stsRepetitionCount: return "sts_repetition_count"
    case .stsRepetitionTime: return "sts_repetition_time"
    case .stsRepetitionVar: return "sts_repetition_var"
    case .stsFatigue: return "sts_fatigue"
    case .stsAngle: return "sts_angle"
    case .rangeOfMotionAngle: return "range_of_motion_angle"
    case .hipExtRangeOfMotionAngle: return "hip_ext_range_of_motion_angle"
    case .hipFlexRangeOfMotionAngle: return "hip_flex_range_of_motion_angle"
    case .hipAbdRangeOfMotionAngle: return "hip_abd_range_of_motion_angle"
    case .hipAddRangeOfMotionAngle: return "hip_add_range_of_motion_angle"
    case .kneeFlexRangeOfMotionAngle: return "knee_flex_range_of_motion_angle"
    case .kneeExtRangeOfMotionAngle: return "knee_ext_range_of_motion_angle"
    case .kneeFlexPassiveRangeOfMotionAngle: return "knee_flex_passive_range_of_motion_angle"
    case .twoMinWalkDistance: return "two_minute_walk_distance_meters"
    case .sixMinWalkDistance: return "six_minute_walk_distance_meters"
    case .sixMinuteWalkLaps: return "six_minute_walk_laps"
    // .walkCourseLength has no KMP OSTParamName counterpart.
    default: return nil
    }
}

/// KMP `OSTParamName` -> native `OSTParamName` (via the shared columnName).
private func nativeParamName(fromKmp kmp: KMPParamName) -> OneStepSDK.OSTParamName? {
    let column = kmp.columnName
    return OneStepSDK.OSTParamName.allCases.first { kmpColumnName(forNative: $0) == column }
}

/// Native `OSTParamName` -> KMP `OSTParamName` (via the shared columnName + the KMP factory).
private func kmpParamName(fromNative native: OneStepSDK.OSTParamName) -> KMPParamName? {
    guard let column = kmpColumnName(forNative: native) else { return nil }
    return OSTParamNameKt.toParamName(column)
}

/// KMP `OSTActivityType.serializedName` -> native `OSTActivityType`. (Native has no counterpart for
/// balance types, which have no gait main-parameter, so those return nil.)
private func nativeActivityType(fromSerializedName name: String) -> OneStepSDK.OSTActivityType? {
    switch name {
    case "walk": return .walk
    case "sts": return .sts
    case "tug": return .tug
    case "rom_knee_flex": return .romKneeFlexionPassive
    case "rom_knee_ext": return .romKneeExtension
    case "dual_task_walk_subtract": return .dualTaskWalkSubtract
    case "walk_6_min_test": return .sixMinWalk
    case "walk_2_min_test": return .twoMinWalk
    case "stairs": return .stairs
    default: return nil
    }
}

/// Native `OSTDiscreteColor` -> the lowercase/snake string the KMP color utilities match on
/// (`ColorUtil.toBubbleColor` / `toPartColor` / `toColorDescription`). Note native `.darkRed` is
/// camelCase (`rawValue == "darkRed"`) while KMP expects `"dark_red"`.
private func kmpColorString(_ color: OneStepSDK.OSTDiscreteColor) -> String {
    switch color {
    case .green: return "green"
    case .yellow: return "yellow"
    case .red: return "red"
    case .darkRed: return "dark_red"
    }
}

/// Native `OSTInsightType` -> the string the KMP `toKmpInsightType` factory matches (which expects
/// `FALL_RISK`, so native `.fallRisk` must map to `"fall_risk"`, not its camelCase rawValue).
private func kmpInsightTypeString(_ type: OneStepSDK.OSTInsightType) -> String {
    switch type {
    case .trend: return "trend"
    case .comparison: return "comparison"
    case .parameter: return "parameter"
    case .education: return "education"
    case .info: return "info"
    case .fallRisk: return "fall_risk"
    }
}

/// Native `OSTParameterMetadata` -> KMP. Native has no low/high range or imperial round digits
/// (ranges live in the norm), so those are nil; `roundDigits` / `sortKey` are Int on native.
private func toKmp(_ meta: OneStepSDK.OSTParameterMetadata) -> KMPParameterMetadata {
    InsightMapperKt.createKmpParameterMetadata(
        activity: meta.activity,
        displayName: meta.displayName,
        units: meta.units,
        imperialUnits: meta.imperialUnits,
        category: meta.category ?? "",
        lowRange: nil,
        sortKey: KotlinFloat(float: Float(meta.sortKey)),
        isMainParam: meta.isMainParam.map { KotlinBoolean(bool: $0) },
        highRange: nil,
        roundDigits: KotlinFloat(float: Float(meta.roundDigits)),
        imperialRoundDigits: nil
    )
}

/// Native `OSTNorm` -> KMP. KMP norms carry `units` (native norms don't — they come from the
/// parameter metadata) and a list of `OSTNormPart`s built from the native `NormSegment`s (closed
/// ranges, so both bounds are inclusive).
private func toKmp(_ norm: OneStepSDK.OSTNorm, units: String?) -> KMPNorm {
    let parts = norm.segments.map { segment in
        InsightMapperKt.createKmpNormPart(
            start: Float(segment.range.lowerBound),
            end: Float(segment.range.upperBound),
            color: kmpColorString(segment.color),
            includeStart: true,
            includeEnd: true
        )
    }
    return InsightMapperKt.createKmpNorm(units: units, parts: parts)
}

/// `MotionDataBridge` backed by the native `OSTMotionDataService`.
final class NativeMotionDataBridge: NSObject, MotionDataBridge {
    private let provider: MotionDataServiceProvider

    init(provider: MotionDataServiceProvider) {
        self.provider = provider
        super.init()
    }

    func mainParam(motionMeasurement: KMPMotionMeasurement) -> KotlinPair<KMPParamName, KotlinFloat>? {
        guard let service = provider.serviceBlocking(),
              let activity = nativeActivityType(fromSerializedName: motionMeasurement.type.serializedName),
              let main = service.getMainParameter(for: activity),
              let kmpParam = kmpParamName(fromNative: main.paramName),
              let value = motionMeasurement.params[kmpParam.columnName]?.floatValue
        else { return nil }
        return KotlinPair(first: kmpParam, second: KotlinFloat(float: value))
    }

    func getAllParametersMetadata() -> [KMPParamName: KMPParameterMetadata] {
        guard let service = provider.serviceBlocking() else { return [:] }
        var result: [KMPParamName: KMPParameterMetadata] = [:]
        for native in OneStepSDK.OSTParamName.allCases {
            guard let meta = service.getParameterMetadata(by: native),
                  let kmpParam = kmpParamName(fromNative: native) else { continue }
            result[kmpParam] = toKmp(meta)
        }
        return result
    }

    func getNormByName(name: KMPParamName?) -> KMPNorm? {
        guard let name,
              let service = provider.serviceBlocking(),
              let native = nativeParamName(fromKmp: name),
              let norm = service.getNorm(by: native)
        else { return nil }
        // Native norms carry no units; the display units live on the parameter metadata.
        let units = service.getParameterMetadata(by: native)?.units
        return toKmp(norm, units: units)
    }

    func getParameterMetadata(paramName: KMPParamName) -> KMPParameterMetadata {
        if let service = provider.serviceBlocking(),
           let native = nativeParamName(fromKmp: paramName),
           let meta = service.getParameterMetadata(by: native) {
            return toKmp(meta)
        }
        // Non-optional contract: fall back to a minimal placeholder so an unmapped param or a
        // not-yet-ready service never crashes the summary.
        return InsightMapperKt.createKmpParameterMetadata(
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

    func isWithinNorms(param: KMPParamName, value: Float) -> KotlinBoolean? {
        guard let service = provider.serviceBlocking(),
              let native = nativeParamName(fromKmp: param),
              let within = service.isWithinNorm(param: native, value: Double(value))
        else { return nil }
        return KotlinBoolean(bool: within)
    }

    func discreteScore(motionMeasurement: KMPMotionMeasurement, value: Float) -> KMPDiscreteColor? {
        // The measurement-level score colors the main-param circle: resolve the activity's main
        // parameter, then score `value` against it (matches `getMeasurementCircleColor`).
        guard let service = provider.serviceBlocking(),
              let activity = nativeActivityType(fromSerializedName: motionMeasurement.type.serializedName),
              let main = service.getMainParameter(for: activity),
              let color = service.discreteScore(for: main.paramName, value: Double(value))
        else { return nil }
        return InsightMapperKt.toKmpDiscreteColor(kmpColorString(color))
    }

    func discreteScore(param: KMPParamName, value: Float) -> KMPDiscreteColor? {
        guard let service = provider.serviceBlocking(),
              let native = nativeParamName(fromKmp: param),
              let color = service.discreteScore(for: native, value: Double(value))
        else { return nil }
        return InsightMapperKt.toKmpDiscreteColor(kmpColorString(color))
    }
}

/// `InsightsBridge` backed by the native `OSTMotionDataService.getInsightsBy(measurementID:)`.
final class NativeInsightsBridge: NSObject, InsightsBridge {
    private let provider: MotionDataServiceProvider

    init(provider: MotionDataServiceProvider) {
        self.provider = provider
        super.init()
    }

    func getInsights(measurement: KMPMotionMeasurement) async throws -> KMPInsights? {
        try await getInsightsByUuid(uuid: measurement.id)
    }

    func getInsightsByUuid(uuid: String) async throws -> KMPInsights? {
        guard let id = UUID(uuidString: uuid),
              let service = await provider.serviceAsync() else { return nil }
        // Crash-safe: swallow SDK throws (network / analysis errors) and surface as "no insights".
        // The summary treats a throw and a nil identically (error empty state), so never propagate.
        let native = (try? await service.getInsightsBy(measurementID: id)) ?? []
        let insights = native.compactMap { toKmpInsight($0) }
        return InsightMapperKt.createKmpInsights(uuid: uuid, insights: insights)
    }

    private func toKmpInsight(_ insight: OneStepSDK.OSTInsight) -> KMPInsight? {
        let kmpParam = insight.paramName
            .flatMap { OneStepSDK.OSTParamName(rawValue: $0) }
            .flatMap { kmpParamName(fromNative: $0) }
        let intent: KMPIntent? = insight.intent.map { InsightMapperKt.toKmpIntent($0.rawValue) }
        let insightType: KMPInsightType =
            InsightMapperKt.toKmpInsightType(kmpInsightTypeString(insight.insightType))
        return InsightMapperKt.createKmpInsight(
            paramName: kmpParam,
            textMarkdown: insight.textMarkdown,
            intent: intent,
            insightType: insightType,
            rank: Float(insight.rank)
        )
    }
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

    // Shared between both direct bridges; warm up now so the summary rarely blocks resolving it.
    let motionDataProvider = MotionDataServiceProvider()
    motionDataProvider.warmUp()

    OSTUIKitIos.shared.configure(
        sdkBridge: sdkAdapter,
        recorderBridge: recorderAdapter,
        motionDataBridge: NativeMotionDataBridge(provider: motionDataProvider),
        insightsBridge: NativeInsightsBridge(provider: motionDataProvider),
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
