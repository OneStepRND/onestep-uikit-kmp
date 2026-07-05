import XCTest

/// End-to-end UI coverage for the OSTUIKit test app: the native Configure Flow (all measurement
/// types + toggles), launching every measurement's recording flow, a deep drive of a single
/// recording, both permission modes, and the care log.
///
/// NOTE on mock recording: these smoke tests drive the *real* recorder (no mock), so on a
/// stationary device an analysis ends in an error/no-cycle state — they assert the flow launches and
/// progresses and capture screenshots, not a successful analyzed measurement. For deterministic
/// analyzed outcomes (the iOS analogue of Android's mock IMU) see `MockRecordingUITests`, which
/// selects a bundled mock recording so the SDK returns a real analyzed measurement even when
/// stationary.
final class AppFlowUITests: XCTestCase {

    override func setUp() {
        super.setUp()
        continueAfterFailure = true
    }

    // MARK: Home + Configure Flow parity

    func testHomeLoads() {
        let app = launchIdentifiedApp()
        XCTAssertTrue(app.buttons["home.configureAndRecord"].exists)
        XCTAssertTrue(app.buttons["home.careLog"].exists)
        XCTAssertTrue(app.buttons["home.measurementSummary"].exists)
        XCTAssertTrue(app.buttons["home.permissionInApp"].exists)
        XCTAssertTrue(app.buttons["home.permissionBackground"].exists)
        attachScreenshot("00-home")
    }

    func testConfigureFlowListsAllActivities() {
        let app = launchIdentifiedApp()
        app.buttons["home.configureAndRecord"].tap()

        XCTAssertTrue(app.buttons["configure.start"].waitForExistence(timeout: 10),
                      "Configure Flow did not open")
        for key in Self.measurementKeys {
            XCTAssertTrue(ensureVisible(app.buttons["activity.\(key)"], in: app),
                          "Activity row missing for \(key)")
        }
        // Toggles present (may sit below the fold — scroll to reveal).
        XCTAssertTrue(ensureVisible(app.switches["toggle.voiceOver"], in: app))
        XCTAssertTrue(ensureVisible(app.switches["toggle.permissionExplanation"], in: app))
        attachScreenshot("01-configure-flow")
    }

    // MARK: Launch every measurement type

    /// Selects each activity in turn and starts its recording flow, verifying the Compose flow
    /// takes over the screen. Relaunches per type for a clean, deterministic starting point.
    func testStartEachMeasurementType() {
        for key in Self.measurementKeys {
            let app = launchIdentifiedApp()
            app.buttons["home.configureAndRecord"].tap()

            let start = app.buttons["configure.start"]
            XCTAssertTrue(start.waitForExistence(timeout: 10),
                          "Configure Flow did not open for \(key)")

            let activity = app.buttons["activity.\(key)"]
            XCTAssertTrue(ensureVisible(activity, in: app), "Activity \(key) missing")
            activity.tap()
            start.tap()

            // The Compose recording flow presents over the Configure sheet: the native START button
            // must no longer be hittable. A permission dialog may appear first on a fresh install.
            dismissSystemAlertsIfPresent(timeout: 2)
            let covered = start.waitUntilNotHittable(timeout: 15)
            let chrome = waitForComposeFlowChrome(app, timeout: 8)
            XCTAssertTrue(covered || chrome,
                          "Recording flow for \(key) did not start")
            attachScreenshot("start-\(key)")
            app.terminate()
        }
    }

    // MARK: Deep drive of a single recording

    /// Drives a Walk recording as far as the UI allows on a stationary device, tapping through the
    /// preparation screens and capturing each phase. Verifies we leave the native shell and the
    /// Compose flow progresses; the analyzed result is not asserted (see file note on mock IMU).
    func testWalkRecordingDeepFlow() {
        let app = launchIdentifiedApp()
        app.buttons["home.walkRecording"].tap()

        dismissSystemAlertsIfPresent(timeout: 3)
        XCTAssertTrue(app.buttons["home.walkRecording"].waitUntilNotHittable(timeout: 15),
                      "Walk recording flow did not launch")
        attachScreenshot("walk-01-launched")
        attachText("walk-hierarchy", app.debugDescription)

        // Try to advance through instruction / get-ready screens by tapping common CTA labels.
        let ctas = ["Start", "I'm ready", "Continue", "Got it", "Next", "Begin", "Allow"]
        for step in 0..<4 {
            dismissSystemAlertsIfPresent(timeout: 1)
            var advanced = false
            for cta in ctas {
                let button = app.buttons.matching(
                    NSPredicate(format: "label CONTAINS[c] %@", cta)
                ).firstMatch
                if button.exists && button.isHittable {
                    button.tap()
                    advanced = true
                    break
                }
            }
            usleep(1_500_000)
            attachScreenshot("walk-0\(step + 2)-step")
            if !advanced { break }
        }

        // Let the recorder run briefly, then screenshot whatever phase we reached.
        sleep(6)
        attachScreenshot("walk-99-final")
    }

    // MARK: Permission flows

    func testPermissionInAppFlow() {
        let app = launchIdentifiedApp()
        app.buttons["home.permissionInApp"].tap()
        XCTAssertTrue(app.buttons["home.permissionInApp"].waitUntilNotHittable(timeout: 15),
                      "In-app permission flow did not launch")
        dismissSystemAlertsIfPresent(timeout: 3)
        sleep(2)
        attachScreenshot("permission-inapp")
    }

    func testPermissionBackgroundFlow() {
        let app = launchIdentifiedApp()
        app.buttons["home.permissionBackground"].tap()
        XCTAssertTrue(app.buttons["home.permissionBackground"].waitUntilNotHittable(timeout: 15),
                      "Background permission flow did not launch")
        dismissSystemAlertsIfPresent(timeout: 3)
        sleep(2)
        attachScreenshot("permission-background")
    }

    // MARK: Care log

    func testCareLogOpens() {
        let app = launchIdentifiedApp()
        app.buttons["home.careLog"].tap()
        XCTAssertTrue(app.buttons["home.careLog"].waitUntilNotHittable(timeout: 15),
                      "Care log did not open")
        sleep(3)
        attachScreenshot("care-log")
    }
}
