package co.onestep.kmp.uikit.testapp

import android.content.Context
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.TimeZone
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

actual fun hmacSha256(secret: ByteArray, message: ByteArray): ByteArray {
    val algorithm = "HmacSHA256"
    val mac = Mac.getInstance(algorithm)
    mac.init(SecretKeySpec(secret, algorithm))
    return mac.doFinal(message)
}

actual val deviceManufacturer: String get() = Build.MANUFACTURER
actual val deviceModel: String get() = Build.MODEL

actual fun localeTimezoneId(): String = TimeZone.getDefault().id

actual suspend fun httpPostJson(url: String, jsonBody: String): HttpTextResponse =
    withContext(Dispatchers.IO) {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 30_000
            readTimeout = 30_000
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
        }
        try {
            conn.outputStream.use { it.write(jsonBody.toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            HttpTextResponse(statusCode = code, body = body)
        } finally {
            conn.disconnect()
        }
    }

/** SharedPreferences-backed [SettingsPrefs], same keys/file as the original androidTestApp. */
fun SettingsPrefs(context: Context): SettingsPrefs = AndroidSettingsPrefs(context)

private class AndroidSettingsPrefs(context: Context) : SettingsPrefs {
    private val prefs =
        context.applicationContext.getSharedPreferences("sdk_settings", Context.MODE_PRIVATE)

    override var environment: String
        get() = prefs.getString(KEY_ENVIRONMENT, SDKEnvironment.PRODUCTION.rawValue)
            ?: SDKEnvironment.PRODUCTION.rawValue
        set(value) = prefs.edit().putString(KEY_ENVIRONMENT, value).apply()

    override var customUrl: String
        get() = prefs.getString(KEY_CUSTOM_URL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CUSTOM_URL, value).apply()

    override var orgName: String
        get() = prefs.getString(KEY_ORG_NAME, Organizations.default.name) ?: Organizations.default.name
        set(value) = prefs.edit().putString(KEY_ORG_NAME, value).apply()

    override var distinctId: String
        get() = prefs.getString(KEY_DISTINCT_ID, AppConstants.DEFAULT_DISTINCT_ID)
            ?: AppConstants.DEFAULT_DISTINCT_ID
        set(value) = prefs.edit().putString(KEY_DISTINCT_ID, value).apply()

    private companion object {
        const val KEY_ENVIRONMENT = "sdk_environment"
        const val KEY_CUSTOM_URL = "sdk_customURL"
        const val KEY_ORG_NAME = "sdk_orgName"
        const val KEY_DISTINCT_ID = "sdk_distinctId"
    }
}
