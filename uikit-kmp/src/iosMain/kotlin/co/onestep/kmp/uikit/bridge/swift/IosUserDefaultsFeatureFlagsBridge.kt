package co.onestep.kmp.uikit.bridge.swift

import co.onestep.kmp.uikit.bridge.FeatureFlagsBridge
import co.onestep.kmp.uikit.models.FeatureFlag
import platform.Foundation.NSUserDefaults

/**
 * iOS [FeatureFlagsBridge] backed by `NSUserDefaults.standardUserDefaults`.
 *
 * Fully implemented in Kotlin. A flag that has never been set falls back to its
 * [FeatureFlag.defaultEnabled]. Keys are namespaced with [KEY_PREFIX].
 */
class IosUserDefaultsFeatureFlagsBridge : FeatureFlagsBridge {

    private val defaults = NSUserDefaults.standardUserDefaults

    private fun keyFor(flag: FeatureFlag): String = KEY_PREFIX + flag.key

    override fun isEnabled(flag: FeatureFlag): Boolean {
        val key = keyFor(flag)
        // Distinguish "unset" (use the flag's default) from an explicit stored value.
        return if (defaults.objectForKey(key) != null) {
            defaults.boolForKey(key)
        } else {
            flag.defaultEnabled
        }
    }

    override fun setEnabled(flag: FeatureFlag, enabled: Boolean) {
        defaults.setBool(enabled, keyFor(flag))
    }

    override fun getAllFlags(): Map<FeatureFlag, Boolean> =
        FeatureFlag.entries.associateWith { isEnabled(it) }

    companion object {
        private const val KEY_PREFIX = "ost_uikit_ff_"
    }
}
