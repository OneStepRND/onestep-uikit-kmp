package co.onestep.kmp.uikit.mapper

import co.onestep.kmp.uikit.models.OSTAssistiveDevice
import co.onestep.kmp.uikit.models.OSTLevelOfAssistance
import co.onestep.kmp.uikit.models.OSTMeasurementSystem

/**
 * iOS SDK enum mappers.
 *
 * String-based mappers for iOS SDK interop. When the iOS OneStep SDK is
 * integrated via cinterop, replace with direct type mapping.
 */
fun String.toKmpAssistiveDevice(): OSTAssistiveDevice =
    when (this.uppercase()) {
        "NONE" -> OSTAssistiveDevice.NONE
        "WALKER" -> OSTAssistiveDevice.WALKER
        "ROLLATOR" -> OSTAssistiveDevice.ROLLATOR
        "CANE" -> OSTAssistiveDevice.CANE
        "CRUTCH_DOUBLE" -> OSTAssistiveDevice.CRUTCH_DOUBLE
        "CRUTCH_SINGLE" -> OSTAssistiveDevice.CRUTCH_SINGLE
        else -> OSTAssistiveDevice.NONE
    }

fun OSTAssistiveDevice.toIosString(): String = name

fun String.toKmpLevelOfAssistance(): OSTLevelOfAssistance =
    when (this.uppercase()) {
        "INDEPENDENT" -> OSTLevelOfAssistance.INDEPENDENT
        "MODIFIED_INDEPENDENT" -> OSTLevelOfAssistance.MODIFIED_INDEPENDENT
        "STANDBY_ASSISTANCE" -> OSTLevelOfAssistance.STANDBY_ASSISTANCE
        "MIN_ASSISTANCE" -> OSTLevelOfAssistance.MIN_ASSISTANCE
        "MODERATE_ASSISTANCE" -> OSTLevelOfAssistance.MODERATE_ASSISTANCE
        "MAX_ASSISTANCE" -> OSTLevelOfAssistance.MAX_ASSISTANCE
        "TOTAL_ASSISTANCE" -> OSTLevelOfAssistance.TOTAL_ASSISTANCE
        "UNABLE_TO_PERFORM" -> OSTLevelOfAssistance.UNABLE_TO_PERFORM
        else -> OSTLevelOfAssistance.INDEPENDENT
    }

fun OSTLevelOfAssistance.toIosString(): String = name

fun String.toKmpMeasurementSystem(): OSTMeasurementSystem =
    when (this.uppercase()) {
        "METRIC" -> OSTMeasurementSystem.METRIC
        "IMPERIAL" -> OSTMeasurementSystem.IMPERIAL
        else -> OSTMeasurementSystem.DEFAULT
    }

fun OSTMeasurementSystem.toIosString(): String = name
