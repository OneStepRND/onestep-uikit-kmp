package co.onestep.kmp.uikit.features.summary.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface OSTSummaryOptions {
    @Serializable
    @SerialName("none")
    data object None : OSTSummaryOptions

    @Serializable
    @SerialName("minimal")
    data object MINIMAL : OSTSummaryOptions

    @Serializable
    @SerialName("full")
    data object Full : OSTSummaryOptions

    /**
     * Web-only summary: the flow finishes with the measurement id and the host app opens
     * the web summary (e.g. Static Balance session summary). No native summary screen is
     * shown. Timeout handling treats [WEB] like [Full] (surfaces an error, not a silent exit).
     */
    @Serializable
    @SerialName("web")
    data object WEB : OSTSummaryOptions
}
