package co.onestep.kmp.uikit.testapp

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Test-only organizations, mirroring `iosTestApp`'s `Constants.swift` so the Android and iOS test
 * apps expose the same Organization picker in the Settings/Login screen.
 *
 * These are the same shared test-org credentials the core SDK repo's demo apps use — not new
 * secrets, and never tied to a real patient (no PII/PHI).
 *
 * WARNING: identity-verification signing belongs on a backend. It is done client-side here strictly
 * because this is an internal testing app.
 */
data class Organization(
    val name: String,
    val displayName: String,
    val appId: String,
    val apiKey: String,
    val identityVerificationSecret: String,
) {
    /** HMAC-SHA256 sign [distinctId] with this org's secret (raw UTF-8 bytes), returning a hex string. */
    fun signIdentity(distinctId: String): String {
        val algorithm = "HmacSHA256"
        val secretKey = SecretKeySpec(identityVerificationSecret.toByteArray(Charsets.UTF_8), algorithm)
        val mac = Mac.getInstance(algorithm)
        mac.init(secretKey)
        val hmacBytes = mac.doFinal(distinctId.toByteArray(Charsets.UTF_8))
        return hmacBytes.joinToString("") { "%02x".format(it) }
    }
}

object Organizations {
    val sdkTesting = Organization(
        name = "sdk_testing",
        displayName = "SDK Testing",
        appId = "3cd1bc3b-51b0-4cda-89e3-d25398c7a52e",
        apiKey = "***REMOVED***",
        identityVerificationSecret = "***REMOVED***",
    )

    val zimmerDev = Organization(
        name = "zimmer_dev",
        displayName = "Zimmer (Dev)",
        appId = "147c6678-4faa-49eb-a4a6-5ab92627b203",
        apiKey = "***REMOVED***",
        identityVerificationSecret = "***REMOVED***",
    )

    val appClip = Organization(
        name = "app_clip",
        displayName = "OneStep App Clip",
        appId = "4486cfd2-9beb-4d46-8d9f-713ea88e5e87",
        apiKey = "***REMOVED***",
        identityVerificationSecret = "***REMOVED***",
    )

    val all: List<Organization> = listOf(sdkTesting, zimmerDev, appClip)

    val default: Organization = sdkTesting

    fun find(byName: String?): Organization? = all.firstOrNull { it.name == byName }
}

object AppConstants {
    const val RELEASE_BASE_URL = "https://app.onestep.co/api/"

    /**
     * Clinician web app host — a DIFFERENT subdomain from the SDK API host ([RELEASE_BASE_URL] /
     * `app.onestep.co`). The hosted login page lives here (`clinic.onestep.co/login?m=2`) and it
     * also proxies the clinician API (`clinic.onestep.co/api/clinician/...`), so it is the single
     * base for both the web sign-in and the OTP exchange.
     */
    const val CLINICIAN_WEB_BASE_URL = "https://clinic.onestep.co/"
    const val AVATAR_AANG_DISTINCT_ID = "018fb9ec-d44b-7232-927b-a9e3612321a3"

    /** Convenience default so the Distinct ID field is never empty on a fresh install. */
    const val DEFAULT_DISTINCT_ID = "uikit-kmp-android-test-user"
}
