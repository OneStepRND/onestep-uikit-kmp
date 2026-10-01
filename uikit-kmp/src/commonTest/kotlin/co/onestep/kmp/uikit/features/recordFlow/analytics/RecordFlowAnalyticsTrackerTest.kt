package co.onestep.kmp.uikit.features.recordFlow.analytics

import co.onestep.kmp.sdk.OSTEvent
import co.onestep.kmp.uikit.OSTUIKitAnalyticsHandler
import co.onestep.kmp.uikit.features.recordFlow.analytics.RecordFlowAnalyticsEvents.TagSource
import co.onestep.kmp.uikit.features.tagging.models.Footwear
import co.onestep.kmp.uikit.models.OSTActivityType
import co.onestep.kmp.uikit.models.OSTAssistiveDevice
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

/** The analytics-parity fixes ported from Android uikit `51f4553` and iOS UI kit `07420e9`. */
class RecordFlowAnalyticsTrackerTest {

    private val events = mutableListOf<OSTEvent>()
    private val tracker = RecordFlowAnalyticsTracker(
        object : OSTUIKitAnalyticsHandler {
            override fun onEvent(event: OSTEvent) {
                events += event
            }
        },
    )

    private fun only(): OSTEvent = events.single()

    @Test
    fun perceptionUuid_isLowercased_whateverCaseTheHostPassed() {
        // iOS hosts pass UUID.uuidString, which is uppercase.
        tracker.trackAddTagsScreen(OSTActivityType.WALK, "8F14E45F-CEEA-467A-9A36-DEDD4BEA2543")

        assertEquals("8f14e45f-ceea-467a-9a36-dedd4bea2543", only().properties[AnalyticsProps.PERCEPTION_UUID])
    }

    @Test
    fun lowercasing_leavesOtherPropsAndUuidlessMapsAlone() {
        val noUuid = mapOf(AnalyticsProps.SCREEN_NAME to "Measurement")
        assertSame(noUuid, noUuid.withLowercasePerceptionUuid())

        val mixed = mapOf(
            AnalyticsProps.SCREEN_NAME to "Measurement",
            AnalyticsProps.PERCEPTION_UUID to "ABC-1",
        ).withLowercasePerceptionUuid()
        assertEquals("Measurement", mixed[AnalyticsProps.SCREEN_NAME])
        assertEquals("abc-1", mixed[AnalyticsProps.PERCEPTION_UUID])
    }

    @Test
    fun submitTags_sendsAnExplicitNone_forNonePicks() {
        tracker.trackSubmitTagsClicked(
            activity = OSTActivityType.WALK,
            source = TagSource.PRE_TAG,
            perceptionUuid = null,
            assistiveDevice = OSTAssistiveDevice.NONE,
            footwear = Footwear.NONE,
        )

        val props = only().properties
        assertEquals("None", props[AnalyticsProps.TAGS_ASSISTIVE_DEVICE])
        assertEquals("None", props[AnalyticsProps.TAGS_FOOTWEAR])
    }

    @Test
    fun submitTags_omitsWhatWasNotAsked() {
        tracker.trackSubmitTagsClicked(
            activity = OSTActivityType.WALK,
            source = TagSource.PRE_TAG,
            perceptionUuid = null,
            assistiveDevice = null,
            footwear = null,
        )

        val props = only().properties
        assertNull(props[AnalyticsProps.PERCEPTION_UUID])
        assertNull(props[AnalyticsProps.TAGS_ASSISTIVE_DEVICE])
        assertNull(props[AnalyticsProps.TAGS_FOOTWEAR])
        assertNull(props[AnalyticsProps.TAGS_HANDS_USED_FOR_SUPPORT])
    }

    @Test
    fun submitTags_carriesTheFixedLabels() {
        tracker.trackSubmitTagsClicked(
            activity = OSTActivityType.WALK,
            source = TagSource.POST_MEASUREMENT,
            perceptionUuid = "m-walk-1",
            assistiveDevice = OSTAssistiveDevice.CANE,
            footwear = Footwear.BAREFOOT,
            handsUsedForSupport = "No",
        )

        val props = only().properties
        assertEquals("Cane", props[AnalyticsProps.TAGS_ASSISTIVE_DEVICE])
        assertEquals("Barefoot", props[AnalyticsProps.TAGS_FOOTWEAR])
        assertEquals("No", props[AnalyticsProps.TAGS_HANDS_USED_FOR_SUPPORT])
    }

    @Test
    fun handsUsedForSupport_resolvesTheLocalizedAnswer_toYesNoOrNull() {
        val used = "Used hands"
        val notUsed = "Did not use hands"

        assertEquals("Yes", RecordFlowAnalyticsTracker.handsUsedForSupport(listOf("x", used), used, notUsed))
        assertEquals("No", RecordFlowAnalyticsTracker.handsUsedForSupport(listOf(notUsed), used, notUsed))
        assertNull(RecordFlowAnalyticsTracker.handsUsedForSupport(listOf("x"), used, notUsed))
        assertNull(RecordFlowAnalyticsTracker.handsUsedForSupport(emptyList(), used, notUsed))
    }

    @Test
    fun stillAnalyzing_sendsSteps_notPedometer() {
        tracker.trackStillAnalyzingScreen(
            activity = OSTActivityType.WALK,
            steps = 56,
            seconds = 30,
            perceptionUuid = "M-1",
        )

        val event = only()
        assertEquals("screen: measurement_still_analyzing", event.name)
        assertEquals("56", event.properties[AnalyticsProps.STEPS])
        assertNull(event.properties["pedometer"])
        assertEquals("30", event.properties[AnalyticsProps.MEASUREMENT_SECONDS])
        assertEquals("m-1", event.properties[AnalyticsProps.PERCEPTION_UUID])
    }
}
