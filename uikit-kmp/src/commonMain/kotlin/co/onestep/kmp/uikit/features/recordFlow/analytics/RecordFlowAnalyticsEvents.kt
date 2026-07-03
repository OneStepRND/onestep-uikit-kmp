package co.onestep.kmp.uikit.features.recordFlow.analytics

import co.onestep.kmp.uikit.models.OSTActivityType

/**
 * Canonical analytics activity name, matching uikit's `OSTActivityType.analyticsName`
 * exactly. All types report their [OSTActivityType.serializedName] EXCEPT
 * [OSTActivityType.STATIC_BALANCE], which reports `"static_balance"` (the PRD Analytics
 * contract: the server type / serializedName stays `"static_balance_test"`, but analytics
 * fire with `activity_name = "static_balance"`).
 */
internal val OSTActivityType.analyticsName: String
    get() = when (this) {
        OSTActivityType.STATIC_BALANCE -> "static_balance"
        else -> serializedName
    }

/**
 * Record-flow analytics event names and property keys, ported verbatim from the Android
 * `uikit` module (`RecordFlowAnalyticsEvents` + the shared `AnalyticsProps`). Event-name
 * strings and property keys MUST stay byte-identical to uikit so both SDKs emit the same
 * analytics contract.
 */
internal object RecordFlowAnalyticsEvents {
    // Pre-existing events (unchanged event-name strings)
    const val HALLWAY_LENGTH_SUBMITTED = "hallway_length_submitted"
    const val SCREEN_SHORT_HALLWAY_POPUP = "screen_short_hallway_popup"
    const val CLICKED_SHORT_HALLWAY_EDIT = "clicked_short_hallway_edit"
    const val CLICKED_SHORT_HALLWAY_START_TEST = "clicked_short_hallway_start_test"
    const val CLICKED_START_MEASUREMENT = "clicked: start_measurement"

    // "Start now" on the Get Ready countdown screen (skips the remaining countdown).
    // Distinct from CLICKED_START_MEASUREMENT, which fires on the StartRecord screen
    // when the countdown begins.
    const val CLICKED_START_MEASUREMENT_NOW = "Clicked: start_measurement_now"

    // OS-15833 measurement-flow events
    const val CLICKED_BACK_BUTTON = "Clicked: back_button"
    const val CLICKED_EXIT_BUTTON = "Clicked: exit_button"
    const val SCREEN_WALK_SELECT_DURATION = "screen: walk_select_duration"
    const val CLICKED_WALK_DURATION_SELECTED = "Clicked: walk_duration_selected"
    const val SCREEN_PRE_TAG = "screen: pre_tag"
    const val SCREEN_MEASUREMENT_INCREASE_VOLUME = "screen: measurement_increase_volume"
    const val SCREEN_MEASUREMENT_START = "screen: measurement_start"
    const val SCREEN_MEASUREMENT_INSTRUCTIONS = "screen: measurement_instructions"
    const val SCREEN_MEASUREMENT_COUNTDOWN = "screen: measurement_countdown"
    const val CLICKED_MEASUREMENT_STOP = "Clicked: measurement_stop"
    const val SCREEN_MEASUREMENT_ANALYZING = "screen: measurement_analyzing"
    const val SCREEN_MEASUREMENT_STILL_ANALYZING = "screen: measurement_still_analyzing"
    const val SCREEN_MEASUREMENT_ERROR = "screen: measurement_error"
    const val CLICKED_MEASUREMENT_TRY_AGAIN = "Clicked: measurement_try_again"
    const val CLICKED_ENTER_RESULTS_MANUALLY = "Clicked: enter_results_manually"
    const val SCREEN_MEASUREMENT_ENTER_RESULTS_MANUALLY = "screen: measurement_enter_results_manually"
    const val CLICKED_ENTER_RESULTS_MANUALLY_SAVE = "Clicked: enter_results_manually_save"
    const val CLICKED_MEASUREMENT_RELOAD = "Clicked: measurement_reload"
    const val CLICKED_MEASUREMENT_ERROR_CONTINUE = "Clicked: measurement_error_continue"
    const val SCREEN_MEASUREMENT_ADD_TAGS = "screen: measurement_add_tags"
    const val CLICKED_MEASUREMENT_SUBMIT_TAGS = "Clicked: measurement_submit_tags"
    const val SCREEN_PRE_RECORDING_ASSISTIVE_DEVICE = "screen: pre_recording_assistive_device"
    const val CLICKED_PRE_RECORDING_ASSISTIVE_DEVICE_SELECTED = "Clicked: pre_recording_assistive_device_selected"
    const val SCREEN_PRE_RECORDING_FOOTWEAR = "screen: pre_recording_footwear"
    const val CLICKED_PRE_RECORDING_FOOTWEAR_SELECTED = "Clicked: pre_recording_footwear_selected"

    // OS-15962 Static Balance Test events (PRD Analytics section).
    // static_balance_card_expanded fires web-side (summary mini-app). Not fired here:
    // static_balance_event_tagged (no event-tags UI), static_balance_lob_* / sway_category
    // (Phase 2 detection).
    const val STATIC_BALANCE_CONDITION_SETUP = "screen: static_balance_condition_setup"
    const val STATIC_BALANCE_CONDITION_CONFIRMED = "static_balance_condition_confirmed"
    const val STATIC_BALANCE_NOTE_ADDED = "static_balance_note_added"
    const val STATIC_BALANCE_ANOTHER_TEST = "static_balance_another_test"
    const val STATIC_BALANCE_GO_TO_SUMMARY = "static_balance_go_to_summary"

    object ScreenNames {
        const val MEASUREMENT = "measurement"
    }

    object TagSource {
        const val PRE_TAG = "pre_tag"
        const val POST_MEASUREMENT = "post_measurement"
    }

    object Units {
        const val METERS = "m"
        const val FEET = "ft"
    }
}

/**
 * Centralized analytics **property keys** (snake_case), ported verbatim from uikit's
 * `AnalyticsProps`. Shared by the record-flow and summary trackers so no key string is
 * duplicated at a call site.
 */
internal object AnalyticsProps {
    // --- Identity / context ---
    const val SCREEN_NAME = "screen_name"
    const val ACTIVITY_NAME = "activity_name"
    const val ACTIVITY_DATE = "activity_date"
    const val PERCEPTION_UUID = "perception_uuid"
    const val APP_SECTION = "app_section"
    const val SOURCE = "source"
    const val SESSION_UUID = "session_uuid"

    // --- Durations / counts ---
    const val SCORE = "score"
    const val STEPS = "steps"
    const val PEDOMETER = "pedometer"
    const val REPS = "reps"
    const val SECONDS = "seconds"
    const val MEASUREMENT_SECONDS = "measurement_seconds"
    const val ELAPSED_SECONDS = "elapsed_seconds"
    const val TIME_REMAINING = "time_remaining"
    const val DISPLAYED_AFTER_SECONDS = "displayed_after_seconds"
    const val DURATION = "duration"

    // --- Gait params ---
    const val CADENCE = "cadence"
    const val CADENCE_VARIABILITY = "cadence_variability"
    const val VELOCITY = "velocity"
    const val VELOCITY_VARIABILITY = "velocity_variability"
    const val CONSISTENCY = "consistency"
    const val STRIDE_LENGTH = "stride_length"
    const val STEP_LENGTH_RIGHT = "step_length_right"
    const val STEP_LENGTH_LEFT = "step_length_left"
    const val STEP_LENGTH_ASYMMETRY = "step_length_asymmetry"
    const val BASE_WIDTH = "base_width"
    const val DOUBLE_SUPPORT = "double_support"
    const val DOUBLE_SUPPORT_ASYMMETRY = "double_support_asymmetry"
    const val SINGLE_SUPPORT_LEFT = "single_support_left"
    const val SINGLE_SUPPORT_RIGHT = "single_support_right"
    const val STANCE_LEFT = "stance_left"
    const val STANCE_RIGHT = "stance_right"
    const val STANCE_ASYMMETRY = "stance_asymmetry"
    const val HIP_RANGE = "hip_range"
    const val TREND = "trend"
    const val FALL_RISK_INDICATORS = "fall_risk_indicators"
    const val ASYMMETRY_SCORE = "asymmetry_score"

    // --- Summary screen ---
    const val TAB_NAME = "tab_name"
    const val ORIGINAL_HALLWAY_VALUE = "original_hallway_value"
    const val UPDATED_HALLWAY_VALUE = "updated_hallway_value"

    // --- Error cluster ---
    const val ERROR_TYPE = "error_type"
    const val ERROR_CODE = "error_code"
    const val PRIOR_ERROR_CODE = "prior_error_code"
    const val ERROR_TITLE_STRING = "error_title_string"
    const val ERROR_SUBTITLE_STRING = "error_subtitle_string"

    // --- Navigation context ---
    const val PRIOR_SCREEN = "prior_screen"
    const val SCREEN_ORIGIN = "screen_origin"
    const val RESULT_VALUE = "result_value"

    // --- Tagging ---
    const val PRE_TAG_SCREEN_NAME = "pre_tag_screen_name"
    const val PRE_TAG_VALUES = "pre_tag_values"
    const val TAGS = "tags"
    const val TAGS_ASSISTIVE_DEVICE = "tags_assistive_device"
    const val TAGS_FOOTWEAR = "tags_footwear"
    const val TAGS_HANDS_USED_FOR_SUPPORT = "tags_hands_used_for_support"
    const val ASSISTIVE_DEVICE = "assistive_device"
    const val FOOTWEAR = "footwear"

    // --- Hallway / timed walk ---
    const val HALLWAY_LENGTH = "hallway_length"
    const val HALLWAY_LENGTH_VALUE = "hallway_length_value"
    const val HALLWAY_LENGTH_UNIT = "hallway_length_unit"
    const val WALK_DURATION = "walk_duration"

    // --- Static Balance condition categories ---
    const val STANCE = "stance"
    const val VISION = "vision"
    const val SURFACE = "surface"
    const val CONDITION_NUMBER = "condition_number"
    const val CONDITION_COUNT = "condition_count"

    // --- Permissions ---
    const val PERMISSION = "permission"
    const val VARIANT = "variant"
    const val FLOW_NAME = "flow_name"
    const val ORGANIZATION_NAME = "organization_name"
    const val PATIENT_UUID = "patient_uuid"
}
