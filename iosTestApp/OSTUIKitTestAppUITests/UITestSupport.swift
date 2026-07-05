import XCTest

/// Shared helpers for the OSTUIKit test-app UI suite.
///
/// The native shell (Home / Configure Flow / Settings / Measurement picker) is SwiftUI and fully
/// queryable via accessibility identifiers. The recording flow, permission flow, care log and
/// measurement summary are Compose Multiplatform (Skia) views: their accessibility tree is thin, so
/// for those screens we verify by (a) confirming we left the native screen and (b) capturing
/// full-screen screenshots as attachments for visual inspection — the same approach the original
/// SummaryUITests established.
extension XCTestCase {

    /// The activity keys shown on the Configure Flow screen. Kept in sync (by hand — the UI-test
    /// target is black-box) with `MeasurementPresets.all` in the app target.
    static let measurementKeys = [
        "WALK",
        "STS",
        "TUG",
        "BALANCE_TEST",
        "STATIC_BALANCE",
        "SIX_MINUTE_WALK",
        "TWO_MINUTE_WALK",
        "DUAL_TASK",
    ]

    /// Launches the app and returns it once the identified Home screen is showing. If the app comes
    /// up unauthenticated (no persisted patient), connects as the built-in "Avatar" test patient.
    @discardableResult
    func launchIdentifiedApp(file: StaticString = #filePath, line: UInt = #line) -> XCUIApplication {
        let app = XCUIApplication()
        app.launch()

        let home = app.buttons["home.configureAndRecord"]
        if home.waitForExistence(timeout: 45) {
            return app
        }

        // Not identified yet — use the Settings "Connect as Avatar" path, then wait for Home.
        let avatar = app.buttons["settings.connectAvatar"]
        if avatar.waitForExistence(timeout: 15) {
            avatar.tap()
        }
        XCTAssertTrue(
            home.waitForExistence(timeout: 90),
            "Home never appeared — SDK failed to initialize/identify",
            file: file, line: line
        )
        return app
    }

    /// Best-effort dismissal of any system permission alert (motion, notifications, microphone,
    /// location). On a device that has already granted these, nothing appears and this is a no-op.
    func dismissSystemAlertsIfPresent(timeout: TimeInterval = 3) {
        let springboard = XCUIApplication(bundleIdentifier: "com.apple.springboard")
        let acceptLabels = [
            "Allow While Using App",
            "Allow Once",
            "Allow",
            "OK",
            "Continue",
        ]
        // A couple of passes: some flows chain two dialogs (e.g. motion then notifications).
        for _ in 0..<3 {
            var tapped = false
            for label in acceptLabels {
                let button = springboard.buttons[label]
                if button.waitForExistence(timeout: timeout) {
                    button.tap()
                    tapped = true
                    break
                }
            }
            if !tapped { break }
        }
    }

    /// Heuristic: does the current screen look like a Compose recording/flow screen? Looks for
    /// common flow chrome that Compose surfaces to accessibility (buttons/text like Start, Continue,
    /// I'm ready, Got it, or a close control). Returns true on the first match.
    @discardableResult
    func waitForComposeFlowChrome(_ app: XCUIApplication, timeout: TimeInterval = 12) -> Bool {
        let needles = ["start", "ready", "begin", "continue", "got it", "next", "close", "cancel", "skip", "allow"]
        let deadline = Date().addingTimeInterval(timeout)
        while Date() < deadline {
            for needle in needles {
                let predicate = NSPredicate(format: "label CONTAINS[c] %@", needle)
                if app.buttons.matching(predicate).firstMatch.exists
                    || app.staticTexts.matching(predicate).firstMatch.exists {
                    return true
                }
            }
            usleep(400_000)
        }
        return false
    }

    /// Attaches a full-screen screenshot kept for post-run inspection.
    func attachScreenshot(_ name: String) {
        let screenshot = XCUIScreen.main.screenshot()
        let attachment = XCTAttachment(screenshot: screenshot)
        attachment.name = name
        attachment.lifetime = .keepAlways
        add(attachment)
    }

    /// Attaches arbitrary text (e.g. an accessibility-tree dump) for post-run inspection.
    func attachText(_ name: String, _ text: String) {
        let attachment = XCTAttachment(string: text)
        attachment.name = name
        attachment.lifetime = .keepAlways
        add(attachment)
    }
}

extension XCTestCase {
    /// Ensures an element is present in the tree, swiping up within a scrollable form/list to reveal
    /// it if it starts below the fold. Returns whether it ended up existing.
    @discardableResult
    func ensureVisible(_ element: XCUIElement, in app: XCUIApplication, maxSwipes: Int = 6) -> Bool {
        if element.waitForExistence(timeout: 2) { return true }
        for _ in 0..<maxSwipes {
            app.swipeUp()
            if element.exists { return true }
        }
        return element.exists
    }
}

extension XCUIElement {
    /// Polls until the element is no longer hittable (e.g. covered by a presented Compose flow).
    @discardableResult
    func waitUntilNotHittable(timeout: TimeInterval) -> Bool {
        let deadline = Date().addingTimeInterval(timeout)
        while Date() < deadline {
            if !isHittable { return true }
            usleep(300_000)
        }
        return !isHittable
    }
}
