package co.onestep.kmp.uikit.features.web

import kotlin.test.Test
import kotlin.test.assertTrue

class InjectedSafeAreaJsTest {

    private val insets = OSTWebSafeAreaInsets(top = 24f, bottom = 16f, left = 0f, right = 0f)

    // REGRESSION GUARD. Android evaluates this script while the next page's document may still have
    // no root element; an unguarded `document.documentElement.style` threw in the page's console on
    // repeat opens of the intake mini-app. The CSS variables must wait for DOMContentLoaded instead.
    @Test
    fun `waits for the root element instead of reading style off a null one`() {
        val script = injectedSafeAreaJs(insets)

        val guard = script.indexOf("if (document.documentElement)")
        val deferral = script.indexOf("document.addEventListener('DOMContentLoaded', apply, { once: true })")
        assertTrue(guard >= 0, "no documentElement guard in:\n$script")
        assertTrue(deferral > guard, "no DOMContentLoaded deferral after the guard in:\n$script")
        assertTrue(
            script.indexOf("document.documentElement.style") < guard,
            "the style read must live in the deferred apply(), not run before the guard:\n$script",
        )
    }

    @Test
    fun `still publishes the insets object synchronously before any deferral`() {
        val script = injectedSafeAreaJs(insets)

        assertTrue(
            script.indexOf("window.OneStep.safeAreaInsets =") < script.indexOf("DOMContentLoaded"),
            "pages read window.OneStep.safeAreaInsets at startup, so it cannot wait:\n$script",
        )
        assertTrue(script.contains("'--safe-area-inset-top', '24.0px'"), script)
    }
}
