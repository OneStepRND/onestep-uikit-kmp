package co.onestep.kmp.uikit.features.recordFlow

/**
 * Why the recording flow ended without a result, for hosts that need to tell a timeout apart from
 * a user cancel.
 *
 * KMP counterpart of the Android uikit's `OSTRecordingFlowActivity.EXTRA_EXIT_REASON` /
 * `EXIT_REASON_UI_TIMEOUT`, which ride on its `RESULT_CANCELED`. Here the reason is an
 * [co.onestep.kmp.sdk.OSTEvent] named [EVENT_NAME] delivered through [OSTRecordingFlow]'s
 * `onResult` immediately before `onDismiss`, with the reason under [KEY_EXIT_REASON]. A plain
 * user cancel emits no event and signals through `onDismiss` alone, as on Android.
 *
 * The event carries the reason only — no measurement or patient identifiers (HIPAA).
 */
object OSTRecordingFlowExit {
    const val EVENT_NAME = "recording_exited"
    const val KEY_EXIT_REASON = "exit_reason"

    /**
     * The analyzing screen got no outcome within 60 seconds while
     * [co.onestep.kmp.uikit.features.summary.models.OSTSummaryOptions.None] was configured, so
     * there was no in-flow screen to show the timeout on. The recording itself may still finish
     * uploading and analyzing in the background.
     */
    const val EXIT_REASON_UI_TIMEOUT = "ui_timeout"
}
