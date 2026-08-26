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
 * @property hallwayLengthMeters The hallway/walkway length the user committed on the hallway screen,
 *   in **meters**, for the 6-minute and 2-minute walk flows. `null` for every other activity, and
 *   when the user skipped entering one.
 *
 *   This exists for **clinician-mode hosts**. In current-user mode the length is persisted to the
 *   SDK-managed custom-metadata store and follows the user across devices, so a host never needs to
 *   see it. In clinician mode that persistence is deliberately suppressed — the hallway belongs to
 *   the clinic, not the patient
 *   ([co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.recording.HallwayDistanceManager])
 *   — and the host is expected to pre-fill
 *   [co.onestep.kmp.uikit.features.recordFlow.configurations.OSTRecordingConfiguration.hallwayLengthMeters]
 *   instead. Without this field a host had no way to learn the value in the first place: it could
 *   pre-fill a length it already knew, but never acquire one, so a per-clinic memory could never be
 *   seeded. Reported in meters regardless of the user's unit system, so the host stores one unit and
 *   converts only for display.
 */
data class OSTRecordingFlowResult(
    val measurementId: String?,
    val summaryUrl: String?,
    val sessionUuid: String? = null,
    val hallwayLengthMeters: Float? = null,
)
