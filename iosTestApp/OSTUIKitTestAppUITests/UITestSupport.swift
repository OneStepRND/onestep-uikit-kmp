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

    /// Mock-recording names bundled in the app (each `<name>.json.gz` in Resources/JSONmocks),
    /// mirroring `MockRecording.options` in the app target. Selecting one makes the SDK upload the
    /// bundled analyzed recording instead of the real (stationary) data, so the flow reaches a
    /// deterministic outcome. `success*` → a full analyzed measurement (`recording_completed`);
    /// `error_*` → the matching analysis-error/empty screen (no completion event).
    static let mockNone = "None"
    static let successMocks = ["successWalk", "stsSuccess", "tugSuccess", "dualTaskSuccess"]
    static let errorMocks = ["error_curvy", "error_no_cycle", "error_position", "error_other", "error_short", "error_static"]

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
    /// Ensures an element is present AND hittable, swiping up within a scrollable form/list to
    /// reveal it if it starts below the fold. Compose (unlike SwiftUI) reports off-screen nodes as
    /// existing, so mere existence is not enough — the hittability check is what triggers the
    /// swipe-to-reveal for the shared KMP screens.
    @discardableResult
    func ensureVisible(_ element: XCUIElement, in app: XCUIApplication, maxSwipes: Int = 6) -> Bool {
        if element.waitForExistence(timeout: 2), element.isHittable { return true }
        for _ in 0..<maxSwipes {
            app.swipeUp()
            if element.exists && element.isHittable { return true }
        }
        return element.exists && element.isHittable
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

    /// Polls until the element is hittable again (e.g. the underlying sheet reappears after a
    /// presented full-screen cover dismisses).
    @discardableResult
    func waitUntilHittable(timeout: TimeInterval) -> Bool {
        let deadline = Date().addingTimeInterval(timeout)
        while Date() < deadline {
            if isHittable { return true }
            usleep(300_000)
        }
        return isHittable
    }
}

// MARK: - Configure Flow + Compose recording helpers

extension XCTestCase {

    /// Reads the native "Last Event" label on Home (set from the recording flow's result callback),
    /// or nil if it isn't currently shown. This is the deterministic channel for asserting a
    /// recording outcome, since the Compose flow/summary themselves expose almost no a11y.
    func lastEventText(_ app: XCUIApplication) -> String? {
        let label = app.staticTexts["home.lastEvent"]
        return label.exists ? label.label : nil
    }

    /// Forces a Configure Flow toggle to a known state (they default on). No-op if already correct.
    func setToggle(_ identifier: String, on: Bool, in app: XCUIApplication) {
        let toggle = app.switches[identifier]
        guard ensureVisible(toggle, in: app) else { return }
        let isOn = (toggle.value as? String) == "1"
        if isOn != on { toggle.tap() }
    }

    /// Selects a mock in the Configure Flow dropdown (shared Compose UI). Opens
    /// `mockRecordingPicker`, then taps the option, which carries a `mockOption.<name>` testTag
    /// (surfaced to iOS as an accessibility identifier), falling back to a label match.
    func selectMock(_ name: String, in app: XCUIApplication) {
        // Mock options are an always-visible inline single-select list that sits below the fold in
        // a Compose scroll container. A tap issued immediately after a swipe can be swallowed by the
        // scroll settle, so tap, let it settle, then CONFIRM the picker header reflects the choice
        // (`Mock recording: <name>`), retrying a few times. The header is the source of truth for
        // what `selectedMock` will pass to `onStartFlow`.
        let header = app.staticTexts["mockRecordingPicker"]
        for _ in 0..<8 {
            if header.exists && header.label.contains(name) { return }
            let option = mockOptionElement(name, in: app)
            if option.exists && option.isHittable {
                option.tap()
                usleep(600_000)
                if header.exists && header.label.contains(name) { return }
            } else {
                app.swipeUp()
                usleep(400_000)
            }
        }
        // Best effort: one final tap if reachable. Downstream assertions verify the real outcome
        // (header may have scrolled off for options deep in the list).
        let option = mockOptionElement(name, in: app)
        if option.exists && option.isHittable { option.tap() }
    }

    /// A mock dropdown option, queried by its `mockOption.<name>` testTag across button/other/
    /// staticText element types (Compose surfaces the DropdownMenuItem inconsistently by type).
    func mockOptionElement(_ name: String, in app: XCUIApplication) -> XCUIElement {
        let id = "mockOption.\(name)"
        let byId = app.descendants(matching: .any).matching(identifier: id).firstMatch
        return byId
    }

    /// Performs the slide-to-stop gesture on the Compose recording screen (a Skia canvas with no
    /// queryable handle) by dragging the handle along the bottom of the window, left to right. The
    /// handle sits near the very bottom (~0.93), starting at the left edge.
    func slideToStop(in app: XCUIApplication) {
        let start = app.coordinate(withNormalizedOffset: CGVector(dx: 0.15, dy: 0.93))
        let end = app.coordinate(withNormalizedOffset: CGVector(dx: 0.9, dy: 0.93))
        start.press(forDuration: 0.2, thenDragTo: end)
    }

    /// Taps the top-left toolbar close/back on a Compose screen by coordinate. Safe to call during
    /// recording/analysis: the flow HIDES its toolbar then (RecordFlowNavGraph hides it once the
    /// flow leaves GET_READY), so the tap is a no-op until a screen with a toolbar (the summary or
    /// an error screen) appears, at which point it dismisses that screen.
    func tapComposeTopLeftClose(in app: XCUIApplication) {
        app.coordinate(withNormalizedOffset: CGVector(dx: 0.08, dy: 0.10)).tap()
    }

    /// Whether a flow element with the exact label exists in ANY element type (Compose surfaces some
    /// controls as buttons, some as static texts / other). Presence-only, no hittability/wait.
    func flowElementExists(_ label: String, in app: XCUIApplication) -> Bool {
        app.descendants(matching: .any).matching(NSPredicate(format: "label == %@", label)).firstMatch.exists
    }

    /// Taps a flow element by exact label only if it is present AND hittable right now (no waiting),
    /// trying buttons then static texts. Iterates ALL matches and taps the first hittable one, so a
    /// covered same-label element (e.g. the Configure Flow `configure.start`, also labeled "Start",
    /// sitting behind the presented flow) is skipped in favor of the visible flow control. Returns
    /// whether it tapped. Use when polling across screens whose order isn't known ahead of time.
    @discardableResult
    func tapIfHittable(_ label: String, in app: XCUIApplication) -> Bool {
        let predicate = NSPredicate(format: "label == %@", label)
        for query in [app.buttons.matching(predicate), app.staticTexts.matching(predicate)] {
            for index in 0..<query.count {
                let element = query.element(boundBy: index)
                if element.exists && element.isHittable {
                    element.tap()
                    return true
                }
            }
        }
        return false
    }

    /// Taps a Compose flow element by its exact accessibility label, trying button then static text
    /// (Compose surfaces some rows as one, some as the other). Returns whether it tapped.
    @discardableResult
    func tapFlowLabel(_ label: String, in app: XCUIApplication, timeout: TimeInterval = 10) -> Bool {
        let button = app.buttons[label].firstMatch
        if button.waitForExistence(timeout: timeout) {
            button.tap()
            return true
        }
        let text = app.staticTexts[label].firstMatch
        if text.waitForExistence(timeout: 2) {
            text.tap()
            return true
        }
        return false
    }

    /// Taps the first visible flow CTA (Continue / Start / I'm ready / …) to advance preparation
    /// screens. Returns whether it tapped anything.
    @discardableResult
    func tapFirstFlowCTA(_ app: XCUIApplication) -> Bool {
        for cta in ["I'm ready", "Continue", "Got it", "Start", "Begin", "Next", "Allow", "Skip"] {
            let button = app.buttons.matching(NSPredicate(format: "label CONTAINS[c] %@", cta)).firstMatch
            if button.exists && button.isHittable {
                button.tap()
                return true
            }
        }
        return false
    }
}
