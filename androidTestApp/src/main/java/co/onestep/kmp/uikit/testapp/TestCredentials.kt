package co.onestep.kmp.uikit.testapp

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Credentials of the shared "onestep-sdk-testing" organization — the same
 * test-only organization used by the core SDK repo's demo app.
 *
 * WARNING: identity verification signing belongs on a backend. It is done
 * client-side here strictly because this is an internal testing app.
 */
object TestCredentials {
    const val CLIENT_TOKEN = "V97CL6uOrvxVMGrRxPM3pslkBzmDcXD0tL26vDgI4OI"
    private const val IDENTITY_VERIFICATION_SECRET = "Rc6xWFBDQxGe7frJhhQ3f9Fm9b44ZnCsQ4QOGxLh0X0"

    const val DEFAULT_USER_ID = "uikit-kmp-android-test-user"

    fun signIdentity(distinctId: String): String {
        val algorithm = "HmacSHA256"
        val secretKey = SecretKeySpec(IDENTITY_VERIFICATION_SECRET.toByteArray(Charsets.UTF_8), algorithm)
        val mac = Mac.getInstance(algorithm)
        mac.init(secretKey)
        val hmacBytes = mac.doFinal(distinctId.toByteArray(Charsets.UTF_8))
        return hmacBytes.joinToString("") { "%02x".format(it) }
    }
}
