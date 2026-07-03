package co.onestep.kmp.uikit.models

sealed interface OSTAnalyserError {

    val error: String?
        get() = null

    data class TooShort(
        override val error: String,
    ) : OSTAnalyserError

    data class General(
        val throwable: Throwable? = null,
        override val error: String,
    ) : OSTAnalyserError

    data class Timeout(
        val throwable: Throwable? = null,
        override val error: String,
    ) : OSTAnalyserError

    data class ServerError(
        val throwable: Throwable? = null,
        override val error: String,
    ) : OSTAnalyserError

    data class NetworkError(
        val throwable: Throwable? = null,
        override val error: String,
    ) : OSTAnalyserError
}
