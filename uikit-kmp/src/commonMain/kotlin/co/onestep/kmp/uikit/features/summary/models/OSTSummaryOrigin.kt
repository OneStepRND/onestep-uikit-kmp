package co.onestep.kmp.uikit.features.summary.models

enum class OSTSummaryOrigin {
    CareLog,
    Recording,
    ;

    companion object {
        fun Int.toSummaryOrigin(): OSTSummaryOrigin =
            when (this) {
                0 -> CareLog
                1 -> Recording
                else -> throw IllegalArgumentException("Unknown origin")
            }
    }
}
