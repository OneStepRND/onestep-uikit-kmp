package co.onestep.kmp.uikit.mapper

import co.onestep.kmp.uikit.models.OSTAssistiveDevice as KmpAssistiveDevice
import co.onestep.kmp.uikit.models.OSTLevelOfAssistance as KmpLevelOfAssistance
import co.onestep.kmp.uikit.models.OSTMeasurementSystem as KmpMeasurementSystem
import co.onestep.android.core.motionLab.OSTAssistiveDevice as CoreAssistiveDevice
import co.onestep.android.core.motionLab.OSTLevelOfAssistance as CoreLevelOfAssistance
import co.onestep.android.core.motionLab.OSTMeasurementSystem as CoreMeasurementSystem

fun CoreAssistiveDevice.toKmp(): KmpAssistiveDevice =
    when (this) {
        CoreAssistiveDevice.NONE -> KmpAssistiveDevice.NONE
        CoreAssistiveDevice.WALKER -> KmpAssistiveDevice.WALKER
        CoreAssistiveDevice.ROLLATOR -> KmpAssistiveDevice.ROLLATOR
        CoreAssistiveDevice.CANE -> KmpAssistiveDevice.CANE
        CoreAssistiveDevice.CRUTCH_DOUBLE -> KmpAssistiveDevice.CRUTCH_DOUBLE
        CoreAssistiveDevice.CRUTCH_SINGLE -> KmpAssistiveDevice.CRUTCH_SINGLE
    }

fun KmpAssistiveDevice.toCore(): CoreAssistiveDevice =
    when (this) {
        KmpAssistiveDevice.NONE -> CoreAssistiveDevice.NONE
        KmpAssistiveDevice.WALKER -> CoreAssistiveDevice.WALKER
        KmpAssistiveDevice.ROLLATOR -> CoreAssistiveDevice.ROLLATOR
        KmpAssistiveDevice.CANE -> CoreAssistiveDevice.CANE
        KmpAssistiveDevice.CRUTCH_DOUBLE -> CoreAssistiveDevice.CRUTCH_DOUBLE
        KmpAssistiveDevice.CRUTCH_SINGLE -> CoreAssistiveDevice.CRUTCH_SINGLE
    }

fun CoreLevelOfAssistance.toKmp(): KmpLevelOfAssistance =
    when (this) {
        CoreLevelOfAssistance.INDEPENDENT -> KmpLevelOfAssistance.INDEPENDENT
        CoreLevelOfAssistance.MODIFIED_INDEPENDENT -> KmpLevelOfAssistance.MODIFIED_INDEPENDENT
        CoreLevelOfAssistance.STANDBY_ASSISTANCE -> KmpLevelOfAssistance.STANDBY_ASSISTANCE
        CoreLevelOfAssistance.MIN_ASSISTANCE -> KmpLevelOfAssistance.MIN_ASSISTANCE
        CoreLevelOfAssistance.MODERATE_ASSISTANCE -> KmpLevelOfAssistance.MODERATE_ASSISTANCE
        CoreLevelOfAssistance.MAX_ASSISTANCE -> KmpLevelOfAssistance.MAX_ASSISTANCE
        CoreLevelOfAssistance.TOTAL_ASSISTANCE -> KmpLevelOfAssistance.TOTAL_ASSISTANCE
        CoreLevelOfAssistance.UNABLE_TO_PERFORM -> KmpLevelOfAssistance.UNABLE_TO_PERFORM
    }

fun KmpLevelOfAssistance.toCore(): CoreLevelOfAssistance =
    when (this) {
        KmpLevelOfAssistance.INDEPENDENT -> CoreLevelOfAssistance.INDEPENDENT
        KmpLevelOfAssistance.MODIFIED_INDEPENDENT -> CoreLevelOfAssistance.MODIFIED_INDEPENDENT
        KmpLevelOfAssistance.STANDBY_ASSISTANCE -> CoreLevelOfAssistance.STANDBY_ASSISTANCE
        KmpLevelOfAssistance.MIN_ASSISTANCE -> CoreLevelOfAssistance.MIN_ASSISTANCE
        KmpLevelOfAssistance.MODERATE_ASSISTANCE -> CoreLevelOfAssistance.MODERATE_ASSISTANCE
        KmpLevelOfAssistance.MAX_ASSISTANCE -> CoreLevelOfAssistance.MAX_ASSISTANCE
        KmpLevelOfAssistance.TOTAL_ASSISTANCE -> CoreLevelOfAssistance.TOTAL_ASSISTANCE
        KmpLevelOfAssistance.UNABLE_TO_PERFORM -> CoreLevelOfAssistance.UNABLE_TO_PERFORM
    }

fun CoreMeasurementSystem.toKmp(): KmpMeasurementSystem =
    when (this) {
        CoreMeasurementSystem.METRIC -> KmpMeasurementSystem.METRIC
        CoreMeasurementSystem.IMPERIAL -> KmpMeasurementSystem.IMPERIAL
        CoreMeasurementSystem.DEFAULT -> KmpMeasurementSystem.DEFAULT
    }

fun KmpMeasurementSystem.toCore(): CoreMeasurementSystem =
    when (this) {
        KmpMeasurementSystem.METRIC -> CoreMeasurementSystem.METRIC
        KmpMeasurementSystem.IMPERIAL -> CoreMeasurementSystem.IMPERIAL
        KmpMeasurementSystem.DEFAULT -> CoreMeasurementSystem.DEFAULT
    }
