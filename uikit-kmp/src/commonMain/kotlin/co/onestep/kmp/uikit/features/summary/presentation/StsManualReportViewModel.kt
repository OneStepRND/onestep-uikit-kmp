package co.onestep.kmp.uikit.features.summary.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.onestep.kmp.uikit.bridge.RecorderBridge
import co.onestep.kmp.uikit.bridge.SelfReportResult
import co.onestep.kmp.uikit.di.UIKitServiceLocator
import co.onestep.kmp.uikit.models.currentTimeMillis
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the STS manual self-report screen.
 *
 * `failureType == null` means no failure. The destination derives "should I show the error
 * screen?" from `failureType != null` and picks the variant (connectivity-with-reload vs
 * server-no-reload) from its value.
 */
internal data class StsManualReportState(
    val selectedValue: Int? = null,
    val submitting: Boolean = false,
    val submitted: Boolean = false,
    val failureType: StsFailureType? = null,
) {
    val failed: Boolean get() = failureType != null
}

/**
 * How a self-report submission failed.
 *
 * Drives whether the user is offered a "Reload" affordance:
 *  - [Network]: transport-level failure (no internet, DNS, timeout). Retryable.
 *  - [Server]: server responded with a non-2xx status (e.g. 403, 500). Not retryable from the
 *    error screen — the user can only dismiss.
 */
internal enum class StsFailureType { Network, Server }

/**
 * ViewModel that owns submission of the user-reported STS repetition count to
 * [RecorderBridge.selfReportMotionMeasurement] (Android delegates to
 * `OSTMotionLab.selfReportMotionMeasurement`).
 *
 * Ported 1:1 from the Android `uikit` `StsManualReportViewModel`. Exposes a [StateFlow] for
 * screen state and remembers the last submission attempt so [retry] can reuse it after a network
 * failure.
 */
internal class StsManualReportViewModel(
    private val recorderBridge: RecorderBridge = UIKitServiceLocator.recorderBridge,
) : ViewModel() {

    private val _state = MutableStateFlow(StsManualReportState())
    val state: StateFlow<StsManualReportState> = _state.asStateFlow()

    private var lastSubmission: Submission? = null

    fun setSelectedValue(value: Int?) {
        _state.update { it.copy(selectedValue = value) }
    }

    /**
     * Submit the user-reported repetition count for the given measurement.
     *
     * Re-entrant calls while a submission is already in flight are ignored.
     */
    fun onSubmit(
        uuid: String,
        value: Int,
    ) {
        if (_state.value.submitting) return
        lastSubmission = Submission(uuid = uuid, value = value)
        _state.update {
            it.copy(
                selectedValue = value,
                submitting = true,
                failureType = null,
            )
        }
        viewModelScope.launch {
            performSubmit(uuid = uuid, value = value)
        }
    }

    /**
     * Re-submit the last attempt after a connectivity failure.
     *
     * Keeps `failureType` set while the retry is in flight so the destination stays on the
     * connectivity-error screen (with the Reload button showing its loading spinner) instead of
     * briefly flipping back to the wheel.
     */
    fun retry() {
        if (_state.value.submitting) return
        val pending = lastSubmission ?: return
        _state.update { it.copy(submitting = true) }
        viewModelScope.launch {
            performSubmit(uuid = pending.uuid, value = pending.value)
        }
    }

    private suspend fun performSubmit(uuid: String, value: Int) {
        val start = currentTimeMillis()
        val result = try {
            recorderBridge.selfReportMotionMeasurement(uuid = uuid, stsRepetitions = value)
        } catch (e: CancellationException) {
            // Honor structured concurrency: never swallow cancellation.
            throw e
        } catch (t: Throwable) {
            // Guard against unexpected throws — treated below as a non-retryable failure.
            SelfReportResult.ServerFailure
        }
        if (result is SelfReportResult.Success) {
            _state.update {
                it.copy(submitting = false, submitted = true, failureType = null)
            }
            return
        }
        // Hold the spinner for a perceivable minimum on failure — otherwise an offline device
        // flashes the loader and the user has no visual confirmation that the network call even
        // happened.
        val elapsed = currentTimeMillis() - start
        if (elapsed < MIN_LOADER_VISIBLE_MS) {
            delay(MIN_LOADER_VISIBLE_MS - elapsed)
        }
        // Retryable only for actual transport-level connectivity issues (NetworkFailure);
        // everything else (ServerFailure for HTTP 4xx/5xx, uninitialized SDK, unexpected throw)
        // is non-retryable and routes the user to the server-error screen.
        val type = if (result is SelfReportResult.NetworkFailure) {
            StsFailureType.Network
        } else {
            StsFailureType.Server
        }
        _state.update { it.copy(submitting = false, failureType = type) }
    }

    fun consumeSubmitted() {
        _state.update { it.copy(submitted = false) }
    }

    fun consumeFailed() {
        _state.update { it.copy(failureType = null) }
    }

    private data class Submission(val uuid: String, val value: Int)

    private companion object {
        const val MIN_LOADER_VISIBLE_MS = 2_000L
    }
}
