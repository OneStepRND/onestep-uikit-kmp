package co.onestep.kmp.uikit.testing

/**
 * The UI kit's stable test identifiers — the contract between this library and the end-to-end
 * suites of the apps that embed it (Maestro flows in the clinician and patient apps, Compose UI
 * tests here).
 *
 * ### How a tag reaches a test
 *
 * Every tag is applied with `Modifier.test(...)`, which sets the Compose `testTag` semantics
 * property and, on Android, `testTagsAsResourceId` on the same node. That makes one string work
 * as a selector on both platforms with no host-app setup:
 *
 * - **Android** — the tag surfaces as the view's `resource-id`, so Maestro matches it with
 *   `- tapOn: { id: "ost_record_start_screen" }`.
 * - **iOS** — Compose Multiplatform surfaces `testTag` as the node's `accessibilityIdentifier`,
 *   which Maestro matches with the same `id:` selector.
 *
 * ### Rules for changing this file
 *
 * - **A tag's value is a published contract.** Renaming a value breaks every flow that selects on
 *   it, in repositories that do not build together with this one. Add a new constant instead, and
 *   retire the old one over a release.
 * - **Never interpolate patient data into a tag** (HIPAA). Tags are readable by test tooling and,
 *   on iOS, by accessibility clients. Index- or structure-derived suffixes only — never a name,
 *   MRN, measurement id, or free-text note. The server-supplied keys used by
 *   [StaticBalance.conditionSection] are configuration identifiers ("stance", "vision"), not
 *   patient data.
 * - **A tag is not a label.** `Modifier.test` deliberately does not set `contentDescription`:
 *   screen readers must announce the control's own text, not its test id.
 *
 * Values that predate this catalog keep their original strings — including the ones that read as
 * prose ("Walk flow screen main button") or carry a typo ("Summery continue button") — because
 * flows already select on them.
 */
object OSTTestTags {

    /** Pre-recording, recording and post-recording screens of [co.onestep.kmp.uikit.features.recordFlow.OSTRecordingFlow]. */
    object RecordFlow {
        // ── Chrome shared by every flow screen ────────────────────────────────────────────
        const val TOOLBAR = "toolbar"
        const val TOOLBAR_START_ICON = "Toolbar start icon"

        /** Prefix of the toolbar's trailing icons; see [toolbarEndIcon]. */
        const val TOOLBAR_END_ICON = "Toolbar end icon"

        /** Trailing toolbar icon at [index], left to right. */
        fun toolbarEndIcon(index: Int): String = "$TOOLBAR_END_ICON: $index"

        // ── The generic flow screen (UiKitScreen) ─────────────────────────────────────────
        /** The big round CTA — "Start", "I'm ready", the record button. */
        const val MAIN_BUTTON = "Walk flow screen main button"

        /** The filled bottom CTA. */
        const val PRIMARY_BUTTON = "walk_flow_screen_brand_button"

        /** The outlined bottom CTA, e.g. "View instructions". */
        const val SECONDARY_BUTTON = "walk_flow_screen_border_brand_button"

        const val TITLE = "ost_record_screen_title"
        const val SUBTITLE = "ost_record_screen_subtitle"
        const val NOTE_BANNER = "ost_record_screen_note_banner"

        /** Row [index] of a selection list (durations, footwear, assistive devices, tag answers). */
        fun selectionItem(index: Int): String = "selection_item_$index"

        // ── Screen roots, one per destination ─────────────────────────────────────────────
        const val SELECT_DURATION_SCREEN = "ost_select_duration_screen"
        const val PRE_ASSISTIVE_DEVICE_SCREEN = "ost_pre_assistive_device_screen"
        const val PRE_FOOTWEAR_SCREEN = "ost_pre_footwear_screen"
        const val CUSTOM_QUESTION_SCREEN = "ost_custom_question_screen"
        const val SOUND_PERMISSION_SCREEN = "ost_sound_permission_screen"
        const val SOUND_INSTRUCTIONS_SCREEN = "ost_sound_instructions_screen"
        const val START_RECORD_SCREEN = "ost_start_record_screen"
        const val NO_SUMMARY_NOTICE_SCREEN = "ost_no_summary_notice_screen"
        const val EMPTY_ANALYSIS_SCREEN = "ost_empty_analysis_screen"
        const val ERROR_SCREEN = "ost_record_error_screen"

        /** The instructions bottom sheet opened from the "View instructions" CTA. */
        const val INSTRUCTIONS_SHEET = "ost_instructions_sheet"

        // ── Hallway length ────────────────────────────────────────────────────────────────
        const val HALLWAY_SCREEN = "ost_hallway_distance_screen"
        const val HALLWAY_INPUT = "ost_hallway_distance_input"
        const val HALLWAY_ERROR = "ost_hallway_distance_error"
        const val HALLWAY_CONTINUE_BUTTON = "ost_hallway_distance_continue_button"
        const val HALLWAY_SKIP_BUTTON = "ost_hallway_distance_skip_button"
        const val HALLWAY_WARNING_DIALOG = "ost_hallway_warning_dialog"

        // ── Recording ─────────────────────────────────────────────────────────────────────
        const val RECORDING_SCREEN = "ost_recording_screen"
        const val RECORDING_TITLE = "ost_recording_title"
        const val RECORDING_INSTRUCTIONS = "ost_recording_instructions"
        const val RECORDING_TIMER = "ost_recording_timer"
        const val RECORDING_VALUE = "ost_recording_value"
        const val RECORDING_ANALYZING_LOADER = "ost_recording_analyzing_loader"

        /** The slide-to-stop control that ends a recording. */
        const val RECORDING_STOP_SLIDER = "ost_recording_stop_slider"

        /** The outlined in-recording CTA, e.g. "Start now" during the countdown. */
        const val RECORDING_BOTTOM_BUTTON = "ost_recording_bottom_button"

        // ── Exit confirmation ─────────────────────────────────────────────────────────────
        const val EXIT_DIALOG = "ost_exit_confirmation_dialog"
    }

    /** Static Balance condition setup and its per-condition "Recording saved" screen. */
    object StaticBalance {
        const val CONDITION_SETUP_SCREEN = "ost_condition_setup_screen"
        const val CONDITION_SETUP_CONTINUE_BUTTON = "condition_setup_continue_button"
        const val CONDITION_SETUP_CLEAR_ALL_BUTTON = "condition_setup_clear_all_button"

        /**
         * The collapsible header of the condition section keyed [sectionId] (a workspace
         * configuration key such as "stance" — never patient data).
         */
        fun conditionSection(sectionId: String): String = "ost_condition_section_$sectionId"

        /** Option [index] inside the condition section keyed [sectionId]. */
        fun conditionOption(sectionId: String, index: Int): String =
            "ost_condition_option_${sectionId}_$index"

        const val RECORDING_SAVED_SCREEN = "ost_recording_saved_screen"
        const val RECORDING_SAVED_NOTE_FIELD = "ost_recording_saved_note_field"
        const val RECORDING_SAVED_GO_TO_SUMMARY_BUTTON = "recording_saved_go_to_summary_button"
        const val RECORDING_SAVED_RECORD_ANOTHER_BUTTON = "recording_saved_record_another_button"
    }

    /** The Generic Recording post-recording notes screen. */
    object GenericRecording {
        const val NOTES_SCREEN = "ost_generic_recording_notes_screen"
        const val NOTES_FIELD = "ost_generic_recording_notes_field"
        const val NOTES_CONTINUE_BUTTON = "generic_recording_notes_continue_button"
    }

    /** The native measurement summary ([co.onestep.kmp.uikit.features.summary.OSTMeasurementSummary]). */
    object Summary {
        const val SCREEN = "ost_summary_screen"
        const val TOOLBAR = "toolbar"
        const val TABS_ROW = "ost_summary_tabs_row"

        /** Prefix the tab ids are built from; see [tab]. */
        const val TAB_TAG_PREFIX = "ost_summary_tab_"

        /** Tab [index] of the summary's tab row (0 = Highlights, 1 = Gait Lab). */
        fun tab(index: Int): String = TAB_TAG_PREFIX + index

        const val HIGHLIGHTS_LIST = "ost_summary_highlights_list"
        const val GAIT_LAB_LIST = "ost_summary_gait_lab_list"
        const val MAIN_PARAM = "ost_summary_main_param"
        const val HALLWAY_EDIT_BUTTON = "ost_summary_hallway_edit_button"
        const val STS_EDIT_BUTTON = "ost_summary_sts_edit_button"
        const val EMPTY_STATE = "ost_summary_empty_state"
        const val SHIMMER = "ost_summary_shimmer"

        /** The partial (steps + duration only) result shown when there is no full analysis. */
        const val PARTIAL_RESULT = "ost_summary_partial_result"

        // ── STS manual self-report ────────────────────────────────────────────────────
        const val STS_MANUAL_REPORT_SCREEN = "ost_sts_manual_report_screen"
        const val STS_MANUAL_REPORT_PICKER = "ost_sts_manual_report_picker"
        const val STS_MANUAL_REPORT_SAVE_BUTTON = "ost_sts_manual_report_save_button"
        const val INFO_SHEET = "ost_summary_info_sheet"

        /** The sticky bottom CTA that leaves the summary. */
        const val CONTINUE_BUTTON = "Summery continue button"

        /** The sticky bottom "Discard" CTA, shown for a not-yet-committed measurement. */
        const val DISCARD_BUTTON = "ost_summary_discard_button"

        const val DISCARD_DIALOG = "discard measurement confirmation"
        const val DISCARD_DIALOG_CONFIRM_BUTTON = "ost_discard_dialog_confirm_button"
        const val DISCARD_DIALOG_CANCEL_BUTTON = "ost_discard_dialog_cancel_button"
        const val DISCARD_DIALOG_CLOSE_BUTTON = "ost_discard_dialog_close_button"
    }

    /** The post-recording tagging screen (assistive device, level of assistance, footwear, note). */
    object Tagging {
        const val SCREEN = "ost_tag_screen"
        const val MAIN_BUTTON = "tag_screen_main_button"
        const val NOTE_TEXT_FIELD = "tag_screen_note_text_field"
        const val ASSISTIVE_DEVICE_EDIT_BUTTON = "tag_screen_assistive_device_edit_button"
        const val ASSISTIVE_DEVICE_TEXT = "tag_screen_assistive_device_text"
        const val LEVEL_OF_ASSISTANCE_EDIT_BUTTON = "tag_screen_level_of_assistance_edit_button"
        const val LEVEL_OF_ASSISTANCE_TEXT = "tag_screen_level_of_assistance_text"
        const val FOOTWEAR_EDIT_BUTTON = "tag_screen_footwear_edit_button"
        const val FOOTWEAR_TEXT = "tag_screen_footwear_text"

        /** The row for configured question [index] (e.g. the use-of-hands question). */
        fun question(index: Int): String = "ost_tag_screen_question_$index"

        /** The answer shown on the row for configured question [index]. */
        fun questionValue(index: Int): String = "ost_tag_screen_question_value_$index"
    }

    /** The care log ([co.onestep.kmp.uikit.features.carlog.OSTCareLog]). */
    object CareLog {
        const val SCREEN = "ost_care_log_screen"
        const val TABS_ROW = "ost_care_log_tabs_row"

        /** Prefix the tab ids are built from; see [tab]. */
        const val TAB_TAG_PREFIX = "ost_care_log_tab_"

        /** Tab [index] of the care log's tab row (0 = In app, 1 = Background). */
        fun tab(index: Int): String = TAB_TAG_PREFIX + index
        const val IN_APP_LIST = "ost_care_log_in_app_list"
        const val BACKGROUND_LIST = "ost_care_log_background_list"
        const val EMPTY_STATE = "ost_care_log_empty_state"
        const val INFO_SHEET = "ost_care_log_info_sheet"
        const val SHIMMER = "ost_care_log_shimmer"
        const val INFO_BUTTON = "ost_care_log_info_button"

        /** A completed measurement row. */
        const val ITEM = "Walk log item"

        /** A measurement row that is still uploading or analyzing. */
        const val PENDING_ITEM = "Pending walk log item"

        /** Notice card [index], counted over the cards currently visible. */
        fun noticeCard(index: Int): String = "ost_care_log_notice_card_$index"
    }

    /** The in-kit web container used for the web summary and other hosted pages. */
    object Web {
        const val SCREEN = "ost.webScreen"
        const val CLOSE_BUTTON = "ost.webScreen.close"
        const val VIEW = "ost.webView"
        const val LOADER = "ost.webView.loader"
        const val ERROR = "ost.webView.error"
        const val RETRY_BUTTON = "ost.webView.retry"
    }

    /** The in-app permission flow, Android and iOS. */
    object Permissions {
        const val EXPLANATION_SCREEN = "ost_permission_explanation_screen"
        const val EXPLANATION_CLOSE_BUTTON = "ost_permission_explanation_close_button"

        /**
         * The Android request screen. Its CTAs carry [RecordFlow.PRIMARY_BUTTON] and
         * [RecordFlow.SECONDARY_BUTTON], because the screen reuses the generic flow layout.
         */
        const val REQUEST_SCREEN = "ost_permission_request_screen"

        const val RATIONALIZATION_SCREEN = "ost_permission_rationalization_screen"
        const val MOTION_SCREEN = "ost_permission_motion_screen"
        const val LOCATION_SCREEN = "ost_permission_location_screen"
        const val HEALTH_KIT_SCREEN = "ost_permission_health_kit_screen"
        const val MICROPHONE_SCREEN = "ost_permission_microphone_screen"

        /** The primary CTA on an iOS permission screen ("Allow", "Open Settings"). */
        const val PRIMARY_BUTTON = "ost_permission_primary_button"

        /** The "Not now" / skip CTA on an iOS permission screen. */
        const val SECONDARY_BUTTON = "ost_permission_secondary_button"

        const val CLOSE_BUTTON = "ost_permission_close_button"
        const val DATA_USAGE_BUTTON = "ost_permission_data_usage_button"
    }
}
