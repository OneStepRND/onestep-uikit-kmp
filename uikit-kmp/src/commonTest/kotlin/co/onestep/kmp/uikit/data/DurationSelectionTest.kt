package co.onestep.kmp.uikit.data

import co.onestep.kmp.uikit.bridge.PermissionStatus
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTRecordingConfiguration
import co.onestep.kmp.uikit.features.recordFlow.destinations.HallwayDistanceDestination
import co.onestep.kmp.uikit.features.recordFlow.destinations.SelectWalkDurationDestination
import co.onestep.kmp.uikit.features.recordFlow.destinations.StartRecordDestination
import co.onestep.kmp.uikit.features.recordFlow.screens.buildPreRecordDestinations
import co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.staticBalance.ConditionSetupDestination
import co.onestep.kmp.uikit.models.OSTActivityType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Covers the per-condition duration selection added to the Static Balance Test (OS-17175):
 * that the shared duration screen is reached for Static Balance, that the walk flows are
 * untouched, and that the screen's option index resolves against the right option set.
 */
class DurationSelectionTest {

    private fun destinationsFor(
        activityType: OSTActivityType,
        duration: Int?,
    ) = buildPreRecordDestinations(
        config = OSTRecordingConfiguration(
            activityType = activityType,
            duration = duration,
            isCountingDown = true,
            playVoiceOver = false,
        ),
        micStatus = { PermissionStatus.GRANTED },
        showSoundInstructions = { false },
    )

    // --- destination ordering -------------------------------------------------------------

    @Test
    fun staticBalanceAsksForTheDurationRightAfterTheConditionIsConfirmed() {
        val destinations = destinationsFor(
            OSTActivityType.STATIC_BALANCE,
            OSTRecordingConfiguration.STATIC_BALANCE_CONDITION_DURATION_SEC,
        )

        assertEquals(
            listOf(
                ConditionSetupDestination,
                SelectWalkDurationDestination,
                StartRecordDestination,
            ),
            destinations,
        )
    }

    @Test
    fun staticBalanceDurationScreenIsNotGatedOnTheConfiguredDuration() {
        // `staticBalance()` ships a non-null 30s default, which is exactly what the generic
        // gate treats as "already chosen". The screen must appear regardless.
        listOf(null, 0, 10, 30).forEach { duration ->
            val destinations = destinationsFor(OSTActivityType.STATIC_BALANCE, duration)
            assertEquals(
                1,
                destinations.count { it == SelectWalkDurationDestination },
                "duration=$duration",
            )
            assertEquals(
                destinations.indexOf(ConditionSetupDestination) + 1,
                destinations.indexOf(SelectWalkDurationDestination),
                "duration=$duration",
            )
        }
    }

    @Test
    fun walkStillShowsTheDurationScreenOnlyWhenNoDurationIsConfigured() {
        assertEquals(
            listOf(SelectWalkDurationDestination, StartRecordDestination),
            destinationsFor(OSTActivityType.WALK, null),
        )
        assertEquals(
            listOf(StartRecordDestination),
            destinationsFor(OSTActivityType.WALK, 60),
        )
    }

    @Test
    fun sixAndTwoMinuteWalksKeepHallwayDistanceBeforeTheDurationScreen() {
        listOf(OSTActivityType.SIX_MINUTE_WALK, OSTActivityType.TWO_MINUTE_WALK).forEach { type ->
            assertEquals(
                listOf(
                    HallwayDistanceDestination,
                    SelectWalkDurationDestination,
                    StartRecordDestination,
                ),
                destinationsFor(type, null),
                type.name,
            )
        }
    }

    // --- option set / index mapping -------------------------------------------------------

    @Test
    fun staticBalanceOffersTenTwentyThirtySecondsAndNoUnrestrictedOption() {
        assertEquals(
            listOf(10, 20, 30),
            WalkDuration.optionsFor(OSTActivityType.STATIC_BALANCE).map { it.duration },
        )
    }

    @Test
    fun everyOtherActivityKeepsTheWalkOptionSet() {
        assertEquals(
            listOf(60, 180, 300, -1),
            WalkDuration.optionsFor(OSTActivityType.WALK).map { it.duration },
        )
        assertEquals(
            WalkDuration.values,
            WalkDuration.optionsFor(OSTActivityType.SIX_MINUTE_WALK),
        )
    }

    @Test
    fun anIndexResolvesAgainstTheOptionSetTheScreenActuallyShowed() {
        // Index 2 is "5 minutes" for a walk but "30 seconds" for Static Balance — the bug this
        // guards against is resolving one screen's index against the other's list.
        assertEquals(300, WalkDuration.durationByIndex(2, OSTActivityType.WALK)?.duration)
        assertEquals(30, WalkDuration.durationByIndex(2, OSTActivityType.STATIC_BALANCE)?.duration)
    }

    @Test
    fun anOutOfRangeIndexResolvesToNullRatherThanThrowing() {
        // Static Balance has no index 3; the caller keeps the configured default instead of
        // losing the recording to an IndexOutOfBounds.
        assertNull(WalkDuration.durationByIndex(3, OSTActivityType.STATIC_BALANCE))
        assertNull(WalkDuration.durationByIndex(-1, OSTActivityType.WALK))
    }
}
