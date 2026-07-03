package co.onestep.kmp.uikit.models

import androidx.compose.runtime.Composable
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.independent
import co.onestep.kmp.uikit_kmp.generated.resources.modified_independent
import co.onestep.kmp.uikit_kmp.generated.resources.standby_assistance
import co.onestep.kmp.uikit_kmp.generated.resources.minimal_assistance
import co.onestep.kmp.uikit_kmp.generated.resources.moderate_assistance
import co.onestep.kmp.uikit_kmp.generated.resources.maximum_assistance
import co.onestep.kmp.uikit_kmp.generated.resources.total_assistance
import co.onestep.kmp.uikit_kmp.generated.resources.unable_to_perform_at_this_time
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

val OSTLevelOfAssistance.displayNameRes: StringResource
    get() = when (this) {
        OSTLevelOfAssistance.INDEPENDENT -> Res.string.independent
        OSTLevelOfAssistance.MODIFIED_INDEPENDENT -> Res.string.modified_independent
        OSTLevelOfAssistance.STANDBY_ASSISTANCE -> Res.string.standby_assistance
        OSTLevelOfAssistance.MIN_ASSISTANCE -> Res.string.minimal_assistance
        OSTLevelOfAssistance.MODERATE_ASSISTANCE -> Res.string.moderate_assistance
        OSTLevelOfAssistance.MAX_ASSISTANCE -> Res.string.maximum_assistance
        OSTLevelOfAssistance.TOTAL_ASSISTANCE -> Res.string.total_assistance
        OSTLevelOfAssistance.UNABLE_TO_PERFORM -> Res.string.unable_to_perform_at_this_time
    }

@Composable
fun OSTLevelOfAssistance.displayName(): String = stringResource(displayNameRes)
