package co.onestep.kmp.uikit.features.audio

import co.onestep.kmp.uikit.bridge.PlatformAudioPlayer
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The Get Ready countdown starts its digits from `playAudio`'s `onStarted`, so the voice and the
 * digits begin together instead of the voice trailing by the clip's load time. That makes the
 * callback's contract load-bearing in both directions:
 * - it must always arrive when nothing was cancelled, or the countdown never starts;
 * - it must never arrive after a cancel, or "Start now" during the load would restart a countdown
 *   the clinician already skipped.
 *
 * Runs against the real [PlatformAudioPlayer] (MediaPlayer is inert on the JVM host) and the real
 * packaged clips.
 */
class PlatformAudioPlayerAdapterTest {

    @Test
    fun onStartedArrivesAfterAPackagedClipIsHandedToThePlayer() = runTest {
        val adapter = PlatformAudioPlayerAdapter(
            PlatformAudioPlayer(),
            CoroutineScope(UnconfinedTestDispatcher(testScheduler) + SupervisorJob()),
        )
        val started = CompletableDeferred<Unit>()

        adapter.playAudio("countdown_from_10") { started.complete(Unit) }

        withTimeout(10_000) { started.await() }
    }

    @Test
    fun onStartedStillArrivesWhenTheClipCannotBeRead() = runTest {
        val adapter = PlatformAudioPlayerAdapter(
            PlatformAudioPlayer(),
            CoroutineScope(UnconfinedTestDispatcher(testScheduler) + SupervisorJob()),
        )
        val started = CompletableDeferred<Unit>()

        adapter.playAudio("no_such_clip") { started.complete(Unit) }

        withTimeout(10_000) { started.await() }
    }

    @Test
    fun onStartedArrivesWhenVoiceOverIsOffButIsNeverCalledInline() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val adapter = PlatformAudioPlayerAdapter(PlatformAudioPlayer(), CoroutineScope(dispatcher + SupervisorJob()))
        adapter.enable(false)
        var calls = 0

        adapter.playAudio("countdown_from_10") { calls++ }
        assertEquals(0, calls, "onStarted must be dispatched, not called inline")

        testScheduler.advanceUntilIdle()
        assertEquals(1, calls)
    }

    @Test
    fun aCancelledLoadNeverReportsAStart() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val adapter = PlatformAudioPlayerAdapter(PlatformAudioPlayer(), CoroutineScope(dispatcher + SupervisorJob()))
        var started = false

        adapter.playAudio("countdown_from_10") { started = true }
        adapter.stopCurrentAudio() // "Start now", or leaving Get Ready, before the clip loads
        testScheduler.advanceUntilIdle()

        assertFalse(started)
        assertFalse(adapter.isPlaying())
    }

    @Test
    fun aNewerClipCancelsTheOlderOnesStart() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val adapter = PlatformAudioPlayerAdapter(PlatformAudioPlayer(), CoroutineScope(dispatcher + SupervisorJob()))
        val starts = mutableListOf<String>()
        val newer = CompletableDeferred<Unit>()

        adapter.playAudio("countdown_from_10") { starts += "older" }
        adapter.playAudio("countdown_from_5") {
            starts += "newer"
            newer.complete(Unit)
        }
        testScheduler.advanceUntilIdle()
        withTimeout(10_000) { newer.await() }

        assertTrue("older" !in starts, "a superseded load reported a start: $starts")
        assertEquals(listOf("newer"), starts)
    }
}
