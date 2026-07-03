package co.onestep.kmp.uikit.models

import kotlinx.serialization.Serializable

@Serializable
data class OSTEvent(
    val name: String,
    val properties: Map<String, String> = emptyMap(),
    val timestamp: Long = currentTimeMillis(),
)

internal expect fun currentTimeMillis(): Long
