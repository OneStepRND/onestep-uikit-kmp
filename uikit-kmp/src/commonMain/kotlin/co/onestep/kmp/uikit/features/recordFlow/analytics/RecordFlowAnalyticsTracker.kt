package co.onestep.kmp.uikit.features.recordFlow.analytics

import co.onestep.kmp.uikit.OSTUIKitAnalyticsHandler
import co.onestep.kmp.uikit.data.WalkDuration
import co.onestep.kmp.uikit.features.recordFlow.analytics.RecordFlowAnalyticsEvents.ScreenNames
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTBalanceCondition
import co.onestep.kmp.uikit.features.tagging.models.Footwear
import co.onestep.kmp.uikit.models.OSTActivityType
import co.onestep.kmp.uikit.models.OSTAssistiveDevice
import co.onestep.kmp.uikit.models.OSTEvent

/**
 * Central analytics tracker for the recording flow, ported from the Android `uikit`
 * `RecordFlowAnalyticsTracker`. Every event name, property key/value and method semantics
 * matches uikit exactly so both SDKs emit an identical analytics contract.
 *
 * HIPAA: this tracker sends NO PII/PHI and NO free-text clinician notes/observations —
 * only typed selections, canonical labels, ids and counts. See [trackSubmitTagsClicked]
 * and [trackStaticBalanceNoteAdded].
 *
 * KMP note: [OSTEvent.properties] is a `Map<String, String>`, so every value is stringified
 * (uikit's `Map<String, Any>` values were auto-stringified downstream by the host handler).
 *
 * @param analytics The analytics handler that emits events.
 */
internal class RecordFlowAnalyticsTracker(
    private val analytics: OSTUIKitAnalyticsHandler,
) {
    fun trackHallwayLengthSubmitted(
        activity: OSTActivityType,
        entered: Boolean,
        value: Int?,
        unit: String?,
    ) {
        val props = buildMap {
            put(AnalyticsProps.ACTIVITY_NAME, activity.analyticsName)
            put(AnalyticsProps.HALLWAY_LENGTH, entered.toString())
            if (value != null) put(AnalyticsProps.HALLWAY_LENGTH_VALUE, value.toString())
            if (unit != null) put(AnalyticsProps.HALLWAY_LENGTH_UNIT, unit)
        }
        trackEvent(RecordFlowAnalyticsEvents.HALLWAY_LENGTH_SUBMITTED, props)
    }

    fun trackShortHallwayPopupShown(activity: OSTActivityType) {
        trackEvent(
            RecordFlowAnalyticsEvents.SCREEN_SHORT_HALLWAY_POPUP,
            mapOf(AnalyticsProps.ACTIVITY_NAME to activity.analyticsName),
        )
    }

    fun trackShortHallwayEditClicked(activity: OSTActivityType) {
        trackEvent(
            RecordFlowAnalyticsEvents.CLICKED_SHORT_HALLWAY_EDIT,
            mapOf(AnalyticsProps.ACTIVITY_NAME to activity.analyticsName),
        )
    }

    fun trackShortHallwayStartTestClicked(activity: OSTActivityType) {
        trackEvent(
            RecordFlowAnalyticsEvents.CLICKED_SHORT_HALLWAY_START_TEST,
            mapOf(AnalyticsProps.ACTIVITY_NAME to activity.analyticsName),
        )
    }

    fun trackStartMeasurementClicked(activity: OSTActivityType) {
        track(RecordFlowAnalyticsEvents.CLICKED_START_MEASUREMENT, activity)
    }

    /**
     * `Clicked: start_measurement_now` — user tapped "Start now" on the Get Ready
     * countdown screen, skipping the remaining countdown. [timeRemaining] is the
     * true seconds-with-millis still left on the countdown at the moment of the tap.
     */
    fun trackStartMeasurementNowClicked(activity: OSTActivityType, timeRemaining: Double) {
        track(RecordFlowAnalyticsEvents.CLICKED_START_MEASUREMENT_NOW, activity) {
            put(AnalyticsProps.TIME_REMAINING, timeRemaining.toString())
        }
    }

    // --- OS-15833 measurement-flow events ---

    fun trackBackClicked(
        screenName: String,
        activity: OSTActivityType?,
        appSection: String? = APP_SECTION_DEFAULT,
    ) {
        val props = buildMap {
            put(AnalyticsProps.SCREEN_NAME, screenName)
            if (activity != null) put(AnalyticsProps.ACTIVITY_NAME, activity.analyticsName)
            if (appSection != null) put(AnalyticsProps.APP_SECTION, appSection)
        }
        trackEvent(RecordFlowAnalyticsEvents.CLICKED_BACK_BUTTON, props)
    }

    /**
     * `Clicked: exit_button`. Emits activity_name, app_section and a clean [screenName].
     * When [errorCode]/[errorTitle] are supplied (exit from an error screen) they are
     * included; nulls are omitted.
     */
    fun trackExitClicked(
        screenName: String,
        activity: OSTActivityType?,
        appSection: String? = APP_SECTION_DEFAULT,
        errorCode: String? = null,
        errorTitle: String? = null,
    ) {
        val props = buildMap {
            put(AnalyticsProps.SCREEN_NAME, screenName)
            if (activity != null) put(AnalyticsProps.ACTIVITY_NAME, activity.analyticsName)
            if (appSection != null) put(AnalyticsProps.APP_SECTION, appSection)
            if (errorCode != null) put(AnalyticsProps.ERROR_CODE, errorCode)
            if (errorTitle != null) put(AnalyticsProps.ERROR_TITLE_STRING, errorTitle)
        }
        trackEvent(RecordFlowAnalyticsEvents.CLICKED_EXIT_BUTTON, props)
    }

    fun trackWalkSelectDurationScreen(activity: OSTActivityType) {
        track(RecordFlowAnalyticsEvents.SCREEN_WALK_SELECT_DURATION, activity)
    }

    /**
     * [selectionIndex] is the tapped option's index (0=1min, 1=3min, 2=5min,
     * 3=long walk) — the value the selection screen reports, NOT a seconds value.
     */
    fun trackWalkDurationSelected(activity: OSTActivityType, selectionIndex: Int) {
        track(RecordFlowAnalyticsEvents.CLICKED_WALK_DURATION_SELECTED, activity) {
            put(AnalyticsProps.WALK_DURATION, walkDurationLabel(selectionIndex))
        }
    }

    fun trackPreTagScreen(activity: OSTActivityType, preTagScreenName: String) {
        track(RecordFlowAnalyticsEvents.SCREEN_PRE_TAG, activity) {
            put(AnalyticsProps.PRE_TAG_SCREEN_NAME, preTagScreenName)
        }
    }

    fun trackIncreaseVolumeScreen(activity: OSTActivityType) {
        track(RecordFlowAnalyticsEvents.SCREEN_MEASUREMENT_INCREASE_VOLUME, activity)
    }

    fun trackMeasurementStartScreen(activity: OSTActivityType) {
        track(RecordFlowAnalyticsEvents.SCREEN_MEASUREMENT_START, activity)
    }

    /**
     * `screen: measurement_instructions`. [priorScreen] records where the instructions
     * sheet was opened from — "measurement_start" (the GO screen) or "measurement_error".
     */
    fun trackMeasurementInstructionsScreen(activity: OSTActivityType, priorScreen: String) {
        track(RecordFlowAnalyticsEvents.SCREEN_MEASUREMENT_INSTRUCTIONS, activity) {
            put(AnalyticsProps.PRIOR_SCREEN, priorScreen)
        }
    }

    fun trackMeasurementCountdownScreen(activity: OSTActivityType) {
        track(RecordFlowAnalyticsEvents.SCREEN_MEASUREMENT_COUNTDOWN, activity)
    }

    /**
     * `Clicked: measurement_stop` — [elapsedSeconds] is the wall-clock recording
     * duration in seconds with ms precision (e.g. 17.372).
     */
    fun trackMeasurementStopClicked(activity: OSTActivityType, elapsedSeconds: Double) {
        track(RecordFlowAnalyticsEvents.CLICKED_MEASUREMENT_STOP, activity) {
            put(AnalyticsProps.ELAPSED_SECONDS, elapsedSeconds.toString())
        }
    }

    /**
     * `screen: measurement_analyzing` — [measurementSeconds] is the recorded
     * measurement length in whole seconds.
     */
    fun trackAnalyzingScreen(activity: OSTActivityType, measurementSeconds: Int) {
        track(RecordFlowAnalyticsEvents.SCREEN_MEASUREMENT_ANALYZING, activity) {
            put(AnalyticsProps.MEASUREMENT_SECONDS, measurementSeconds.toString())
        }
    }

    /**
     * `screen: measurement_still_analyzing` (analysis took ≥30s). Emits the recorded
     * [pedometer]/[seconds]/[perceptionUuid] when known. `displayed_after_seconds` is NOT
     * sent (no on-device source for the elapsed time without adding new timing state).
     */
    fun trackStillAnalyzingScreen(
        activity: OSTActivityType,
        pedometer: Int?,
        seconds: Int?,
        perceptionUuid: String?,
    ) {
        track(RecordFlowAnalyticsEvents.SCREEN_MEASUREMENT_STILL_ANALYZING, activity) {
            if (pedometer != null) put(AnalyticsProps.PEDOMETER, pedometer.toString())
            if (seconds != null) put(AnalyticsProps.MEASUREMENT_SECONDS, seconds.toString())
            if (perceptionUuid != null) put(AnalyticsProps.PERCEPTION_UUID, perceptionUuid)
        }
    }

    /**
     * `screen: measurement_error`. Emits the canonical [error_code][canonicalErrorCode]
     * (not the granular [errorType]), the localized title/subtitle strings, and the
     * recorded measurement metadata. Every nullable param is omitted when null.
     */
    fun trackErrorScreen(
        activity: OSTActivityType,
        errorType: String,
        measurementSeconds: Int?,
        steps: Int?,
        perceptionUuid: String?,
        titleString: String?,
        subtitleString: String?,
        appSection: String?,
    ) {
        track(RecordFlowAnalyticsEvents.SCREEN_MEASUREMENT_ERROR, activity) {
            put(AnalyticsProps.ERROR_CODE, canonicalErrorCode(errorType))
            if (titleString != null) put(AnalyticsProps.ERROR_TITLE_STRING, titleString)
            if (subtitleString != null) put(AnalyticsProps.ERROR_SUBTITLE_STRING, subtitleString)
            if (measurementSeconds != null) put(AnalyticsProps.MEASUREMENT_SECONDS, measurementSeconds.toString())
            if (steps != null) put(AnalyticsProps.STEPS, steps.toString())
            if (perceptionUuid != null) put(AnalyticsProps.PERCEPTION_UUID, perceptionUuid)
            if (appSection != null) put(AnalyticsProps.APP_SECTION, appSection)
        }
    }

    /**
     * `Clicked: measurement_try_again`. Emits the canonical
     * [prior_error_code][canonicalErrorCode] of the error the user is retrying, plus
     * app_section and screen_name when known.
     */
    fun trackTryAgainClicked(
        activity: OSTActivityType,
        errorType: String,
        appSection: String?,
        screenName: String?,
    ) {
        track(RecordFlowAnalyticsEvents.CLICKED_MEASUREMENT_TRY_AGAIN, activity) {
            put(AnalyticsProps.PRIOR_ERROR_CODE, canonicalErrorCode(errorType))
            if (appSection != null) put(AnalyticsProps.APP_SECTION, appSection)
            if (screenName != null) put(AnalyticsProps.SCREEN_NAME, screenName)
        }
    }

    /**
     * `Clicked: enter_results_manually`. [screenOrigin] clarifies whether the manual-entry
     * flow was entered from an error screen ("error") or the summary screen ("summary").
     */
    fun trackEnterResultsManuallyClicked(
        activity: OSTActivityType,
        screenOrigin: String,
        appSection: String? = APP_SECTION_DEFAULT,
    ) {
        track(RecordFlowAnalyticsEvents.CLICKED_ENTER_RESULTS_MANUALLY, activity) {
            if (appSection != null) put(AnalyticsProps.APP_SECTION, appSection)
            put(AnalyticsProps.SCREEN_ORIGIN, screenOrigin)
        }
    }

    /** `screen: measurement_enter_results_manually`. See [trackEnterResultsManuallyClicked]. */
    fun trackEnterResultsManuallyScreen(
        activity: OSTActivityType,
        screenOrigin: String,
        appSection: String? = APP_SECTION_DEFAULT,
    ) {
        track(RecordFlowAnalyticsEvents.SCREEN_MEASUREMENT_ENTER_RESULTS_MANUALLY, activity) {
            if (appSection != null) put(AnalyticsProps.APP_SECTION, appSection)
            put(AnalyticsProps.SCREEN_ORIGIN, screenOrigin)
        }
    }

    fun trackEnterResultsManuallySaveClicked(activity: OSTActivityType, value: String) {
        track(RecordFlowAnalyticsEvents.CLICKED_ENTER_RESULTS_MANUALLY_SAVE, activity) {
            put(AnalyticsProps.RESULT_VALUE, value)
        }
    }

    fun trackReloadClicked(activity: OSTActivityType) {
        track(RecordFlowAnalyticsEvents.CLICKED_MEASUREMENT_RELOAD, activity)
    }

    /**
     * `Clicked: measurement_error_continue`. The spec wants `screen_name` carrying the
     * error identity (e.g. "measurement error: OTHER"), built from the canonical code of
     * the granular [errorType].
     */
    fun trackErrorContinueClicked(activity: OSTActivityType, errorType: String) {
        track(RecordFlowAnalyticsEvents.CLICKED_MEASUREMENT_ERROR_CONTINUE, activity) {
            put(AnalyticsProps.SCREEN_NAME, "measurement error: ${canonicalErrorCode(errorType)}")
        }
    }

    /**
     * `screen: measurement_add_tags`. [perceptionUuid] is the measurement id; the key
     * is omitted when it is not yet known (null).
     */
    fun trackAddTagsScreen(activity: OSTActivityType, perceptionUuid: String?) {
        track(RecordFlowAnalyticsEvents.SCREEN_MEASUREMENT_ADD_TAGS, activity) {
            if (perceptionUuid != null) put(AnalyticsProps.PERCEPTION_UUID, perceptionUuid)
        }
    }

    /**
     * `Clicked: measurement_submit_tags`. Emits the typed tag selections, never the
     * free-text clinician note (HIPAA — `notes` is intentionally not sent). A key is
     * omitted when its value is null.
     */
    fun trackSubmitTagsClicked(
        activity: OSTActivityType,
        source: String,
        perceptionUuid: String?,
        assistiveDevice: OSTAssistiveDevice?,
        footwear: Footwear?,
        handsUsedForSupport: String? = null,
        // Pre-tag path only: the pre-tag screen name and a JSON-string of the selected
        // pre-tag answers. Null/blank on the post-measurement path.
        preTagScreenName: String? = null,
        preTagValues: String? = null,
    ) {
        track(RecordFlowAnalyticsEvents.CLICKED_MEASUREMENT_SUBMIT_TAGS, activity) {
            put(AnalyticsProps.SOURCE, source)
            if (perceptionUuid != null) put(AnalyticsProps.PERCEPTION_UUID, perceptionUuid)
            assistiveDeviceLabel(assistiveDevice)?.let { put(AnalyticsProps.TAGS_ASSISTIVE_DEVICE, it) }
            footwearLabel(footwear)?.let { put(AnalyticsProps.TAGS_FOOTWEAR, it) }
            // "Used hands for support" is captured as a tagging question, so the caller
            // resolves Yes/No/null from the selected answer rather than a typed model field.
            handsUsedForSupport?.let { put(AnalyticsProps.TAGS_HANDS_USED_FOR_SUPPORT, it) }
            if (preTagScreenName != null) put(AnalyticsProps.PRE_TAG_SCREEN_NAME, preTagScreenName)
            if (preTagValues != null) put(AnalyticsProps.PRE_TAG_VALUES, preTagValues)
        }
    }

    fun trackPreRecordingAssistiveDeviceScreen(activity: OSTActivityType) {
        track(RecordFlowAnalyticsEvents.SCREEN_PRE_RECORDING_ASSISTIVE_DEVICE, activity)
    }

    fun trackPreRecordingAssistiveDeviceSelected(
        activity: OSTActivityType,
        device: OSTAssistiveDevice,
    ) {
        track(RecordFlowAnalyticsEvents.CLICKED_PRE_RECORDING_ASSISTIVE_DEVICE_SELECTED, activity) {
            put(AnalyticsProps.ASSISTIVE_DEVICE, device.name)
        }
    }

    fun trackPreRecordingFootwearScreen(activity: OSTActivityType) {
        track(RecordFlowAnalyticsEvents.SCREEN_PRE_RECORDING_FOOTWEAR, activity)
    }

    fun trackPreRecordingFootwearSelected(
        activity: OSTActivityType,
        footwear: Footwear,
    ) {
        track(RecordFlowAnalyticsEvents.CLICKED_PRE_RECORDING_FOOTWEAR_SELECTED, activity) {
            put(AnalyticsProps.FOOTWEAR, footwear.name)
        }
    }

    // --- OS-15962 Static Balance Test events ---

    /**
     * `screen: static_balance_condition_setup` — Condition Setup screen viewed.
     * [conditionNumber] is the 1-based index of the condition within the session.
     */
    fun trackStaticBalanceConditionSetupScreen(
        conditionNumber: Int,
        sessionUuid: String,
    ) {
        track(RecordFlowAnalyticsEvents.STATIC_BALANCE_CONDITION_SETUP, OSTActivityType.STATIC_BALANCE) {
            put(AnalyticsProps.CONDITION_NUMBER, conditionNumber.toString())
            put(AnalyticsProps.SESSION_UUID, sessionUuid)
        }
    }

    /**
     * `static_balance_condition_confirmed` — user tapped Continue on Condition Setup.
     * Emits the full condition config (canonical serialized values) per the PRD;
     * [footwear] is omitted when the optional selection was skipped.
     */
    fun trackStaticBalanceConditionConfirmed(
        condition: OSTBalanceCondition,
        durationSeconds: Int,
        conditionNumber: Int,
        sessionUuid: String,
    ) {
        track(RecordFlowAnalyticsEvents.STATIC_BALANCE_CONDITION_CONFIRMED, OSTActivityType.STATIC_BALANCE) {
            // One property per selected category, keyed by the category key (e.g. stance,
            // vision, surface, footwear). Reproduces the PRD props for the shared categories
            // and extends naturally to any server-added category.
            condition.selections.forEach { put(it.categoryKey, it.code) }
            // PRD prop name is `duration`; `session_uuid` is kept as the session grouping key.
            put(AnalyticsProps.DURATION, durationSeconds.toString())
            put(AnalyticsProps.CONDITION_NUMBER, conditionNumber.toString())
            put(AnalyticsProps.SESSION_UUID, sessionUuid)
        }
    }

    /** `static_balance_note_added` — a non-empty note was submitted on Recording Saved. */
    fun trackStaticBalanceNoteAdded(sessionUuid: String) {
        track(RecordFlowAnalyticsEvents.STATIC_BALANCE_NOTE_ADDED, OSTActivityType.STATIC_BALANCE) {
            put(AnalyticsProps.SESSION_UUID, sessionUuid)
        }
    }

    /**
     * Recording Saved CTA: `static_balance_another_test` (record another condition)
     * or `static_balance_go_to_summary` (finish the flow → web summary).
     */
    fun trackStaticBalanceConditionChoice(
        recordAnother: Boolean,
        conditionCount: Int,
        sessionUuid: String,
    ) {
        val event =
            if (recordAnother) {
                RecordFlowAnalyticsEvents.STATIC_BALANCE_ANOTHER_TEST
            } else {
                RecordFlowAnalyticsEvents.STATIC_BALANCE_GO_TO_SUMMARY
            }
        track(event, OSTActivityType.STATIC_BALANCE) {
            put(AnalyticsProps.CONDITION_COUNT, conditionCount.toString())
            put(AnalyticsProps.SESSION_UUID, sessionUuid)
        }
    }

    // --- helpers ---

    // Spec-canonical labels (independent of the localized UI strings).

    private fun walkDurationLabel(selectionIndex: Int): String =
        when (WalkDuration.values.getOrNull(selectionIndex)) {
            is WalkDuration.OneMinute -> "1 minute"
            is WalkDuration.ThreeMinute -> "3 minutes"
            is WalkDuration.FiveMinute -> "5 minutes"
            is WalkDuration.Unrestricted, null -> "Long walk (up to 30 min)"
        }

    /** Returns null for NONE / null so the analytics key is omitted. */
    private fun assistiveDeviceLabel(device: OSTAssistiveDevice?): String? =
        when (device) {
            OSTAssistiveDevice.WALKER -> "Walker"
            OSTAssistiveDevice.ROLLATOR -> "Rollator"
            OSTAssistiveDevice.CANE -> "Cane"
            OSTAssistiveDevice.CRUTCH_DOUBLE -> "2 crutches"
            OSTAssistiveDevice.CRUTCH_SINGLE -> "1 crutch"
            OSTAssistiveDevice.NONE, null -> null
        }

    /** Returns null for NONE / null so the analytics key is omitted. */
    private fun footwearLabel(footwear: Footwear?): String? =
        when (footwear) {
            Footwear.WITH_SHOES -> "With shoes"
            Footwear.BAREFOOT -> "Barefoot"
            Footwear.BRACE -> "Brace"
            Footwear.SHOE_ADJUSTMENT -> "Shoe adjustment"
            Footwear.ONE_INSOLE -> "1 insoles"
            Footwear.UCBL_BRACE -> "UCBL brace"
            Footwear.SMO_BRACE -> "SMO brace"
            Footwear.SLIPPERS -> "Slippers"
            Footwear.NON_SKID_SOCKS -> "Non-skid socks"
            Footwear.NONE, null -> null
        }

    private fun track(
        event: String,
        activity: OSTActivityType?,
        extra: (MutableMap<String, String>.() -> Unit)? = null,
    ) {
        val props = buildMap {
            put(AnalyticsProps.SCREEN_NAME, ScreenNames.MEASUREMENT)
            if (activity != null) put(AnalyticsProps.ACTIVITY_NAME, activity.analyticsName)
            if (extra != null) extra()
        }
        trackEvent(event, props)
    }

    private fun trackEvent(eventName: String, properties: Map<String, String>) {
        analytics.onEvent(OSTEvent(name = eventName, properties = properties))
    }

    companion object {
        // The SDK cannot truly know the host app's section; the measurement flow is
        // always entered from the host's Activities area, so we report that as a fixed
        // assumption rather than inventing a per-screen value.
        const val APP_SECTION_DEFAULT = "activities"

        // Pre-tag and post-measurement screen_origin values for enter_results_manually.
        const val SCREEN_ORIGIN_ERROR = "error"
        const val SCREEN_ORIGIN_SUMMARY = "summary"

        // prior_screen values for measurement_instructions.
        const val PRIOR_SCREEN_MEASUREMENT_START = "measurement_start"
        const val PRIOR_SCREEN_MEASUREMENT_ERROR = "measurement_error"

        /**
         * Collapses a granular error-screen type (e.g. "sts_static", "walk_short") into the
         * spec's canonical error_code bucket. "short" is checked before "static" so
         * "static_balance_short" maps to SHORT, not STATIC.
         */
        fun canonicalErrorCode(errorType: String): String =
            when {
                errorType.contains("short") -> "SHORT"
                errorType.contains("static") -> "STATIC"
                errorType.contains("position") -> "POSITION"
                errorType == "curvy" -> "CURVY"
                errorType == "walk_non_repetitive" -> "NON_REPETITIVE"
                errorType == "connectivity" -> "CONNECTION_ISSUE"
                errorType == "server_issue" -> "UNABLE_TO_REACH_SERVER"
                else -> "OTHER"
            }
    }
}
