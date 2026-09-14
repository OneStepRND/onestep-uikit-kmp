package co.onestep.kmp.uikit.features.recordFlow.configurations

import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.image_indoors
import co.onestep.kmp.uikit_kmp.generated.resources.image_outdoors
import co.onestep.kmp.uikit_kmp.generated.resources.image_pants_loose
import co.onestep.kmp.uikit_kmp.generated.resources.image_pants_medium
import co.onestep.kmp.uikit_kmp.generated.resources.image_pants_tight
import co.onestep.kmp.uikit_kmp.generated.resources.image_pocket_left
import co.onestep.kmp.uikit_kmp.generated.resources.image_pocket_right
import co.onestep.kmp.uikit_kmp.generated.resources.image_shorts
import org.jetbrains.compose.resources.DrawableResource

/**
 * Resolves the leading illustration for a custom recording question's answer, from the answer's
 * own tag value.
 *
 * Same shape and same reason as [BalanceIcons]: a host's questions are server-authored
 * ([OSTRecordingQuestionData.tagsValues] comes straight off `config/measurement-overrides`) and
 * that schema carries no icons, so the SDK keeps the registry. The key is the tag value itself
 * because it is the only per-answer identity the payload has — and it is a backend `Tag` enum
 * value, not free copy, so it is as stable as a code would be. Changing one changes what is
 * stored on the measurement, which is why it is safe to key on.
 *
 * Unknown values return `null` rather than a fallback glyph: most custom questions have no
 * artwork and must keep rendering as text-only rows, which is the opposite of the balance case
 * where every option is expected to have one.
 *
 * The current set is the HSL dual-task vocabulary (OS-16296): clothing, pocket side and
 * environment. Add a branch when a new answer gains an asset.
 */
internal object RecordingQuestionIcons {

    fun iconFor(tagValue: String): DrawableResource? =
        when (tagValue) {
            // Pants or shorts. "Pants" shares the medium-tightness drawing, as legacy iOS does.
            "Pants" -> Res.drawable.image_pants_medium
            "Shorts" -> Res.drawable.image_shorts
            // Tightness
            "Tight pants" -> Res.drawable.image_pants_tight
            "Medium pants" -> Res.drawable.image_pants_medium
            "Loose pants" -> Res.drawable.image_pants_loose
            // Pocket side
            "Left pocket" -> Res.drawable.image_pocket_left
            "Right pocket" -> Res.drawable.image_pocket_right
            // Environment
            "Indoors" -> Res.drawable.image_indoors
            "Outdoors" -> Res.drawable.image_outdoors
            else -> null
        }
}
