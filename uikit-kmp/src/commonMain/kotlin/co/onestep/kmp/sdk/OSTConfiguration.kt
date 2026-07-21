package co.onestep.kmp.sdk

/**
 * Configuration for the OneStep SDK.
 *
 * Minimal configuration surface with sensible defaults.
 * Most configuration happens at the product level (Monitoring, MotionLab).
 */
data class OSTConfiguration(
    /**
     * Additional configuration values that can be used by the SDK.
     *
     * Extensibility mechanism for passing custom configuration without
     * modifying the OSTConfiguration class. Useful for:
     * - Feature flags
     * - Environment overrides
     * - Custom behavior toggles
     *
     * Keys and behavior are SDK-version-specific and may not be stable.
     */
    val additionalConfig: Map<String, Any> = emptyMap(),
) {
    companion object {
        /** @suppress */
        const val ENABLE_STS_MANUAL_REPORT: String = "EnableStsManualReport"
    }
}
