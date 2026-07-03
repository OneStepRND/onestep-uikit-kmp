package co.onestep.kmp.uikit.bridge

import co.onestep.kmp.uikit.models.OSTDailyBackgroundMeasurement
import co.onestep.kmp.uikit.models.OSTEvent
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
}
