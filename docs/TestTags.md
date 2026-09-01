# Test tags — selecting UI kit screens from an app's E2E suite

The UI kit renders inside other apps, so its screens are the part of a Maestro flow the host
repository cannot see: its strings live here, its layout lives here, and a flow that reaches a
recording or a summary has nothing of its own to select on. This document is the contract that
closes that gap.

**Source of truth:** `uikit-kmp/src/commonMain/kotlin/co/onestep/kmp/uikit/testing/OSTTestTags.kt`.
The tables below are a reading aid; the file is what ships.

## How a tag becomes a selector

Tags are applied with the module-internal `Modifier.test(tag)`
(`utils/TestUtil.kt`, with an `actual` per platform):

| Platform | What the modifier sets | What a flow writes |
|---|---|---|
| Android | `testTag` **and** `testTagsAsResourceId` on the same node, so the tag is published as the view's `resource-id` | `- tapOn: { id: "ost_start_record_screen" }` |
| iOS | `testTag`, which Compose Multiplatform exposes as the node's `accessibilityIdentifier` | the same `id:` selector |

Setting `testTagsAsResourceId` on the tagged node itself — rather than relying on the host app to
set it at its root — has two consequences worth knowing:

- **A host app needs no setup.** The clinician app happens to wrap itself in
  `testTagsAsResourceIdCompat()`, but the kit's tags do not depend on it.
- **Dialogs and bottom sheets work too.** They compose in their own semantics owner and never
  inherit a flag set at the app root, which is why the kit's popups and sheets carry their tags
  directly.

`Modifier.test` deliberately does **not** set `contentDescription`. A tag on a merged node would
replace the control's own label, and a screen reader would read out `walk_flow_screen_brand_button`
instead of "Continue".

## Rules

- **A value is a published contract.** Renaming one breaks flows in repositories that do not build
  with this one. Add a new constant and retire the old one over a release.
- **Never interpolate patient data into a tag** (HIPAA). Tags are readable by test tooling and, on
  iOS, by accessibility clients. Index- and structure-derived suffixes only. The section keys in
  `StaticBalance.conditionSection(...)` are workspace configuration keys ("stance", "vision").
- **One id, one node.** Two nodes sharing an id makes a selector resolve to whichever the driver
  finds first — the defect this pass fixed on the discard dialog and the tagging screen's question
  rows.
- Prefer a tag over a string. SDK-rendered copy is localized and not visible to the host repo, which
  is why so many flows in the clinician app carry a `VERIFY-ON-DEVICE` note.

## Catalog

### `OSTTestTags.RecordFlow`

| Constant | Value | Node |
|---|---|---|
| `TOOLBAR` | `toolbar` | flow toolbar |
| `TOOLBAR_START_ICON` | `Toolbar start icon` | back / close |
| `toolbarEndIcon(i)` | `Toolbar end icon: 0` | trailing icon *i* |
| `MAIN_BUTTON` | `Walk flow screen main button` | round CTA (Start / I'm ready) |
| `PRIMARY_BUTTON` | `walk_flow_screen_brand_button` | filled bottom CTA |
| `SECONDARY_BUTTON` | `walk_flow_screen_border_brand_button` | outlined bottom CTA |
| `TITLE` / `SUBTITLE` / `NOTE_BANNER` | `ost_record_screen_*` | flow-screen text |
| `selectionItem(i)` | `selection_item_0` | selection row *i* |
| `SELECT_DURATION_SCREEN` | `ost_select_duration_screen` | duration picker |
| `PRE_ASSISTIVE_DEVICE_SCREEN` | `ost_pre_assistive_device_screen` | assistive-device question |
| `PRE_FOOTWEAR_SCREEN` | `ost_pre_footwear_screen` | footwear question |
| `CUSTOM_QUESTION_SCREEN` | `ost_custom_question_screen` | configured pre-recording question |
| `SOUND_PERMISSION_SCREEN` | `ost_sound_permission_screen` | mic permission + denied-always |
| `SOUND_INSTRUCTIONS_SCREEN` | `ost_sound_instructions_screen` | raise-volume screen |
| `START_RECORD_SCREEN` | `ost_start_record_screen` | pre-recording start screen |
| `NO_SUMMARY_NOTICE_SCREEN` | `ost_no_summary_notice_screen` | "no summary" notice |
| `EMPTY_ANALYSIS_SCREEN` | `ost_empty_analysis_screen` | not-enough-data result |
| `ERROR_SCREEN` | `ost_record_error_screen` | flow error screen |
| `INSTRUCTIONS_SHEET` | `ost_instructions_sheet` | instructions bottom sheet |
| `HALLWAY_SCREEN` … `HALLWAY_WARNING_DIALOG` | `ost_hallway_*` | hallway length screen, input, error, both CTAs, short-hallway popup |
| `RECORDING_SCREEN` | `ost_recording_screen` | the recording screen |
| `RECORDING_TITLE` / `RECORDING_INSTRUCTIONS` / `RECORDING_TIMER` / `RECORDING_VALUE` | `ost_recording_*` | live recording readouts |
| `RECORDING_STOP_SLIDER` | `ost_recording_stop_slider` | slide-to-stop |
| `RECORDING_BOTTOM_BUTTON` | `ost_recording_bottom_button` | in-recording outlined CTA ("Start now") |
| `RECORDING_ANALYZING_LOADER` | `ost_recording_analyzing_loader` | analyzing spinner |
| `EXIT_DIALOG` | `ost_exit_confirmation_dialog` | stop-recording confirmation |

### `OSTTestTags.StaticBalance`

`CONDITION_SETUP_SCREEN`, `CONDITION_SETUP_CONTINUE_BUTTON`, `CONDITION_SETUP_CLEAR_ALL_BUTTON`,
`conditionSection(sectionId)`, `conditionOption(sectionId, index)`, `RECORDING_SAVED_SCREEN`,
`RECORDING_SAVED_NOTE_FIELD`, `RECORDING_SAVED_GO_TO_SUMMARY_BUTTON`,
`RECORDING_SAVED_RECORD_ANOTHER_BUTTON`.

The condition list is server-driven: its labels are workspace data, its section keys are not, which
is why the option rows are addressed as `ost_condition_option_stance_0` rather than by label.

### `OSTTestTags.GenericRecording`

`NOTES_SCREEN`, `NOTES_FIELD`, `NOTES_CONTINUE_BUTTON`.

### `OSTTestTags.Summary`

`SCREEN`, `TOOLBAR`, `TABS_ROW`, `tab(index)`, `HIGHLIGHTS_LIST`, `GAIT_LAB_LIST`, `MAIN_PARAM`,
`HALLWAY_EDIT_BUTTON`, `STS_EDIT_BUTTON`, `EMPTY_STATE`, `SHIMMER`, `PARTIAL_RESULT`, `INFO_SHEET`,
`CONTINUE_BUTTON`, `DISCARD_BUTTON`, `DISCARD_DIALOG`, `DISCARD_DIALOG_CONFIRM_BUTTON`,
`DISCARD_DIALOG_CANCEL_BUTTON`, `DISCARD_DIALOG_CLOSE_BUTTON`, and the STS manual self-report
screen: `STS_MANUAL_REPORT_SCREEN`, `STS_MANUAL_REPORT_PICKER`, `STS_MANUAL_REPORT_SAVE_BUTTON`.

`SHIMMER` and `PARTIAL_RESULT` are what a flow waits on: `SHIMMER` disappearing means the summary
loaded, and `PARTIAL_RESULT` present means the measurement came back with steps and duration but no
full analysis.

`CONTINUE_BUTTON` is `Summery continue button` — the original typo, kept because flows select on it.

### `OSTTestTags.Tagging`

`SCREEN`, `MAIN_BUTTON`, `NOTE_TEXT_FIELD`, the assistive-device / level-of-assistance / footwear
edit buttons and value texts, and `question(index)` / `questionValue(index)` for each configured
question (the use-of-hands question and friends).

### `OSTTestTags.CareLog`

`SCREEN`, `TABS_ROW`, `tab(index)`, `IN_APP_LIST`, `BACKGROUND_LIST`, `EMPTY_STATE`, `INFO_BUTTON`,
`INFO_SHEET`, `SHIMMER`, `ITEM` (`Walk log item`), `PENDING_ITEM` (`Pending walk log item`),
`noticeCard(index)`.

### `OSTTestTags.Web`

`SCREEN`, `CLOSE_BUTTON`, `VIEW`, `LOADER`, `ERROR`, `RETRY_BUTTON` — the kit's own web container
(`ost.webScreen.*`, `ost.webView.*`). A host app's own web chrome is separate; the clinician app's
`web_view_close`, for instance, is its own.

### `OSTTestTags.Permissions`

`EXPLANATION_SCREEN`, `EXPLANATION_CLOSE_BUTTON`, `REQUEST_SCREEN` (Android), and the iOS screens
`RATIONALIZATION_SCREEN`, `MOTION_SCREEN`, `LOCATION_SCREEN`, `HEALTH_KIT_SCREEN`,
`MICROPHONE_SCREEN` plus their shared `PRIMARY_BUTTON`, `CLOSE_BUTTON` and `DATA_USAGE_BUTTON`.

The Android request screen reuses the generic flow layout, so its CTAs carry
`RecordFlow.PRIMARY_BUTTON` / `RecordFlow.SECONDARY_BUTTON`.

## Known gaps

- **Popup buttons.** `OSPopup` comes from `design-system-kmp`; only its container can be tagged from
  here. The short-hallway and exit-confirmation dialogs are addressable by id, but their buttons are
  still selected by text. Tagging them means a change in the design system.
- **Summary list rows.** The lists are tagged; individual parameter rows are not, because their
  identity is server data. Scope a selector to `HIGHLIGHTS_LIST` / `GAIT_LAB_LIST` and match the
  parameter name inside it.
- **`PERMISSION_REQUEST_BUTTON`** in `androidMain/.../PermissionRequestScreen.kt` is declared but
  never applied — it predates this catalog. Do not select on it.

## Adding a tag

1. Add the constant to the right group in `OSTTestTags`, snake_case, prefixed `ost_`.
2. Apply it with `Modifier.test(...)` — never a bare `testTag`, which skips the Android resource-id
   step.
3. For a screen rendered through `UiKitScreen`, pass `screenTag = ...` instead of putting the tag in
   `modifier`: the root already applies it in the one place that yields a single node.
4. Run the compile gate:
   `./gradlew :uikit-kmp:compileAndroidMain :uikit-kmp:compileKotlinIosSimulatorArm64`.
5. Note the new id in the flow that needed it, so the app repo's suite and this catalog stay in step.
