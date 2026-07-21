package co.onestep.kmp.sdk

import kotlinx.serialization.Serializable
import kotlin.time.Clock

@Serializable
data class OSTEvent(
    val name: String,
    val properties: Map<String, String> = emptyMap(),
    val timestamp: Long = currentTimeMillis(),
)

/** Current wall-clock time in epoch milliseconds. Platform-independent (`kotlinx-datetime`). */
internal fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
