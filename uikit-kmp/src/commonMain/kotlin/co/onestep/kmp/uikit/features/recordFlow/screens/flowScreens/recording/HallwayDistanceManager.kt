package co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.recording

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import co.onestep.kmp.uikit.bridge.PreferencesBridge
import co.onestep.kmp.uikit.features.recordFlow.screensData.HallwayDistanceScreenState
import co.onestep.kmp.uikit.features.recordFlow.screensData.filterHallwayDigits
import co.onestep.kmp.uikit.features.recordFlow.screensData.hallwayRange
import co.onestep.kmp.uikit.features.recordFlow.screensData.hallwayRecommended
import co.onestep.kmp.uikit.models.OSTActivityType
import co.onestep.kmp.uikit.utils.METERS_TO_FEET_RATIO
import co.onestep.kmp.uikit.utils.ResourceProvider
import co.onestep.kmp.uikit.utils.useImperialSystem
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.change_if_your_testing_area_is_different
import co.onestep.kmp.uikit_kmp.generated.resources.enter_hallway_length
import co.onestep.kmp.uikit_kmp.generated.resources.hallway_length_error_range
import co.onestep.kmp.uikit_kmp.generated.resources.hallway_length_hint
import co.onestep.kmp.uikit_kmp.generated.resources.last_saved_hallway_length
import co.onestep.kmp.uikit_kmp.generated.resources.unit_feet
import co.onestep.kmp.uikit_kmp.generated.resources.unit_meters
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.StringResource

/**
 * Owns the hallway-distance input state, its validation, the six-vs-two-minute preference
 * read/write, and the short-hallway warning dialog state. Extracted from
 * [MotionRecorderViewModel] (OS God-class decomposition) so the hallway concern — which has no
 * dependency on the recorder/audio path — lives on its own. The ViewModel keeps the same public
 * surface by delegating to this manager.
 *
 * [activityTypeProvider] returns the current activity type; it drives the six-vs-two-minute
 * preference selection and the recommended length, so it always reflects the ViewModel's
 * current configuration.
 */
internal class HallwayDistanceManager(
    private val resourceProvider: ResourceProvider,
    private val preferenceManager: PreferencesBridge,
    private val activityTypeProvider: () -> OSTActivityType,
) {
    private val isSixMin: Boolean get() = activityTypeProvider() == OSTActivityType.SIX_MINUTE_WALK

    private fun hallwayLengthPref(): Float? =
        if (isSixMin) preferenceManager.sixMinHallwayLengthM else preferenceManager.twoMinHallwayLengthM

    private fun saveHallwayLengthPref(value: Float) {
        if (isSixMin) preferenceManager.sixMinHallwayLengthM = value
        else preferenceManager.twoMinHallwayLengthM = value
    }

    private var suppressShortHallwayWarning: Boolean
        get() = if (isSixMin) preferenceManager.suppressShortHallwayWarning6Min else preferenceManager.suppressShortHallwayWarning2Min
        set(value) {
            if (isSixMin) preferenceManager.suppressShortHallwayWarning6Min = value
            else preferenceManager.suppressShortHallwayWarning2Min = value
        }

    /** The committed hallway length for the current test (display units), or null if skipped. */
    var hallwayLengthForCurrentTest: Int? = null
        private set

    private var savedHallwayLength: Int? = null

    var hallwayDistanceState: MutableState<HallwayDistanceScreenState> =
        mutableStateOf(
            HallwayDistanceScreenState(
                title = resourceProvider.getString(Res.string.enter_hallway_length),
                subtitle = resourceProvider.getString(Res.string.hallway_length_hint),
                unitText = resourceProvider.getString(if (isImperialSystem()) Res.string.unit_feet else Res.string.unit_meters),
                inputValue = "",
                errorText = null,
                canContinue = false,
                showShortHallwayDialog = false,
                recommendedValue = hallwayRecommended(isImperialSystem()),
                suppressShortHallwayWarning = suppressShortHallwayWarning,
            ),
        )
        private set

    fun isImperialSystem(): Boolean = useImperialSystem(preferenceManager)

    /**
     * Loads the saved hallway length from preferences into the input state. Called by the
     * ViewModel from `setConfiguration` once the configuration (and thus the activity type)
     * is known.
     */
    fun loadSavedLength() {
        val savedMeters = hallwayLengthPref()
        savedHallwayLength = savedMeters?.let {
            if (isImperialSystem()) (it * METERS_TO_FEET_RATIO).roundToInt() else it.roundToInt()
        }
        rebuildHallwayState(inputValue = savedHallwayLength?.toString() ?: "")
    }

    fun onHallwayInputChanged(rawValue: String) {
        val digits = filterHallwayDigits(rawValue)
        rebuildHallwayState(
            inputValue = digits,
            errorText = hallwayErrorFor(digits),
            showShortHallwayDialog = false,
        )
    }

    fun onHallwayContinue(): Boolean {
        val inputValue = hallwayDistanceState.value.inputValue
        val value = inputValue.toIntOrNull()
        val error = hallwayErrorFor(inputValue, emptyIsError = true)

        if (value == null || error != null) {
            rebuildHallwayState(errorText = error, showShortHallwayDialog = false)
            return false
        }

        val recommended = hallwayRecommended(isImperialSystem(), activityTypeProvider())
        if (!suppressShortHallwayWarning && value < recommended) {
            rebuildHallwayState(
                errorText = null,
                showShortHallwayDialog = true,
            )
            return false
        }

        commitHallwayLength(value)
        return true
    }

    fun onShortHallwayStartTest(): Boolean {
        val inputValue = hallwayDistanceState.value.inputValue
        val value = inputValue.toIntOrNull() ?: return false
        val error = hallwayErrorFor(inputValue)

        if (error != null) {
            dismissShortHallwayDialog()
            rebuildHallwayState(errorText = error, showShortHallwayDialog = false)
            return false
        }

        commitHallwayLength(value)
        dismissShortHallwayDialog()
        return true
    }

    fun onHallwaySkip() {
        hallwayLengthForCurrentTest = null

        rebuildHallwayState(
            errorText = hallwayErrorFor(hallwayDistanceState.value.inputValue),
            showShortHallwayDialog = false,
        )
    }

    fun dismissShortHallwayDialog() {
        rebuildHallwayState(showShortHallwayDialog = false)
    }

    fun onSuppressShortHallwayWarningChanged(suppress: Boolean) {
        suppressShortHallwayWarning = suppress
        rebuildHallwayState() // refresh the state so the checkbox reflects immediately
    }

    fun saveHallwayDistanceToPreferences() {
        val displayValue = hallwayLengthForCurrentTest ?: return
        val valueInMeters: Float = if (isImperialSystem()) {
            displayValue / METERS_TO_FEET_RATIO
        } else {
            displayValue.toFloat()
        }
        saveHallwayLengthPref(valueInMeters)
    }

    private fun rebuildHallwayState(
        inputValue: String = hallwayDistanceState.value.inputValue,
        errorText: String? = hallwayDistanceState.value.errorText,
        showShortHallwayDialog: Boolean = hallwayDistanceState.value.showShortHallwayDialog,
    ) {
        val isImperial = isImperialSystem()
        val titleRes: StringResource =
            if (savedHallwayLength != null) {
                Res.string.last_saved_hallway_length
            } else {
                Res.string.enter_hallway_length
            }

        val subtitleRes: StringResource =
            if (savedHallwayLength != null) {
                Res.string.change_if_your_testing_area_is_different
            } else {
                Res.string.hallway_length_hint
            }

        hallwayDistanceState.value =
            HallwayDistanceScreenState(
                title = resourceProvider.getString(titleRes),
                subtitle = resourceProvider.getString(subtitleRes),
                unitText = resourceProvider.getString(if (isImperial) Res.string.unit_feet else Res.string.unit_meters),
                inputValue = inputValue,
                errorText = errorText,
                canContinue =
                    inputValue.toIntOrNull()?.let {
                        val (rangeMin, rangeMax) = hallwayRange(isImperial)
                        it in rangeMin..rangeMax
                    } ?: false,
                showShortHallwayDialog = showShortHallwayDialog,
                recommendedValue = hallwayRecommended(isImperial, activityTypeProvider()),
                suppressShortHallwayWarning = suppressShortHallwayWarning,
            )
    }

    private fun hallwayErrorFor(inputValue: String, emptyIsError: Boolean = false): String? {
        val (min, max) = hallwayRange(isImperialSystem())
        val value = inputValue.toIntOrNull()
        return when {
            value != null && value in min..max -> null
            inputValue.isEmpty() && !emptyIsError -> null
            else -> resourceProvider.getString(
                Res.string.hallway_length_error_range,
                min, max,
                resourceProvider.getString(if (isImperialSystem()) Res.string.unit_feet else Res.string.unit_meters),
            )
        }
    }

    private fun commitHallwayLength(displayValue: Int) {
        hallwayLengthForCurrentTest = displayValue

        rebuildHallwayState(
            inputValue = displayValue.toString(),
            errorText = null,
            showShortHallwayDialog = false,
        )
    }
}
