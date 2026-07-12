package co.onestep.kmp.uikit.testapp

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * The platform shell behind the shared test-app UI: everything that must talk to the *native*
 * OneStep SDK (Android `co.onestep.android:core`, iOS `OneStepSDK`) and platform services the
 * common code cannot reach. Implemented by `MainActivity` (Android) and the Swift app (iOS).
 */
interface TestAppShell {
    /** False when native SDK initialization failed at app start (shows the init-failed screen). */
    val sdkAvailable: Boolean

    /** Currently identified patient id, or null while unidentified. */
    val identifiedPatientId: StateFlow<String?>

    /**
     * Identify the SDK as [distinctId] under [org] (signing the identity client-side — test app
     * only). Returns null on success, or a user-displayable error message.
     */
    suspend fun setPatient(org: Organization, distinctId: String): String?

    /** Drop the identified patient (logout). */
    fun clearPatient()

    /**
     * Platform mock-recording options, first entry = the "successful" default. Android: the native
     * `OSTMockIMU` entries; iOS: the bundled mock-recording names.
     */
    val mockOptions: List<String>

    /** Select the mock used by the next recording ([mockOptions] entry). */
    fun setMock(name: String)

    /** (Re-)apply the baseline "successful" mock — MotionLab is per patient context, so this must
     * run after each identification. */
    fun applyBaselineMock()

    /**
     * Open the clinician web-login page. Android opens [url] in the browser and receives the
     * `<scheme>://open/otp` redirect as a deep link; iOS uses ASWebAuthenticationSession with
     * [callbackScheme] and pushes the callback into [TestAppDeepLinks].
     */
    fun openWebLogin(url: String, callbackScheme: String)
}

/**
 * Deep-link handoff from the platform shells into the shared UI (clinician web-login
 * `<scheme>://open/otp?...` callbacks). Shells push the URL; [TestAppRoot] consumes and clears it.
 */
object TestAppDeepLinks {
    val events: MutableStateFlow<String?> = MutableStateFlow(null)

    fun onUrl(url: String) {
        events.value = url
    }

    fun consume() {
        events.value = null
    }
}
