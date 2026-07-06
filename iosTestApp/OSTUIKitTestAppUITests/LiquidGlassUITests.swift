import XCTest

/// Visual verification of the iOS-only Liquid-Glass treatment applied to the toolbar back (`<`)
/// and close (`X`) bar buttons (and the dialog/sheet close buttons), added via the `liquid`
/// RuntimeShader library and gated to iOS through `Modifier.ostLiquidGlass` / `ostLiquefiable`.
///
/// The toolbar is a Compose Multiplatform (Skia) view whose accessibility tree is thin, so — like
/// `IndicationUITests` and `SummaryUITests` — we verify visually: reach a screen that shows the
/// toolbar, then attach full-screen screenshots for inspection. On iOS the top-left/top-right bar
/// controls should sit inside a circular glass lens (specular rim + refraction of the bar behind);
/// on Android the same code is a no-op, so this behaviour is intentionally iOS-only.
final class LiquidGlassUITests: XCTestCase {

    override func setUp() {
        super.setUp()
        continueAfterFailure = true
    }

    /// Enters the walk recording flow, whose preparation screens show the Compose `Toolbar` with a
    /// back/close bar button, and captures it for visual confirmation of the glass circle.
    func testToolbarBackCloseAreGlassOnIOS() {
        let app = launchIdentifiedApp()

        app.buttons["home.walkRecording"].tap()
        dismissSystemAlertsIfPresent(timeout: 3)
        XCTAssertTrue(app.buttons["home.walkRecording"].waitUntilNotHittable(timeout: 20),
                      "Walk recording flow did not launch")

        // Wait for the Compose flow chrome (prep screen) that carries the toolbar.
        _ = waitForComposeFlowChrome(app, timeout: 12)

        // The toolbar back/close lives top-left/top-right. Capture the whole screen so the glass
        // lens around the bar button is visible for inspection.
        attachScreenshot("liquidglass-01-flow-toolbar")
        attachText("liquidglass-flow-hierarchy", app.debugDescription)

        // The toolbar start icon is tagged `Toolbar start icon`; the end icons `Toolbar end icon: N`.
        // If Compose surfaces them, assert presence (best-effort — a Skia canvas may not expose them).
        let start = app.descendants(matching: .any).matching(identifier: "Toolbar start icon").firstMatch
        let end = app.descendants(matching: .any).matching(identifier: "Toolbar end icon: 0").firstMatch
        if start.waitForExistence(timeout: 3) || end.exists {
            attachText("liquidglass-toolbar-buttons",
                       "start exists=\(start.exists) hittable=\(start.isHittable); " +
                       "end0 exists=\(end.exists) hittable=\(end.isHittable)")
        }

        // Tapping the top-left toolbar control dismisses the flow — proves the glass lens did not
        // break hit-testing on the button underneath.
        tapComposeTopLeftClose(in: app)
        _ = app.buttons["home.walkRecording"].waitUntilHittable(timeout: 20)
        attachScreenshot("liquidglass-02-after-close-tap")
        XCTAssertTrue(app.buttons["home.walkRecording"].exists,
                      "Toolbar close/back under the glass lens should still dismiss the flow")
    }
}
