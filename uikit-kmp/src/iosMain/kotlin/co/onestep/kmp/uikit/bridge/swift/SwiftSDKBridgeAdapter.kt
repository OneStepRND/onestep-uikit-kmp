package co.onestep.kmp.uikit.bridge.swift

import co.onestep.kmp.uikit.bridge.OSTSDKBridge
import co.onestep.kmp.uikit.mapper.createKmpState
import co.onestep.kmp.uikit.models.OSTDailyBackgroundMeasurement
import co.onestep.kmp.sdk.OSTEvent
import co.onestep.kmp.uikit.models.OSTState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Swift-facing delegate for [SwiftSDKBridgeAdapter]. A Swift class implements this by delegating to
 * the native OneStep SDK. Suspend functions on [OSTSDKBridge] are exposed here as ObjC-friendly
 * completion-handler methods; [StateFlow]/[Flow] never cross the boundary (the adapter owns them).
 */
interface IosSDKDelegate {
    fun isInitialized(): Boolean
    fun isMonitoringActive(): Boolean
    fun sendEvent(event: OSTEvent, completion: () -> Unit)
    fun getDailySummaries(completion: (List<OSTDailyBackgroundMeasurement>) -> Unit)
    fun optInToMonitoring()

    /**
     * Reads the identified user's SDK-managed custom metadata. The Swift side calls the native
     * `getUserAttributes()` and returns `customAttributes` (OSTMixedType values flattened to
     * Kotlin numbers/strings). Returns an empty map on failure — never fails the completion.
     */
    fun getCustomMetadata(completion: (Map<String, Any>) -> Unit)

    /**
     * Merges [metadata] into the identified user's SDK-managed custom metadata via the native
     * `updateCustomMetadata(_:)` merge endpoint, returning the full merged map (or [metadata]
     * unchanged on failure).
     */
    fun updateCustomMetadata(metadata: Map<String, Any>, completion: (Map<String, Any>) -> Unit)
}

/**
 * [OSTSDKBridge] implementation that owns the coroutine/flow machinery in Kotlin and delegates
 * ObjC-friendly work to an [IosSDKDelegate] Swift implementation.
 *
 * Swift pushes state via [onSdkStateChanged] and [emitEvent]; the adapter exposes those as the
 * [sdkState] [StateFlow] (initial [OSTState.Uninitialized]) and the [events] [Flow].
 */
class SwiftSDKBridgeAdapter(private val delegate: IosSDKDelegate) : OSTSDKBridge {

    private val _sdkState = MutableStateFlow<OSTState>(OSTState.Uninitialized)
    override val sdkState: StateFlow<OSTState> = _sdkState.asStateFlow()

    private val _events = MutableSharedFlow<OSTEvent>(extraBufferCapacity = 64)
    override val events: Flow<OSTEvent> = _events.asSharedFlow()

    // --- Swift push functions ---

    /** Push a new SDK state from Swift. Uses the existing [createKmpState] mapper. */
    fun onSdkStateChanged(
        stateName: String,
        userId: String? = null,
        errorCode: Int = -1,
        errorMessage: String? = null,
    ) {
        _sdkState.value = createKmpState(
            stateName = stateName,
            userId = userId,
            errorCode = errorCode,
            errorMessage = errorMessage,
        )
    }

    /** Emit an analytics/SDK event from Swift. */
    fun emitEvent(event: OSTEvent) {
        _events.tryEmit(event)
    }

    // --- OSTSDKBridge ---

    override fun isInitialized(): Boolean = delegate.isInitialized()

    override suspend fun sendEvent(event: OSTEvent) {
        suspendCancellableCoroutine { continuation ->
            delegate.sendEvent(event) { continuation.resume(Unit) }
        }
    }

    override val isMonitoringActive: Boolean
        get() = delegate.isMonitoringActive()

    override suspend fun getDailySummaries(): List<OSTDailyBackgroundMeasurement> =
        suspendCancellableCoroutine { continuation ->
            delegate.getDailySummaries { summaries -> continuation.resume(summaries) }
        }

    override fun optInToMonitoring() {
        delegate.optInToMonitoring()
    }

    override suspend fun getCustomMetadata(): Map<String, Any> =
        suspendCancellableCoroutine { continuation ->
            delegate.getCustomMetadata { metadata -> continuation.resume(metadata) }
        }

    override suspend fun updateCustomMetadata(metadata: Map<String, Any>): Map<String, Any> =
        suspendCancellableCoroutine { continuation ->
            delegate.updateCustomMetadata(metadata) { merged -> continuation.resume(merged) }
        }
}
