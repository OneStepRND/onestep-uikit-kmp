package co.onestep.kmp.uikit.bridge

import co.onestep.kmp.uikit.models.FeatureFlag

/**
 * Bridge interface abstracting FeatureFlags access.
 * Android delegates to the real FeatureFlags object backed by SharedPreferences.
 * iOS implementation uses UserDefaults or equivalent.
 */
interface FeatureFlagsBridge {
    fun isEnabled(flag: FeatureFlag): Boolean
    fun setEnabled(flag: FeatureFlag, enabled: Boolean)
    fun getAllFlags(): Map<FeatureFlag, Boolean>
}
