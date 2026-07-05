import XCTest

/// End-to-end coverage for the post-recording **tagging** screen ("Review the following tags"),
/// reached by driving a mock WALK to its summary and tapping the summary's **Continue** action.
///
/// Regression under test (iOS): the shared `Toolbar` is an *overlay* that reserves
/// `statusBars inset + ToolBarHeight`, but the tagging screen used to reserve only `ToolBarHeight`.
/// On iOS the top safe-area inset is large, so the title "Review the following tags" rendered
/// *behind* the toolbar. The fix reserves the status-bar inset too (matching `RecordFlowNavGraph`),
/// so the title clears the toolbar.
///
/// Assertion channel: unlike most of the Compose flow, the tagging screen surfaces queryable
/// accessibility — the title is a Compose text (a `staticText`) and the shared toolbar carries
/// `contentDescription`s ("toolbar", "Toolbar start icon"). So we can assert the title's frame sits
/// fully **below** the toolbar's frame (no overlap) — a check that FAILS before the fix and PASSES
/// after — in addition to attaching screenshots for visual inspection.
///
/// Like `MockRecordingUITests`, this performs a real recording + upload + analysis on device, so it
/// is slow and connectivity-dependent.
final class TaggingScreenUITests: XCTestCase {

    override func setUp() {
        super.setUp()
        continueAfterFailure = true
    }

    func testTaggingScreenTitleClearsToolbar() {
        let app = launchIdentifiedApp()
        startWalkFlow(app, mock: "successWalk")

        // 1) Advance the pre-recording screens until recording begins (the flow HIDES its toolbar).
        XCTAssertTrue(advanceWalkPrepUntilRecording(app), "Walk flow never began recording")

        // 2) The 1-minute walk auto-stops; wait for the summary and tap its Continue action, which
        //    (for a WALK, whose config carries OSTPostTaggingScreen) opens the tagging screen.
        XCTAssertTrue(tapSummaryContinue(app), "Summary 'Continue' action never appeared")

        // 3) The tagging screen. Its title surfaces as a Compose staticText.
        let title = app.staticTexts["Review the following tags"]
        XCTAssertTrue(title.waitForExistence(timeout: 20),
                      "Tagging screen title 'Review the following tags' never appeared")
        attachScreenshot("tagging-screen")
        attachText("tagging-hierarchy", app.debugDescription)

        // 4) Regression assertion: the title must not be covered by the toolbar overlay. Prefer the
        //    whole toolbar container's frame; fall back to the back chevron if the container isn't a
        //    distinct accessibility element. The title's top must sit at or below the toolbar bottom.
        let toolbar = app.otherElements["toolbar"]
        let back = app.buttons["Toolbar start icon"]
        let toolbarBottom: CGFloat
        if toolbar.exists, toolbar.frame.height > 0 {
            toolbarBottom = toolbar.frame.maxY
        } else {
            XCTAssertTrue(back.waitForExistence(timeout: 5),
                          "Neither the toolbar container nor its back chevron were queryable")
            toolbarBottom = back.frame.maxY
        }

        XCTAssertGreaterThan(toolbarBottom, 0, "Could not resolve the toolbar's frame")
        XCTAssertGreaterThanOrEqual(
            title.frame.minY, toolbarBottom,
            "Tagging title is covered by the toolbar: title.minY=\(title.frame.minY) " +
            "< toolbar.maxY=\(toolbarBottom). The toolbar overlay reserves (statusBars + ToolBarHeight); " +
            "the tagging screen must reserve the same, else the title renders behind it on iOS."
        )

        // The row labels and note section should also be present below the title.
        XCTAssertTrue(app.staticTexts["Add a note about the measurement"].waitForExistence(timeout: 5),
                      "Tagging note section missing — screen did not render fully")
    }

    /// Verifies the **assistive-device selection** screen ("What assistive device did you use?"),
    /// reached from the tagging screen's "Assistive device → Edit". Matches the Figma "Card selector"
    /// design: a left-aligned title clearing the toolbar, over a list of icon+label option cards
    /// (Walker, Rollator …, Cane, 2 crutches, 1 crutch, None).
    func testAssistiveDeviceSelectionMatchesDesign() {
        let app = launchIdentifiedApp()
        startWalkFlow(app, mock: "successWalk")
        XCTAssertTrue(advanceWalkPrepUntilRecording(app), "Walk flow never began recording")
        XCTAssertTrue(tapSummaryContinue(app), "Summary 'Continue' action never appeared")

        // On the tagging screen, open the assistive-device selection via its Edit button.
        XCTAssertTrue(app.staticTexts["Review the following tags"].waitForExistence(timeout: 20),
                      "Tagging screen never appeared")
        let editAssistive = app.buttons["tag_screen_assistive_device_edit_button"]
        XCTAssertTrue(editAssistive.waitForExistence(timeout: 10), "Assistive-device Edit button missing")
        editAssistive.tap()

        // The selection screen title surfaces as a Compose staticText.
        let title = app.staticTexts["What assistive device did you use?"]
        XCTAssertTrue(title.waitForExistence(timeout: 15),
                      "Assistive-device selection title never appeared")
        attachScreenshot("assistive-device-selection")
        attachText("assistive-device-hierarchy", app.debugDescription)

        // Regression: the title must not be covered by the toolbar overlay (same class of bug the
        // tagging screen had — the destination used a flat top padding instead of the safe-area inset).
        let toolbar = app.otherElements["toolbar"]
        let back = app.buttons["Toolbar start icon"]
        let toolbarBottom: CGFloat
        if toolbar.exists, toolbar.frame.height > 0 {
            toolbarBottom = toolbar.frame.maxY
        } else {
            XCTAssertTrue(back.waitForExistence(timeout: 5),
                          "Neither the toolbar container nor its back chevron were queryable")
            toolbarBottom = back.frame.maxY
        }
        XCTAssertGreaterThan(toolbarBottom, 0, "Could not resolve the toolbar's frame")
        XCTAssertGreaterThanOrEqual(
            title.frame.minY, toolbarBottom,
            "Assistive-device title is covered by the toolbar: title.minY=\(title.frame.minY) " +
            "< toolbar.maxY=\(toolbarBottom)."
        )

        // The Figma option cards render, each below the title (icon + label list).
        for option in ["Walker", "Cane", "None"] {
            let card = app.staticTexts[option]
            XCTAssertTrue(card.waitForExistence(timeout: 5),
                          "Assistive-device option '\(option)' missing")
            XCTAssertGreaterThanOrEqual(card.frame.minY, title.frame.minY,
                                        "Option '\(option)' should render below the title")
        }
    }
}

// MARK: - Flow driving (file-private; mirrors MockRecordingUITests without disturbing it)

private extension TaggingScreenUITests {

    /// Opens the Configure Flow, selects WALK + the given mock, minimizes optional prep screens for
    /// determinism, and starts the flow (accepting any first-run permission dialog).
    func startWalkFlow(_ app: XCUIApplication, mock: String) {
        app.buttons["home.configureAndRecord"].tap()
        XCTAssertTrue(app.buttons["configure.start"].waitForExistence(timeout: 10),
                      "Configure Flow did not open")

        let activity = app.buttons["activity.WALK"]
        XCTAssertTrue(ensureVisible(activity, in: app), "WALK activity missing")
        activity.tap()

        setToggle("toggle.voiceOver", on: false, in: app)
        selectMock(mock, in: app)

        let start = app.buttons["configure.start"]
        XCTAssertTrue(ensureVisible(start, in: app), "Start button missing")
        start.tap()
        dismissSystemAlertsIfPresent(timeout: 3)
    }

    /// Advances the pre-recording screens by their real Compose labels until recording begins, which
    /// the flow signals by HIDING its toolbar (RecordFlowNavGraph hides it once the flow leaves
    /// GET_READY). Returns true once the toolbar is gone. Mirrors MockRecordingUITests' prep loop.
    func advanceWalkPrepUntilRecording(_ app: XCUIApplication) -> Bool {
        let toolbarBack = app.buttons["Toolbar start icon"]

        guard app.buttons["1 minute"].waitForExistence(timeout: 20)
            || app.staticTexts["1 minute"].waitForExistence(timeout: 2) else {
            attachScreenshot("tagging-noDuration")
            return false
        }

        let advanceLabels = [
            "1 minute",
            "I'm ready", "Continue", "Got it", "Start now", "Start", "Begin", "Next", "Skip", "Allow",
        ]
        let deadline = Date().addingTimeInterval(45)
        while Date() < deadline {
            if !toolbarBack.isHittable { return true }   // toolbar gone → recording/analysis began
            var advanced = false
            for label in advanceLabels where tapIfHittable(label, in: app) {
                advanced = true
                break
            }
            if !advanced { tapFirstFlowCTA(app) }
            usleep(1_500_000)
        }
        attachScreenshot("tagging-prepTimeout")
        return !toolbarBack.isHittable
    }

    /// Waits for the analyzed summary and taps its bottom **Continue** action, which navigates to the
    /// tagging screen for a WALK. The 1-minute walk auto-stops, so no slide-to-stop is needed; we just
    /// poll (upload + analysis can take up to ~150s) for the Continue control to appear and tap it.
    func tapSummaryContinue(_ app: XCUIApplication) -> Bool {
        let deadline = Date().addingTimeInterval(180)
        var shot = false
        while Date() < deadline {
            // The tagging screen may already be up if Continue was tapped; short-circuit.
            if app.staticTexts["Review the following tags"].exists { return true }
            if !shot, app.buttons["Toolbar start icon"].isHittable {
                attachScreenshot("tagging-summary")   // one screenshot once the summary chrome is back
                shot = true
            }
            if tapIfHittable("Continue", in: app) { return true }
            usleep(2_000_000)
        }
        attachScreenshot("tagging-summaryTimeout")
        attachText("tagging-summaryTimeout-hierarchy", app.debugDescription)
        return false
    }
}
