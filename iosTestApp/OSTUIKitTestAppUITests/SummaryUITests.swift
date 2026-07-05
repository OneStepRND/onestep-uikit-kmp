import XCTest

/// Drives the test app on-device to verify the Measurement Summary renders real data through the
/// (now non-stubbed) iOS motion-data/insights bridges. The summary itself is a Compose/Skia view
/// with a thin accessibility tree, so verification is visual: we navigate the native SwiftUI
/// picker, then capture full-screen screenshots (kept as attachments) of the loaded summary and
/// each tab. Screenshots are pulled out of the .xcresult afterwards for inspection.
final class SummaryUITests: XCTestCase {

    override func setUp() {
        super.setUp()
        continueAfterFailure = true
    }

    func testMeasurementSummaryRenders() throws {
        let app = XCUIApplication()
        app.launch()

        // Home is native SwiftUI. Open the Measurement Summary picker.
        let summaryButton = app.buttons["Measurement Summary"]
        XCTAssertTrue(summaryButton.waitForExistence(timeout: 30),
                      "Home 'Measurement Summary' button never appeared")
        attach("00-home")
        summaryButton.tap()

        // Picker sheet: fetchRecentKmpMeasurements loads recent measurements (needs identified SDK).
        let picker = app.navigationBars["Select Measurement"]
        XCTAssertTrue(picker.waitForExistence(timeout: 30), "Measurement picker never appeared")
        sleep(4) // let the fetch populate the list
        attach("01-picker")

        // Pick the first measurement row. Picker rows are buttons labeled "WALK, ID: <uuid>";
        // scope by "ID:" so we never match the home-screen "Walk Recording" button (which sits in
        // the tree behind the sheet and is not hittable).
        let row = app.buttons.matching(
            NSPredicate(format: "label CONTAINS %@", "ID: ")
        ).firstMatch
        XCTAssertTrue(row.waitForExistence(timeout: 15), "No measurement rows found in picker")
        row.tap()

        // Summary is a Compose (Skia) fullScreenCover — cannot reliably query elements. Give the
        // bridge time to resolve the OSTMotionDataService and fetch norms (local) + insights
        // (network), then screenshot the default (Highlights) tab.
        sleep(14)
        attach("02-summary-highlights")
        attachText("02-hierarchy", app.debugDescription)

        // Switch to the Gait-Lab tab: prefer a queryable element if Compose exposes it, else tap by
        // normalized coordinate (right tab, just under the main-param card).
        let gaitText = app.staticTexts.matching(
            NSPredicate(format: "label CONTAINS[c] %@", "gait")
        ).firstMatch
        let gaitButton = app.buttons.matching(
            NSPredicate(format: "label CONTAINS[c] %@", "gait")
        ).firstMatch
        if gaitButton.waitForExistence(timeout: 2) {
            gaitButton.tap()
        } else if gaitText.waitForExistence(timeout: 2) {
            gaitText.tap()
        } else {
            app.coordinate(withNormalizedOffset: CGVector(dx: 0.72, dy: 0.32)).tap()
        }
        sleep(4)
        attach("03-summary-gaitlab")
    }

    private func attachText(_ name: String, _ text: String) {
        let attachment = XCTAttachment(string: text)
        attachment.name = name
        attachment.lifetime = .keepAlways
        add(attachment)
    }

    private func attach(_ name: String) {
        let screenshot = XCUIScreen.main.screenshot()
        let attachment = XCTAttachment(screenshot: screenshot)
        attachment.name = name
        attachment.lifetime = .keepAlways
        add(attachment)
    }
}
