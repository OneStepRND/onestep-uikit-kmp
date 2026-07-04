package co.onestep.kmp.uikit.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import co.onestep.kmp.uikit.features.recordFlow.destinations.ChoosePlacementDestination
import co.onestep.kmp.uikit.features.recordFlow.destinations.SelectWalkDurationDestination
import co.onestep.kmp.uikit.features.recordFlow.destinations.StartRecordDestination
import co.onestep.kmp.uikit.features.recordFlow.screens.ErrorResultDestination
import co.onestep.kmp.uikit.features.recordFlow.screens.RecordingDestination
import co.onestep.kmp.uikit.features.recordFlow.screens.SummaryResultDestination
import co.onestep.kmp.uikit.features.summary.screens.navigation.StsManualReportDestination
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UIktBackStackTest {

    @Test
    fun popRemovesTopEntry() {
        val stack = NavBackStack<NavKey>(SelectWalkDurationDestination, ChoosePlacementDestination)

        assertTrue(stack.pop())

        assertEquals(listOf<NavKey>(SelectWalkDurationDestination), stack.toList())
    }

    @Test
    fun popOnRootIsRefusedSoCallerCanDismiss() {
        val stack = NavBackStack<NavKey>(SelectWalkDurationDestination)

        assertFalse(stack.pop())

        assertEquals(listOf<NavKey>(SelectWalkDurationDestination), stack.toList())
    }

    @Test
    fun popUpToInclusiveRemovesTargetAndEverythingAbove() {
        // STS manual-report submit: [.., Error, StsManualReport] -> pop through Error -> + Summary
        val stack = NavBackStack<NavKey>(
            SelectWalkDurationDestination,
            ErrorResultDestination,
            StsManualReportDestination(uuid = "uuid-1", initialValue = null),
        )

        stack.popUpToInclusive(ErrorResultDestination)
        stack.add(SummaryResultDestination)

        assertEquals(
            listOf<NavKey>(SelectWalkDurationDestination, SummaryResultDestination),
            stack.toList(),
        )
    }

    @Test
    fun popUpToInclusiveIsNoOpWhenTargetIsMissing() {
        val stack = NavBackStack<NavKey>(SelectWalkDurationDestination, ChoosePlacementDestination)

        stack.popUpToInclusive(ErrorResultDestination)

        assertEquals(
            listOf<NavKey>(SelectWalkDurationDestination, ChoosePlacementDestination),
            stack.toList(),
        )
    }

    @Test
    fun popUpToInclusiveOnRootSupportsReplaceRootPattern() {
        // StartRecord "Start" tap when StartRecord is the only entry:
        // popUpTo(StartRecord) { inclusive = true } + navigate(Recording)
        val stack = NavBackStack<NavKey>(StartRecordDestination)

        stack.popUpToInclusive(StartRecordDestination)
        stack.add(RecordingDestination)

        assertEquals(listOf<NavKey>(RecordingDestination), stack.toList())
    }

    @Test
    fun popUpToInclusiveMatchesDataClassKeysByValue() {
        val stack = NavBackStack<NavKey>(
            StartRecordDestination,
            StsManualReportDestination(uuid = "uuid-1", initialValue = 3),
        )

        // A structurally equal key (different instance) must match.
        stack.popUpToInclusive(StsManualReportDestination(uuid = "uuid-1", initialValue = 3))

        assertEquals(listOf<NavKey>(StartRecordDestination), stack.toList())
    }
}
