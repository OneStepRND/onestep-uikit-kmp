package co.onestep.kmp.uikit.features.recordFlow

/**
 * Terminal result of the recording flow, delivered to the host app via
 * [OSTRecordingFlow]'s `onFinished` callback immediately before `onDismiss`.
 *
 * This is the KMP counterpart of the Android uikit's `OSTRecordingFlowResult`
 * (delivered through `OSTRecordingFlowContract`) and the iOS uikit's
 * `OSTRecordingFlowResult` (delivered through `onDismissResult`).
 *
 * It is intentionally NOT an [co.onestep.kmp.sdk.OSTEvent]: the analytics event stream must
 * never carry a patient-scoped summary link (HIPAA). Hosts get the URL here and nowhere else.
 *
 * @property measurementId UUID of the finished measurement. `null` only when the flow ended
 *   without producing one.
 * @property summaryUrl Shareable web summary URL for the analyzed measurement, when the server
 *   returned one. The host app opens this in a web view. `null` when the measurement failed
 *   analysis or the server did not return a URL.
 * @property sessionUuid Session grouping key for multi-condition activities (Static Balance);
 *   `null` for single-measurement activities.
 */
data class OSTRecordingFlowResult(
    val measurementId: String?,
    val summaryUrl: String?,
    val sessionUuid: String? = null,
)
