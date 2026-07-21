package co.onestep.kmp.sdk

import co.onestep.kmp.uikit.models.OSTMotionMeasurement

/**
 * MotionLab product surface for a patient scope.
 *
 * Obtained via [OSTPatientScope.getMotionLab]. Mirror of the native
 * `co.onestep.android.core.motionLab.OSTMotionLab` reached through `scope.getMotionLab()`; the
 * returned instance is bound to the patient of the enclosing [OneStep.withPatient] block.
 */
interface OSTMotionLab {

    /**
     * Sets the display unit system for this patient's measurements.
     *
     * Native: `OSTMotionLab.setMeasurementUnits(OSTMeasurementSystem)`.
     *
     * @param system The unit system to apply.
     * @return [OSTResult.Success] once applied, [OSTResult.Error] otherwise.
     */
    suspend fun setMeasurementUnits(system: OSTMeasurementSystem): OSTResult<Unit>

    /**
     * Reads one analyzed measurement from the SDK store by id.
     *
     * The returned [OSTMotionMeasurement] carries `summaryUrl` for opening the web summary.
     * Native: `OSTMotionLab.readSingleMotionMeasurement(id)` → `OSTResult<OSTMotionMeasurement>`.
     *
     * @param measurementId The measurement UUID.
     * @return [OSTResult.Success] wrapping the measurement, [OSTResult.Error] if not found / on failure.
     */
    suspend fun readSingleMotionMeasurement(measurementId: String): OSTResult<OSTMotionMeasurement>
}
