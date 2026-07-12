package co.onestep.kmp.uikit.testapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.lifecycle.lifecycleScope
import co.onestep.android.core.OSTIdentificationState
import co.onestep.android.core.OneStep
import co.onestep.android.core.motionLab.OSTMockIMU
import co.onestep.android.core.onError
import co.onestep.android.core.onSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Thin Android shell around the shared test app ([TestAppRoot] in :testAppShared). All UI and
 * flow logic is shared with iOS; this class only wires the native OneStep SDK, deep links, and
 * the browser launch.
 */
class MainActivity : ComponentActivity() {

    private val testApplication get() = application as TestApplication

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleClinicianRedirect(intent)
        val shell = AndroidTestAppShell(
            sdk = testApplication.oneStepSdk,
            setMockIMU = testApplication::setMockIMU,
            openUrl = ::openInBrowser,
            scope = lifecycleScope,
        )
        val prefs = SettingsPrefs(applicationContext)

        setContent {
            // Expose Compose `testTag`s as resource-ids so adb/UIAutomator (and Appium) can
            // find them by id — the Android analogue of iOS `accessibilityIdentifier`.
            Box(
                Modifier
                    .fillMaxSize()
                    .semantics { testTagsAsResourceId = true },
            ) {
                TestAppRoot(shell = shell, prefs = prefs)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleClinicianRedirect(intent)
    }

    /**
     * Forward the clinician web-login deep-link callback (`<scheme>://open/otp?uuid=&otp=`) into
     * the shared UI. Non-matching intents (e.g. LAUNCHER) are filtered by the shared parser.
     */
    private fun handleClinicianRedirect(intent: Intent?) {
        val url = intent?.data?.toString() ?: return
        TestAppDeepLinks.onUrl(url)
    }

    private fun openInBrowser(url: String) {
        // shortcut: plain browser intent (ceiling: no in-app Custom Tab chrome/theming). Upgrade
        // path: androidx.browser CustomTabsIntent, as the production clinician app uses.
        startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}

/** [TestAppShell] backed by the native Android OneStep SDK. */
private class AndroidTestAppShell(
    private val sdk: OneStep?,
    private val setMockIMU: (OSTMockIMU) -> Unit,
    private val openUrl: (String) -> Unit,
    scope: CoroutineScope,
) : TestAppShell {

    override val sdkAvailable: Boolean = sdk != null

    override val identifiedPatientId: StateFlow<String?> =
        sdk?.identificationState
            ?.map { state -> (state as? OSTIdentificationState.Identified)?.patientId?.value }
            ?.stateIn(scope, SharingStarted.Eagerly, null)
            ?: MutableStateFlow(null)

    override suspend fun setPatient(org: Organization, distinctId: String): String? {
        val sdk = sdk ?: return "OneStep SDK not initialized"
        var error: String? = null
        sdk.setPatient(
            apiKey = org.apiKey,
            customerPatientId = distinctId,
            identityVerification = org.signIdentity(distinctId),
        ) {
            withFirstName("UIKit")
            withLastName("Tester")
        }.onSuccess {
            Log.i(TAG, "Connected as $distinctId (${org.name})")
        }.onError { sdkError ->
            Log.e(TAG, "Connect failed: ${sdkError.cause}")
            error = sdkError.cause.message ?: "Connect failed"
        }
        return error
    }

    override fun clearPatient() {
        sdk?.clearPatient()
    }

    override val mockOptions: List<String> = OSTMockIMU.entries.map { it.name }

    override fun setMock(name: String) {
        val mock = OSTMockIMU.entries.firstOrNull { it.name == name } ?: return
        setMockIMU(mock)
    }

    override fun applyBaselineMock() {
        setMockIMU(OSTMockIMU.SUCCESSFUL)
    }

    override fun openWebLogin(url: String, callbackScheme: String) {
        // Android receives the redirect via the MainActivity deep-link intent filter; the
        // callback scheme is only needed by iOS's ASWebAuthenticationSession.
        openUrl.invoke(url)
    }

    private companion object {
        const val TAG = "AndroidTestAppShell"
    }
}
