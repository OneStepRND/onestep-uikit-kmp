# Patient scope / clinician mode — KMP uikit design

Status: PROPOSAL (July 2026). Ports the SDK `withPatient` multi-patient capability into
`uikit-kmp`, giving hosts an explicit clinician mode. Grounded on three precedents:

- **Android SDK** — `OneStep.withPatient(patientId) { scope -> ... }` (`OneStep.kt:260`)
  creates an `OSTPatientScopeImpl` with its own `PatientContext` and per-scope instance
  cache; `scope.getMotionLab()` / `scope.getInsights()` vend **patient-bound** product
  instances distinct from the auth-bound singleton's.
- **Android uikit (legacy)** — clinician `buildIntent(patientScope, ...)` overload +
  `PatientScopeRegistry` (token, 60s TTL) + `PatientScopeGate` installing
  `UiKitServiceLocator.activeScope` per Activity. The token machinery exists only because
  Intents cannot carry object references — it does NOT need porting.
- **iOS uikit (OS-15573)** — the cleanest precedent: `OSTRecordingConfiguration.patientId:
  OSTPatientId?` plus an internal `PatientScopedSDK` adapter that keeps the existing
  `OneStepProtocol` seam and swaps in patient-bound products from
  `OneStep.withPatient(patientId)`. The SDK's identification state stays `.unidentified`;
  no SDK-wide identity is written. `withPatientBinding { }` pins the patient around
  `OSTRecorder.start`, which captures the recording's owner ambiently at start time.

Both native SDKs create a scope from **just a patient id** (Android `OSTPatientId` is a
`value class` over `String`). That is the whole trick: the KMP layer only needs to carry a
`String` across `commonMain`, and each platform builds its scope internally. No platform
scope type ever crosses the KMP boundary, and no registry/token/TTL is needed — the entry
composable is a plain function call in the host's process.

## Current gap

`uikit-kmp` is single-user only. `AndroidRecorderBridge(oneStep).getMotionLab()`,
`AndroidInsightsBridge` / `AndroidMotionDataBridge` → `oneStep.getInsights()`, and the iOS
`NativeRecorderDelegate` → `OneStep.shared().motionLab()` all resolve the **auth-bound
singleton**. `UIKitServiceLocator.configure(...)` is called once at app startup, so bridge
identity is app-lifetime. A clinician host cannot launch any KMP flow for a patient.

## External API

### Mode model: a nullable `patientId: String?` parameter on each public entry point

```kotlin
@Composable
fun OSTRecordingFlow(
    config: OSTRecordingConfiguration,
    patientId: String? = null,        // null = current-user (patient-app) mode
    onResult: (OSTEvent) -> Unit,
    ...
)
```

Same parameter, same semantics, on `OSTMeasurementSummary` and `OSTCareLog` when those get
clinician support. Swift wrappers mirror it:

```swift
OSTRecordingFlowView(config: cfg, patientId: patient.uuid) { result in ... }
```

- `nil`/`null` (default) → **current-user mode**: resolves the SDK's auth-bound patient
  exactly as today; single-patient hosts are unaffected (source-compatible).
- Non-null → **clinician mode**: every SDK touchpoint in the flow (recorder, measurement
  CRUD, insights, norms) runs patient-scoped; the SDK's identification state may stay
  unidentified.

Considered and rejected:

- **Field on `OSTRecordingConfiguration`** (the iOS-uikit shape). Rejected for KMP because
  the config is a `@Serializable` public data class: (a) adding a field breaks Swift
  `doCopy`/`init` callers in a way only an iOS xcodebuild catches; (b) it would serialize
  patient identity into nav saved state — unnecessary HIPAA surface; (c) session identity
  is not a recording parameter — summary/care-log need the same value, so it belongs beside
  the config, not inside one feature's config. Hosts migrating from the iOS uikit map
  `config.patientId` → the view initializer's `patientId` 1:1.
- **Public `LocalPatientScope` CompositionLocal**. Swift hosts cannot provide
  CompositionLocals, the consumers are ViewModels/managers outside composition, and a
  nullable ambient default fails silently into wrong-user attribution.
- **`UIKitServiceLocator.setActivePatient(...)`**. App-lifetime slot for per-launch state —
  the classic stale-scope bug (patient A's scope leaking into the next recording).
- **Sealed `OSTSessionContext`**. More self-documenting, but sealed hierarchies export
  poorly to Swift and a nullable id is the minimum that works (scope ladder rung 1).

## Internal seam: per-session bridge bundle

The SDK models patient scope as *different product instances*; the KMP analogue is
*different bridge instances* behind the same interfaces (exactly how `PatientScopedSDK`
kept the `OneStepProtocol` seam on iOS). Bridge method signatures do not change.

```kotlin
// commonMain — the patient-bound subset of the bridge surface
class PatientScopedBridges(
    val recorderBridge: RecorderBridge,
    val insightsBridge: InsightsBridge,
    val motionDataBridge: MotionDataBridge,
)

// commonMain — implemented per platform, registered at configure time
interface PatientScopedBridgesFactory {
    /** Called once per flow launch. Implementations may cache per patientId. */
    fun create(patientId: String): PatientScopedBridges
}
```

- `UIKitServiceLocator.configure(...)` gains an optional
  `patientScopedBridgesFactory: PatientScopedBridgesFactory? = null`.
- Each entry composable resolves once per launch:

```kotlin
val bridges = remember(patientId) {
    if (patientId == null) SessionBridges.currentUser()      // today's singletons
    else UIKitServiceLocator.patientScopedBridgesFactory
        ?.create(patientId)
        ?: error("patientId passed but no PatientScopedBridgesFactory configured")
}
```

  Failing fast here is deliberate: silently falling back to the singleton would attribute a
  patient recording to the wrong identity (the failure mode the legacy `PatientScopeGate`
  logs warn about).
- The bundle flows to ViewModels through the existing constructor paths (all
  `UIKitServiceLocator.<bridge>` reads live in the three entry files, so the change is
  localized). An `internal` CompositionLocal may distribute it if screen-level access is
  ever needed — never public API.
- Lifetime = the flow's composition. When the composable leaves, the bundle is dropped;
  no registry, no TTL, no release hook.

### Android factory (ships in `androidMain`)

`androidMain` already depends on the SDK, so uikit-kmp provides this out of the box:

```kotlin
class AndroidPatientScopedBridgesFactory : PatientScopedBridgesFactory {
    override fun create(patientId: String): PatientScopedBridges =
        OneStep.withPatient(OSTPatientId(patientId)) {
            PatientScopedBridges(
                recorderBridge = AndroidRecorderBridge(motionLab = getMotionLab()),
                insightsBridge = AndroidInsightsBridge(insights = getInsights()),
                motionDataBridge = AndroidMotionDataBridge(insights = getInsights()),
            )
        }
}
```

Prerequisite refactor (uikit-only): the Android bridges currently take the `OneStep`
handle and call `oneStep.getMotionLab()` lazily; they need constructor overloads taking the
resolved product (the singleton path keeps today's lazy behavior).

⚠️ **The motion-data bridge takes a provider, not a resolved service, and must stay that way
(0.6.15).** The snippet above is the design sketch; the shipped factory passes
`AndroidMotionDataBridge.deferred { … }`. Resolving an `OSTMotionDataService` runs
`MotionDataServiceImpl.initialize()`, which fires the norms and parameter-metadata requests and
awaits both — and `create()` is called from composition, on the main thread. Because
`RecordFlowNavGraph` reads only `recorderBridge`, resolving eagerly cost the recording flow **667ms
of a 716ms first frame** in HTTP it never uses (Pixel 10 Pro emulator against production,
2026-09-03); the clinician saw it as a blank screen between tapping Start and the flow appearing.
It is per-resolve, not once per process: each `withPatient` scope vends a fresh service whose
`isInitialized` starts false, so a second launch paid it again. The summary flow, which does read
the bridge, resolves its own bundle and pays the cost where it is needed.

The iOS factory never had this problem and is the precedent: `MotionDataServiceProvider.warmUp()`
is a `Task.detached`, and its blocking accessor is documented "MUST be called off the main thread".
`AndroidMotionDataBridgeLazinessTest` pins the Android half.

**No SDK change required.** `OSTPatientId`'s primary constructor is `internal`
(`OneStepImpl.kt:37`) but the SDK ships a public factory `OSTPatientId.fromString(String)`
(`OneStepImpl.kt:39`), and `OneStep.withPatient(OSTPatientId, ...)` is already public
(`OneStep.kt:260`). So the factory above compiles against the shipped SDK as-is.

### iOS factory (Kotlin `iosMain` + Swift package)

The Swift delegates resolve products at init, so the factory shape is a delegate factory
registered from Swift:

```kotlin
// iosMain — OSTUIKitIos.configure gains:
patientScopedRecorderDelegateFactory: ((String) -> IosRecorderDelegate)? = null
// (+ insights/motion-data provider factory following the same pattern)
```

```swift
// OSTUIKitKMP Swift package — mirrors OneStepUIKit's PatientScopedSDK
final class PatientScopedRecorderDelegate: NativeRecorderDelegate {
    init(patientId: OSTPatientId) {
        let lab = OneStep.withPatient(patientId) { $0.getMotionLab() }
        super.init(motionLab: lab)   // requires extracting an injectable init
    }
    // OSTRecorder.start captures the recording's owner ambiently — pin it:
    override func start(...) {
        OneStep.withPatient(patientId) { _ in super.start(...) }
    }
}
```

`configureOSTUIKitKMPWithNativeSDK()` wires the factories automatically — Swift hosts get
clinician mode with no extra setup beyond passing `patientId` to the view.

## Behavior matrix (clinician mode)

| Concern | Current-user mode | Clinician mode |
|---|---|---|
| Record / analyze / measurement CRUD | singleton MotionLab | patient-scoped MotionLab |
| Insights, norms, parameter metadata | singleton Insights | patient-scoped Insights |
| Hallway length persistence (`ost.ui.hallway_length_*`) | read/write user's custom metadata | **no read, no write** — host pre-fills via `config.hallwayLengthMeters` (already shipped) |
| Monitoring, daily summaries, opt-in | as today | unavailable — empty/no-op (matches iOS `PatientScopedSDK.monitoring() → .notIdentified`) |
| `sdkState` / `events` | as today | unchanged (singleton); flows must not gate on an identified state when `patientId != null` |
| Analytics | as today | unchanged — `patientId` must NEVER be attached to analytics events, logs, or screen names (HIPAA) |

Hallway-length note: the length is a property of the **clinic hallway**, not the patient —
per-patient persistence would scatter the same physical value across patient stores, and
writing it to the *clinician's* store leaks session state across patients. Suppress + host
pre-fill (the legacy Android-uikit behavior) is semantically right, not just parity. The
`// Note: KMP has no patient-scope concept` comments in `OSTRecordingConfiguration.
hallwayLengthMeters` and `HallwayDistanceManager` are updated by this work.
`HallwayDistanceManager` gains an `isPatientSession: Boolean` (plumbed from `patientId !=
null`) gating `loadSavedLength()`'s metadata read and `saveHallwayLengthToMetadata()`.

## Implementation plan (verified against the current tree, July 2026)

Ordered by the >10-file split-by-risk convention. Every block compiles on its own; the
feature is not reachable until block 3. Compile gate each block with
`./gradlew :uikit-kmp:compileDebugKotlinAndroid :uikit-kmp:compileKotlinIosSimulatorArm64`;
any block touching a public data class / entry signature also needs an iOS `xcodebuild`
(Swift `doCopy`/`init` breakage is invisible to the Kotlin gate — see
[[kmp-dataclass-param-breaks-swift]]).

### Block 2 — new, dead code (no entry point references it yet)

`commonMain`:
- `bridge/PatientScopedBridges.kt` — `class PatientScopedBridges(val recorderBridge, val
  insightsBridge, val motionDataBridge)` and `interface PatientScopedBridgesFactory {
  fun create(patientId: String): PatientScopedBridges }`.
- `di/UIKitServiceLocator.kt` — add nullable `_patientScopedBridgesFactory` slot + getter +
  a `patientScopedBridgesFactory` param on `configure(...)` (defaulted null) and clear it in
  `reset()`. This is app-lifetime config (the *factory*, not a scope), so it belongs here;
  the per-launch scope never does.

`androidMain`:
- Give `AndroidRecorderBridge` a secondary constructor `(motionLab: CoreMotionLab)` and
  `AndroidInsightsBridge` `(insights: Insights)` and `AndroidMotionDataBridge`
  `(service: OSTMotionDataService)`. Keep the existing `(oneStep: OneStep)` constructors —
  the singleton path is unchanged. (Today all three resolve the product lazily from
  `oneStep`; the new ctors take an already-resolved product so a `withPatient { }` block can
  build them.)
- `bridge/android/AndroidPatientScopedBridgesFactory.kt` — `OneStep.withPatient(
  OSTPatientId.fromString(patientId)) { PatientScopedBridges(AndroidRecorderBridge(
  getMotionLab()), AndroidInsightsBridge(getInsights()), AndroidMotionDataBridge(
  getInsights().getMotionDataService()...)) }`. Uses only public SDK API.

`iosMain` + Swift package:
- `IosEntryPoint.kt` `configure(...)` gains a defaulted
  `patientScopedBridgesFactory: PatientScopedBridgesFactory? = null` forwarded to the
  service locator.
- Swift `NativeSDKBridges.swift` — extract injectable inits: `NativeRecorderDelegate(
  motionLab:)` (today it reads `OneStep.shared().motionLab()` in `init`) and a
  patient-scoped `MotionDataServiceProvider.fetch` variant. Add a Kotlin
  `IosPatientScopedBridgesFactory` (in `iosMain`) whose `create` calls back into a
  registered Swift closure that builds the scoped delegates via `OneStep.withPatient`.

### Block 3 — wiring & behavior (gated by `patientId != null`; the only runtime-behavior block)

- `OSTRecordingFlow.kt` — add `patientId: String? = null` after `config`.
- `RecordFlowNavGraph.kt` — add `patientId` param; resolve the bundle once with
  `remember(patientId) { if (patientId == null) currentUserBundle() else
  UIKitServiceLocator.patientScopedBridgesFactory?.create(patientId) ?: error(...) }` and
  feed `recorderBridge`/`sdkBridge`/insights into the `MotionRecorderViewModel(...)` build
  (lines ~236–251) from the bundle instead of reading each `UIKitServiceLocator.<bridge>`
  directly. **Fail fast** when a `patientId` is supplied but no factory is configured —
  never silently fall back to the singleton (wrong-user attribution).
- `MotionRecorderViewModel.kt` — thread an `isPatientSession: Boolean` into
  `HallwayDistanceManager`.
- `HallwayDistanceManager.kt` — gate `loadSavedLength()`'s metadata read and
  `saveHallwayLengthToMetadata()` on `!isPatientSession`; host still pre-fills via
  `config.hallwayLengthMeters`. Update the two `// Note: KMP has no patient-scope concept`
  comments here and in `OSTRecordingConfiguration.hallwayLengthMeters`.
- `IosEntryPoint.kt` — add `patientId: String? = null` to both
  `createRecordingFlowViewController` overloads, passed through to `OSTRecordingFlow`.
- Swift `SwiftUIViews.swift` — add `patientId: String? = nil` to `OSTRecordingFlowView.init`
  and forward it. `configureOSTUIKitKMPWithNativeSDK()` registers the Swift scoped-delegate
  closure so hosts get clinician mode with no extra setup.
- Monitoring / daily-summaries stay no-op/unavailable in scope (matches iOS
  `PatientScopedSDK.monitoring() → .notIdentified`).

### Block 4 — tests

- `PatientScopedBridgesFactory` resolution returns distinct bundles for distinct
  `patientId`s (stale-session guard: two sequential launches don't share a bridge).
- Fail-fast: non-null `patientId` + no factory configured throws, does not fall through to
  the singleton.
- Hallway: `isPatientSession = true` issues no metadata read and no write; host pre-fill
  still applies.

## Effort / surface

~8 new/edited files in block 2, ~7 in block 3 — each block is under the 10-file PR ceiling.
No `strings/assets` block is needed. No change to either `onestep-sdk-*` repo.

Compile gate for every PR: `./gradlew :uikit-kmp:compileDebugKotlinAndroid
:uikit-kmp:compileKotlinIosSimulatorArm64`, plus an iOS xcodebuild for any public-surface
change (Swift `doCopy`/`init` breakage is invisible to the Kotlin gate).

## Core (SDK) impact

**None. This is a uikit-only change on both platforms.** The SDK already exposes
everything the factories need:

- Android core (`onestep-sdk-android`): `OSTPatientId.fromString(String)` public
  (`OneStepImpl.kt:39`); `OneStep.withPatient(OSTPatientId) { scope -> ... }` public
  (`OneStep.kt:260`); `scope.getMotionLab()` / `getInsights()` / `getPatientAdmin()` public.
- iOS core (`onestep-sdk-ios`, pinned `exact: 2.0.10` in `OSTUIKitKMP/Package.swift`):
  `OSTPatientId(rawValue: String)` public (RawRepresentable); `OneStep.withPatient(_:_:)`
  public in both sync and `async` overloads; `OSTPatientScope.getMotionLab()` /
  `getInsights()` / `getPatientAdmin()` public. (Multi-patient shipped in SDK 2.0.6.)

All new code lives in `uikit-kmp` (`commonMain` interfaces, `androidMain` factory + bridge
constructor overloads) and the `OSTUIKitKMP` Swift package (delegate factory). The host
apps gain a `patientId` argument; nothing in either SDK repo changes.

## Recorder ownership (confirmed)

The iOS `MotionLab` protocol (SDK 2.0.10 swiftinterface) exposes `recorder` as a plain
property and has **no per-call owner parameter** on any recorder/measurement method — the
recorder resolves its owner *ambiently* at `start`. So a patient-scoped recording must pin
`start` inside `OneStep.withPatient(patientId) { ... }`, exactly as OneStepUIKit's
`PatientScopedSDK.withPatientBinding` does. Block 3's Swift scoped delegate wraps its
`start` override accordingly. Android's `OSTMotionLab` is resolved from inside the
`withPatient { }` block by the factory, so its recorder is already owner-bound.

## Open items

- Confirm at implementation time that measurement *read/update* paths in scope (analyze,
  updateMeasurement, updateCourseLength) also resolve through the scoped product on iOS and
  don't need per-call pinning like `start` does — the async `withPatient` overload is
  available if any do.
- `OSTMeasurementSummary` / `OSTCareLog` clinician support can land in a follow-up using
  the same `patientId` parameter + bundle resolution — the seam is identical.
- Care-log history on iOS is still a `readMotionMeasurements` no-op shortcut; patient
  scoping doesn't change that but inherits it.
