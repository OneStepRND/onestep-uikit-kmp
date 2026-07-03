package co.onestep.kmp.uikit

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Factory functions for creating Kotlin Flow instances from Swift.
 * Kotlin/Native does not export MutableStateFlow/MutableSharedFlow constructors,
 * so these helpers bridge the gap for iOS mock implementations.
 */
object FlowFactories {
    fun <T : Any> createStateFlow(initialValue: T): MutableStateFlow<T> =
        MutableStateFlow(initialValue)

    fun <T : Any> createSharedFlow(): MutableSharedFlow<T> =
        MutableSharedFlow()

    fun <T : Any> stateFlowOf(value: T): StateFlow<T> =
        MutableStateFlow(value)

    fun <T : Any> emptyFlow(): Flow<T> =
        MutableSharedFlow()
}
