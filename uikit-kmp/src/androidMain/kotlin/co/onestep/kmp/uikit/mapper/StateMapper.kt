package co.onestep.kmp.uikit.mapper

import co.onestep.kmp.uikit.models.OSTAnalyserError as KmpAnalyserError
import co.onestep.kmp.uikit.models.OSTAnalyserState as KmpAnalyserState
import co.onestep.kmp.uikit.models.OSTRecorderState as KmpRecorderState
import co.onestep.kmp.uikit.models.OSTState as KmpState
import co.onestep.android.core.OSTIdentificationState as CoreState
import co.onestep.android.core.motionLab.OSTAnalyserError as CoreAnalyserError
import co.onestep.android.core.motionLab.OSTAnalyserState as CoreAnalyserState
import co.onestep.android.core.motionLab.OSTRecorderState as CoreRecorderState

fun CoreState.toKmp(): KmpState =
    when (this) {
        is CoreState.Unidentified -> KmpState.Ready
        is CoreState.Identified -> KmpState.Identified(patientId.value)
        // shortcut: OSTError has no numeric code in the new API; 0 is a sentinel.
        is CoreState.Lost -> KmpState.Error(code = 0, message = cause.message)
    }

fun CoreRecorderState.toKmp(): KmpRecorderState =
    when (this) {
        CoreRecorderState.INITIALIZED -> KmpRecorderState.INITIALIZED
        CoreRecorderState.RECORDING -> KmpRecorderState.RECORDING
        CoreRecorderState.FINALIZING -> KmpRecorderState.FINALIZING
        CoreRecorderState.DONE -> KmpRecorderState.DONE
    }

fun CoreAnalyserState.toKmp(): KmpAnalyserState =
    when (this) {
        is CoreAnalyserState.Idle -> KmpAnalyserState.Idle
        is CoreAnalyserState.Uploading -> KmpAnalyserState.Uploading
        is CoreAnalyserState.Analyzing -> KmpAnalyserState.Analyzing
        is CoreAnalyserState.Analyzed -> KmpAnalyserState.Analyzed
        is CoreAnalyserState.Failed -> KmpAnalyserState.Failed(
            throwable = throwable,
            error = error.toKmp(),
        )
    }

fun CoreAnalyserError.toKmp(): KmpAnalyserError =
    when (this) {
        is CoreAnalyserError.TooShort -> KmpAnalyserError.TooShort(error ?: "")
        is CoreAnalyserError.General -> KmpAnalyserError.General(throwable, error ?: "")
        is CoreAnalyserError.Timeout -> KmpAnalyserError.Timeout(throwable, error ?: "")
        is CoreAnalyserError.ServerError -> KmpAnalyserError.ServerError(throwable, error ?: "")
        is CoreAnalyserError.NetworkError -> KmpAnalyserError.NetworkError(throwable, error ?: "")
    }
