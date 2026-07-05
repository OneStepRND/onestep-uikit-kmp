import XCTest

/// End-to-end coverage of the **mock-recording** path — the iOS analogue of Android's mock IMU.
///
/// Selecting a mock in the Configure Flow sets `UserDefaults["MockRecordingName"]`; at S3 upload
/// time the SDK (`SDKNetworkService.mockFileData`) swaps the real recorded data for the bundled
/// `<name>.json.gz`, so the backend analyzes the mock and returns a real, deterministic
/// measurement even though the device is stationary. This lets us drive the whole recording flow to
/// a genuine analyzed outcome instead of the error state a stationary real recording produces.
///
/// Assertion channel: the Compose recording flow and summary expose almost no accessibility, so we
/// assert on the native "Last Event" label on Home, which the flow's result callback populates.
/// A `success*` mock reaches the summary → dismiss → `recording_completed`; an `error_*` mock
/// reaches an error/empty screen → dismiss with **no** completion event. That contrast is the test.
///
/// These tests perform a real recording + network upload + analysis on device, so they are slower
/// and depend on connectivity; screenshots are attached throughout for visual verification.
final class MockRecordingUITests: XCTestCase {

    override func setUp() {
        super.setUp()
        continueAfterFailure = true
    }

    // MARK: Configurability (native, reliable — no Compose gestures)

    /// Verifies the Configure Flow exposes the mock picker with every bundled mock, matching the
    /// Android test app's configurability. Pure native SwiftUI, so fully deterministic.
    func testConfigureFlowExposesMockPicker() {
        let app = launchIdentifiedApp()
        app.buttons["home.configureAndRecord"].tap()
        XCTAssertTrue(app.buttons["configure.start"].waitForExistence(timeout: 10),
                      "Configure Flow did not open")

        let picker = app.buttons["mockRecordingPicker"]
        XCTAssertTrue(ensureVisible(picker, in: app), "Mock picker missing")
        picker.tap()

        XCTAssertTrue(app.buttons[Self.mockNone].waitForExistence(timeout: 3),
                      "Mock picker did not open")
        for mock in Self.successMocks + Self.errorMocks {
            XCTAssertTrue(app.buttons[mock].exists, "Mock option missing: \(mock)")
        }
        attachScreenshot("configure-mock-picker")
        app.buttons["successWalk"].tap()
    }

    // MARK: Mock-driven end-to-end recordings

    /// The primary happy path: a WALK driven with the `successWalk` mock must reach a real analyzed
    /// summary and report `recording_completed` (only success outcomes carry `measurement_id`).
    func testWalkSuccessMockReachesCompletedResult() {
        let app = launchIdentifiedApp()
        configureAndStart(app, activityKey: "WALK", mock: "successWalk")

        XCTAssertTrue(driveWalkToTerminal(app, screenshotPrefix: "mock-walk-success"),
                      "Walk flow never reached a terminal screen for successWalk mock")

        let event = returnHomeAndReadEvent(app)
        attachText("mock-walk-success-event", event ?? "nil")
        XCTAssertEqual(event?.contains("recording_completed"), true,
                       "Expected recording_completed from successWalk mock, got: \(event ?? "nil")")
    }

    /// Contrast: an error mock (`error_short`) must NOT produce `recording_completed` — the flow
    /// lands on an analysis-error screen and dismisses without a completion event. Proves the mock
    /// payload (not the stationary device) drives the outcome.
    func testWalkErrorMockDoesNotComplete() {
        let app = launchIdentifiedApp()
        configureAndStart(app, activityKey: "WALK", mock: "error_short")

        XCTAssertTrue(driveWalkToTerminal(app, screenshotPrefix: "mock-walk-error"),
                      "Walk flow never reached a terminal screen for error_short mock")

        let event = returnHomeAndReadEvent(app)
        attachText("mock-walk-error-event", event ?? "nil")
        XCTAssertNotEqual(event?.contains("recording_completed"), true,
                          "error_short mock unexpectedly produced recording_completed: \(event ?? "nil")")
    }
}

// MARK: - Flow driving

private extension MockRecordingUITests {

    /// Opens the Configure Flow, selects the activity + mock, minimizes optional prep screens for
    /// determinism, and starts the flow (accepting any first-run permission dialog).
    func configureAndStart(_ app: XCUIApplication, activityKey: String, mock: String) {
        app.buttons["home.configureAndRecord"].tap()
        XCTAssertTrue(app.buttons["configure.start"].waitForExistence(timeout: 10),
                      "Configure Flow did not open for \(activityKey)")

        let activity = app.buttons["activity.\(activityKey)"]
        XCTAssertTrue(ensureVisible(activity, in: app), "Activity \(activityKey) missing")
        activity.tap()

        // Fewer prep screens → fewer blind gestures. Phone-position adds a required selection;
        // voice-over just adds audio. Leave permission-explanation on (it self-dismisses when
        // permissions are already granted on the device).
        setToggle("toggle.phonePosition", on: false, in: app)
        setToggle("toggle.voiceOver", on: false, in: app)

        selectMock(mock, in: app)

        let start = app.buttons["configure.start"]
        XCTAssertTrue(ensureVisible(start, in: app), "Start button missing")
        start.tap()
        dismissSystemAlertsIfPresent(timeout: 3)
    }

    /// Drives the WALK recording flow to a terminal screen (summary or error) and dismisses it,
    /// returning true once dismissed. Uses real Compose accessibility labels rather than blind
    /// coordinates, keyed on the flow's shared toolbar back button (`"Toolbar start icon"`), which
    /// the flow HIDES during recording/analysis and shows again on the terminal screen:
    ///
    ///   duration picker → prep/get-ready (toolbar visible) → recording/analysis (toolbar hidden)
    ///   → summary/error (toolbar visible again) → dismiss.
    func driveWalkToTerminal(_ app: XCUIApplication, screenshotPrefix: String) -> Bool {
        let toolbarBack = app.buttons["Toolbar start icon"]

        // 1) Confirm the flow opened on the walk-duration screen.
        if !app.buttons["1 minute"].waitForExistence(timeout: 20)
            && !app.staticTexts["1 minute"].waitForExistence(timeout: 2) {
            attachScreenshot("\(screenshotPrefix)-noDuration")
            return false
        }

        // 2) Advance the pre-recording screens by their real Compose labels — duration ("1 minute")
        //    → phone placement ("In the pocket") → instructions / get-ready CTAs — until recording
        //    begins, which the flow signals by HIDING its toolbar (RecordFlowNavGraph hides it once
        //    the flow leaves GET_READY). The get-ready countdown also auto-advances on its own.
        // Order matters when a screen shows more than one: prefer the specific progression choices
        // (duration, placement) before generic CTAs. "Start" taps the big record-ready button; the
        // covered Configure Flow "Start" is skipped because it isn't hittable. "View instructions"
        // is deliberately excluded (it opens a sheet).
        let advanceLabels = [
            "1 minute", "In the pocket",
            "I'm ready", "Continue", "Got it", "Start now", "Start", "Begin", "Next", "Skip", "Allow",
        ]
        let prepDeadline = Date().addingTimeInterval(45)
        while Date() < prepDeadline {
            if !toolbarBack.isHittable { break }   // toolbar gone → recording/analysis began
            var advanced = false
            for label in advanceLabels where tapIfHittable(label, in: app) {
                advanced = true
                break
            }
            if !advanced { tapFirstFlowCTA(app) }
            usleep(1_500_000)
        }
        attachScreenshot("\(screenshotPrefix)-recording")

        // 3) Wait for a terminal screen and dismiss it. The "1 minute" walk auto-stops on its own,
        //    so no slide-to-stop is needed (and sliding risks hitting the error screen's bottom
        //    "View instructions" button). The two outcomes differ in chrome:
        //    • SUCCESS → summary: has the top-left back chevron ("Toolbar start icon"; the only
        //      labeled toolbar control). Tapping it dismisses the summary → emits recording_completed.
        //    • ERROR/too-short → error screen: NO back chevron; shows "Try again" + "View
        //      instructions" and a top-right ✕. Detect via those labels and dismiss via the ✕
        //      (top-right coordinate) — NOT "Try again" (restarts) or "View instructions" (opens a
        //      sheet). For the error assertion, reaching the terminal is what matters; the ✕ tap is
        //      best-effort (the test relaunches regardless).
        let terminalDeadline = Date().addingTimeInterval(150)
        while Date() < terminalDeadline {
            if toolbarBack.isHittable {
                attachScreenshot("\(screenshotPrefix)-terminal")
                toolbarBack.tap()
                return true
            }
            if flowElementExists("Try again", in: app) || flowElementExists("View instructions", in: app) {
                attachScreenshot("\(screenshotPrefix)-terminal")
                app.coordinate(withNormalizedOffset: CGVector(dx: 0.9, dy: 0.085)).tap()
                return true
            }
            usleep(2_000_000)
        }
        attachScreenshot("\(screenshotPrefix)-timeout")
        return false
    }

    /// Dismisses the Configure Flow sheet (Cancel) to reveal Home, then reads the result label the
    /// recording flow reported.
    func returnHomeAndReadEvent(_ app: XCUIApplication) -> String? {
        let cancel = app.buttons["configure.cancel"]
        if cancel.waitForExistence(timeout: 5) { cancel.tap() }
        _ = app.buttons["home.configureAndRecord"].waitForExistence(timeout: 10)
        return lastEventText(app)
    }
}
