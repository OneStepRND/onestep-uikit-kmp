package co.onestep.kmp.uikit.testapp

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * Clinician "web login" flow, ported from the legacy clinician app (onestep-sdk-android
 * `NavigatorActivity` + `AuthRepository`). It is INDEPENDENT of the OneStep SDK's patient
 * identification used by the rest of this test app.
 *
 * Mechanism (Google sign-in + OTP happen entirely on the hosted web page, "behind the scenes"):
 *  1. Open `<clinicBase>login?m=2` in the system browser ([loginUrl] via [TestAppShell.openUrl]).
 *  2. The clinician signs in with Google on that page; the backend issues a one-time code.
 *  3. The page redirects to `<scheme>://open/otp?uuid=&otp=` — caught by the platform shell's
 *     deep-link handling and pushed into [TestAppDeepLinks].
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
     * host (`app.onestep.co`). Production uses the dedicated clinic host; Custom treats the typed
     * URL as the clinic web base verbatim.
     */
    fun clinicBaseUrl(environment: String, customUrl: String): String {
        val raw = when (SDKEnvironment.fromRaw(environment)) {
            SDKEnvironment.PRODUCTION -> AppConstants.CLINICIAN_WEB_BASE_URL
            SDKEnvironment.CUSTOM -> customUrl.ifBlank { AppConstants.CLINICIAN_WEB_BASE_URL }
        }
        val trimmed = raw.trim()
        return if (trimmed.endsWith("/")) trimmed else "$trimmed/"
    }

    /** The hosted clinician login page for [environment]; open via [TestAppShell.openUrl]. */
    fun loginUrl(environment: String, customUrl: String): String =
        clinicBaseUrl(environment, customUrl) + "login?m=2"

    /**
     * Custom URL scheme the hosted login page redirects back to. Production uses [SCHEME_PROD];
     * every other environment uses [SCHEME_DEV]. Both are registered as deep links by both shells.
     */
    fun redirectScheme(environment: String): String =
        when (SDKEnvironment.fromRaw(environment)) {
            SDKEnvironment.PRODUCTION -> SCHEME_PROD
            SDKEnvironment.CUSTOM -> SCHEME_DEV
        }

    /**
     * Parse an incoming deep-link URL; returns (uuid, otp) if it is our `/otp` callback, else null.
     * String-based (not platform Uri) so it runs in commonMain — the URL shape is fixed:
     * `<scheme>://open/otp?uuid=...&otp=...`.
     */
    fun parseRedirect(url: String?): Pair<String, String>? {
        if (url == null) return null
        val scheme = url.substringBefore("://", missingDelimiterValue = "")
        if (scheme != SCHEME_PROD && scheme != SCHEME_DEV) return null
        val afterScheme = url.substringAfter("://")
        val path = afterScheme.substringBefore("?").substringAfter("/", missingDelimiterValue = "")
        if (path.trimEnd('/') != "otp") return null
        val query = afterScheme.substringAfter("?", missingDelimiterValue = "")
        val params = query.split("&").mapNotNull { param ->
            val key = param.substringBefore("=")
            val value = param.substringAfter("=", missingDelimiterValue = "")
            if (key.isNotEmpty()) key to value else null
        }.toMap()
        val uuid = params["uuid"]?.takeIf { it.isNotEmpty() } ?: return null
        val otp = params["otp"]?.takeIf { it.isNotEmpty() } ?: return null
        return uuid to otp
    }

    /** Exchange the one-time (uuid, otp) for a clinician JWT via the backend. */
    suspend fun exchangeOtp(
        environment: String,
        customUrl: String,
        uuid: String,
        otp: String,
    ): Result<ClinicianSession> = runCatching {
        val endpoint = clinicBaseUrl(environment, customUrl) + "api/clinician/v1/auth/login/otp/"
        val payload = buildJsonObject {
            put("uuid", uuid)
            put("otp", otp)
            put("deviceId", "uikit-kmp-test-app")
            put("flavour", 1)
            put("localeTimezone", localeTimezoneId())
            put("manufacturer", deviceManufacturer)
            put("model", deviceModel)
            put("version", "1.0")
            put("versionCode", 1)
        }.toString()

        val response = httpPostJson(endpoint, payload)
        check(response.statusCode in 200..299) { "OTP exchange failed: HTTP ${response.statusCode}" }
        val json = Json.parseToJsonElement(response.body).jsonObject
        ClinicianSession(
            token = json.getValue("token").jsonPrimitive.content,
            userUuid = json["user"]?.jsonObject?.get("uuid")?.jsonPrimitive?.content?.ifBlank { null },
        )
    }

    private const val SCHEME_PROD = "onestep-prod"
    private const val SCHEME_DEV = "onestep-dev"
}

/** Result of a successful clinician web login. JWT + opaque user uuid only — no PII/PHI (HIPAA). */
data class ClinicianSession(
    val token: String,
    val userUuid: String?,
)

/** UI state for the clinician web-login flow, owned by [TestAppRoot]. */
sealed interface ClinicianLoginUiState {
    data object Idle : ClinicianLoginUiState
    data object InProgress : ClinicianLoginUiState
    data object Exchanging : ClinicianLoginUiState
    data class Success(val session: ClinicianSession) : ClinicianLoginUiState
    data class Error(val message: String) : ClinicianLoginUiState
}
