package co.onestep.kmp.uikit.models

import co.onestep.kmp.uikit.utils.monotonicNowMillis

/**
 * The timing of a single recording session: when sensor capture began and when the recorder will
 * auto-stop.
 *
 * Only the SDK can own recording time — capture keeps running to its deadline after the recording
 * screen is gone, so a UI that counts its own seconds inevitably disagrees with the recorder.
 * Render from this window instead: every read recomputes from an absolute deadline rather than
 * incrementing a counter, so the display cannot accumulate drift and is correct immediately after
 * a dropped tick, backgrounding, screen-off, or reopening the screen mid-recording.
 *
 * ## Which clock
 *
 * [startedAtMonotonicMillis] and [willEndAtMonotonicMillis] are [monotonicNowMillis] readings —
 * **all duration math must use these two**. [startedAtEpochMillis] is wall-clock time for display
 * and logging only; it can jump on an NTP sync or a user clock change.
 *
 * @property startedAtMonotonicMillis [monotonicNowMillis] when sensor capture began.
 * @property willEndAtMonotonicMillis Deadline the recorder auto-stops at, on the same clock. An
 *   early stop means the recording ends before this.
 * @property startedAtEpochMillis Wall-clock time capture began. Display and logging only.
 */
data class OSTRecordingWindow(
    val startedAtMonotonicMillis: Long,
    val willEndAtMonotonicMillis: Long,
    val startedAtEpochMillis: Long,
) {
    /** Total length of the window — the duration this recording was started with. */
    val totalMillis: Long get() = willEndAtMonotonicMillis - startedAtMonotonicMillis

    /**
     * Milliseconds of recording elapsed at [nowMonotonicMillis] (a [monotonicNowMillis] reading).
     * Never negative.
     */
    fun elapsedMillisAt(nowMonotonicMillis: Long): Long =
        (nowMonotonicMillis - startedAtMonotonicMillis).coerceAtLeast(0)

    /**
     * Milliseconds until the recorder auto-stops, at [nowMonotonicMillis] (a [monotonicNowMillis]
     * reading). Reaches 0 and stays there.
     */
    fun remainingMillisAt(nowMonotonicMillis: Long): Long =
        (willEndAtMonotonicMillis - nowMonotonicMillis).coerceAtLeast(0)
}
