package co.onestep.kmp.uikit.testapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.TimeZone

/**
 * Clinician "web login" flow, ported from the legacy clinician app (onestep-sdk-android
 * `NavigatorActivity` + `AuthRepository`). It is INDEPENDENT of the OneStep SDK's patient
 * identification used by the rest of this test app.
 *
 * Mechanism (Google sign-in + OTP happen entirely on the hosted web page, "behind the scenes"):
 *  1. Open `<clinicBase>login?m=2` in the system browser.
 *  2. The clinician signs in with Google on that page; the backend issues a one-time code.
 *  3. The page redirects to `<scheme>://open/otp?uuid=&otp=` — caught by MainActivity's deep link.
 *  4. [exchangeOtp] POSTs the one-time code to the backend and receives a clinician JWT.
 *
 * The single runtime unknown is whether the hosted login page redirects to the scheme this app
 * registers ([SCHEME_PROD]/[SCHEME_DEV]); scheme deep links are not app-verified, so any app that
 * registers the scheme can catch the callback.
 */
object ClinicianWebLogin {

    /**
     * Clinician web base URL (always ends with '/'), derived from the test app's environment.
     *
     * The clinician web app is served from its OWN host (`clinic.onestep.co`) — NOT the SDK API
     * host (`app.onestep.co`). Deriving it from the API base by stripping `api/` produced
     * `app.onestep.co/login?m=2`, which 404s. Production uses the dedicated clinic host; Custom
     * treats the typed URL as the clinic web base verbatim.
     */
    fun clinicBaseUrl(environment: String, customUrl: String): String {
        val raw = when (SDKEnvironment.fromRaw(environment)) {
            SDKEnvironment.PRODUCTION -> AppConstants.CLINICIAN_WEB_BASE_URL
            SDKEnvironment.CUSTOM -> customUrl.ifBlank { AppConstants.CLINICIAN_WEB_BASE_URL }
        }
        val trimmed = raw.trim()
        return if (trimmed.endsWith("/")) trimmed else "$trimmed/"
    }

    /**
     * Custom URL scheme the hosted login page redirects back to. Production uses [SCHEME_PROD];
     * every other environment uses [SCHEME_DEV]. Both are registered as MainActivity deep links.
     */
    fun redirectScheme(environment: String): String =
        when (SDKEnvironment.fromRaw(environment)) {
            SDKEnvironment.PRODUCTION -> SCHEME_PROD
            SDKEnvironment.CUSTOM -> SCHEME_DEV
        }

    /** Open the hosted clinician login page in the system browser. */
    fun launch(context: Context, environment: String, customUrl: String) {
        val url = clinicBaseUrl(environment, customUrl) + "login?m=2"
        // shortcut: plain browser intent (ceiling: no in-app Custom Tab chrome/theming). Upgrade
        // path: androidx.browser CustomTabsIntent, as the production clinician app uses.
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    /** Parse an incoming deep link; returns (uuid, otp) if it is our /otp callback, else null. */
    fun parseRedirect(uri: Uri?): Pair<String, String>? {
        if (uri == null) return null
        if (uri.scheme != SCHEME_PROD && uri.scheme != SCHEME_DEV) return null
        if (uri.path != "/otp") return null
        val uuid = uri.getQueryParameter("uuid") ?: return null
        val otp = uri.getQueryParameter("otp") ?: return null
        return uuid to otp
    }

    /** Exchange the one-time (uuid, otp) for a clinician JWT via the backend. */
    suspend fun exchangeOtp(
        environment: String,
        customUrl: String,
        uuid: String,
        otp: String,
    ): Result<ClinicianSession> = withContext(Dispatchers.IO) {
        runCatching {
            val endpoint = clinicBaseUrl(environment, customUrl) + "api/clinician/v1/auth/login/otp/"
            val payload = JSONObject().apply {
                put("uuid", uuid)
                put("otp", otp)
                put("deviceId", "uikit-kmp-test-app")
                put("flavour", 1)
                put("localeTimezone", TimeZone.getDefault().id)
                put("manufacturer", Build.MANUFACTURER)
                put("model", Build.MODEL)
                put("version", "1.0")
                put("versionCode", 1)
            }.toString()

            val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 30_000
                readTimeout = 30_000
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }
            conn.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val response = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            conn.disconnect()

            check(code in 200..299) { "OTP exchange failed: HTTP $code" }
            val json = JSONObject(response)
            ClinicianSession(
                token = json.getString("token"),
                userUuid = json.optJSONObject("user")?.optString("uuid")?.ifBlank { null },
            )
        }
    }

    private const val SCHEME_PROD = "onestep-prod"
    private const val SCHEME_DEV = "onestep-dev"
}

/** Result of a successful clinician web login. JWT + opaque user uuid only — no PII/PHI (HIPAA). */
data class ClinicianSession(
    val token: String,
    val userUuid: String?,
)

/** UI state for the clinician web-login flow, owned by MainActivity. */
sealed interface ClinicianLoginUiState {
    data object Idle : ClinicianLoginUiState
    data object InProgress : ClinicianLoginUiState
    data object Exchanging : ClinicianLoginUiState
    data class Success(val session: ClinicianSession) : ClinicianLoginUiState
    data class Error(val message: String) : ClinicianLoginUiState
}
