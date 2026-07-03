package co.onestep.kmp.uikit.models

sealed class OSTOrder {
    data object ASCENDING : OSTOrder()
    data object DESCENDING : OSTOrder()
}
