package co.onestep.kmp.uikit.features.summary.models

internal sealed interface SummaryListState {
    sealed class Insights : SummaryListState {
        data class Success(
            val insightItems: List<SummaryScreenItem>,
        ) : Insights()

        data object Loading : Insights()

        data class Error(
            val emptyStateData: EmptyStateData,
        ) : Insights()
    }

    sealed class GaitLab : SummaryListState {
        data class Success(
            val gaitLabItems: List<SummaryScreenItem>,
        ) : GaitLab()

        data object Loading : GaitLab()

        data class Error(
            val emptyStateData: EmptyStateData,
        ) : GaitLab()
    }

    sealed class Partial : SummaryListState {
        data class Success(
            val title: String,
            val subtitle: String,
            val steps: Int? = null,
            val durationText: String? = null,
        ) : Partial()

        data object Loading : Partial()
    }
}
