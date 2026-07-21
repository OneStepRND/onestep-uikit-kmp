package co.onestep.kmp.sdk

import co.onestep.kmp.sdk.OSTUserAttributes.Companion.AGE
import co.onestep.kmp.sdk.OSTUserAttributes.Companion.DATE_OF_BIRTH
import co.onestep.kmp.sdk.OSTUserAttributes.Companion.EMAIL
import co.onestep.kmp.sdk.OSTUserAttributes.Companion.EMR_ID
import co.onestep.kmp.sdk.OSTUserAttributes.Companion.FIRST_NAME
import co.onestep.kmp.sdk.OSTUserAttributes.Companion.HEIGHT_CM
import co.onestep.kmp.sdk.OSTUserAttributes.Companion.LAST_NAME
import co.onestep.kmp.sdk.OSTUserAttributes.Companion.PHONE
import co.onestep.kmp.sdk.OSTUserAttributes.Companion.PROFILE_IMAGE_URL
import co.onestep.kmp.sdk.OSTUserAttributes.Companion.SEX
import co.onestep.kmp.sdk.OSTUserAttributes.Companion.WEIGHT_KG
import co.onestep.kmp.sdk.OSTUserAttributes.Sex
import kotlinx.datetime.LocalDate

/**
 * Represents demographic and clinical attributes associated with an SDK user.
 *
 * Attributes are split into two maps:
 * - [attributes]: well-known, predefined fields (name, DOB, etc.)
 * - [customAttributes]: arbitrary key/value pairs supplied by the host application.
 *
 * Use [OSTUserAttributesScope] to construct instances in a readable, chainable way.
 */
class OSTUserAttributes internal constructor(
    /** Well-known predefined attribute map keyed by the companion-object constants. */
    val attributes: Map<String, Any?>,
    /** Arbitrary custom attributes supplied by the host application. */
    val customAttributes: Map<String, Any>,
) {
    internal companion object {
        /** Key for the user's first name. */
        const val FIRST_NAME = "first_name"
        /** Key for the user's last name. */
        const val LAST_NAME = "last_name"
        /** Key for the user's email address. */
        const val EMAIL = "email"
        /** Key for the user's phone number. */
        const val PHONE = "phone"
        /** Key for the URL of the user's profile image. */
        const val PROFILE_IMAGE_URL = "profile_image_url"
        /** Key for the user's Electronic Medical Record identifier. */
        const val EMR_ID = "emr_id"
        /** Key for the user's date of birth (stored as [LocalDate]). */
        const val DATE_OF_BIRTH = "date_of_birth"
        /** Key for the user's biological sex (stored as a lowercase string). */
        const val SEX = "sex"
        /** Key for the user's height in centimetres. */
        const val HEIGHT_CM = "height_cm"
        /** Key for the user's weight in kilograms. */
        const val WEIGHT_KG = "weight_kg"
        /** Key for the user's age in years. */
        const val AGE = "age"
    }

    // Getters

    /** The user's first name, or `null` if not set. */
    val firstName: String?
        get() = attributes[FIRST_NAME] as? String?

    /** The user's last name, or `null` if not set. */
    val lastName: String?
        get() = attributes[LAST_NAME] as? String?

    /** The user's email address, or `null` if not set. */
    val email: String?
        get() = attributes[EMAIL] as? String?

    /** The user's phone number, or `null` if not set. */
    val phone: String?
        get() = attributes[PHONE] as? String?

    /** The URL of the user's profile image, or `null` if not set. */
    val profileImageUrl: String?
        get() = attributes[PROFILE_IMAGE_URL] as? String?

    /** The user's Electronic Medical Record identifier, or `null` if not set. */
    val emrId: String?
        get() = attributes[EMR_ID] as? String?

    /** The user's date of birth, or `null` if not set. */
    val dateOfBirth: LocalDate?
        get() = attributes[DATE_OF_BIRTH] as? LocalDate?

    /** The user's biological sex as a lowercase string (e.g. `"male"`), or `null` if not set. */
    val sex: String?
        get() = attributes[SEX] as? String?

    /** The user's height in centimetres, or `null` if not set. */
    val heightCm: Int?
        get() = (attributes[HEIGHT_CM] as? Number)?.toInt()

    /** The user's weight in kilograms, or `null` if not set. */
    val weightKg: Int?
        get() = (attributes[WEIGHT_KG] as? Number)?.toInt()

    /** The user's age in years, or `null` if not set. */
    val age: Int?
        get() = (attributes[AGE] as? Number)?.toInt()

    /**
     * Biological sex of the user.
     *
     * The [description] value is the lowercase string sent to the backend.
     */
    enum class Sex(
        val description: String,
    ) {
        /** Male biological sex. */
        MALE("male"),
        /** Female biological sex. */
        FEMALE("female"),
        ;

        override fun toString(): String = description
    }
}

class OSTUserAttributesScope {

    /** Well-known predefined attribute map keyed by the companion-object constants. */
    private val attributes = mutableMapOf<String, Any?>()
    /** Arbitrary custom attributes supplied by the host application. */
    val customAttributes = mutableMapOf<String, Any>()

    /**
     * Sets the user's first name.
     *
     * Ignored if [name] is empty.
     */
    fun withFirstName(name: String) {
        if (name.isNotEmpty()) {
            this.attributes[FIRST_NAME] = name
        }
    }

    /**
     * Sets the user's last name.
     *
     * Ignored if [name] is empty.
     */
    fun withLastName(name: String) {
        if (name.isNotEmpty()) {
            this.attributes[LAST_NAME] = name
        }
    }

    /**
     * Sets the URL of the user's profile image.
     *
     * Ignored if [url] is empty.
     */
    fun withProfileImage(url: String) {
        if (url.isNotEmpty()) {
            this.attributes[PROFILE_IMAGE_URL] = url
        }
    }

    /**
     * Sets the user's email address.
     *
     * Ignored if [email] is empty.
     */
    fun withEmail(email: String) {
        if (email.isNotEmpty()) {
            this.attributes[EMAIL] = email
        }
    }

    /**
     * Sets the user's phone number.
     *
     * Ignored if [phone] is empty.
     */
    fun withPhone(phone: String) {
        if (phone.isNotEmpty()) {
            this.attributes[PHONE] = phone
        }
    }

    /**
     * Sets the user's Electronic Medical Record identifier.
     *
     * Ignored if [emrId] is empty.
     */
    fun withEmrId(emrId: String) {
        if (emrId.isNotEmpty()) {
            this.attributes[EMR_ID] = emrId
        }
    }

    /**
     * Sets the user's date of birth.
     *
     * The value is stored as-is and serialised in `yyyy-MM-dd` format when transmitted.
     */
    fun withDateOfBirth(dateOfBirth: LocalDate) {
        // yyyy-MM-dd
        this.attributes[DATE_OF_BIRTH] = dateOfBirth
    }

    /**
     * Sets the user's age in years.
     *
     * Ignored if [age] is not a positive integer.
     */
    fun withAge(age: Int) {
        if (age > 0) {
            this.attributes[AGE] = age
        }
    }

    /**
     * Sets the user's biological sex.
     *
     * @param sex One of [Sex.MALE] or [Sex.FEMALE].
     */
    fun withSex(sex: Sex) {
        this.attributes[SEX] = sex.description
    }

    /**
     * Sets the user's height in centimetres.
     *
     * Ignored if [heightCm] is not a positive integer.
     */
    fun withHeightCm(heightCm: Int) {
        if (heightCm > 0) {
            this.attributes[HEIGHT_CM] = heightCm
        }
    }

    /**
     * Sets the user's weight in kilograms.
     *
     * Ignored if [weightKg] is not a positive integer.
     */
    fun withWeightKg(weightKg: Int) {
        if (weightKg > 0) {
            this.attributes[WEIGHT_KG] = weightKg
        }
    }

    /**
     * Adds an arbitrary custom attribute to the user profile.
     *
     * Custom attributes are forwarded to the backend alongside well-known attributes
     * and can be used for application-specific segmentation or filtering.
     *
     * @param key   Unique identifier for the attribute.
     * @param value Attribute value; must be a type supported by the serialisation layer.
     */
    fun withCustomAttribute(
        key: String,
        value: Any,
    ) {
        this.customAttributes[key] = value
    }

    /** Populates the scope with values from an existing [OSTUserAttributes] instance. */
    fun fromUserAttributes(userAttributes: OSTUserAttributes) {
        this.attributes.putAll(userAttributes.attributes)
        this.customAttributes.putAll(userAttributes.customAttributes)
    }

    /** Builds and returns the configured [OSTUserAttributes] instance. */
    fun toOSTUserAttributes(): OSTUserAttributes = OSTUserAttributes(this.attributes, this.customAttributes)
}