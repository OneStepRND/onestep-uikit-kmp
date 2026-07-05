import XCTest

/// Visual verification that iOS press feedback is the UIKit-style opacity dim, NOT the Android
/// Material ripple. We reach the walk-flow main CTA, capture it idle, then capture it again while
/// the finger is held down (the press gesture runs on a background queue so the main thread can
/// screenshot mid-press). Compare the two attachments:
///   - Fixed (iOS): the whole button is uniformly dimmed (~0.7 opacity), no expanding circle.
///   - Regression (Android ripple): a ripple scrim/circle appears over the button.
final class IndicationUITests: XCTestCase {

    override func setUp() {
        super.setUp()
        continueAfterFailure = true
    }

    func testWalkMainButtonPressIsIOSDimNotRipple() {
        let app = launchIdentifiedApp()

        // Enter the walk recording flow (Compose, wrapped in OneStepUiKitTheme).
        app.buttons["home.walkRecording"].tap()
        dismissSystemAlertsIfPresent(timeout: 3)
        XCTAssertTrue(app.buttons["home.walkRecording"].waitUntilNotHittable(timeout: 20),
                      "Walk recording flow did not launch")
        _ = waitForComposeFlowChrome(app, timeout: 10)

        // Target the walk-flow main CTA (Compose `.test("Walk flow screen main button")` exposes it
        // via both identifier and label). Fall back to the first hittable button in the flow.
        let target = resolveTarget(app)
        XCTAssertTrue(target.waitForExistence(timeout: 15) && target.isHittable,
                      "No hittable button found in the walk flow to test press feedback")

        // 1) Idle screenshot.
        attachScreenshot("indication-01-idle")

        // 2) Held-down screenshot. XCUIElement events must run on the main thread, so we block the
        //    main thread with press(forDuration:) (which holds the touch down for its duration) and
        //    capture the screen from a background queue while the touch is still held.
        let captured = expectation(description: "pressed-capture")
        DispatchQueue.global(qos: .userInitiated).asyncAfter(deadline: .now() + 0.5) {
            let shot = XCUIScreen.main.screenshot()
            let a = XCTAttachment(screenshot: shot)
            a.name = "indication-02-pressed"
            a.lifetime = .keepAlways
            self.add(a)
            captured.fulfill()
        }
        target.press(forDuration: 1.4)
        wait(for: [captured], timeout: 6)

        // 3) Released screenshot (dim should be gone).
        Thread.sleep(forTimeInterval: 0.4)
        attachScreenshot("indication-03-released")

        attachText("indication-hierarchy", app.debugDescription)
    }

    /// Resolve the button to press: prefer the tagged walk-flow main CTA, else the first hittable
    /// button surfaced by the Compose flow.
    private func resolveTarget(_ app: XCUIApplication) -> XCUIElement {
        // Prefer a large tappable CARD/BUTTON (the analog of the design-system buttons that were
        // rippling), so the dim is obvious. The walk flow opens on a duration-selection screen.
        for label in ["minute", "Start", "I'm ready", "Continue", "Got it", "Next", "Begin"] {
            let pred = NSPredicate(format: "label CONTAINS[c] %@", label)
            for q in [app.buttons, app.otherElements, app.staticTexts] {
                let e = q.matching(pred).firstMatch
                if e.waitForExistence(timeout: 3) && e.isHittable { return e }
            }
        }
        let tag = "Walk flow screen main button"
        let byId = app.descendants(matching: .any).matching(identifier: tag).firstMatch
        if byId.waitForExistence(timeout: 5) { return byId }
        return app.buttons.firstMatch
    }
}
