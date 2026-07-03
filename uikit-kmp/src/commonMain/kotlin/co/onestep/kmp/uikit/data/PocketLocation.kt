package co.onestep.kmp.uikit.data

import androidx.compose.runtime.Composable
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.against_the_thigh
import co.onestep.kmp.uikit_kmp.generated.resources.image_in_pocket
import co.onestep.kmp.uikit_kmp.generated.resources.image_in_thigh
import co.onestep.kmp.uikit_kmp.generated.resources.in_the_pocket
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.stringResource

/**
 * PocketLocation represents the different locations where the phone can be placed.
 * Uses string keys instead of Android resource IDs for multiplatform compatibility.
 */
internal sealed class PocketLocation(
    open val getReadyTitleKey: String,
    open val displayTitleKey: String,
    open val imageKey: String,
) {
    data class Pocket(
        override val getReadyTitleKey: String = "place_the_phone_in_the_pocket",
        override val displayTitleKey: String = "in_the_pocket",
        override val imageKey: String = "image_in_pocket",
    ) : PocketLocation(getReadyTitleKey, displayTitleKey, imageKey)

    data class Thigh(
        override val getReadyTitleKey: String = "place_the_phone_against_the_thigh",
        override val displayTitleKey: String = "against_the_thigh",
        override val imageKey: String = "image_in_thigh",
    ) : PocketLocation(getReadyTitleKey, displayTitleKey, imageKey)

    fun imageResource(): DrawableResource = when (this) {
        is Pocket -> Res.drawable.image_in_pocket
        is Thigh -> Res.drawable.image_in_thigh
    }

    @Composable
    fun displayTitle(): String = when (this) {
        is Pocket -> stringResource(Res.string.in_the_pocket)
        is Thigh -> stringResource(Res.string.against_the_thigh)
    }

    companion object {
        val values = listOf(Pocket(), Thigh())

        fun pocketLocationByIndex(int: Int) = values[int]
    }
}
