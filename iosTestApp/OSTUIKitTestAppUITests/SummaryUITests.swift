import XCTest

/// Drives the app on-device to verify the Measurement Summary renders real data through the iOS
/// motion-data/insights bridges. The summary itself is a Compose/Skia view with a thin accessibility
/// tree, so verification is visual: we navigate the native SwiftUI picker, then capture full-screen
/// screenshots (kept as attachments) of the loaded summary and each tab.
final class SummaryUITests: XCTestCase {

    override func setUp() {
        super.setUp()
        continueAfterFailure = true
    }

    func testMeasurementSummaryRenders() throws {
        let app = launchIdentifiedApp()

        // Open the Measurement Summary picker from Home.
        let summaryButton = app.buttons["home.measurementSummary"]
        XCTAssertTrue(summaryButton.waitForExistence(timeout: 15),
                      "Home 'Measurement Summary' button never appeared")
        attachScreenshot("00-home")
        summaryButton.tap()

        // Picker sheet: fetchRecentKmpMeasurements loads recent measurements (needs identified SDK).
        let picker = app.navigationBars["Select Measurement"]
        XCTAssertTrue(picker.waitForExistence(timeout: 30), "Measurement picker never appeared")

        // Wait for the fetch to populate rows (or the empty state).
        let firstRow = app.buttons["summary.row"].firstMatch
        let hasRows = firstRow.waitForExistence(timeout: 20)
        attachScreenshot("01-picker")

        guard hasRows else {
            // No history for this patient — the empty state is a valid outcome; record it and stop.
            XCTAssertTrue(app.otherElements["summary.empty"].exists
                          || app.staticTexts["No Measurements"].exists,
                          "Picker showed neither rows nor the empty state")
            attachScreenshot("01b-empty")
            return
        }

        firstRow.tap()

        // Summary is a Compose (Skia) fullScreenCover — give the bridge time to resolve the
        // OSTMotionDataService and fetch norms (local) + insights (network), then screenshot.
        sleep(14)
        attachScreenshot("02-summary-highlights")
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
        attachScreenshot("03-summary-gaitlab")
    }
}
