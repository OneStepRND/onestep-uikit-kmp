package co.onestep.kmp.uikit.mapper

import co.onestep.kmp.uikit.models.OSTParamName as KmpParamName
import co.onestep.android.core.OSTParamName as CoreParamName

private val coreToKmpMap: Map<CoreParamName, KmpParamName> by lazy {
    CoreParamName.entries.associateWith { coreParam ->
        KmpParamName.entries.first { it.columnName == coreParam.columnName }
    }
}

private val kmpToCoreMap: Map<KmpParamName, CoreParamName> by lazy {
    KmpParamName.entries.associateWith { kmpParam ->
        CoreParamName.entries.first { it.columnName == kmpParam.columnName }
    }
}

fun CoreParamName.toKmp(): KmpParamName =
    coreToKmpMap[this] ?: error("Unknown param name: $this")

fun KmpParamName.toCore(): CoreParamName =
    kmpToCoreMap[this] ?: error("Unknown param name: $this")

fun Map<CoreParamName, Float>.toKmpParamMap(): Map<KmpParamName, Float> =
    mapKeys { (key, _) -> key.toKmp() }

fun Map<KmpParamName, Float>.toCoreParamMap(): Map<CoreParamName, Float> =
    mapKeys { (key, _) -> key.toCore() }
