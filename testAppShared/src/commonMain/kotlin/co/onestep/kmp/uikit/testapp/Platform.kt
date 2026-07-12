package co.onestep.kmp.uikit.testapp

/** HMAC-SHA256 of [message] keyed by [secret] (both UTF-8). Used for test-only identity signing. */
expect fun hmacSha256(secret: ByteArray, message: ByteArray): ByteArray

internal fun ByteArray.toHexString(): String =
    joinToString("") { byte ->
        val v = byte.toInt() and 0xFF
        val hex = v.toString(16)
        if (hex.length == 1) "0$hex" else hex
    }

/** Device descriptors for the clinician OTP-exchange payload (no PII). */
expect val deviceManufacturer: String
expect val deviceModel: String

/** IANA timezone id, e.g. "America/New_York". */
expect fun localeTimezoneId(): String

/**
 * Minimal JSON-over-HTTP POST used by the clinician OTP exchange.
 * shortcut: raw platform HTTP (HttpURLConnection / NSURLSession) — ceiling: no retries/interceptors;
 * upgrade path: ktor-client if the test app ever needs more than this single call.
 */
expect suspend fun httpPostJson(url: String, jsonBody: String): HttpTextResponse

data class HttpTextResponse(val statusCode: Int, val body: String)

/**
 * Test-app settings persistence (SharedPreferences / NSUserDefaults), sharing the same keys both
 * native test apps historically used (`sdk_environment`, `sdk_customURL`, `sdk_orgName`,
 * `sdk_distinctId`) so an update keeps existing selections.
 */
interface SettingsPrefs {
    var environment: String
    var customUrl: String
    var orgName: String
    var distinctId: String
}

/** Mirrors iOS `SDKEnvironment` (Production / Custom URL). */
enum class SDKEnvironment(val rawValue: String) {
    PRODUCTION("Production"),
    CUSTOM("Custom URL");

    companion object {
        fun fromRaw(raw: String?): SDKEnvironment =
            entries.firstOrNull { it.rawValue == raw } ?: PRODUCTION
    }
}
