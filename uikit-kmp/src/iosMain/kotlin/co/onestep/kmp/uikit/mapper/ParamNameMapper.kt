package co.onestep.kmp.uikit.mapper

import co.onestep.kmp.uikit.models.OSTParamName

/**
 * iOS SDK param name mapper.
 *
 * Maps column name strings from the iOS SDK to KMP OSTParamName enum values.
 * The iOS SDK likely uses the same column names as Android.
 */
private val columnNameToKmpMap: Map<String, OSTParamName> by lazy {
    OSTParamName.entries.associateBy { it.columnName }
}

fun String.toKmpParamName(): OSTParamName =
    columnNameToKmpMap[this] ?: error("Unknown param column name: $this")

fun OSTParamName.toIosColumnName(): String = columnName

fun Map<String, Float>.toKmpParamMap(): Map<String, Float> = this
