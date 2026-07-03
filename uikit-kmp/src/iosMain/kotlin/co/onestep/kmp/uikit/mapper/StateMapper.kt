package co.onestep.kmp.uikit.mapper

import co.onestep.kmp.uikit.models.OSTAnalyserError
import co.onestep.kmp.uikit.models.OSTAnalyserState
import co.onestep.kmp.uikit.models.OSTRecorderState
import co.onestep.kmp.uikit.models.OSTState

/**
 * iOS SDK state mappers.
 *
 * Maps iOS SDK state strings/values to KMP state types.
 * When the iOS OneStep SDK is integrated via cinterop, add direct mapping.
 */
fun createKmpState(
    stateName: String,
    userId: String? = null,
    errorCode: Int? = null,
    errorMessage: String? = null,
): OSTState =
    when (stateName.uppercase()) {
        "UNINITIALIZED" -> OSTState.Uninitialized
        "READY" -> OSTState.Ready
        "IDENTIFIED" -> OSTState.Identified(userId ?: "")
        "ERROR" -> OSTState.Error(errorCode ?: -1, errorMessage ?: "Unknown error")
        else -> OSTState.Uninitialized
    }

fun String.toKmpRecorderState(): OSTRecorderState =
    when (this.uppercase()) {
        "INITIALIZED" -> OSTRecorderState.INITIALIZED
        "RECORDING" -> OSTRecorderState.RECORDING
        "FINALIZING" -> OSTRecorderState.FINALIZING
        "DONE" -> OSTRecorderState.DONE
        else -> OSTRecorderState.INITIALIZED
    }

fun createKmpAnalyserState(
    stateName: String,
    throwable: Throwable? = null,
    errorMessage: String? = null,
): OSTAnalyserState =
    when (stateName.uppercase()) {
        "IDLE" -> OSTAnalyserState.Idle
        "UPLOADING" -> OSTAnalyserState.Uploading
        "ANALYZING" -> OSTAnalyserState.Analyzing
        "ANALYZED" -> OSTAnalyserState.Analyzed
        "FAILED" -> OSTAnalyserState.Failed(
            throwable = throwable,
            error = createKmpAnalyserError("GENERAL", throwable, errorMessage),
        )
        else -> OSTAnalyserState.Idle
    }

fun createKmpAnalyserError(
    type: String,
    throwable: Throwable? = null,
    message: String? = null,
): OSTAnalyserError =
    when (type.uppercase()) {
        "TOO_SHORT" -> OSTAnalyserError.TooShort(message ?: "Recording too short")
        "TIMEOUT" -> OSTAnalyserError.Timeout(throwable, message ?: "Analysis timeout")
        "SERVER_ERROR" -> OSTAnalyserError.ServerError(throwable, message ?: "Server error")
        "NETWORK_ERROR" -> OSTAnalyserError.NetworkError(throwable, message ?: "Network error")
        else -> OSTAnalyserError.General(throwable, message ?: "Unknown error")
    }
