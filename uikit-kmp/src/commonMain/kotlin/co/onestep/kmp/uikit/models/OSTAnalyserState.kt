package co.onestep.kmp.uikit.models

sealed class OSTAnalyserState {

    data object Idle : OSTAnalyserState()

    data object Uploading : OSTAnalyserState()

    data object Analyzing : OSTAnalyserState()

    data object Analyzed : OSTAnalyserState()

    data class Failed(
        val throwable: Throwable? = null,
        val error: OSTAnalyserError,
    ) : OSTAnalyserState()
}
