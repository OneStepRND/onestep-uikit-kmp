package co.onestep.kmp.uikit.bridge

import co.onestep.kmp.uikit.models.OSTDailyBackgroundMeasurement
import co.onestep.kmp.sdk.OSTEvent
import co.onestep.kmp.uikit.models.OSTState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Bridge interface abstracting the OneStep SDK singleton.
 * Android implementation delegates to the real OneStep object.
 * iOS implementation will delegate to the iOS SDK.
 */
interface OSTSDKBridge {
    val sdkState: StateFlow<OSTState>
    val events: Flow<OSTEvent>

    fun isInitialized(): Boolean
    suspend fun sendEvent(event: OSTEvent)

    val isMonitoringActive: Boolean
    suspend fun getDailySummaries(): List<OSTDailyBackgroundMeasurement>
    fun optInToMonitoring()

    /**
     * Reads the identified user's SDK-managed custom metadata — the `custom_metadata` store the
     * SDK rehydrates from the backend on identify. Returns an empty map when the SDK is not
     * initialized / no user is identified, or on any failure; never throws.
     *
     * UIKit uses this as a lightweight per-user key-value store (for example the last-entered
     * hallway length), so a stored value follows the user across devices and survives logout.
     * Keys prefixed with `ost.` are reserved for SDK/UIKit use.
     */
    suspend fun getCustomMetadata(): Map<String, Any>

    /**
     * Merges [metadata] into the identified user's SDK-managed custom metadata via the dedicated
     * metadata endpoint. The backend merges by key, so keys absent from [metadata] are preserved.
     * Returns the full merged map on success, or [metadata] unchanged on failure; never throws.
     * The endpoint only accepts non-null values.
     */
    suspend fun updateCustomMetadata(metadata: Map<String, Any>): Map<String, Any>

    /**
     * Triggers an immediate SDK data sync for the identified user — uploads pending recordings and
     * pulls the latest analyzed results from the backend. Returns true on success, false on failure
     * (or when no user is identified); never throws. Backed by OSTPatientScope.sync().
     */
    suspend fun sync(): Boolean
}
