package co.onestep.kmp.uikit.features.tagging.models

import androidx.compose.runtime.Composable
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.barefoot
import co.onestep.kmp.uikit_kmp.generated.resources.brace
import co.onestep.kmp.uikit_kmp.generated.resources.ic_barefoot
import co.onestep.kmp.uikit_kmp.generated.resources.ic_brace
import co.onestep.kmp.uikit_kmp.generated.resources.ic_none
import co.onestep.kmp.uikit_kmp.generated.resources.ic_non_skid_socks
import co.onestep.kmp.uikit_kmp.generated.resources.ic_one_insoles
import co.onestep.kmp.uikit_kmp.generated.resources.ic_shoe_adjustment
import co.onestep.kmp.uikit_kmp.generated.resources.ic_slippers
import co.onestep.kmp.uikit_kmp.generated.resources.ic_smo_brace
import co.onestep.kmp.uikit_kmp.generated.resources.ic_ucbl_brace
import co.onestep.kmp.uikit_kmp.generated.resources.ic_with_shoes
import co.onestep.kmp.uikit_kmp.generated.resources.non_skid_socks
import co.onestep.kmp.uikit_kmp.generated.resources.none
import co.onestep.kmp.uikit_kmp.generated.resources.one_insoles
import co.onestep.kmp.uikit_kmp.generated.resources.shoe_adjustment
import co.onestep.kmp.uikit_kmp.generated.resources.slippers
import co.onestep.kmp.uikit_kmp.generated.resources.smo_brace
import co.onestep.kmp.uikit_kmp.generated.resources.ucbl_brace
import co.onestep.kmp.uikit_kmp.generated.resources.with_shoes
import co.onestep.kmp.uikit.utils.ResourceProvider
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

enum class Footwear(
    val displayNameRes: StringResource,
    val icon: DrawableResource,
) {
    WITH_SHOES(Res.string.with_shoes, Res.drawable.ic_with_shoes),
    BAREFOOT(Res.string.barefoot, Res.drawable.ic_barefoot),
    BRACE(Res.string.brace, Res.drawable.ic_brace),
    SHOE_ADJUSTMENT(Res.string.shoe_adjustment, Res.drawable.ic_shoe_adjustment),
    ONE_INSOLE(Res.string.one_insoles, Res.drawable.ic_one_insoles),
    UCBL_BRACE(Res.string.ucbl_brace, Res.drawable.ic_ucbl_brace),
    SMO_BRACE(Res.string.smo_brace, Res.drawable.ic_smo_brace),
    SLIPPERS(Res.string.slippers, Res.drawable.ic_slippers),
    NON_SKID_SOCKS(Res.string.non_skid_socks, Res.drawable.ic_non_skid_socks),
    NONE(Res.string.none, Res.drawable.ic_none),
    ;

    @Composable
    fun displayName(): String = stringResource(displayNameRes)

    companion object {
        fun Int.toFootwear(): Footwear =
            when (this) {
                WITH_SHOES.ordinal -> WITH_SHOES
                BAREFOOT.ordinal -> BAREFOOT
                BRACE.ordinal -> BRACE
                SHOE_ADJUSTMENT.ordinal -> SHOE_ADJUSTMENT
                ONE_INSOLE.ordinal -> ONE_INSOLE
                UCBL_BRACE.ordinal -> UCBL_BRACE
                SMO_BRACE.ordinal -> SMO_BRACE
                SLIPPERS.ordinal -> SLIPPERS
                NON_SKID_SOCKS.ordinal -> NON_SKID_SOCKS
                else -> NONE
            }

        @Composable
        fun String.toFootwear(): Footwear =
            entries.firstOrNull { stringResource(it.displayNameRes) == this } ?: NONE

        @Composable
        fun String.isFootwear(): Boolean =
            entries.any { stringResource(it.displayNameRes) == this }

        /** Non-composable version for ViewModel use */
        fun String.isFootwear(resourceProvider: ResourceProvider): Boolean =
            entries.any { resourceProvider.getString(it.displayNameRes) == this }

        /** Non-composable version for ViewModel use */
        fun String.toFootwear(resourceProvider: ResourceProvider): Footwear =
            entries.firstOrNull { resourceProvider.getString(it.displayNameRes) == this } ?: NONE
    }
}
