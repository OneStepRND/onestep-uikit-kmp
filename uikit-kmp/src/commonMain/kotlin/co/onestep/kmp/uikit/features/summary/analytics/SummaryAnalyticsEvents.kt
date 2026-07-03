package co.onestep.kmp.uikit.features.summary.analytics

/**
 * Summary analytics event names, ported verbatim from the Android `uikit` module
 * (`SummaryAnalyticsEvents`). Event-name strings MUST stay byte-identical to uikit.
 * Property keys are shared via `RecordFlowAnalyticsEvents`'s `AnalyticsProps`.
 */
internal object SummaryAnalyticsEvents {
    const val SUMMARY_EDIT_HALLWAY = "summary_edit_hallway"

    // OS-15833 activity-summary events
    const val SCREEN_ACTIVITY_SUMMARY = "screen: activity_summary"
    const val SCREEN_GAIT_DATA = "screen: gait_data"
    const val CLICKED_ACTIVITY_SUMMARY_TAB = "Clicked: activity_summary_tab"
    const val CLICKED_DISCARD_MEASUREMENT = "Clicked: discard_measurement"
    const val SCREEN_MEASUREMENT_DELETED = "screen: measurement_deleted"

    object ScreenNames {
        const val ACTIVITY_SUMMARY = "activity_summary"
        const val MEASUREMENT = "measurement"
    }

    object Tabs {
        const val SUMMARY = "highlights"
        const val GAIT_DATA = "gait_data"
    }

    /**
     * `app_section` values the SDK can attribute. The summary is reached either at the
     * end of the recording flow (host "Activities" area) or from the host's history/care
     * log — the only two origins the SDK actually distinguishes.
     */
    object AppSection {
        const val ACTIVITIES = "activities"
        const val HISTORY = "history"
    }
}
