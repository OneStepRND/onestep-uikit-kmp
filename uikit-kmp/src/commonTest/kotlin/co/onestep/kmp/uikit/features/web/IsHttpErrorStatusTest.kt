package co.onestep.kmp.uikit.features.web

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsHttpErrorStatusTest {

    @Test
    fun `a successful or redirected page is not a failure`() {
        listOf(200, 204, 301, 304).forEach { assertFalse(isHttpErrorStatus(it), "status $it") }
    }

    @Test
    fun `every client and server error status is a failure, not just 404`() {
        listOf(400, 401, 403, 404, 410, 429, 500, 502, 503, 504).forEach {
            assertTrue(isHttpErrorStatus(it), "status $it")
        }
    }
}
