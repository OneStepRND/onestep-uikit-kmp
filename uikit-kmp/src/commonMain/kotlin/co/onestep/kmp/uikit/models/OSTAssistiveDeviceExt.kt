package co.onestep.kmp.uikit.models

import androidx.compose.runtime.Composable
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.none
import co.onestep.kmp.uikit_kmp.generated.resources.walker
import co.onestep.kmp.uikit_kmp.generated.resources.rollator_four_wheeled_walker
import co.onestep.kmp.uikit_kmp.generated.resources.cane
import co.onestep.kmp.uikit_kmp.generated.resources.two_crutches
import co.onestep.kmp.uikit_kmp.generated.resources.one_crutch
import co.onestep.kmp.uikit_kmp.generated.resources.ic_none
import co.onestep.kmp.uikit_kmp.generated.resources.ic_walker
import co.onestep.kmp.uikit_kmp.generated.resources.ic_rollator
import co.onestep.kmp.uikit_kmp.generated.resources.ic_cane
import co.onestep.kmp.uikit_kmp.generated.resources.ic_two_crutches
import co.onestep.kmp.uikit_kmp.generated.resources.ic_one_crutch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

val OSTAssistiveDevice.displayNameRes: StringResource
    get() = when (this) {
        OSTAssistiveDevice.NONE -> Res.string.none
        OSTAssistiveDevice.WALKER -> Res.string.walker
        OSTAssistiveDevice.ROLLATOR -> Res.string.rollator_four_wheeled_walker
        OSTAssistiveDevice.CANE -> Res.string.cane
        OSTAssistiveDevice.CRUTCH_DOUBLE -> Res.string.two_crutches
        OSTAssistiveDevice.CRUTCH_SINGLE -> Res.string.one_crutch
    }

val OSTAssistiveDevice.icon: DrawableResource
    get() = when (this) {
        OSTAssistiveDevice.NONE -> Res.drawable.ic_none
        OSTAssistiveDevice.WALKER -> Res.drawable.ic_walker
        OSTAssistiveDevice.ROLLATOR -> Res.drawable.ic_rollator
        OSTAssistiveDevice.CANE -> Res.drawable.ic_cane
        OSTAssistiveDevice.CRUTCH_DOUBLE -> Res.drawable.ic_two_crutches
        OSTAssistiveDevice.CRUTCH_SINGLE -> Res.drawable.ic_one_crutch
    }

@Composable
fun OSTAssistiveDevice.displayName(): String = stringResource(displayNameRes)
