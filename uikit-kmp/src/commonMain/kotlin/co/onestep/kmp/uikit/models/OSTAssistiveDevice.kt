package co.onestep.kmp.uikit.models

import kotlinx.serialization.Serializable

@Serializable
enum class OSTAssistiveDevice(
    val value: Int,
    val displayNameKey: String,
) {
    NONE(0, "none"),
    WALKER(5, "walker"),
    ROLLATOR(6, "rollator_four_wheeled_walker"),
    CANE(2, "cane"),
    CRUTCH_DOUBLE(4, "two_crutches"),
    CRUTCH_SINGLE(3, "one_crutch"),
    ;

    override fun toString(): String = "AssistiveDevice(value=$value)"

    companion object {
        fun Int.toAssistiveDevice(): OSTAssistiveDevice =
            when (this) {
                NONE.value -> NONE
                CANE.value -> CANE
                CRUTCH_SINGLE.value -> CRUTCH_SINGLE
                CRUTCH_DOUBLE.value -> CRUTCH_DOUBLE
                WALKER.value -> WALKER
                ROLLATOR.value -> ROLLATOR
                else -> NONE
            }

        fun Int.fromIndex(devices: List<OSTAssistiveDevice>): OSTAssistiveDevice {
            return if (this in devices.indices) devices[this] else NONE
        }
    }
}
