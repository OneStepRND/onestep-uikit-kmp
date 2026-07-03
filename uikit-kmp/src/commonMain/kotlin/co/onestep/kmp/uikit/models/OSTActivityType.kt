package co.onestep.kmp.uikit.models

import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.balance_test
import co.onestep.kmp.uikit_kmp.generated.resources.dual_task_activity_display_name
import co.onestep.kmp.uikit_kmp.generated.resources.knee_extension
import co.onestep.kmp.uikit_kmp.generated.resources.knee_flexion
import co.onestep.kmp.uikit_kmp.generated.resources.sit_to_stand
import co.onestep.kmp.uikit_kmp.generated.resources.six_minute_walk
import co.onestep.kmp.uikit_kmp.generated.resources.stairs
import co.onestep.kmp.uikit_kmp.generated.resources.static_balance_test
import co.onestep.kmp.uikit_kmp.generated.resources.timed_up_and_go
import co.onestep.kmp.uikit_kmp.generated.resources.two_minute_walk
import co.onestep.kmp.uikit_kmp.generated.resources.walk
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

@Serializable
enum class OSTActivityType(
    val serializedName: String,
    val displayNameKey: String,
) {
    @SerialName("walk")
    WALK("walk", "walk"),

    @SerialName("sts")
    STS("sts", "sit_to_stand"),

    @SerialName("tug")
    TUG("tug", "timed_up_and_go"),

    @SerialName("rom_knee_flexion_passive")
    ROM_KNEE_FLEX("rom_knee_flex", "knee_flexion"),

    @SerialName("rom_knee_ext")
    ROM_KNEE_EXT("rom_knee_ext", "knee_extension"),

    @SerialName("balance_test")
    BALANCE_TEST("balance_test", "balance_test"),

    // Static Balance Test (OS-15960) — clinician-operated per-condition postural-sway
    // session; distinct from the legacy [BALANCE_TEST]. type / @SerialName mirrors core's
    // "static_balance_test".
    @SerialName("static_balance_test")
    STATIC_BALANCE("static_balance_test", "static_balance_test"),

    @SerialName("dual_task_walk_subtract")
    DUAL_TASK_WALK_SUBTRACT("dual_task_walk_subtract", "dual_task_walk"),

    @SerialName("walk_6_min_test")
    SIX_MINUTE_WALK("walk_6_min_test", "six_minute_walk"),

    @SerialName("walk_2_min_test")
    TWO_MINUTE_WALK("walk_2_min_test", "two_minute_walk"),

    STAIRS("stairs", "stairs"),
}

fun OSTActivityType.base(): OSTActivityType =
    when (this) {
        OSTActivityType.DUAL_TASK_WALK_SUBTRACT -> OSTActivityType.WALK
        else -> this
    }

fun String.isUnknownActivityType() =
    OSTActivityType.entries.toTypedArray().none { it.serializedName == this }

val OSTActivityType.displayNameRes: StringResource
    get() = when (this) {
        OSTActivityType.WALK -> Res.string.walk
        OSTActivityType.STS -> Res.string.sit_to_stand
        OSTActivityType.TUG -> Res.string.timed_up_and_go
        OSTActivityType.ROM_KNEE_FLEX -> Res.string.knee_flexion
        OSTActivityType.ROM_KNEE_EXT -> Res.string.knee_extension
        OSTActivityType.BALANCE_TEST -> Res.string.balance_test
        OSTActivityType.STATIC_BALANCE -> Res.string.static_balance_test
        OSTActivityType.DUAL_TASK_WALK_SUBTRACT -> Res.string.dual_task_activity_display_name
        OSTActivityType.SIX_MINUTE_WALK -> Res.string.six_minute_walk
        OSTActivityType.TWO_MINUTE_WALK -> Res.string.two_minute_walk
        OSTActivityType.STAIRS -> Res.string.stairs
    }
