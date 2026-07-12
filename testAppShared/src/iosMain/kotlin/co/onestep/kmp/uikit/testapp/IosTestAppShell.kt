package co.onestep.kmp.uikit.testapp

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * What the Swift app implements: completion-handler methods against the native OneStepSDK.
 * (Swift cannot construct Kotlin StateFlows or implement suspend members directly, so the
 * [IosTestAppShell] adapter below owns those and delegates the native calls here.)
 */
interface IosTestAppShellDelegate {
    val sdkAvailable: Boolean

    /**
     * Natively initialize-if-needed + `setPatient` as [distinctId] under [org] (sign identity via
     * [Organization.signIdentity]). Call [completion] with null on success or an error message.
     */
    fun setPatient(org: Organization, distinctId: String, completion: (String?) -> Unit)

    fun clearPatient()

    /** Bundled mock-recording names, first entry = the "successful" default. */
    val mockOptions: List<String>

    fun setMock(name: String)
    fun applyBaselineMock()

    /** Start ASWebAuthenticationSession for [url]/[callbackScheme]; on callback, push the URL into
     * [TestAppDeepLinks.onUrl]. */
    fun openWebLogin(url: String, callbackScheme: String)
}

/** [TestAppShell] adapter over the Swift [IosTestAppShellDelegate]. */
class IosTestAppShell(
    private val delegate: IosTestAppShellDelegate,
) : TestAppShell {

    private val identified = MutableStateFlow<String?>(null)

    override val sdkAvailable: Boolean get() = delegate.sdkAvailable
    override val identifiedPatientId: StateFlow<String?> = identified

    /**
     * Swift-side override for identification changes that don't come through [setPatient] —
     * e.g. auto-login at launch from stored credentials, or auth-lost.
     */
    fun setIdentifiedPatient(patientId: String?) {
        identified.value = patientId
    }

    override suspend fun setPatient(org: Organization, distinctId: String): String? =
        suspendCancellableCoroutine { continuation ->
            delegate.setPatient(org, distinctId) { error ->
                if (error == null) identified.value = distinctId
                continuation.resume(error)
            }
        }

    override fun clearPatient() {
        delegate.clearPatient()
        identified.value = null
    }

    override val mockOptions: List<String> get() = delegate.mockOptions
    override fun setMock(name: String) = delegate.setMock(name)
    override fun applyBaselineMock() = delegate.applyBaselineMock()
    override fun openWebLogin(url: String, callbackScheme: String) =
        delegate.openWebLogin(url, callbackScheme)
}
