package co.onestep.kmp.uikit

import co.onestep.kmp.uikit.models.OSTEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Main entry point for UIKit events.
 * Mirrors the Android OSTUIKit object for multiplatform usage.
 */
object OSTUIKit {
    private val _uiEvents = MutableSharedFlow<OSTEvent>(
        replay = 0,
        extraBufferCapacity = 64,
    )

    val uiEvents: Flow<OSTEvent> = _uiEvents.asSharedFlow()

    suspend fun emitEvent(event: OSTEvent) {
        _uiEvents.emit(event)
    }

    fun tryEmitEvent(event: OSTEvent): Boolean {
        return _uiEvents.tryEmit(event)
    }
}
