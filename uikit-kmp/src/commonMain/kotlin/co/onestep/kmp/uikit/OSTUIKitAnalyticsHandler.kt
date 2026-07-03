package co.onestep.kmp.uikit

import co.onestep.kmp.uikit.models.OSTEvent

/**
 * Interface for handling UIKit analytics events.
 * Consumers implement this to receive analytics from UIKit screens.
 */
interface OSTUIKitAnalyticsHandler {
    fun onEvent(event: OSTEvent)
}
