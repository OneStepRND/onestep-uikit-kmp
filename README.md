# OneStep UIKit KMP

Kotlin Multiplatform UI Kit for the OneStep SDK. Extracted from
[`onestep-sdk-android`](https://github.com/OneStepRND/onestep-sdk-android) (`uikit-kmp` module)
into a standalone repo.

- **Android**: published as a Maven artifact — `co.onestep.kmp:uikit-kmp` — to this
  repo's **GitHub Packages** Maven repository (private)
- **iOS**: published as an SPM package wrapping the `OSTUIKit.xcframework` binary +
  a Swift bridge layer (`OSTUIKitKMP` module) that connects to the native
  [`onestep-sdk-ios`](https://github.com/OneStepRND/onestep-sdk-ios)

> **Not published to public Maven Central.** Distribution is GitHub Packages (Android)
> and SPM (iOS) only. Consumers need OneStepRND repo/package access — see below.

## Versions

Current pinned versions (generated from `uikit-kmp/build.gradle.kts` and
`gradle/libs.versions.toml` — do not edit by hand; run
`./scripts/update-readme-versions.sh` after a bump):

<!-- versions:start -->
![uikit-kmp](https://img.shields.io/badge/uikit--kmp-0.6.5-blue)
![core](https://img.shields.io/badge/core-2.1.2--SNAPSHOT-orange)
![design-system](https://img.shields.io/badge/design--system-1.3.1-green)
<!-- versions:end -->

### What `-SNAPSHOT` means here

**It marks the internal distribution channel — it is not a mutable Maven snapshot.** OneStep
reuses the Maven suffix to separate the two audiences, and suffixed builds are immutable:

| Version form | Published to | Consumed by |
|---|---|---|
| `0.6.3-SNAPSHOT`, `core:2.1.1-SNAPSHOT` | GitHub Packages (private) | OneStep patient app and clinician app — **this is what ships to production internally** |
| `0.6.3`, `core:2.1.1` | Maven Central (public) | External clients (e.g. Zimmer) |

So a `-SNAPSHOT` uikit-kmp depending on a `-SNAPSHOT` core is the normal internal release, not
a provisional build: it is reproducible and safe to ship. Dropping the suffix is a *distribution*
decision — opening the version up to external clients — not a stability promotion.

## Consuming

### Android / KMP apps (Maven)

Add the GitHub Packages repository and the dependency:

```kotlin
// settings.gradle.kts
maven {
    url = uri("https://maven.pkg.github.com/OneStepRND/onestep-uikit-kmp")
    credentials {
        username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
        password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("GITHUB_TOKEN")
    }
}

// build.gradle.kts — see the Versions table above for the current version
implementation("co.onestep.kmp:uikit-kmp:0.2.0-SNAPSHOT")
```

### iOS apps (Swift Package Manager)

Add this repository as a package dependency in Xcode:

```
https://github.com/OneStepRND/onestep-uikit-kmp
```

- **Snapshots**: track the `main` branch (the CI job rewrites `Package.swift` to point at the
  latest snapshot XCFramework release).
- **Releases**: use a `uikit-kmp-X.Y.Z` tag.

Then `import OSTUIKitKMP`.

## Clinician mode (patient scope) — planned

> **Status: designed, not yet implemented.** Full design + file-by-file implementation plan:
> [`docs/patient-scope-clinician-mode-design.md`](docs/patient-scope-clinician-mode-design.md).

By default every flow operates on the SDK's auth-bound patient (**patient-app / current-user
mode**). Clinician hosts that operate on behalf of many patients will pass a `patientId` to
the flow entry points; the flow then runs patient-scoped (SDK `withPatient`), and the SDK's
own identification state may stay unidentified.

```kotlin
// Kotlin / Compose Multiplatform
OSTRecordingFlow(config = config, onResult = ::handle)                 // current-user mode
OSTRecordingFlow(config = config, patientId = patient.uuid, onResult = ::handle)  // clinician mode
```

```swift
// iOS
OSTRecordingFlowView(config: config, patientId: patient.uuid) { event in handle(event) }
```

Semantics when a `patientId` is supplied:

- Recording, analysis, measurement CRUD, insights and norms resolve **patient-scoped**
  product instances instead of the singleton's.
- Hallway length is **not** persisted to/read from custom metadata (it is a property of the
  clinic, not the patient); supply `OSTRecordingConfiguration.hallwayLengthMeters` to
  pre-fill it.
- Monitoring and daily summaries are unavailable in scope.
- `patientId` must **never** appear in analytics, logs, or screen names (HIPAA).

This is a **uikit-only** change — no `onestep-sdk-android` / `onestep-sdk-ios` change is
required (both already expose `withPatient` and public `OSTPatientId` construction). Hosts
must register a `PatientScopedBridgesFactory` via `UIKitServiceLocator.configure(...)`
(Android) — iOS wires it automatically in `configureOSTUIKitKMPWithNativeSDK()`. Passing a
`patientId` with no factory configured fails fast rather than silently recording as the
wrong identity.

## Publishing

### Local scripts

```bash
./scripts/publish.sh                 # Android → Maven Local (~/.m2)
./scripts/publish.sh -s              # Android snapshot → GitHub Packages
./scripts/publish-xcframework.sh --snapshot   # iOS snapshot → GitHub Release + Package.swift
./scripts/publish-xcframework.sh --local      # iOS: build + zip only (local testing)
```

Requirements: `gpr.user` / `gpr.key` (a PAT with `read:packages`/`write:packages`) in
`~/.gradle/gradle.properties`, and an authenticated `gh` CLI for iOS publishing.

### CI (GitHub Actions, manual dispatch)

| Workflow | What it does |
|---|---|
| **Publish Android (Maven Snapshot)** | `assembleRelease` + publishes all KMP publications as `-SNAPSHOT` to this repo's GitHub Packages maven repo |
| **Publish iOS (XCFramework / SPM)** | Builds `OSTUIKit.xcframework`, uploads it to a GitHub Release (`uikit-kmp-<version>[-SNAPSHOT]`), refreshes checked-in compose-resources, rewrites root `Package.swift` with URL + checksum, and commits |

**Required repo secret**: `GH_PACKAGES_TOKEN` — a PAT with `read:packages` on the OneStepRND org.
Needed because the build resolves `co.onestep.android:core` (from `onestep-sdk-android` packages)
and `co.onestep:design-system-kmp` (from `design-system-kmp` packages), and the default `github.token`
cannot read another repo's packages.

## Dependencies of note

- `co.onestep.android:core` — Android-only (androidMain). Pinned by `coreVersion` in
  `gradle/libs.versions.toml`, used verbatim (nothing appends the suffix for you). A
  `-SNAPSHOT` pin resolves from GitHub Packages, a plain version from Maven Central — see
  the channel note below.
- `co.onestep:design-system-kmp` — KMP design system, from GitHub Packages.
- iOS Swift bridge depends on `onestep-sdk-ios` (exact version pinned in `Package.swift`).

## Development

Standard KMP module layout under `uikit-kmp/`:

- `src/commonMain` — shared Compose Multiplatform UI
- `src/androidMain` — Android bridges to `co.onestep.android:core`
- `src/iosMain` — iOS actuals
- `OSTUIKitKMP/` — Swift package for local iOS development (Xcode) + the Swift bridge sources
  and bundled resources shipped to SPM consumers

Build checks:

```bash
./gradlew uikit-kmp:assembleRelease                          # Android
./gradlew uikit-kmp:assembleOSTUIKitReleaseXCFramework       # iOS (macOS only)
```

## iOS test app

`iosTestApp/` hosts **OSTUIKitTestApp**, a thin Swift shell around the shared KMP test app
(`testAppShared/` — the same Compose UI the Android harness uses). It consumes the local
`uikit-kmp/OSTUIKitKMP` package, whose binary framework is built from `:testAppShared` by
`rebuild.sh` (XcodeGen project — the xcodeproj is generated, not committed):

```bash
./iosTestApp/rebuild.sh   # builds the debug XCFramework, refreshes compose resources,
                          # regenerates the Xcode project and opens it
```

Then select an iOS 16+ simulator and run.
