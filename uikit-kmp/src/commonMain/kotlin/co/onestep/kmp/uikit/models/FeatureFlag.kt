package co.onestep.kmp.uikit.models

enum class FeatureFlag(
    val key: String,
    val defaultEnabled: Boolean,
) {
    STS_MANUAL_REPORT("ff_sts_manual_report", defaultEnabled = false),
}
