package co.onestep.kmp.uikit.features.web

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.serialization.json.JsonPrimitive

class ParseHostMessageTest {

    @Test
    fun `parses the mini-app dirty message with its fields intact`() {
        val message = parseHostMessage("""{"type":"onestep:dirty","dirty":true}""")

        assertEquals("onestep:dirty", message?.type)
        assertEquals(JsonPrimitive(true), message?.payload?.get("dirty"))
    }

    @Test
    fun `drops anything that is not a JSON object`() {
        assertNull(parseHostMessage("not json"))
        assertNull(parseHostMessage("""["onestep:dirty"]"""))
        assertNull(parseHostMessage("\"onestep:dirty\""))
        // What an Android @JavascriptInterface receives when a page passes an object, not a string.
        assertNull(parseHostMessage("[object Object]"))
    }

    @Test
    fun `drops a message without a usable string type`() {
        assertNull(parseHostMessage("""{"dirty":true}"""))
        assertNull(parseHostMessage("""{"type":""}"""))
        assertNull(parseHostMessage("""{"type":42}"""))
        assertNull(parseHostMessage("""{"type":null}"""))
    }

    @Test
    fun `drops an oversized message rather than parsing it`() {
        val padding = "x".repeat(MAX_HOST_MESSAGE_LENGTH)
        assertNull(parseHostMessage("""{"type":"onestep:dirty","pad":"$padding"}"""))
    }
}
