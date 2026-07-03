package co.onestep.kmp.uikit.bridge.android

import co.onestep.android.core.OneStep
import co.onestep.android.core.getOr
import co.onestep.android.core.monitoring.OSTMonitoringRuntimeState
import co.onestep.android.core.monitoring.getMonitoring
import co.onestep.kmp.uikit.bridge.OSTSDKBridge
import co.onestep.kmp.uikit.models.OSTDailyBackgroundMeasurement
import co.onestep.kmp.uikit.models.OSTEvent
import co.onestep.kmp.uikit.models.OSTState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

class AndroidSDKBridge(private val oneStep: OneStep) : OSTSDKBridge {

    override val sdkState: StateFlow<OSTState>
        get() = object : StateFlow<OSTState> {
            private val delegate = oneStep.identificationState
            override val replayCache: List<OSTState> get() = delegate.replayCache.map { it.toKmp() }
            override val value: OSTState get() = delegate.value.toKmp()
            override suspend fun collect(collector: kotlinx.coroutines.flow.FlowCollector<OSTState>): Nothing {
                delegate.collect { collector.emit(it.toKmp()) }
            }
        }

    override val events: Flow<OSTEvent>
        get() = oneStep.events.map { it.toKmp() }

    override fun isInitialized(): Boolean = true

    override suspend fun sendEvent(event: OSTEvent) {
        // UIKit analytics events — no public core API to forward these
    }

    override val isMonitoringActive: Boolean
        get() = oneStep.getMonitoring().getOr(null)?.state?.value is OSTMonitoringRuntimeState.Active

    override suspend fun getDailySummaries(): List<OSTDailyBackgroundMeasurement> =
        oneStep.getMonitoring().getOr(null)?.getDailySummaries()?.getOr(emptyList())?.map { it.toKmp() }
            ?: emptyList()

    override fun optInToMonitoring() {
        oneStep.getMonitoring().getOr(null)?.optIn()
    }
}
