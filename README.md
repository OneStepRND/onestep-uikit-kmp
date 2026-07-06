# OneStep UIKit KMP

Kotlin Multiplatform UI Kit for the OneStep SDK. Extracted from
[`onestep-sdk-android`](https://github.com/OneStepRND/onestep-sdk-android) (`uikit-kmp` module)
into a standalone repo.

- **Android**: published as a Maven artifact — `co.onestep.kmp:uikit-kmp`
- **iOS**: published as an SPM package wrapping the `OSTUIKit.xcframework` binary +
  a Swift bridge layer (`OSTUIKitKMP` module) that connects to the native
  [`onestep-sdk-ios`](https://github.com/OneStepRND/onestep-sdk-ios)

## Versions

Current pinned versions (generated from `uikit-kmp/build.gradle.kts` and
`gradle/libs.versions.toml` — do not edit by hand; run
`./scripts/update-readme-versions.sh` after a bump):

<!-- versions:start -->
![uikit-kmp](https://img.shields.io/badge/uikit--kmp-0.2.0-blue)
![core](https://img.shields.io/badge/core-2.0.3--SB7--SNAPSHOT-orange)
![design-system](https://img.shields.io/badge/design--system-1.1.0-green)
<!-- versions:end -->

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
and `co.onestep:design-system` (from `PatientApp` packages), and the default `github.token`
cannot read another repo's packages.

## Dependencies of note

- `co.onestep.android:core` — Android-only (androidMain). Snapshot builds use
  `<coreVersion>-SNAPSHOT` from GitHub Packages; release builds use Maven Central.
- `co.onestep:design-system` — KMP design system, from GitHub Packages.
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

`iosTestApp/` hosts **OSTUIKitTestApp**, a small SwiftUI harness that consumes the local
`uikit-kmp/OSTUIKitKMP` package (XcodeGen project — the xcodeproj is generated, not committed):

```bash
./iosTestApp/rebuild.sh   # builds the debug XCFramework, refreshes compose resources,
                          # regenerates the Xcode project and opens it
```

Then select an iOS 16+ simulator and run.
