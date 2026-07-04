@file:OptIn(ExperimentalForeignApi::class)

package co.onestep.kmp.uikit.spike

import SpikeObjCKit.SpikeGreeter
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Phase 0 spike: proves a Swift package's @objc API is callable from iosMain via spm4Kmp.
 * Not shipped — delete when the spike concludes.
 *
 * Finding: swift-build-generated ObjC headers carry no SWIFT_ASYNC attributes, so
 * Kotlin/Native does NOT auto-map completion handlers to suspend functions here; we wrap
 * manually. The wrapper is still pure Kotlin — no Swift-side plumbing needed.
 */
object Spm4KmpSpike {

    private val greeter = SpikeGreeter()

    /** Pattern 1: plain sync call across the boundary. */
    fun greet(): String = greeter.greetWithName("KMP")

    /** Pattern 2: Swift async fn (ObjC completion handler) wrapped as suspend in Kotlin. */
    suspend fun fetchMeasurement(): String = suspendCancellableCoroutine { cont ->
        greeter.fetchMeasurementWithCompletionHandler { measurement, error ->
            when {
                error != null -> cont.resumeWithException(
                    RuntimeException(error.localizedDescription),
                )
                else -> cont.resume("${measurement?.identifier()}:${measurement?.steps()}")
            }
        }
    }

    /** Pattern 3: block-based callback registration wrapped in a cold Flow. */
    fun ticks(): Flow<Int> = callbackFlow {
        greeter.onTick { value -> trySend(value.toInt()) }
        awaitClose()
    }
}
