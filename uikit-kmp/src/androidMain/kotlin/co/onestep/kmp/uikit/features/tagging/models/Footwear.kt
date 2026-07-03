@file:JvmName("FootwearAndroidExt")
package co.onestep.kmp.uikit.features.tagging.models

import android.content.Context

private val Footwear.displayNameKey: String
    get() = when (this) {
        Footwear.WITH_SHOES -> "with_shoes"
        Footwear.BAREFOOT -> "barefoot"
        Footwear.BRACE -> "brace"
        Footwear.SHOE_ADJUSTMENT -> "shoe_adjustment"
        Footwear.ONE_INSOLE -> "one_insoles"
        Footwear.UCBL_BRACE -> "ucbl_brace"
        Footwear.SMO_BRACE -> "smo_brace"
        Footwear.SLIPPERS -> "slippers"
        Footwear.NON_SKID_SOCKS -> "non_skid_socks"
        Footwear.NONE -> "none"
    }

fun String.isFootwear(context: Context): Boolean =
    Footwear.entries.any { entry ->
        val resId = context.resources.getIdentifier(entry.displayNameKey, "string", context.packageName)
        resId != 0 && context.getString(resId) == this
    }

fun String.toFootwear(context: Context): Footwear =
    Footwear.entries.firstOrNull { entry ->
        val resId = context.resources.getIdentifier(entry.displayNameKey, "string", context.packageName)
        resId != 0 && context.getString(resId) == this
    } ?: Footwear.NONE
