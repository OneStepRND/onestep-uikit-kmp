package co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.staticBalance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import co.onestep.kmp.uikit.features.recordFlow.components.SelectableOption
import co.onestep.kmp.uikit.features.recordFlow.components.SelectableSection
import co.onestep.kmp.uikit.features.recordFlow.components.SelectableSections
import co.onestep.kmp.uikit.features.recordFlow.components.SelectableSectionsScreen
import co.onestep.kmp.uikit.features.recordFlow.configurations.BalanceIcons
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTBalance
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTBalanceCondition
import co.onestep.kmp.uikit.ui.theme.PreviewTheme
import co.onestep.kmp.uikit.utils.UIktDestination
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.static_balance_choose_conditions
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlinx.serialization.Serializable

const val CONDITION_SETUP_CONTINUE_BUTTON = "condition_setup_continue_button"
const val CONDITION_SETUP_CLEAR_ALL_BUTTON = "condition_setup_clear_all_button"

@Serializable
data object ConditionSetupDestination : UIktDestination

/**
 * Static Balance Test condition setup (OS-15960, PRD §4.3).
 *
 * Thin adapter over the domain-agnostic [SelectableSectionsScreen]: it builds one section
 * per [OSTBalance.Category] (delivered via `OSTRecordingConfiguration.balance`), in order,
 * and maps the returned per-section selection back into an [OSTBalanceCondition]. Each
 * category's [OSTBalance.Category.required] flag drives whether Continue waits on it. No
 * note is collected here — the single per-condition note is entered post-recording on the
 * "Recording saved" screen. A category with an empty option list is hidden.
 */
fun NavGraphBuilder.conditionSetupScreen(
    balance: OSTBalance,
    onScreenView: () -> Unit,
    onContinue: (OSTBalanceCondition) -> Unit,
) {
    composable<ConditionSetupDestination> {
        ConditionSetupScreen(
            balance = balance,
            onScreenView = onScreenView,
            onContinue = onContinue,
        )
    }
}

@Composable
internal fun ConditionSetupScreen(
    balance: OSTBalance,
    modifier: Modifier = Modifier,
    onScreenView: () -> Unit = {},
    onContinue: (OSTBalanceCondition) -> Unit,
) {
    // Build the section list once per config so each list keeps a stable identity and the
    // section cards can skip recomposition. Labels are server-provided, used verbatim;
    // icons resolve from the option code. Categories with no options are dropped.
    val sections = remember(balance) {
        SelectableSections(
            balance.categories.mapNotNull { category ->
                if (category.options.isEmpty()) {
                    null
                } else {
                    SelectableSection(
                        id = category.key,
                        title = category.displayName,
                        required = category.required,
                        options = category.options.map {
                            SelectableOption(it.displayName, BalanceIcons.iconFor(it.code))
                        },
                    )
                }
            },
        )
    }

    SelectableSectionsScreen(
        title = stringResource(Res.string.static_balance_choose_conditions),
        sections = sections,
        modifier = modifier,
        onScreenView = onScreenView,
        continueButtonTestTag = CONDITION_SETUP_CONTINUE_BUTTON,
        clearButtonTestTag = CONDITION_SETUP_CLEAR_ALL_BUTTON,
        onContinue = { selections, note ->
            // selections maps category.key -> chosen option index, for selected sections only.
            val chosen = balance.categories.mapNotNull { category ->
                val index = selections[category.key] ?: return@mapNotNull null
                val option = category.options.getOrNull(index) ?: return@mapNotNull null
                OSTBalanceCondition.Selection(
                    categoryKey = category.key,
                    code = option.code,
                    displayName = option.displayName,
                )
            }
            onContinue(OSTBalanceCondition(selections = chosen, notes = note))
        },
    )
}

@Preview
@Composable
private fun ConditionSetupScreenPreview() {
    PreviewTheme {
        ConditionSetupScreen(
            balance = OSTBalance(),
            onContinue = {},
        )
    }
}
