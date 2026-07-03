package co.onestep.kmp.uikit.features.recordFlow

import co.onestep.kmp.uikit.models.OSTActivityType
import co.onestep.kmp.uikit.models.OSTAnalyserError
import co.onestep.kmp.uikit.models.OSTAnalysisError
import co.onestep.kmp.uikit.models.OSTMotionMeasurement
import co.onestep.kmp.uikit.models.OSTResultState
import co.onestep.kmp.uikit.models.toAnalysisError

/**
 * The outcome the record flow should present for a completed analysis.
 *
 * Ported from the Android `uikit` module's `ResultHandler`
 * (`features/recordFlow/ResultHandler.kt`). The KMP variant returns a value instead of calling a
 * navigation lambda per branch, so the NavGraph decides how to route each outcome.
 */
sealed interface RecordFlowOutcome {
    /** FULL_ANALYSIS — proceed to the measurement summary. */
    data class Summary(val measurement: OSTMotionMeasurement) : RecordFlowOutcome

    /** Empty/partial walk (or dual-task) analysis that still captured steps. */
    data object EmptyAnalysisWithSteps : RecordFlowOutcome

    /** Show a specific error screen. */
    data class Error(val error: RecordFlowError) : RecordFlowOutcome
}

/**
 * Replicates the routing logic of uikit's `ResultHandler` for the KMP record flow.
 */
internal object ResultHandler {

    /**
     * Maps a completed [motionMeasurement] to a [RecordFlowOutcome].
     *
     * Mirrors `ResultHandler.handleMeasurementResult` in uikit: FULL_ANALYSIS -> summary;
     * EMPTY/PARTIAL -> the EmptyAnalysisWithSteps screen for walk/dual-task with steps, otherwise
     * the per-(analysisError x activityType) error screen; a null result state -> the general error.
     */
    fun handleMeasurementResult(motionMeasurement: OSTMotionMeasurement?): RecordFlowOutcome =
        when (motionMeasurement?.resultState) {
            OSTResultState.FULL_ANALYSIS -> RecordFlowOutcome.Summary(motionMeasurement)

            OSTResultState.EMPTY_ANALYSIS, OSTResultState.PARTIAL_ANALYSIS -> {
                val hasSteps = (motionMeasurement.metadata.steps ?: 0) > 0
                val requiresSteps =
                    motionMeasurement.type in
                        setOf(
                            OSTActivityType.WALK,
                            OSTActivityType.DUAL_TASK_WALK_SUBTRACT,
                        )
                val shouldDisplayEmptyScreenWithSteps = requiresSteps && hasSteps
                if (shouldDisplayEmptyScreenWithSteps) {
                    RecordFlowOutcome.EmptyAnalysisWithSteps
                } else {
                    RecordFlowOutcome.Error(
                        analysisError(
                            motionMeasurement.error?.code.toAnalysisError(),
                            motionMeasurement.type,
                        ),
                    )
                }
            }

            null -> RecordFlowOutcome.Error(RecordFlowError.General)
        }

    /**
     * Maps a technical analyser error to a [RecordFlowError].
     *
     * Mirrors `ResultHandler.onAnalyseError` in uikit ("not getting any result from the lab").
     */
    fun onAnalyseError(
        analyserError: OSTAnalyserError? = null,
        activityType: OSTActivityType? = null,
        networkStatus: Boolean,
    ): RecordFlowError =
        when {
            analyserError is OSTAnalyserError.TooShort ->
                when (activityType) {
                    OSTActivityType.WALK -> RecordFlowError.WalkShort
                    OSTActivityType.STS -> RecordFlowError.StsShort
                    OSTActivityType.TUG -> RecordFlowError.TugShort
                    OSTActivityType.ROM_KNEE_FLEX, OSTActivityType.ROM_KNEE_EXT ->
                        RecordFlowError.RomShort
                    OSTActivityType.BALANCE_TEST -> RecordFlowError.StaticBalanceShort
                    else -> RecordFlowError.WalkShort
                }

            analyserError is OSTAnalyserError.Timeout -> RecordFlowError.Timeout
            analyserError is OSTAnalyserError.General -> RecordFlowError.General
            analyserError is OSTAnalyserError.ServerError -> RecordFlowError.ServerIssue
            analyserError is OSTAnalyserError.NetworkError || !networkStatus ->
                RecordFlowError.Connectivity

            else -> RecordFlowError.General
        }

    private fun analysisError(
        analysisError: OSTAnalysisError,
        activityType: OSTActivityType,
    ): RecordFlowError =
        when (analysisError) {
            OSTAnalysisError.Static ->
                when (activityType) {
                    OSTActivityType.WALK -> RecordFlowError.StaticWalk
                    OSTActivityType.STS -> RecordFlowError.StaticSts
                    OSTActivityType.TUG -> RecordFlowError.StaticTug
                    OSTActivityType.ROM_KNEE_FLEX, OSTActivityType.ROM_KNEE_EXT ->
                        RecordFlowError.StaticRom
                    else -> RecordFlowError.StaticWalk
                }

            OSTAnalysisError.Position ->
                when (activityType) {
                    OSTActivityType.WALK -> RecordFlowError.WalkPosition
                    OSTActivityType.STS -> RecordFlowError.StsPosition
                    OSTActivityType.TUG -> RecordFlowError.TugPosition
                    OSTActivityType.ROM_KNEE_FLEX, OSTActivityType.ROM_KNEE_EXT ->
                        RecordFlowError.RomPosition
                    else -> RecordFlowError.WalkPosition
                }

            OSTAnalysisError.Short ->
                when (activityType) {
                    OSTActivityType.WALK -> RecordFlowError.WalkShort
                    OSTActivityType.STS -> RecordFlowError.StsShort
                    OSTActivityType.TUG -> RecordFlowError.TugShort
                    OSTActivityType.ROM_KNEE_FLEX, OSTActivityType.ROM_KNEE_EXT ->
                        RecordFlowError.RomShort
                    OSTActivityType.BALANCE_TEST -> RecordFlowError.StaticBalanceShort
                    else -> RecordFlowError.WalkShort
                }

            OSTAnalysisError.Curvy -> RecordFlowError.Curvy
            OSTAnalysisError.NonRepetitiveMovement -> RecordFlowError.WalkNonRepetitive

            else -> RecordFlowError.General
        }
}
