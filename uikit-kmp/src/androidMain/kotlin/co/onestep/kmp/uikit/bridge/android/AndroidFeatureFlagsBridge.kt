package co.onestep.kmp.uikit.bridge.android

import co.onestep.android.core.platform.SdkFlags
import co.onestep.kmp.uikit.bridge.FeatureFlagsBridge
import co.onestep.kmp.uikit.models.FeatureFlag

class AndroidFeatureFlagsBridge : FeatureFlagsBridge {

    // Flags driven from OSTConfiguration.additionalConfig at OneStep.initialize time are
    // read from core's SdkFlags (mirrors uikit, which reads SdkFlags.stsManualReportEnabled).
    // Flags without a core-config source fall back to their compile-time default.
    override fun isEnabled(flag: FeatureFlag): Boolean =
        when (flag) {
            FeatureFlag.STS_MANUAL_REPORT -> SdkFlags.stsManualReportEnabled
            else -> flag.defaultEnabled
        }

    // No runtime setter: these flags are config-driven at init, not toggled at runtime
    // (matches uikit's read-only-from-config model).
    override fun setEnabled(flag: FeatureFlag, enabled: Boolean) { /* no-op */ }

    override fun getAllFlags(): Map<FeatureFlag, Boolean> =
        FeatureFlag.entries.associateWith { isEnabled(it) }
}
