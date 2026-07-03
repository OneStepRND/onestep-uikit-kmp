package co.onestep.kmp.uikit.mapper

import co.onestep.kmp.uikit.models.OSTActivityType as KmpActivityType
import co.onestep.android.core.OSTActivityType as CoreActivityType

// Name-based mapping (mirrors ModelMappers.kt) tolerates core-only entries such as
// STATIC_BALANCE that have no KMP counterpart, defaulting to WALK.
fun CoreActivityType.toKmp(): KmpActivityType =
    KmpActivityType.entries.firstOrNull { it.name == this.name } ?: KmpActivityType.WALK

fun KmpActivityType.toCore(): CoreActivityType =
    CoreActivityType.entries.firstOrNull { it.name == this.name } ?: CoreActivityType.WALK
