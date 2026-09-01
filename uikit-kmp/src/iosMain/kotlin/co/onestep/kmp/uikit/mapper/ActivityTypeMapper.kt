package co.onestep.kmp.uikit.mapper

import co.onestep.kmp.uikit.models.OSTActivityType

/**
 * iOS SDK activity type mapper.
 *
 * When the iOS OneStep SDK is integrated via cinterop, replace the String-based
 * mapper with direct type mapping similar to Android's:
 * ```
 * fun IosActivityType.toKmp(): OSTActivityType = when (this) { ... }
 * fun OSTActivityType.toIos(): IosActivityType = when (this) { ... }
 * ```
 */
fun String.toKmpActivityType(): OSTActivityType =
    when (this.uppercase()) {
        "WALK" -> OSTActivityType.WALK
        "STS" -> OSTActivityType.STS
        "TUG" -> OSTActivityType.TUG
        "ROM_KNEE_FLEX" -> OSTActivityType.ROM_KNEE_FLEX
        "ROM_KNEE_EXT" -> OSTActivityType.ROM_KNEE_EXT
        "BALANCE_TEST" -> OSTActivityType.BALANCE_TEST
        "DUAL_TASK_WALK_SUBTRACT" -> OSTActivityType.DUAL_TASK_WALK_SUBTRACT
        "SIX_MINUTE_WALK" -> OSTActivityType.SIX_MINUTE_WALK
        "TWO_MINUTE_WALK" -> OSTActivityType.TWO_MINUTE_WALK
        "STAIRS" -> OSTActivityType.STAIRS
        "GENERIC_RECORDING" -> OSTActivityType.GENERIC_RECORDING
        else -> OSTActivityType.WALK
    }

fun OSTActivityType.toIosString(): String = name
