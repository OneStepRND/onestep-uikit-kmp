package co.onestep.kmp.uikit.features.recordFlow.screens.flowScreens.recording

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import co.onestep.kmp.uikit.bridge.OSTSDKBridge
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

/**
 * Owns the hallway-distance input state, its validation, the six-vs-two-minute length
 * persistence, and the short-hallway warning dialog state. Extracted from
 * [MotionRecorderViewModel] (OS God-class decomposition) so the hallway concern — which has no
 * dependency on the recorder/audio path — lives on its own. The ViewModel keeps the same public
 * surface by delegating to this manager.
 *
 * ## Persistence
 *
 * The last-entered hallway length is stored in the SDK-managed custom-metadata store (via
 * [OSTSDKBridge.getCustomMetadata] / [OSTSDKBridge.updateCustomMetadata]) under the keys
 * [KEY_HALLWAY_LENGTH_6MIN] / [KEY_HALLWAY_LENGTH_2MIN], mirroring the Android uikit. The value
 * therefore follows the user across devices and survives logout (it is rehydrated from the
 * backend on the next identify), unlike the previous device-local [PreferencesBridge] storage.
 *
 * Reads are asynchronous (the SDK fetches from its cache/backend), so [loadSavedLength] restores
 * the value into the input state whenever it arrives — as long as the user has not already typed
 * one. When the host supplies [hostHallwayLengthMetersProvider] (via
 * [co.onestep.kmp.uikit.features.recordFlow.configurations.OSTRecordingConfiguration.hallwayLengthMeters])
 * that value pre-fills the screen synchronously and no metadata read is issued.
 *
 * Note: in current-user (patient-app) mode the SDK metadata is the single-user store, so the
 * last-entered length follows the user across devices. In **clinician mode** ([isPatientSession] =
 * true) both the metadata read and write are suppressed: the hallway length is a property of the
 * clinic hallway, not the patient, so per-patient persistence would scatter the same physical value
 * across patient stores, and writing it to the clinician's own store would leak session state
 * across patients. The clinician host instead pre-fills the value via
 * [co.onestep.kmp.uikit.features.recordFlow.configurations.OSTRecordingConfiguration.hallwayLengthMeters]
 * (still honored through [hostHallwayLengthMetersProvider]). This matches the legacy Android-uikit
 * behavior, which gated persistence on an active patient scope. The short-hallway *suppression*
 * preference remains device-local on [PreferencesBridge] in both modes.
 *
 * [activityTypeProvider] returns the current activity type; it drives the six-vs-two-minute
 * key/preference selection and the recommended length, so it always reflects the ViewModel's
 * current configuration.
 */
internal class HallwayDistanceManager(
    private val resourceProvider: ResourceProvider,
    private val preferenceManager: PreferencesBridge,
    private val sdkBridge: OSTSDKBridge,
    private val coroutineScope: CoroutineScope,
    private val activityTypeProvider: () -> OSTActivityType,
    private val hostHallwayLengthMetersProvider: () -> Float? = { null },
    /**
     * True for clinician-mode (patient-scoped) flows. When true, the SDK custom-metadata read in
     * [loadSavedLength] and the write in [saveHallwayLengthToMetadata] are both suppressed; the
     * host still pre-fills via [hostHallwayLengthMetersProvider].
     */
    private val isPatientSession: Boolean = false,
) {
    private val isSixMin: Boolean get() = activityTypeProvider() == OSTActivityType.SIX_MINUTE_WALK

    /** Custom-metadata key for the current activity's last-entered hallway length, in meters. */
    private val hallwayLengthKey: String
        get() = if (isSixMin) KEY_HALLWAY_LENGTH_6MIN else KEY_HALLWAY_LENGTH_2MIN

    /**
     * Last hallway length (meters) known this session, keyed by [hallwayLengthKey]. Seeded by the
     * async metadata read and updated on save, so re-entering the screen within a flow does not
     * need another network round-trip.
     */
    private val cachedMetersByKey: MutableMap<String, Float> = mutableMapOf()

    private var suppressShortHallwayWarning: Boolean
        get() = if (isSixMin) preferenceManager.suppressShortHallwayWarning6Min else preferenceManager.suppressShortHallwayWarning2Min
        set(value) {
            if (isSixMin) preferenceManager.suppressShortHallwayWarning6Min = value
            else preferenceManager.suppressShortHallwayWarning2Min = value
        }

    /** The committed hallway length for the current test (display units), or null if skipped. */
    var hallwayLengthForCurrentTest: Int? = null
        private set

    /**
     * [hallwayLengthForCurrentTest] normalised to **meters**, or null when no length was committed.
     *
     * Meters regardless of the user's unit system so a single stored value serves both: the display
     * unit is a per-user preference, while the hallway is a physical property of the room. Reported
     * to the host on [co.onestep.kmp.uikit.features.recordFlow.OSTRecordingFlowResult], which is the
     * only way a clinician-mode host can learn the value — its own persistence is suppressed here.
     */
    val committedHallwayLengthMeters: Float?
        get() {
            val displayValue = hallwayLengthForCurrentTest ?: return null
            return if (isImperialSystem()) displayValue / METERS_TO_FEET_RATIO else displayValue.toFloat()
        }

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
     * Restores the saved hallway length into the input state. Called by the ViewModel from
     * `setConfiguration` once the configuration (and thus the activity type) is known.
     *
     * A host-supplied length pre-fills synchronously; otherwise the value is fetched from SDK
     * custom metadata asynchronously and applied on arrival, unless the user has meanwhile typed
     * a value (guarded by [isInputUntouched]).
     */
    fun loadSavedLength() {
        val hostMeters = hostHallwayLengthMetersProvider()
        if (hostMeters != null) {
            applySavedMeters(hostMeters)
            return
        }

        // Clinician mode: no metadata read — the length is not persisted per patient. The host
        // pre-fill above is the only source; absent that the clinician enters it each session.
        if (isPatientSession) return

        val key = hallwayLengthKey
        cachedMetersByKey[key]?.let {
            applySavedMeters(it)
            return
        }

        // Async read from the SDK-managed metadata store. Apply only if the user hasn't typed yet.
        coroutineScope.launch {
            val meters = sdkBridge.getCustomMetadata().asFloatFlag(key) ?: return@launch
            cachedMetersByKey[key] = meters
            if (isInputUntouched()) {
                applySavedMeters(meters)
            }
        }
    }

    private fun isInputUntouched(): Boolean =
        hallwayLengthForCurrentTest == null && hallwayDistanceState.value.inputValue.isEmpty()

    /** Converts a saved length in meters to the current display unit and seeds the input state. */
    private fun applySavedMeters(meters: Float) {
        savedHallwayLength =
            if (isImperialSystem()) (meters * METERS_TO_FEET_RATIO).roundToInt() else meters.roundToInt()
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

    /**
     * Persists the committed hallway length to the SDK-managed custom-metadata store so it follows
     * the user across devices. Optimistically updates the in-memory cache, then fires a best-effort
     * merge PATCH; a failure just means the next successful identify rehydrates from the backend.
     */
    fun saveHallwayLengthToMetadata() {
        // Clinician mode: never write the length to a per-user metadata store (would leak the
        // clinic hallway value into the clinician's or a patient's store). The committed length is
        // still attached to the measurement's walkCourseLength — that is not suppressed here — and
        // is reported to the host on the flow result ([committedHallwayLengthMeters]), which is how
        // a clinician host builds a per-clinic memory of its own.
        if (isPatientSession) return
        val valueInMeters = committedHallwayLengthMeters ?: return
        val key = hallwayLengthKey
        cachedMetersByKey[key] = valueInMeters
        coroutineScope.launch {
            sdkBridge.updateCustomMetadata(mapOf(key to valueInMeters))
        }
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

    companion object {
        // Last-entered hallway length, in meters, per activity type. `ost.`-prefixed keys are
        // SDK/UIKit-reserved in the custom-metadata store; hosts must not write them directly.
        const val KEY_HALLWAY_LENGTH_6MIN = "ost.ui.hallway_length_6min"
        const val KEY_HALLWAY_LENGTH_2MIN = "ost.ui.hallway_length_2min"
    }
}

/**
 * Float accessor tolerant of how custom-metadata numbers arrive per platform: Android decodes
 * server JSON numbers as `Double`, while the iOS bridge hands values across the ObjC boundary as
 * `NSNumber` (which is not a Kotlin [Number]). The `toString()` fallback covers the latter.
 * Returns null when the key is absent or the value is not numeric.
 */
private fun Map<String, Any>.asFloatFlag(key: String): Float? = when (val v = this[key]) {
    is Number -> v.toFloat()
    is String -> v.toFloatOrNull()
    else -> v?.toString()?.toFloatOrNull()
}
