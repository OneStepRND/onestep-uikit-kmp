package co.onestep.kmp.uikit.testapp

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreCrypto.CCHmac
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.CoreCrypto.kCCHmacAlgSHA256
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSString
import platform.Foundation.NSTimeZone
import platform.Foundation.NSURL
import platform.Foundation.NSURLSession
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDefaults
import platform.Foundation.create
import platform.Foundation.dataTaskWithRequest
import platform.Foundation.dataUsingEncoding
import platform.Foundation.localTimeZone
import platform.Foundation.setHTTPBody
import platform.Foundation.setHTTPMethod
import platform.Foundation.setValue
import platform.UIKit.UIDevice
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@OptIn(ExperimentalForeignApi::class)
actual fun hmacSha256(secret: ByteArray, message: ByteArray): ByteArray {
    require(secret.isNotEmpty() && message.isNotEmpty()) { "hmacSha256: empty input" }
    val digest = ByteArray(CC_SHA256_DIGEST_LENGTH)
    secret.usePinned { secretPinned ->
        message.usePinned { messagePinned ->
            digest.usePinned { digestPinned ->
                CCHmac(
                    algorithm = kCCHmacAlgSHA256,
                    key = secretPinned.addressOf(0),
                    keyLength = secret.size.convert(),
                    data = messagePinned.addressOf(0),
                    dataLength = message.size.convert(),
                    macOut = digestPinned.addressOf(0),
                )
            }
        }
    }
    return digest
}

actual val deviceManufacturer: String get() = "Apple"
actual val deviceModel: String get() = UIDevice.currentDevice.model

actual fun localeTimezoneId(): String = NSTimeZone.localTimeZone.name

@OptIn(ExperimentalForeignApi::class)
actual suspend fun httpPostJson(url: String, jsonBody: String): HttpTextResponse =
    suspendCancellableCoroutine { continuation ->
        val nsUrl = NSURL.URLWithString(url)
            ?: run {
                continuation.resumeWithException(IllegalArgumentException("Bad URL"))
                return@suspendCancellableCoroutine
            }
        val request = NSMutableURLRequest(uRL = nsUrl).apply {
            setHTTPMethod("POST")
            setValue("application/json", forHTTPHeaderField = "Content-Type")
            setValue("application/json", forHTTPHeaderField = "Accept")
            @Suppress("CAST_NEVER_SUCCEEDS")
            setHTTPBody((jsonBody as NSString).dataUsingEncoding(NSUTF8StringEncoding))
        }
        val task = NSURLSession.sharedSession.dataTaskWithRequest(request) { data, response, error ->
            when {
                error != null -> continuation.resumeWithException(
                    RuntimeException("OTP exchange transport error: ${error.localizedDescription}"),
                )
                else -> {
                    val statusCode = (response as? NSHTTPURLResponse)?.statusCode?.toInt() ?: 0
                    val body = data?.let {
                        @Suppress("CAST_NEVER_SUCCEEDS")
                        NSString.create(data = it, encoding = NSUTF8StringEncoding) as? String
                    }.orEmpty()
                    continuation.resume(HttpTextResponse(statusCode = statusCode, body = body))
                }
            }
        }
        continuation.invokeOnCancellation { task.cancel() }
        task.resume()
    }

/** NSUserDefaults-backed [SettingsPrefs], same keys the original iosTestApp used. */
fun SettingsPrefs(): SettingsPrefs = IosSettingsPrefs()

private class IosSettingsPrefs : SettingsPrefs {
    private val defaults = NSUserDefaults.standardUserDefaults

    override var environment: String
        get() = defaults.stringForKey(KEY_ENVIRONMENT) ?: SDKEnvironment.PRODUCTION.rawValue
        set(value) = defaults.setObject(value, forKey = KEY_ENVIRONMENT)

    override var customUrl: String
        get() = defaults.stringForKey(KEY_CUSTOM_URL) ?: ""
        set(value) = defaults.setObject(value, forKey = KEY_CUSTOM_URL)

    override var orgName: String
        get() = defaults.stringForKey(KEY_ORG_NAME) ?: Organizations.default.name
        set(value) = defaults.setObject(value, forKey = KEY_ORG_NAME)

    override var distinctId: String
        get() = defaults.stringForKey(KEY_DISTINCT_ID) ?: AppConstants.DEFAULT_DISTINCT_ID
        set(value) = defaults.setObject(value, forKey = KEY_DISTINCT_ID)

    private companion object {
        const val KEY_ENVIRONMENT = "sdk_environment"
        const val KEY_CUSTOM_URL = "sdk_customURL"
        const val KEY_ORG_NAME = "sdk_orgName"
        const val KEY_DISTINCT_ID = "sdk_distinctId"
    }
}
