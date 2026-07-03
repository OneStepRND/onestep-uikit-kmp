# UIKit KMP Migration Plan — From Android-Only to Cross-Platform

## Current State (Post Phase H)

| Source Set | Files | Content |
|-----------|-------|---------|
| **commonMain** | 163 | UI composables, models, theme, components, bridges — **already cross-platform** |
| **androidMain** | 60 | ViewModels, navigation, permissions, mappers, platform actuals |
| **iosMain** | 10 | 4 implemented actuals + 5 stubs + 1 intentional no-op |

**Build status:** All 3 targets compile clean (commonMain, androidDebug, iosArm64).

---

## Phase I: Quick Wins — Move Already-Portable Files to commonMain

**Effort:** Small (~1 hour)
**Risk:** Very low
**Prerequisite:** None

These 4 androidMain files have **zero Android-specific imports** and can move to commonMain as-is:

| File | Current Location (androidMain) | Why It's Portable |
|------|-------------------------------|-------------------|
| `SummaryDataFactory.kt` | `features/summary/` | Pure Compose + generated `Res.*` resources only |
| `RecordFlowDataFactory.kt` | `features/recordFlow/` | Pure Compose + generated `Res.*` resources only |
| `AnalysisBannerType.kt` | `features/summary/presentation/` | Pure Kotlin enum, no imports |
| `InsightTypeExt.kt` | `features/summary/presentation/` | Uses KMP `OSTInsightType` + `DrawableResource` |

### Steps
1. Move each file from `src/androidMain/kotlin/...` → `src/commonMain/kotlin/...` (same package path)
2. Remove any leftover Android imports (there shouldn't be any)
3. Build all 3 targets to verify:
   ```bash
   ./gradlew :uikit-kmp:compileCommonMainKotlinMetadata
   ./gradlew :uikit-kmp:compileDebugKotlinAndroid
   ./gradlew :uikit-kmp:compileKotlinIosArm64
   ```

### Verification
- Zero compile errors on all 3 targets
- `InsightTypeExt.kt` resolves `DrawableResource` and `Res.drawable.*` from commonMain

---

## Phase J: ResourceProvider Abstraction

**Effort:** Medium (~2-3 hours)
**Risk:** Low
**Prerequisite:** None (can run in parallel with Phase I)

`ResourceProvider` wraps `android.content.Context.getString()`. It's used by ViewModels to get localized strings outside of `@Composable` scope. This is a **critical blocker** for moving ViewModels to commonMain.

### Current Implementation (androidMain)
```kotlin
class ResourceProvider(private val context: Context) {
    fun getString(resId: Int): String = context.getString(resId)
    fun getString(resId: Int, vararg args: Any): String = context.getString(resId, *args)
}
```

### Strategy: expect/actual

**commonMain** — define interface:
```kotlin
// utils/ResourceProvider.kt
expect class ResourceProvider {
    fun getString(key: StringResource): String
    fun getString(key: StringResource, vararg args: Any): String
}
```

**androidMain** — keep current Context-based implementation as `actual`:
```kotlin
actual class ResourceProvider(private val context: Context) {
    actual fun getString(key: StringResource): String = /* resolve from context or Res */
    actual fun getString(key: StringResource, vararg args: Any): String = /* resolve */
}
```

**iosMain** — implement using CMP resource API:
```kotlin
actual class ResourceProvider {
    actual fun getString(key: StringResource): String = /* use CMP getString() */
    actual fun getString(key: StringResource, vararg args: Any): String = /* use CMP getString() */
}
```

### Alternative (Simpler)
Replace `resourceProvider.getString(R.string.x)` calls in ViewModels with CMP's `Res.string.x` references, and resolve them in Composables via `stringResource()`. This removes the need for `ResourceProvider` entirely but requires larger refactoring in ViewModels.

### Steps
1. Audit all `resourceProvider.getString()` calls — identify which R.string keys are used
2. Ensure all needed strings exist in `commonMain/composeResources/values/strings.xml`
3. Choose strategy (expect/actual vs. remove ResourceProvider)
4. Implement and build-verify all 3 targets

### Verification
- `ResourceProvider` usable from commonMain
- All string resolutions work on Android (iOS can return placeholder strings for now)

---

## Phase K: KMP ViewModel

**Effort:** Medium (~3-4 hours)
**Risk:** Medium
**Prerequisite:** Phase J (ResourceProvider)

### Current State
3 ViewModels in androidMain extend `androidx.lifecycle.ViewModel`:
- `SummaryViewModel.kt` — largest, ~500 lines, heavy core SDK usage
- `MotionRecorderViewModel.kt` — ~400 lines, recorder state management
- `PermissionWizardViewModel.kt` — stays in androidMain (permissions are platform-specific)

### Strategy: JetBrains Lifecycle ViewModel KMP

Google/JetBrains now ships `lifecycle-viewmodel-compose` for KMP:

```kotlin
// build.gradle.kts — commonMain dependencies
implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
implementation("org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
```

This gives you `ViewModel` and `viewModelScope` in commonMain. The API is identical to Android's.

### Android Core SDK Dependency (Big Blocker)

Both ViewModels import heavily from `co.onestep.android.core.*`:

**SummaryViewModel uses:**
- `OneStep` (SDK singleton), `OSTRecorder`, `OSTMotionDataService`, `OSTInsights`
- `OSTMotionMeasurement`, `OSTResult`, `OSTResultState`
- `OSTNorm`, `OSTParameterMetadata`, `OSTParamName`
- `OSTInsight`, `OSTInsightType`
- `OSTOrder`, `OSTTimeRangeFilter`, `OSTTimeRangedDataRequest`
- `OSTActivityType`, `OSTWalkCourseLength`, `OSTDailyBackgroundMeasurement`

**MotionRecorderViewModel uses:**
- `OneStep`, `OSTRecorder`, `OSTRecorder.Companion.READY_FOR_ANALYSIS_KEY`
- `OSTUserInputMetaData`, `OSTMotionMeasurement`, `OSTWalkCourseLength`
- `OSTAnalyserError`, `OSTAnalyserState`, `OSTRecorderState`

### Strategy: Bridge Interfaces

The existing bridge pattern (`RecorderBridge`, `MotionDataBridge`, `OSTSDKBridge`, `InsightsBridge`) is the right approach. Extend it:

1. **Audit** which core SDK calls each ViewModel makes
2. **Add methods** to existing bridges (or create new ones) in commonMain
3. **Implement** bridge methods in androidMain using actual core SDK calls
4. **Replace** direct `co.onestep.android.core.*` usage in VMs with bridge calls
5. **Move** ViewModels to commonMain

### Files to Move (after bridge work)
| File | Blocker | Bridge Needed |
|------|---------|---------------|
| `SummaryViewModel.kt` | Core SDK types, R.string | `MotionDataBridge` + `InsightsBridge` extension, ResourceProvider |
| `MotionRecorderViewModel.kt` | Core SDK types, R.raw audio | `RecorderBridge` extension, audio bridge |

### What Stays in androidMain
- `PermissionWizardViewModel.kt` — permissions are platform-specific by design
- `mapper/` package — Android SDK ↔ KMP type converters

### Steps
1. Add KMP lifecycle-viewmodel-compose dependency
2. Audit all core SDK calls in both ViewModels
3. Extend bridge interfaces with needed methods
4. Implement bridge methods in androidMain (delegates to actual SDK)
5. Create stub bridge implementations in iosMain
6. Refactor ViewModels to use bridges instead of direct SDK access
7. Move ViewModels to commonMain
8. Build-verify all 3 targets

### Verification
- ViewModels compile in commonMain
- Android app still works (bridge delegates to real SDK)
- iOS target compiles (bridge stubs return defaults)

---

## Phase L: KMP Navigation

**Effort:** Large (~1-2 days)
**Risk:** Medium-High
**Prerequisite:** Phase K (ViewModels in commonMain)

### Current Navigation Architecture

All navigation uses **androidx.navigation.compose**:
- `SummaryMainFlow.kt` — NavHost with 8 composable routes
- `PermissionsFlowScreen.kt` — NavHost with 8 permission destinations
- Recording flow — destinations defined but not yet wired

Pattern used throughout:
```kotlin
@Serializable data object MyDestination : UIktDestination
fun NavGraphBuilder.myScreen(...) { composable<MyDestination> { ... } }
NavHost(navController, startDestination = MyDestination) { myScreen(...) }
```

### Strategy: JetBrains Navigation Compose KMP

JetBrains ships a KMP version of navigation-compose:

```kotlin
// build.gradle.kts — commonMain dependencies
implementation("org.jetbrains.androidx.navigation:navigation-compose:2.8.0-alpha10")
```

**Key advantage:** The API is nearly identical to what you already use (`NavHost`, `NavController`, `composable<T>`). Migration is mostly import swaps.

### Android-Specific Blockers in Navigation Files

| Blocker | Files Affected | Resolution |
|---------|---------------|------------|
| `android.content.Context` / `LocalContext` | SummaryMainFlow | Replace `context.getString()` with `stringResource()` or ResourceProvider |
| `BackHandler` | SummaryMainFlow | CMP has `BackHandler` in `org.jetbrains.compose.ui` (or use expect/actual) |
| `R.string.*` references | SummaryMainFlow, destinations | Replace with `Res.string.*` (CMP resources) |
| `CustomNavType` (serialization) | CustomQuestionDestination | Keep `@Serializable` + `typeOf<T>()` — works in KMP |

### Files to Move to commonMain

| File | Effort | Notes |
|------|--------|-------|
| `UIktDestination.kt` | Trivial | Marker interface, no Android deps |
| `SummaryScreenDestination.kt` | Low | @Serializable object |
| `TaggingScreenDestination.kt` | Low | @Serializable object |
| `EditAssistiveDeviceDestination.kt` | Low | @Serializable object |
| `EditFootwearDestination.kt` | Low | @Serializable object |
| `EditLevelOfAssistanceDestination.kt` | Low | @Serializable object |
| `CustomQuestionDestination.kt` | Medium | Has CustomNavType, verify KMP serialization |
| `CustomNavType.kt` | Medium | Uses `NavType<T>`, check KMP navigation API |
| `SummaryMainFlow.kt` | High | NavHost + Context + BackHandler + string resources |
| Record flow destinations | Low | @Serializable objects |

### Files That Stay in androidMain
| File | Reason |
|------|--------|
| `PermissionsFlowScreen.kt` | Permissions are platform-specific |
| All `permissions/destinations/*.kt` | Permissions are platform-specific |
| `PermissionWizardViewModel.kt` | Permissions are platform-specific |

### Steps
1. Add JetBrains navigation-compose KMP dependency (replace Android one for commonMain)
2. Keep Android navigation-compose in androidMain for permissions flow
3. Move `UIktDestination.kt` to commonMain
4. Move all summary + record flow destination files to commonMain
5. Refactor `SummaryMainFlow.kt`:
   - Replace `LocalContext.current.getString()` → `stringResource(Res.string.x)`
   - Replace `android.content.Context` parameter → remove or use bridge
   - Verify `BackHandler` works with CMP
6. Move `SummaryMainFlow.kt` to commonMain
7. Build-verify all 3 targets

### Verification
- Navigation works on Android (regression test the flows)
- iOS target compiles
- Permission flow unchanged (still in androidMain)

---

## Phase M: iOS Actual Implementations

**Effort:** Large (~3-5 days, needs iOS expertise)
**Risk:** Medium
**Prerequisite:** Phases I-L complete (or can start audio/video in parallel)

### Current iosMain Status

| File | Status | iOS Framework Needed |
|------|--------|---------------------|
| `RandomUUID.ios.kt` | **Done** | Foundation (NSUUID) |
| `CurrentTimeMillis.ios.kt` | **Done** | Foundation (NSDate) |
| `PlatformLocale.ios.kt` | **Done** | Foundation (NSLocale) |
| `TimeUtil.ios.kt` | **Done** | Foundation (NSDateFormatter) |
| `IosEntryPoint.kt` | **Done** | ComposeUIViewController |
| `SystemBarEffect.ios.kt` | **Done** (no-op) | N/A — managed by host app |
| `PlatformAudioPlayer.ios.kt` | **Stub** | AVFoundation (AVAudioPlayer) |
| `PlatformVideoPlayer.ios.kt` | **Stub** | AVKit (AVPlayer + UIKitView) |
| `PlatformPermissions.ios.kt` | **Stub** | CoreMotion + UserNotifications |
| `PlatformPermissionFlow.ios.kt` | **Stub** | iOS-native permission UI |

### New actuals needed (from Phases J-L)

| expect | iosMain actual | iOS Framework |
|--------|---------------|---------------|
| `ResourceProvider` | CMP string resolution | compose.resources |
| Bridge implementations | iOS OneStep SDK calls | iOS OneStep SDK |
| `DateUtil` functions (if extracted) | NSDateFormatter | Foundation |
| `BackHandler` (if expect/actual) | iOS back gesture | UIKit |

### Steps
1. Implement `PlatformAudioPlayer` — wrap AVAudioPlayer for sound effects, AVSpeechSynthesizer for TTS
2. Implement `PlatformVideoPlayer` — wrap AVPlayer in UIKitView composable
3. Implement `PlatformPermissions` — CoreMotion (activity recognition), UNUserNotificationCenter (notifications)
4. Implement `PlatformPermissionFlow` — iOS-native permission request UI
5. Implement iOS bridge implementations (delegates to iOS OneStep SDK)
6. Implement `ResourceProvider` actual for iOS (if expect/actual strategy chosen)
7. Build-verify iosArm64 target

### Verification
- All 3 targets compile
- iOS app launches and renders shared composables
- Audio playback works on iOS
- Permission requests trigger native iOS dialogs

---

## Phase N: iOS SDK Mappers

**Effort:** Medium (~1-2 days)
**Risk:** Low
**Prerequisite:** Phase M (iOS actuals), iOS OneStep SDK available

### What Exists for Android

`androidMain/mapper/` contains 6 mapper files that convert `co.onestep.android.core.*` types → `co.onestep.kmp.uikit.*` KMP types:

| Mapper | Converts |
|--------|----------|
| `ActivityTypeMapper.kt` | `core.OSTActivityType` ↔ `kmp.OSTActivityType` |
| `EnumMappers.kt` | Various enum conversions |
| `InsightMapper.kt` | `core.OSTInsight` → `kmp.OSTInsight` |
| `MeasurementMapper.kt` | `core.OSTMotionMeasurement` → `kmp.OSTMotionMeasurement` |
| `ParamNameMapper.kt` | `core.OSTParamName` ↔ `kmp.OSTParamName` |
| `StateMapper.kt` | `core.OSTState` → `kmp.OSTState` |

### iOS Equivalent Needed

Create `iosMain/mapper/` with equivalent mappers that convert **iOS OneStep SDK types** → same `co.onestep.kmp.uikit.*` KMP types.

The exact mapper signatures depend on the iOS SDK's type names and structure.

### Steps
1. Review iOS OneStep SDK type definitions
2. Create mapper files mirroring the Android ones
3. Wire mappers into iOS bridge implementations
4. Build-verify iosArm64

---

## Phase O: iOS Entry Point & Integration

**Effort:** Medium (~1 day)
**Risk:** Low
**Prerequisite:** Phases M + N

### Current iOS Entry Point

`IosEntryPoint.kt` provides `OSTUIKitIos` object with factory methods:
```kotlin
object OSTUIKitIos {
    fun createCareLogViewController(config: ...): UIViewController
    fun createRecordingFlowViewController(config: ...): UIViewController
    fun createSummaryViewController(config: ...): UIViewController
    fun createPermissionFlowViewController(config: ...): UIViewController
}
```

### Steps
1. Wire factory methods to actual compose screens with proper bridge initialization
2. Create iOS demo app (SwiftUI) that calls these factories
3. Test full flows: care log → recording → summary → tagging
4. Test permission flow separately

### Verification
- iOS app renders all shared screens
- Navigation works within flows
- Data flows from iOS SDK → mappers → bridges → ViewModels → UI

---

## Dependency Graph

```
Phase I (Quick Wins)          Phase J (ResourceProvider)
       │                              │
       └──────────┬───────────────────┘
                  │
           Phase K (KMP ViewModel)
                  │
           Phase L (KMP Navigation)
                  │
        ┌─────────┴─────────┐
        │                    │
Phase M (iOS Actuals)   Can start audio/video
        │                in parallel with K/L
        │
Phase N (iOS Mappers)
        │
Phase O (iOS Entry Point)
```

**Phases I + J can run in parallel.**
**Phase M audio/video actuals can start anytime (no dependency on K/L).**

---

## Gradle Dependency Changes Summary

### Add to commonMain
```kotlin
commonMain.dependencies {
    // Phase K: KMP ViewModel
    implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose:2.8.4")

    // Phase L: KMP Navigation
    implementation("org.jetbrains.androidx.navigation:navigation-compose:2.8.0-alpha10")
}
```

### Keep in androidMain (for permissions)
```kotlin
androidMain.dependencies {
    // Permissions flow still uses Android navigation
    implementation(libs.navigation.compose)  // existing Android nav
}
```

### Remove from androidMain (after migration)
```kotlin
// These move to commonMain KMP equivalents:
// - lifecycle-viewmodel-compose (replaced by JetBrains KMP version)
// - navigation-compose (replaced by JetBrains KMP version for non-permission flows)
```

---

## Target Architecture (Post Phase O)

```
commonMain (~90% of code)
├── UI composables (screens, components, theme)
├── Models (all KMP types)
├── ViewModels (SummaryVM, MotionRecorderVM)
├── Navigation (SummaryMainFlow, destinations)
├── Bridge interfaces
├── Data factories
└── Utilities

androidMain (~5%)               iosMain (~5%)
├── Bridge actuals               ├── Bridge actuals
│   (delegates to Android SDK)   │   (delegates to iOS SDK)
├── Mappers (core→kmp)           ├── Mappers (core→kmp)
├── Platform actuals             ├── Platform actuals
│   (audio, video, locale)       │   (audio, video, locale)
├── Permission flow              ├── Permission flow
│   (Android-specific screens    │   (iOS-specific screens
│    + PermissionWizardVM)       │    + HealthKit/CoreMotion)
└── ResourceProvider actual      └── ResourceProvider actual
```

---

## Session Guide

Each phase is designed to be a **self-contained session**:

| Session | Start With | End With |
|---------|-----------|----------|
| Phase I | "Move SummaryDataFactory, RecordFlowDataFactory, AnalysisBannerType, InsightTypeExt from androidMain to commonMain" | 3-target build passes |
| Phase J | "Create expect/actual ResourceProvider so it works from commonMain" | ResourceProvider usable in commonMain, 3-target build |
| Phase K | "Add KMP lifecycle-viewmodel-compose, refactor SummaryViewModel and MotionRecorderViewModel to use bridges, move to commonMain" | ViewModels in commonMain, 3-target build |
| Phase L | "Add KMP navigation-compose, move summary/record destinations and SummaryMainFlow to commonMain" | Navigation in commonMain, 3-target build |
| Phase M | "Implement iOS actuals: audio (AVFoundation), video (AVPlayer), permissions (CoreMotion/UNNotification)" | iOS actuals implemented, 3-target build |
| Phase N | "Create iosMain/mapper/ mirroring androidMain/mapper/ for iOS SDK types" | iOS mappers complete, 3-target build |
| Phase O | "Wire IosEntryPoint factories to real screens, test in iOS demo app" | iOS app renders all flows |
