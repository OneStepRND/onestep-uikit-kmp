package co.onestep.kmp.uikit.features.recordFlow.configurations

import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.ic_balance_option_fallback
import co.onestep.kmp.uikit_kmp.generated.resources.ic_footwear_barefoot
import co.onestep.kmp.uikit_kmp.generated.resources.ic_footwear_orthotics
import co.onestep.kmp.uikit_kmp.generated.resources.ic_footwear_shoes
import co.onestep.kmp.uikit_kmp.generated.resources.ic_footwear_socks
import co.onestep.kmp.uikit_kmp.generated.resources.ic_stance_feet_together
import co.onestep.kmp.uikit_kmp.generated.resources.ic_stance_narrow_base
import co.onestep.kmp.uikit_kmp.generated.resources.ic_stance_seated
import co.onestep.kmp.uikit_kmp.generated.resources.ic_stance_semi_tandem
import co.onestep.kmp.uikit_kmp.generated.resources.ic_stance_shoulder_width
import co.onestep.kmp.uikit_kmp.generated.resources.ic_stance_single_leg_left
import co.onestep.kmp.uikit_kmp.generated.resources.ic_stance_single_leg_right
import co.onestep.kmp.uikit_kmp.generated.resources.ic_stance_tandem
import co.onestep.kmp.uikit_kmp.generated.resources.ic_surface_dome
import co.onestep.kmp.uikit_kmp.generated.resources.ic_surface_firm
import co.onestep.kmp.uikit_kmp.generated.resources.ic_surface_foam
import co.onestep.kmp.uikit_kmp.generated.resources.ic_surface_uneven
import co.onestep.kmp.uikit_kmp.generated.resources.ic_vision_eyes_closed
import co.onestep.kmp.uikit_kmp.generated.resources.ic_vision_eyes_open
import org.jetbrains.compose.resources.DrawableResource

/**
 * Resolves the leading icon for a Static Balance option from its canonical [OSTBalance.Option.code].
 *
 * The server-driven condition schema carries no icons, so the SDK keeps a code→drawable
 * registry for all known codes and renders [Res.drawable.ic_balance_option_fallback] for any
 * unknown (e.g. newly added server) code. Add a `when` branch here when a new code gains a
 * dedicated asset.
 */
internal object BalanceIcons {

    fun iconFor(code: String): DrawableResource =
        when (code) {
            // Stance
            "shoulder_width" -> Res.drawable.ic_stance_shoulder_width
            "feet_together" -> Res.drawable.ic_stance_feet_together
            "narrow_base" -> Res.drawable.ic_stance_narrow_base
            "semi_tandem" -> Res.drawable.ic_stance_semi_tandem
            "tandem" -> Res.drawable.ic_stance_tandem
            "single_leg_left" -> Res.drawable.ic_stance_single_leg_left
            "single_leg_right" -> Res.drawable.ic_stance_single_leg_right
            "seated" -> Res.drawable.ic_stance_seated
            // Vision
            "eyes_open" -> Res.drawable.ic_vision_eyes_open
            "eyes_closed" -> Res.drawable.ic_vision_eyes_closed
            // Surface
            "firm" -> Res.drawable.ic_surface_firm
            "foam" -> Res.drawable.ic_surface_foam
            "dome" -> Res.drawable.ic_surface_dome
            "uneven" -> Res.drawable.ic_surface_uneven
            // Footwear
            "shoes" -> Res.drawable.ic_footwear_shoes
            "barefoot" -> Res.drawable.ic_footwear_barefoot
            "socks" -> Res.drawable.ic_footwear_socks
            "orthotics" -> Res.drawable.ic_footwear_orthotics
            // Unknown / future server codes
            else -> Res.drawable.ic_balance_option_fallback
        }
}
