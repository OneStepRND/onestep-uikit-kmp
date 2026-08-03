package co.onestep.kmp.uikit.features.web

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsLoadableWebUrlTest {

    @Test
    fun `accepts https urls`() {
        assertTrue(isLoadableWebUrl("https://app.onestep.co/summary/abc?x=1#frag"))
        assertTrue(isLoadableWebUrl("HTTPS://app.onestep.co/summary"))
        assertTrue(isLoadableWebUrl("  https://app.onestep.co/summary  "))
    }

    @Test
    fun `rejects script and data urls that would run in the authenticated origin`() {
        assertFalse(isLoadableWebUrl("javascript:alert(document.cookie)"))
        assertFalse(isLoadableWebUrl("data:text/html;base64,PHNjcmlwdD4="))
    }

    @Test
    fun `rejects non-https schemes`() {
        assertFalse(isLoadableWebUrl("http://app.onestep.co/summary"))
        assertFalse(isLoadableWebUrl("file:///etc/passwd"))
        assertFalse(isLoadableWebUrl("onestep://patient/close"))
    }

    @Test
    fun `rejects a missing host`() {
        assertFalse(isLoadableWebUrl("https://"))
        assertFalse(isLoadableWebUrl("https:///path"))
        assertFalse(isLoadableWebUrl("https://?q=1"))
    }

    @Test
    fun `rejects embedded whitespace and control characters`() {
        assertFalse(isLoadableWebUrl("https://app.onestep.co/a b"))
        assertFalse(isLoadableWebUrl("https://app.onestep.co/\njavascript:alert(1)"))
    }

    @Test
    fun `rejects blank`() {
        assertFalse(isLoadableWebUrl(""))
        assertFalse(isLoadableWebUrl("   "))
    }
}

class EnhanceOSTSummaryUrlTest {

    @Test
    fun `adds the constant host context params`() {
        val out = enhanceOSTSummaryUrl("https://example.com/summary", origin = "pa_recorder")
        assertEquals(
            "https://example.com/summary" +
                "?embedded=true&platform=mobile&overlay_close=true&origin=pa_recorder",
            out,
        )
    }

    @Test
    fun `merges with an existing query without a second question mark`() {
        val out = enhanceOSTSummaryUrl(
            "https://example.com/summary?foo=bar",
            origin = "ca_carelog",
        )
        assertTrue(out.startsWith("https://example.com/summary?foo=bar&"))
        assertEquals(1, out.count { it == '?' })
    }

    @Test
    fun `added params win over colliding existing ones`() {
        val out = enhanceOSTSummaryUrl(
            "https://example.com/s?embedded=false&platform=desktop&keep=1",
            origin = "pa_recorder",
        )
        assertTrue(out.contains("keep=1"))
        assertTrue(out.contains("embedded=true"))
        assertTrue(out.contains("platform=mobile"))
        assertFalse(out.contains("embedded=false"))
        assertFalse(out.contains("platform=desktop"))
    }

    @Test
    fun `keeps the fragment at the end`() {
        val out = enhanceOSTSummaryUrl("https://example.com/s#section", origin = "pa_recorder")
        assertTrue(out.endsWith("#section"))
        assertTrue(out.contains("?embedded=true"))
    }

    @Test
    fun `emits supported locales and drops unsupported ones`() {
        assertTrue(
            enhanceOSTSummaryUrl("https://e.com/s", origin = "o", language = "he")
                .contains("locale=he"),
        )
        assertFalse(
            enhanceOSTSummaryUrl("https://e.com/s", origin = "o", language = "ro")
                .contains("locale="),
        )
        assertFalse(
            enhanceOSTSummaryUrl("https://e.com/s", origin = "o", language = null)
                .contains("locale="),
        )
    }

    @Test
    fun `normalizes the legacy hebrew code`() {
        assertTrue(
            enhanceOSTSummaryUrl("https://e.com/s", origin = "o", language = "iw")
                .contains("locale=he"),
        )
    }

    @Test
    fun `omits a blank unit system`() {
        assertFalse(
            enhanceOSTSummaryUrl("https://e.com/s", origin = "o", unitSystem = "  ")
                .contains("unitSystem"),
        )
        assertTrue(
            enhanceOSTSummaryUrl("https://e.com/s", origin = "o", unitSystem = "metric")
                .contains("unitSystem=metric"),
        )
    }

    @Test
    fun `overlay close reflects who draws the close affordance`() {
        assertTrue(
            enhanceOSTSummaryUrl("https://e.com/s", origin = "o", overlayClose = false)
                .contains("overlay_close=false"),
        )
    }

    @Test
    fun `returns a blank url untouched`() {
        assertEquals("", enhanceOSTSummaryUrl("", origin = "o"))
        assertEquals("   ", enhanceOSTSummaryUrl("   ", origin = "o"))
    }
}
