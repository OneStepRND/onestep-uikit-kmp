package co.onestep.kmp.sdk

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * The SDK's own [OSTError] implementation, carrying the numeric code and detail string the
 * native SDKs report alongside the message.
 *
 * Internal on purpose: [OSTError] is the published contract, so consumers bring their own
 * implementation. SDK code builds these through `String.asErrorResult(...)`.
 */
@Serializable
internal data class OSTSDKError(
    @SerialName("code") val code: Int,
    @SerialName("message") override val message: String,
    @SerialName("details") val details: String? = null,
) : OSTError

/**
 * The numeric code of an SDK-produced error, or null when the error came from a host-supplied
 * [OSTError] implementation (which only guarantees a message).
 */
internal val OSTError.sdkCode: Int?
    get() = (this as? OSTSDKError)?.code

/**
 * Serializes any [OSTError] in the [OSTSDKError] shape (`code` / `message` / `details`) and always
 * decodes back into an [OSTSDKError].
 *
 * [OSTError] is an interface, so serializable models holding one would otherwise fall back to
 * polymorphic serialization — which changes the JSON shape and needs a `SerializersModule`
 * registration that iOS (no reflection) cannot infer. Host-supplied implementations contribute
 * their [OSTError.message]; the code/details of a non-SDK error are not part of the contract.
 */
internal object OSTErrorSerializer : KSerializer<OSTError> {

    private val delegate = OSTSDKError.serializer()

    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun serialize(encoder: Encoder, value: OSTError) =
        delegate.serialize(encoder, value as? OSTSDKError ?: OSTSDKError(code = 0, message = value.message))

    override fun deserialize(decoder: Decoder): OSTError = delegate.deserialize(decoder)
}
