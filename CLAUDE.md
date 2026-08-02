# CLAUDE.md — onestep-uikit-kmp

Kotlin Multiplatform UI kit for the OneStep SDK (Compose Multiplatform, `uikit-kmp` module).
Targets: `androidTarget`, `iosArm64`, `iosSimulatorArm64` — **no iosX64** (CMP stopped
publishing x64 iOS artifacts after 1.11.0-alpha01; do not re-add it).
Published as an Android library (Maven) and an iOS `OSTUIKit.xcframework` wrapped by the
root `Package.swift` (SPM). Test harness: `testAppShared/` (shared Compose UI + `TestAppShell`
native-SDK abstraction, unpublished) with thin platform shells in `androidTestApp/` and
`iosTestApp/`. New harness features go in `testAppShared/` commonMain, not the shells.
This is a **library**: every dependency and public API is inherited by every consuming app.

## Library discipline (scope ladder — stop at the first rung that holds)

1. Does it need to exist? (YAGNI — skip it)
2. Kotlin stdlib / coroutines / kotlinx already do it? Use it.
3. Compose / Jetpack / platform feature covers it? Use it.
4. An already-integrated dependency solves it? Use it — **never add a new dependency if
   avoidable**: it is forced onto every consumer app and widens the HIPAA + supply-chain
   review surface. New deps must be multiplatform (or wired per-platform via expect/actual).
5. Only then: the minimum that works. Keep the published API surface minimal
   (`internal` by default; `public` only for intentional API).

Mark deliberate shortcuts with a `// shortcut:` comment naming the ceiling and upgrade path.
Never trade away: input validation at trust boundaries, error handling that prevents data
loss, thread-safety, PII/PHI handling, accessibility, anything explicitly requested.

## KMP rules

- **Every change must compile for Android AND iOS.** The compile gate is:
  `./gradlew :uikit-kmp:compileAndroidMain :uikit-kmp:compileKotlinIosSimulatorArm64`
  Run it before claiming any task complete — an Android-only compile proves nothing here.
- Code goes in `commonMain` unless it genuinely needs platform APIs; then `expect`/`actual`
  or an interface implemented in `androidMain`/`iosMain`. Don't leak platform types into
  common APIs.
- iOS has **no reflection**: anything relying on reflection-based serialization or class
  lookup on Android needs an explicit registration path for iOS (see Navigation below).
- Compose resources are NOT embedded in the static iOS framework — they are copied beside
  the XCFramework (`iosTestApp/rebuild.sh` and `scripts/publish-xcframework.sh` handle it).
  If iOS shows missing strings/images after a resource change, re-run the copy step.

## Navigation (Navigation 3)

All navigation is Navigation 3 (`NavDisplay` + `rememberNavBackStack` + `entryProvider`).
Shared conventions (transitions, `pop()`/`popUpToInclusive()` helpers) live in
`uikit-kmp/src/commonMain/kotlin/co/onestep/kmp/uikit/navigation/UIktNavDisplay.kt`.

- **Every new destination must be registered in `UIktNavSavedStateConfiguration`**
  (`UIktNavDisplay.kt`) or back-stack state saving fails on iOS. Registration is manual:
  no reflection on iOS, and destinations span packages so a sealed hierarchy is impossible.
- New destinations: `@Serializable` `data object`/`data class` implementing `UIktDestination`,
  plus an `EntryProviderScope<NavKey>.xScreen(...)` extension. Navigation events are hoisted
  lambdas; screens never receive the back stack.
- Analytics screen names derive from the key's simple class name — renaming a destination
  class changes emitted analytics; check the trackers before renaming.

## Compose

Global Compose rules apply (stable params, hoisted state, previews). KMP additions:
- Previews live in `commonMain` via `org.jetbrains.compose.ui.tooling.preview.Preview`.
- Strings/images go through compose resources (`Res.*`) — never Android-only resources in
  `commonMain`.

## Build, test, verify

| What | Command |
|---|---|
| Compile gate (both platforms) | `./gradlew :uikit-kmp:compileAndroidMain :uikit-kmp:compileKotlinIosSimulatorArm64` |
| Unit tests | `./gradlew :uikit-kmp:iosSimulatorArm64Test` (android host tests are not enabled) |
| Android harness APK | `./gradlew :androidTestApp:assembleDebug` |
| iOS harness (XCFramework + resources + xcodeproj) | `./iosTestApp/rebuild.sh` |
| Publishing | see `README.md` / `scripts/publish*.sh` |

- Never pipe build/install commands through filters that mask exit codes; check the
  command's own exit status. Gradle's `kmpPartiallyResolvedDependenciesChecker` prints `e:`
  lines that are non-fatal — "has `e:` output" ≠ "failed"; "BUILD SUCCESSFUL" is the signal.
- After deploying an artifact (APK / XCFramework / AAR), verify the artifact itself changed
  before testing against it — a distinctive new log line is cheap and conclusive.
- Test-app gotchas: rotation resets the Android harness to Home (its screen state is a plain
  `remember`, not a library bug); the mock IMU requires a patient context to be set up.

## Debugging

- Root cause first: read the error/stack trace before changing code; fix the missing case,
  not a filter around it. Don't mask symptoms with workarounds.
- When a fix depends on runtime data not visible in the code (API payloads, bridge values,
  serialized state), get a concrete sample (log line / payload) before proposing the fix.
- Version bumps here have cross-cutting fallout (compileSdk floors, dropped targets,
  transitive androidx requirements) — read the resolution error fully before "fixing" code.

## Versioning & release channels

**`-SNAPSHOT` here is a distribution channel, not a Maven snapshot.** OneStep reuses the
Maven suffix to mark the **internal** channel, and those builds are **immutable**:

| Version form | Channel | Consumed by |
|---|---|---|
| `2.1.1-SNAPSHOT`, `0.6.3-SNAPSHOT` | GitHub Packages (private) | OneStep patient app, clinician app — **this is what ships to production internally** |
| `2.1.1`, `0.6.3` | Maven Central (public) | External clients (e.g. Zimmer) |

Consequences — get these right, the standard Maven reading of `-SNAPSHOT` is wrong here:

- Never call a OneStep `-SNAPSHOT` pin mutable, unreproducible, or unsafe to ship, and never
  advise "resolve it to a released version before a production cut". The suffixed version *is*
  the internal production artifact.
- Publishing a `-SNAPSHOT` `uikit-kmp` that depends on a `-SNAPSHOT` `core` is the normal
  internal release path, not a smell.
- Dropping the suffix is a **distribution decision** (open the version to external clients), not
  a stability promotion. Don't do it as part of a routine bump.

Version numbers live in `uikit-kmp/build.gradle.kts` (`versionMajor/Minor/Patch`) and dependency
pins in `gradle/libs.versions.toml`. After any bump run `./scripts/update-readme-versions.sh` —
the README badges are generated, never hand-edited. Feature work takes a **patch** bump here
(0.5.11 → 0.5.12 carried a feature); reserve minor bumps for genuinely breaking API changes.

Releasing is CI, not local: dispatch `publish-android.yml` then `publish-ios.yml` (`snapshot: true`)
against `main`. The iOS job tags, creates the GitHub release, uploads `OSTUIKit.xcframework.zip`,
and commits the `Package.swift` URL+checksum rewrite back to `main` — so `git pull` afterwards.
Verify the published zip's `swift package compute-checksum` matches `Package.swift` before
declaring a release good; a stale checksum fails SPM resolution for every consumer.

## Git & PRs

- Commit style (match `git log`): `Feat:` / `Fix:` / `Chore:` / `Test:` / `Docs:` prefix,
  imperative summary ≤72 chars, body explains the why.
- Do NOT commit or push unless explicitly asked; when finishing uncommitted work, print a
  ready-to-use commit message instead.
- Branch naming, Jira ticket format, and the >10-file PR-splitting-by-risk convention are in
  the global CLAUDE.md — they apply to this repo.

## HIPAA

OneStep is HIPAA-compliant. Never put PII/PHI (patient names, free-text clinician notes,
health values tied to identity) into analytics events, logs, string resources, commit
messages, PRs, or Jira tickets. Existing analytics call sites mark these constraints with
`(HIPAA)` comments — preserve them when refactoring.

## Continuous improvement

When a task needed correction rounds (fix didn't work, user pushed back, or something had to
be explained that was traceable from the repo), after solving it: identify the failure
pattern and propose a CLAUDE.md update — written as a generalizable principle, not an
overfitted example.
