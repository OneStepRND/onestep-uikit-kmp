package co.onestep.kmp.uikit.features.summary.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.onestep.kmp.uikit.bridge.InsightsBridge
import co.onestep.kmp.uikit.bridge.MotionDataBridge
import co.onestep.kmp.uikit.bridge.PreferencesBridge
import co.onestep.kmp.uikit.bridge.RecorderBridge
import co.onestep.kmp.uikit.di.UIKitServiceLocator
import co.onestep.kmp.uikit.models.FeatureFlag
import co.onestep.kmp.uikit.features.recordFlow.screensData.AnalysisBannerData
import co.onestep.kmp.uikit.features.recordFlow.screensData.IconData
import co.onestep.kmp.uikit.features.recordFlow.screensData.TextData
import co.onestep.kmp.uikit.features.recordFlow.screensData.hallwayRange
import co.onestep.kmp.uikit.features.recordFlow.screensData.isSixOrTwoMinWalk
import co.onestep.kmp.uikit.features.summary.components.OSTTabData
import co.onestep.kmp.uikit.features.summary.models.EmptyStateData
import co.onestep.kmp.uikit.features.summary.models.MainParamItem
import co.onestep.kmp.uikit.features.summary.models.SummaryListState
import co.onestep.kmp.uikit.features.summary.models.SummaryScreenItem
import co.onestep.kmp.uikit.features.summary.models.TugChairData
import co.onestep.kmp.uikit.features.summary.models.TugComponentData
import co.onestep.kmp.uikit.models.OSTActivityType
import co.onestep.kmp.uikit.models.OSTDailyBackgroundMeasurement
import co.onestep.kmp.uikit.models.OSTInsight
import co.onestep.kmp.uikit.models.OSTInsightType
import co.onestep.kmp.uikit.models.OSTInsights
import co.onestep.kmp.uikit.models.OSTMotionMeasurement
import co.onestep.kmp.uikit.models.OSTNorm
import co.onestep.kmp.uikit.models.OSTNormPart
import co.onestep.kmp.uikit.models.OSTOrder
import co.onestep.kmp.uikit.models.OSTParamName
import co.onestep.kmp.uikit.models.OSTParameterMetadata
import co.onestep.kmp.uikit.models.OSTResultState
import co.onestep.kmp.uikit.models.OSTTimeRangeFilter
import co.onestep.kmp.uikit.models.OSTTimeRangedDataRequest
import co.onestep.kmp.uikit.models.OSTWalkCourseLength
import co.onestep.kmp.uikit.models.OSTWalkCourseLength.Companion.FEET_UNIT
import co.onestep.kmp.uikit.models.OSTWalkCourseLength.Companion.METERS_UNIT
import co.onestep.kmp.uikit.models.OSTWalkCourseLength.Companion.getWalkCourseLength
import co.onestep.kmp.uikit.utils.CM_UNITS
import co.onestep.kmp.uikit.utils.ConversionResult
import co.onestep.kmp.uikit.utils.METERS_TO_FEET_RATIO
import co.onestep.kmp.uikit.utils.ResourceProvider
import co.onestep.kmp.uikit.utils.toBubbleColor
import co.onestep.kmp.uikit.utils.toLocalizedTimeString
import co.onestep.kmp.uikit.utils.toMainParamTitle
import co.onestep.kmp.uikit.utils.useImperialSystem
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.connection_error_message
import co.onestep.kmp.uikit_kmp.generated.resources.default_insight
import co.onestep.kmp.uikit_kmp.generated.resources.degrees
import co.onestep.kmp.uikit_kmp.generated.resources.gait_lab
import co.onestep.kmp.uikit_kmp.generated.resources.hallway_length_error_range
import co.onestep.kmp.uikit_kmp.generated.resources.highlights
import co.onestep.kmp.uikit_kmp.generated.resources.ic_attention
import co.onestep.kmp.uikit_kmp.generated.resources.ic_info_circle
import co.onestep.kmp.uikit_kmp.generated.resources.ic_message_alert
import co.onestep.kmp.uikit_kmp.generated.resources.minimal_analysis_banner_subtitle
import co.onestep.kmp.uikit_kmp.generated.resources.minimal_analysis_banner_title
import co.onestep.kmp.uikit_kmp.generated.resources.no_additional_insights
import co.onestep.kmp.uikit_kmp.generated.resources.no_score_server_error_text
import co.onestep.kmp.uikit_kmp.generated.resources.no_score_system_error_text
import co.onestep.kmp.uikit_kmp.generated.resources.not_available
import co.onestep.kmp.uikit_kmp.generated.resources.of_100
import co.onestep.kmp.uikit_kmp.generated.resources.partial_analysis_banner_subtitle
import co.onestep.kmp.uikit_kmp.generated.resources.partial_analysis_banner_title
import co.onestep.kmp.uikit_kmp.generated.resources.partial_summary_subtitle
import co.onestep.kmp.uikit_kmp.generated.resources.partial_summary_title
import co.onestep.kmp.uikit_kmp.generated.resources.reps_plural
import co.onestep.kmp.uikit_kmp.generated.resources.seconds
import co.onestep.kmp.uikit_kmp.generated.resources.system_error_highlights_message
import co.onestep.kmp.uikit_kmp.generated.resources.unavailable
import co.onestep.kmp.uikit_kmp.generated.resources.unit_feet
import co.onestep.kmp.uikit_kmp.generated.resources.unit_meters
import co.onestep.kmp.uikit_kmp.generated.resources.walk_duration_unit
import co.onestep.kmp.uikit_kmp.generated.resources.you_did_great_we_encountered_a_problem_in_loading_additional_insights
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Represents the hallway-related state for the Summary screen.
 */
internal data class HallwayState(
    val isSixMinuteWalk: Boolean = false,
    val hallwayLength: Int? = null,
    val hallwayUnitText: String = "",
    val showHallwayEdit: Boolean = false,
    val hallwayWarningActive: Boolean = false,
    val showEditDialog: Boolean = false,
    val editValue: String = "",
    val editError: String? = null,
)

internal class SummaryViewModel(
    private val resourceProvider: ResourceProvider,
    private val preferenceManager: PreferencesBridge,
    private val recorderBridge: RecorderBridge,
    private val motionDataBridge: MotionDataBridge,
    private val insightsBridge: InsightsBridge,
) : ViewModel() {

    var isLoading = mutableStateOf(true)
    var motionMeasurement = mutableStateOf<OSTMotionMeasurement?>(null)
    var gatLabScreenState = mutableStateOf<SummaryListState>(SummaryListState.GaitLab.Loading)
    var insightsScreenState = mutableStateOf<SummaryListState>(SummaryListState.Insights.Loading)
    var partialScreenState = mutableStateOf<SummaryListState?>(null)
    var mainParamCardItem = mutableStateOf<MainParamItem?>(null)
    var hallwayState = mutableStateOf(HallwayState())
    private var previousMeasurementParams: Map<OSTParamName, Float> = emptyMap()
    private var previousMeasurement: OSTMotionMeasurement? = null

    fun createSummaryItems(uuid: String) {
        resetScreen()
        viewModelScope.launch(Dispatchers.Default) {
            motionMeasurement.value = getMotionMeasurement(uuid)

            val measurement = motionMeasurement.value ?: return@launch

            // Fetch previous measurement and its parameters
            previousMeasurement = getPreviousMeasurement(measurement.timestamp)
            previousMeasurementParams = previousMeasurement?.paramsByName() ?: emptyMap()

            withContext(Dispatchers.Default) {
                // ===== PHASE 1: Emit main param immediately with gray color =====
                val mainParamValue = computeMainParamValue(measurement)
                val mainParamText = computeMainParamText(measurement.type, mainParamValue)

                withContext(Dispatchers.Main) {
                    mainParamCardItem.value =
                        buildMainParamCardItem(
                            measurement,
                            mainParamValue,
                            mainParamText,
                            Color.Gray,
                        )
                    isLoading.value = false // UNBLOCK UI NOW
                }

                // handle minimal walk result
                if (partialResultHandled()) return@withContext

                // ===== PHASE 2: Emit Gait Lab + real color (local, fast) =====
                val params = measurement.paramsByName()
                val circleColor = getMeasurementCircleColor()
                val gaitLabState = buildGaitLabState(params)

                withContext(Dispatchers.Main) {
                    gatLabScreenState.value = gaitLabState
                    // Update with real color (triggers animateColorAsState)
                    mainParamCardItem.value =
                        buildMainParamCardItem(
                            measurement,
                            mainParamValue,
                            mainParamText,
                            circleColor,
                        )
                }

                // ===== PHASE 3: Fetch insights (network, slow) =====
                val insights = insightsBridge.getInsightsByUuid(uuid)
                val tugItem =
                    if (measurement.type == OSTActivityType.TUG) {
                        measurement.toTugItemOrNull()
                    } else {
                        null
                    }

                withContext(Dispatchers.Main) {
                    insightsScreenState.value = buildInsightsState(insights, tugItem)
                }
            }
        }
    }

    /**
     * Creates summary items from a full OSTMotionMeasurement object.
     * This overload avoids fetching the measurement from the database since it's already provided.
     *
     * @param measurement The complete motion measurement object to display
     */
    fun createSummaryItems(measurement: OSTMotionMeasurement) {
        resetScreen()
        viewModelScope.launch(Dispatchers.Default) {
            motionMeasurement.value = measurement // Use provided object directly


            // Fetch previous measurement and its parameters
            previousMeasurement = getPreviousMeasurement(measurement.timestamp)
            previousMeasurementParams = previousMeasurement?.paramsByName() ?: emptyMap()

            withContext(Dispatchers.Default) {
                // ===== PHASE 1: Emit main param immediately with gray color =====
                val mainParamValue = computeMainParamValue(measurement)
                val mainParamText = computeMainParamText(measurement.type, mainParamValue)

                // handle minimal walk result
                if (partialResultHandled()) {
                    isLoading.value = false // UNBLOCK UI NOW
                    return@withContext
                }

                withContext(Dispatchers.Main) {
                    mainParamCardItem.value =
                        buildMainParamCardItem(
                            measurement,
                            mainParamValue,
                            mainParamText,
                            Color.Gray,
                        )
                    isLoading.value = false // UNBLOCK UI NOW
                }

                // ===== PHASE 2: Emit Gait Lab + real color (local, fast) =====
                val params = measurement.paramsByName()
                val circleColor = getMeasurementCircleColor()
                val gaitLabState = buildGaitLabState(params)

                withContext(Dispatchers.Main) {
                    gatLabScreenState.value = gaitLabState
                    // Update with real color (triggers animateColorAsState)
                    mainParamCardItem.value =
                        buildMainParamCardItem(
                            measurement,
                            mainParamValue,
                            mainParamText,
                            circleColor,
                        )
                }

                // ===== PHASE 3: Fetch insights (network, slow) =====
                val insights = insightsBridge.getInsightsByUuid(measurement.id)
                val tugItem =
                    if (measurement.type == OSTActivityType.TUG) {
                        measurement.toTugItemOrNull()
                    } else {
                        null
                    }

                withContext(Dispatchers.Main) {
                    insightsScreenState.value = buildInsightsState(insights, tugItem)
                }
            }
        }
    }

    private fun computeMainParamValue(measurement: OSTMotionMeasurement): Float? =
        motionDataBridge.mainParam(measurement)?.let { mainParam ->
            mainParam.second
                .adjustToImperialIfNeeded(mainParam.first)
                ?.roundValue(
                    motionDataBridge.getParameterMetadata(mainParam.first),
                    preferenceManager,
                )
        }

    private fun computeMainParamText(
        activityType: OSTActivityType?,
        mainParamValue: Float?,
    ): String? =
        when (activityType) {
            OSTActivityType.WALK, OSTActivityType.DUAL_TASK_WALK_SUBTRACT ->
                resourceProvider.getString(
                    Res.string.of_100,
                )

            OSTActivityType.SIX_MINUTE_WALK -> {
                val parameterMetadata =
                    motionDataBridge.getParameterMetadata(OSTParamName.SIX_MINUTE_WALK_DISTANCE_METERS)
                if (useImperialSystem(preferenceManager)) {
                    parameterMetadata.imperialUnits
                } else {
                    parameterMetadata.units
                }
            }

            OSTActivityType.TWO_MINUTE_WALK -> {
                val parameterMetadata =
                    motionDataBridge.getParameterMetadata(OSTParamName.TWO_MINUTE_WALK_DISTANCE_METERS)
                if (useImperialSystem(preferenceManager)) {
                    parameterMetadata.imperialUnits
                } else {
                    parameterMetadata.units
                }
            }

            OSTActivityType.STS -> {
                mainParamValue?.toInt()?.let {
                    resourceProvider.getQuantityString(
                        Res.plurals.reps_plural,
                        it,
                    )
                } ?: resourceProvider.getString(Res.string.unavailable)
            }

            OSTActivityType.TUG -> resourceProvider.getString(Res.string.seconds)
            OSTActivityType.ROM_KNEE_FLEX -> resourceProvider.getString(Res.string.degrees)
            OSTActivityType.ROM_KNEE_EXT -> resourceProvider.getString(Res.string.degrees)
            else -> null
        }

    private fun buildMainParamCardItem(
        measurement: OSTMotionMeasurement,
        mainParamValue: Float?,
        mainParamText: String?,
        circleColor: Color,
    ): MainParamItem =
        measurement.toMainParamCardItem(
            durationUnits = resourceProvider.getString(Res.string.walk_duration_unit),
            mainParamCircleColor = circleColor,
            defaultText = resourceProvider.getString(Res.string.not_available),
            mainParamText = mainParamText,
            mainParam = mainParamValue,
        )

    private fun buildMinimalWalkParamCardItem(measurement: OSTMotionMeasurement?): MainParamItem {
        val metadata = measurement?.metadata
        return MainParamItem(
            title = motionMeasurement.value?.timestamp?.toMainParamTitle() ?: "",
            steps = metadata?.steps ?: 0,
            duration = metadata?.seconds?.toStringDuration()
                ?: resourceProvider.getString(Res.string.not_available),
            durationUnits = resourceProvider.getString(Res.string.walk_duration_unit),
            animateMainParam = false,
            showTabs = false,
            tabs = emptyList(),
            showMetadata = false,
            showValues = false,
            showTrashIcon = true,
            mainParamValue = null,
            mainParamColor = getMeasurementCircleColor(),
            analysisBannerData = getBannerErrorData(null)
        )
    }

    private suspend fun buildInsightsState(
        insights: OSTInsights?,
        tugItem: TugComponentData?,
    ): SummaryListState =
        when {
            insights != null -> {
                if (insights.insights.isEmpty() && tugItem == null) {
                    SummaryListState.Insights.Error(
                        EmptyStateData(
                            subtitle =
                                TextData(
                                    text =
                                        resourceProvider.getString(
                                            Res.string.connection_error_message,
                                        ),
                                    textSize = 18.sp,
                                    fontWeight = FontWeight.W400,
                                ),
                        ),
                    )
                } else {
                    SummaryListState.Insights.Success(
                        createInsights(insights.insights).apply {
                            tugItem?.let {
                                add(
                                    0,
                                    SummaryScreenItem.TugSummaryComponentItem(it),
                                )
                            }
                        },
                    )
                }
            }

            else ->
                SummaryListState.Insights.Error(
                    EmptyStateData(
                        subtitle =
                            TextData(
                                text =
                                    resourceProvider.getString(
                                        Res.string.system_error_highlights_message,
                                    ),
                                textSize = 18.sp,
                                fontWeight = FontWeight.W400,
                            ),
                    ),
                )
        }

    private suspend fun buildGaitLabState(params: Map<OSTParamName, Float>): SummaryListState {
        val norms =
            params
                .mapNotNull { (paramName, _) ->
                    motionDataBridge.getNormByName(paramName)?.let { norm ->
                        paramName to norm
                    }
                }.toMap()

        return when {
            norms.isEmpty() ->
                SummaryListState.GaitLab.Error(
                    EmptyStateData(
                        subtitle =
                            TextData(
                                text =
                                    resourceProvider.getString(
                                        Res.string.connection_error_message,
                                    ),
                                textSize = 18.sp,
                                fontWeight = FontWeight.W400,
                            ),
                    ),
                )

            else ->
                SummaryListState.GaitLab.Success(
                    createGaitLabItems(
                        norms,
                        params,
                    ),
                )
        }
    }

    /**
     * Creates summary items for a daily background measurement (from background monitoring).
     * Shows single Gait Lab tab with parameters and norms. No insights available.
     */
    fun createSummaryItems(dailyBackgroundMeasurement: OSTDailyBackgroundMeasurement) {
        resetScreen()
        viewModelScope.launch(Dispatchers.Default) {

            withContext(Dispatchers.Default) {
                // Extract WALKING_WALK_SCORE as main parameter
                val walkingWalkScore =
                    dailyBackgroundMeasurement.parametersByName()[OSTParamName.WALKING_WALK_SCORE]

                // Background measurements don't have insights
                withContext(Dispatchers.Main) {
                    isLoading.value = false

                    insightsScreenState.value =
                        SummaryListState.Insights.Error(
                            EmptyStateData(
                                icon = IconData(Res.drawable.ic_message_alert),
                                title =
                                    TextData(
                                        text = resourceProvider.getString(Res.string.no_additional_insights),
                                        textSize = 28.sp,
                                        fontWeight = FontWeight.W700,
                                    ),
                                subtitle =
                                    TextData(
                                        text =
                                            resourceProvider.getString(
                                                Res.string.you_did_great_we_encountered_a_problem_in_loading_additional_insights,
                                            ),
                                        textSize = 18.sp,
                                        fontWeight = FontWeight.W400,
                                    ),
                            ),
                        )

                    // Process gait lab items from parameters + norms
                    val params = dailyBackgroundMeasurement.parametersByName()
                    val norms =
                        params
                            .mapNotNull { (paramName, _) ->
                                motionDataBridge.getNormByName(paramName)?.let { norm ->
                                    paramName to norm
                                }
                            }.toMap()

                    gatLabScreenState.value =
                        when {
                            norms.isEmpty() ->
                                SummaryListState.GaitLab.Error(
                                    EmptyStateData(
                                        icon = IconData(Res.drawable.ic_message_alert),
                                        title =
                                            TextData(
                                                text = resourceProvider.getString(Res.string.no_additional_insights),
                                                textSize = 28.sp,
                                                fontWeight = FontWeight.W700,
                                            ),
                                        subtitle =
                                            TextData(
                                                text =
                                                    resourceProvider.getString(
                                                        Res.string.you_did_great_we_encountered_a_problem_in_loading_additional_insights,
                                                    ),
                                                textSize = 18.sp,
                                                fontWeight = FontWeight.W400,
                                            ),
                                    ),
                                )

                            else ->
                                SummaryListState.GaitLab.Success(
                                    createGaitLabItems(norms, params),
                                )
                        }

                    // Create MainParamItem for daily background measurement
                    mainParamCardItem.value =
                        MainParamItem(
                            title =
                                dailyBackgroundMeasurement.timestamp
                                    .toMainParamTitle(showDuration = false),
                            steps =
                                (dailyBackgroundMeasurement.parametersByName()
                                    [OSTParamName.WALKING_STEPS] ?: 0f).toInt(),
                            duration = "", // Not displayed for background measurements
                            durationUnits = resourceProvider.getString(Res.string.walk_duration_unit),
                            animateMainParam = true,
                            showTabs = true,
                            tabs =
                                listOf(
                                    OSTTabData(
                                        resourceProvider.getString(Res.string.gait_lab),
                                        0,
                                    ),
                                ),
                            showTrashIcon = false,
                            showMetadata = true,
                            showValues = false,
                            mainParamValue = walkingWalkScore,
                            mainParamText = resourceProvider.getString(Res.string.of_100),
                            mainParamColor = getBackgroundRecordCircleColor(walkingWalkScore),
                        )
                }
            }
        }
    }

    /**
     * Determines the circle color for background records based on walking_walk_score thresholds.
     */
    private fun getBackgroundRecordCircleColor(score: Float?): Color =
        when {
            score == null -> Color.Gray
            score >= 70 -> Color(0xFF2C9D72)  // health_healthy_p1
            score >= 40 -> Color(0xFFF09846)  // med
            else -> Color(0xFFF05F46)          // bad
        }

    private fun partialResultHandled(): Boolean {
        val params = motionMeasurement.value?.paramsByName() ?: emptyMap()
        if (motionMeasurement.value?.resultState.isPartialOrEmpty() ||
            params.size < 3
        ) {
            when (motionMeasurement.value?.type) {
                OSTActivityType.TUG -> {
                    val state = SummaryListState.Partial.Success(
                        title = resourceProvider.getString(Res.string.partial_summary_title),
                        subtitle = resourceProvider.getString(Res.string.partial_summary_subtitle),
                    )
                    partialScreenState.value = state
                    insightsScreenState.value = state
                    return true
                }

                OSTActivityType.WALK,
                OSTActivityType.SIX_MINUTE_WALK,
                OSTActivityType.TWO_MINUTE_WALK,
                OSTActivityType.DUAL_TASK_WALK_SUBTRACT -> {
                    val metadata = motionMeasurement.value?.metadata
                    mainParamCardItem.value = buildMinimalWalkParamCardItem(motionMeasurement.value)
                    val state = SummaryListState.Partial.Success(
                        title = resourceProvider.getString(Res.string.partial_summary_title),
                        subtitle = resourceProvider.getString(Res.string.partial_summary_subtitle),
                        steps = metadata?.steps,
                        durationText = metadata?.seconds?.toStringDuration(),
                    )
                    partialScreenState.value = state
                    insightsScreenState.value = state
                    return true
                }

                else -> {
                    val state = SummaryListState.Partial.Success(
                        title = resourceProvider.getString(Res.string.partial_summary_title),
                        subtitle = resourceProvider.getString(Res.string.partial_summary_subtitle),
                    )
                    partialScreenState.value = state
                    insightsScreenState.value = state
                    return true
                }
            }
        }
        return false
    }

    private fun resetScreen() {
        mainParamCardItem.value = null
        motionMeasurement.value = null
        gatLabScreenState.value = SummaryListState.GaitLab.Loading
        insightsScreenState.value = SummaryListState.Insights.Loading
    }

    fun isImperialSystem(): Boolean = useImperialSystem(preferenceManager)

    fun updateMotionMeasurement() {
        viewModelScope.launch {
            motionMeasurement.value?.id?.let { motionMeasurement.value = getMotionMeasurement(it) }
        }
    }

    fun updateSixMinuteWalkCourseLength(
        uuid: String,
        value: Int,
    ) {
        viewModelScope.launch {
            resetScreen()

            val body = getWalkCourseLength(value, isImperialSystem())
            // Call the dedicated API endpoint
            recorderBridge.updateSixMinuteWalkCourseLength(uuid, body)
            // Re-analyze so results reflect the new course length
            recorderBridge.reset()
            recorderBridge.analyze(uuid)
            // Refresh the summary
            createSummaryItems(uuid)
        }
    }

    /**
     * Updates the hallway state based on the current motion measurement.
     * Should be called after motionMeasurement changes.
     */
    fun updateHallwayState() {
        val isSixOrTwoMinWalk = isSixOrTwoMinWalk(motionMeasurement.value?.type)
        val walkCourseLength =
            motionMeasurement.value?.metadata?.walkCourseLength?.toUserPreferredSystem(
                preferenceManager,
            )
        val isImperial = isImperialSystem()
        val unitText = resourceProvider.getString(if (isImperial) Res.string.unit_feet else Res.string.unit_meters)

        val laps = motionMeasurement.value?.paramsByName()?.get(OSTParamName.SIX_MINUTE_WALK_LAPS)

        // Warning active when: hallway exists AND (laps missing OR laps <= 0)
        val showsHallwayWarning =
            isSixOrTwoMinWalk &&
                    walkCourseLength?.value != null &&
                    (laps == null || laps <= 0f)

        // Show edit only when: hallway exists AND laps > 0
        val showEdit =
            isSixOrTwoMinWalk &&
                    walkCourseLength?.value != null &&
                    laps != null &&
                    laps > 0f

        hallwayState.value =
            hallwayState.value.copy(
                isSixMinuteWalk = isSixOrTwoMinWalk,
                hallwayLength = walkCourseLength?.value,
                hallwayUnitText = unitText,
                showHallwayEdit = showEdit,
                hallwayWarningActive = showsHallwayWarning,
            )
    }

    fun showHallwayEditDialog() {
        val displayValue = hallwayState.value.hallwayLength
        hallwayState.value =
            hallwayState.value.copy(
                showEditDialog = true,
                editValue = displayValue?.toString().orEmpty(),
                editError = null,
            )
    }

    fun hideHallwayEditDialog() {
        hallwayState.value =
            hallwayState.value.copy(
                showEditDialog = false,
                editValue = "",
                editError = null,
            )
    }

    fun updateHallwayEditValue(value: String) {
        val intValue = value.toIntOrNull()
        val isImperial = isImperialSystem()
        val (min, max) = hallwayRange(isImperial)

        val error =
            when {
                value.isEmpty() || intValue == null -> null
                intValue !in min..max -> {
                    val unitText = resourceProvider.getString(if (isImperial) Res.string.unit_feet else Res.string.unit_meters)
                    resourceProvider.getString(
                        Res.string.hallway_length_error_range,
                        min,
                        max,
                        unitText
                    )
                }

                else -> null
            }

        hallwayState.value =
            hallwayState.value.copy(
                editValue = value,
                editError = error,
            )
    }

    fun setHallwayEditError(error: String) {
        hallwayState.value = hallwayState.value.copy(editError = error)
    }

    fun OSTWalkCourseLength.toUserPreferredSystem(preferenceManager: PreferencesBridge): OSTWalkCourseLength {
        val shouldUseImperial = useImperialSystem(preferenceManager)

        return when {
            // Already in imperial and user wants imperial
            shouldUseImperial && unit == FEET_UNIT -> this

            // Already in metric and user wants metric
            !shouldUseImperial && unit == METERS_UNIT -> this

            // Convert from meters to feet
            shouldUseImperial && unit == METERS_UNIT -> {
                OSTWalkCourseLength(
                    value = (value * METERS_TO_FEET_RATIO).toInt(),
                    unit = FEET_UNIT,
                )
            }

            // Convert from feet to meters
            !shouldUseImperial && unit == FEET_UNIT -> {
                OSTWalkCourseLength(
                    value = (value / METERS_TO_FEET_RATIO).toInt(),
                    unit = METERS_UNIT,
                )
            }

            // Fallback: return as-is if unit is unrecognized
            else -> this
        }
    }

    private fun OSTMotionMeasurement.toMainParamCardItem(
        durationUnits: String,
        mainParamCircleColor: Color,
        defaultText: String,
        mainParamText: String? = null,
        mainParam: Float?,
    ): MainParamItem =
        MainParamItem(
            title = timestamp.toMainParamTitle(),
            mainParamValue = mainParam,
            duration = metadata.seconds?.toStringDuration() ?: defaultText,
            durationUnits = durationUnits,
            steps = metadata.steps ?: 0,
            animateMainParam = shouldAnimateMainParam(),
            showTabs = shouldShowTabs(),
            tabs =
                listOf(
                    OSTTabData(
                        resourceProvider.getString(Res.string.highlights),
                        0,
                    ),
                    OSTTabData(
                        resourceProvider.getString(Res.string.gait_lab),
                        1,
                    ),
                ),
            showTrashIcon = true,
            showMetadata = shouldShowMetaData(),
            showValues = true,
            mainParamText = mainParamText,
            mainParamColor = mainParamCircleColor,
            analysisBannerData = getBannerErrorData(mainParam),
            activityType = type,
            selfReport = metadata.selfReport,
            // STS-only manual edit affordance, gated by the SDK feature flag (mirrors uikit's
            // `editable = type == STS && SdkFlags.stsManualReportEnabled`).
            editable = type == OSTActivityType.STS &&
                UIKitServiceLocator.featureFlagsBridge.isEnabled(FeatureFlag.STS_MANUAL_REPORT),
        )

    private fun Float?.adjustToImperialIfNeeded(mainParam: OSTParamName?) =
        if (useImperialSystem(preferenceManager)) {
            this?.toImperial(motionDataBridge.getNormByName(mainParam)?.units)
        } else {
            this
        }

    private fun shouldAnimateMainParam(): Boolean =
        motionMeasurement.value?.type == OSTActivityType.WALK ||
                motionMeasurement.value?.type == OSTActivityType.DUAL_TASK_WALK_SUBTRACT ||
                motionMeasurement.value?.type == OSTActivityType.SIX_MINUTE_WALK ||
                motionMeasurement.value?.type == OSTActivityType.TWO_MINUTE_WALK

    private fun shouldShowMetaData(): Boolean =
        motionMeasurement.value?.type == OSTActivityType.WALK ||
                motionMeasurement.value?.type == OSTActivityType.DUAL_TASK_WALK_SUBTRACT ||
                motionMeasurement.value?.type == OSTActivityType.SIX_MINUTE_WALK ||
                motionMeasurement.value?.type == OSTActivityType.TWO_MINUTE_WALK

    private fun shouldShowTabs() =
        when {
            gatLabScreenState.value is SummaryListState.GaitLab.Error &&
                    insightsScreenState.value is SummaryListState.Insights.Error -> false

            motionMeasurement.value?.type == OSTActivityType.TUG -> false
            motionMeasurement.value?.type == OSTActivityType.STS -> false
            motionMeasurement.value?.type == OSTActivityType.ROM_KNEE_FLEX -> false
            motionMeasurement.value?.type == OSTActivityType.ROM_KNEE_EXT -> false
            else -> true
        }

    private fun getBannerErrorData(mainParam: Float?): AnalysisBannerData? {
        if (mainParam != null) return null
        return when {
            // if it's walk
            shouldShowMetaData() -> if (motionMeasurement.value?.resultState?.isPartialOrEmpty() == true && (motionMeasurement.value?.params
                    ?: emptyMap()).size < 3
            ) {
                AnalysisBannerData(
                    title = TextData(
                        resourceProvider.getString(Res.string.minimal_analysis_banner_title),
                        14.sp,
                        FontWeight.Bold
                    ),
                    subtitle = TextData(
                        resourceProvider.getString(Res.string.minimal_analysis_banner_subtitle),
                        14.sp,
                        FontWeight.Normal
                    ),
                )
            } else {
                AnalysisBannerData(
                    title = TextData(
                        resourceProvider.getString(Res.string.partial_analysis_banner_title),
                        14.sp,
                        FontWeight.Bold
                    ),
                    subtitle = TextData(
                        resourceProvider.getString(Res.string.partial_analysis_banner_subtitle),
                        14.sp,
                        FontWeight.Normal
                    ),
                )
            }

            else -> if (motionMeasurement.value?.error != null) {
                AnalysisBannerData(
                    subtitle = TextData(
                        resourceProvider.getString(Res.string.no_score_system_error_text),
                        14.sp,
                        FontWeight.Normal
                    )
                )
            } else {
                AnalysisBannerData(
                    subtitle = TextData(
                        resourceProvider.getString(Res.string.no_score_server_error_text),
                        14.sp,
                        FontWeight.Normal
                    )
                )
            }
        }
    }

    private fun OSTResultState?.isPartialOrEmpty(): Boolean =
        (this == OSTResultState.PARTIAL_ANALYSIS || this == OSTResultState.EMPTY_ANALYSIS)

    private fun Int?.toStringDuration(): String {
        val minutes = this?.div(60) ?: 0
        val remainingSeconds = this?.rem(60) ?: 0
        return "${minutes.toString().padStart(2, '0')}:${
            remainingSeconds.toString().padStart(2, '0')
        }"
    }

    private suspend fun createGaitLabItems(
        norms: Map<OSTParamName, OSTNorm>,
        params: Map<OSTParamName, Float>,
    ): MutableList<SummaryScreenItem> =
        withContext(Dispatchers.Default) {
            val items = mutableListOf<SummaryScreenItem>()
            // Group norms by the category of their metadata

            val groupedByCategorySorted = transformAndSortData(params, norms)

            groupedByCategorySorted.forEach { (category, gaitLabItems) ->
                // Add category header
                items.add(
                    SummaryScreenItem.GaitLabCategoryItem(
                        TextData(
                            text = category.uppercase(),
                            textSize = 20.sp,
                            fontWeight = FontWeight.W800,
                        ),
                    ),
                )

                // Add items under this category
                gaitLabItems.forEach { itemData ->
                    val (localizedValue, adjustedNorm) =
                        adjustToImperialIfNeeded(
                            itemData.value,
                            itemData.norm,
                            itemData.metadata,
                        )
                    val roundedValue =
                        localizedValue.roundValue(itemData.metadata, preferenceManager)

                    // Get previous value and apply same imperial conversion
                    val previousRawValue = previousMeasurementParams[itemData.paramName]
                    val previousLocalizedValue =
                        previousRawValue?.let { rawValue ->
                            val (prevLocalizedValue, _) =
                                adjustToImperialIfNeeded(
                                    rawValue,
                                    itemData.norm,
                                    itemData.metadata,
                                )
                            prevLocalizedValue.roundValue(itemData.metadata, preferenceManager)
                        }

                    adjustedNorm?.let {
                        // Format previous measurement timestamp
                        val previousLocalizedTime =
                            previousMeasurement?.timestamp?.toLocalizedTimeString()

                        val normItem =
                            SummaryScreenItem.GaitLabItem(
                                norm = it,
                                metaData = itemData.metadata,
                                value = roundedValue,
                                previousValue = previousLocalizedValue,
                                previousLocalizedTime = previousLocalizedTime,
                                units =
                                    if (useImperialSystem(preferenceManager)) {
                                        itemData.metadata.imperialUnits
                                            ?: itemData.metadata.units.orEmpty()
                                    } else {
                                        itemData.metadata.units.orEmpty()
                                    },
                            )
                        items.add(normItem)
                    }
                }
            }

            return@withContext items
        }

    /**
     * Adjusts value and norm to imperial if needed.
     * Returns KMP OSTNorm since GaitLabItem/ParameterItem expect KMP types.
     */
    private fun adjustToImperialIfNeeded(
        value: Float,
        norm: OSTNorm?,
        metadata: OSTParameterMetadata?,
    ): Pair<Float, OSTNorm?> {
        when {
            metadata?.imperialUnits == null -> return value to norm
            useImperialSystem(preferenceManager) -> {
                val conversion = norm.toImperial(value)
                return conversion.value to conversion.norm
            }

            else -> return value to norm
        }
    }

    private data class GaitLabItemData(
        val value: Float,
        val norm: OSTNorm,
        val metadata: OSTParameterMetadata,
        val paramName: OSTParamName,
    )

    private fun transformAndSortData(
        params: Map<OSTParamName, Float>,
        norms: Map<OSTParamName, OSTNorm>,
    ): Map<String, List<GaitLabItemData>> {
        // Create an initial mutable map to gather data by category
        val categorizedData =
            mutableMapOf<String, MutableList<GaitLabItemData>>()

        // Iterate through norms as it defines the basis of the data structure
        norms.forEach { (paramName, norm) ->
            // Retrieve corresponding value and metadata
            val value = params[paramName]
            val metadata = motionDataBridge.getParameterMetadata(paramName)

            // Continue only if value is available
            if (value != null) {
                val itemData = GaitLabItemData(value, norm, metadata, paramName)
                categorizedData.getOrPut(metadata.category) { mutableListOf() }.add(itemData)
            }
        }

        // Return the map with each category's list sorted by sortKey, and the map itself sorted by the first entry's sortKey in each list
        return categorizedData
            .mapValues { entry ->
                entry.value.sortedBy { it.metadata.sortKey }
            }.entries
            .sortedBy { (category, _) ->
                categorizedData[category]?.firstOrNull()?.metadata?.sortKey ?: Float.MAX_VALUE
            }
            .associate { it.toPair() }
    }

    private suspend fun createInsights(insights: List<OSTInsight>): MutableList<SummaryScreenItem> =
        withContext(Dispatchers.Default) {
            val insightsItems =
                mutableListOf<SummaryScreenItem>().apply {
                    insights.forEach { insight ->
                        when (insight.insightType) {
                            OSTInsightType.TREND ->
                                add(
                                    SummaryScreenItem.TrendItem(
                                        TextData(
                                            insight.textMarkdown,
                                            textSize = 20.sp,
                                            fontWeight = FontWeight.Normal,
                                        ),
                                        icon =
                                            IconData(
                                                insight.insightType?.toIcon(insight.intent)
                                                    ?: Res.drawable.default_insight,
                                            ),
                                    ),
                                )

                            OSTInsightType.EDUCATION ->
                                add(
                                    SummaryScreenItem.EducationalItem(
                                        TextData(
                                            insight.textMarkdown,
                                            textSize = 20.sp,
                                            fontWeight = FontWeight.Normal,
                                        ),
                                        icon = IconData(Res.drawable.ic_attention),
                                    ),
                                )

                            OSTInsightType.INFO ->
                                add(
                                    SummaryScreenItem.InfoItem(
                                        TextData(
                                            insight.textMarkdown,
                                            textSize = 20.sp,
                                            fontWeight = FontWeight.Normal,
                                        ),
                                        icon = IconData(Res.drawable.ic_info_circle),
                                    ),
                                )

                            OSTInsightType.PARAMETER -> {
                                val paramName = insight.paramName
                                val rawValue = paramName?.let {
                                    motionMeasurement.value?.paramsByName()?.get(it)
                                } ?: 0f

                                // Retrieve the norm and metadata for this parameter, if available.
                                val norm = paramName?.let { motionDataBridge.getNormByName(it) }
                                val metadata =
                                    paramName?.let { motionDataBridge.getParameterMetadata(it) }

                                // Adjust to imperial units if needed.
                                val (localizedValue, adjustedNorm) =
                                    adjustToImperialIfNeeded(
                                        rawValue,
                                        norm,
                                        metadata,
                                    )
                                // Round the localized value using the metadata (if available).
                                val roundedValue =
                                    metadata?.let {
                                        localizedValue.roundValue(
                                            it,
                                            preferenceManager,
                                        )
                                    }
                                        ?: localizedValue

                                // Get previous value and apply same imperial conversion
                                val previousRawValue =
                                    paramName?.let { previousMeasurementParams[it] }
                                val previousLocalizedValue =
                                    previousRawValue?.let { rawValue ->
                                        val (prevLocalizedValue, _) =
                                            adjustToImperialIfNeeded(
                                                rawValue,
                                                norm,
                                                metadata,
                                            )
                                        metadata?.let {
                                            prevLocalizedValue.roundValue(it, preferenceManager)
                                        } ?: prevLocalizedValue
                                    }

                                // Format previous measurement timestamp
                                val previousLocalizedTime =
                                    previousMeasurement?.timestamp?.toLocalizedTimeString()

                                add(
                                    SummaryScreenItem.ParameterItem(
                                        TextData(
                                            insight.textMarkdown,
                                            textSize = 20.sp,
                                            fontWeight = FontWeight.Normal,
                                        ),
                                        icon =
                                            IconData(
                                                insight.insightType?.toIcon(insight.intent)
                                                    ?: Res.drawable.default_insight,
                                            ),
                                        value = roundedValue,
                                        norm = adjustedNorm,
                                        metaData =
                                            insight.paramName?.let { param ->
                                                motionDataBridge.getParameterMetadata(
                                                    param,
                                                )
                                            },
                                        previousValue = previousLocalizedValue,
                                        previousLocalizedTime = previousLocalizedTime,
                                        displayName =
                                            metadata?.displayName
                                                ?: insight.defaultName(),
                                        units =
                                            if (useImperialSystem(preferenceManager)) {
                                                metadata?.imperialUnits
                                                    ?: metadata?.units.orEmpty()
                                            } else {
                                                metadata?.units.orEmpty()
                                            },
                                    ),
                                )
                            }

                            OSTInsightType.FALL_RISK ->
                                add(
                                    SummaryScreenItem.FallRiskItem(
                                        TextData(
                                            insight.textMarkdown,
                                            textSize = 20.sp,
                                            fontWeight = FontWeight.Normal,
                                        ),
                                        icon = IconData(Res.drawable.ic_attention),
                                    ),
                                )

                            OSTInsightType.COMPARISON -> Unit
                            null -> Unit
                        }
                    }
                }
            return@withContext insightsItems
        }

    private fun OSTInsight.defaultName() = paramName?.columnName?.replace("_", " ").orEmpty()

    private suspend fun getMotionMeasurement(motionMeasurementId: String): OSTMotionMeasurement? =
        recorderBridge.readSingleMotionMeasurement(motionMeasurementId)

    private suspend fun getPreviousMeasurement(currentTimestamp: Long): OSTMotionMeasurement? {
        val currentActivityType = motionMeasurement.value?.type?.serializedName
        val request =
            OSTTimeRangedDataRequest(
                limit = 1,
                order = OSTOrder.DESCENDING,
                timeRangeFilter = OSTTimeRangeFilter.before(currentTimestamp),
                activityType = currentActivityType,
            )
        return recorderBridge.readMotionMeasurements(request).firstOrNull()
    }

    private fun getMeasurementCircleColor(): Color =
        motionMeasurement.value?.let {
            val discreteScore =
                motionDataBridge
                    .discreteScore(
                        it,
                        motionDataBridge.mainParam(it)?.second ?: 0f,
                    )
            discreteScore?.value?.toBubbleColor()
        } ?: Color.Gray

    fun deleteMotionMeasurement(uuid: String) {
        viewModelScope.launch {
            recorderBridge.deleteMotionMeasurement(uuid)
        }
    }

    /**
     * Returns `null` if *any* of the required params is missing,
     * or a fully non-null TugComponentData otherwise.
     */
    private fun OSTMotionMeasurement.toTugItemOrNull(): TugComponentData? {
        val paramMap = paramsByName()
        // grab each one, returning null immediately if any are missing
        val forward = paramMap[OSTParamName.TUG_FORWARD_SECONDS] ?: return null
        val backward = paramMap[OSTParamName.TUG_BACKWARD_SECONDS] ?: return null
        val turning = paramMap[OSTParamName.TUG_TURNING_SECONDS] ?: return null
        val turningToChair = paramMap[OSTParamName.TUG_TURNING_TO_CHAIR_SECONDS] ?: return null
        val distance = paramMap[OSTParamName.TUG_DISTANCE_METERS] ?: return null

        // chair sub-section
        val standing = paramMap[OSTParamName.TUG_STANDING_SECONDS] ?: return null
        val sitting = paramMap[OSTParamName.TUG_SITTING_SECONDS] ?: return null

        val duration = paramMap[OSTParamName.TUG_DURATION_SECONDS] ?: return null

        // if we got here, none were null
        return TugComponentData(
            forward = forward,
            backward = backward,
            turning = turning,
            turningToChair = turningToChair,
            distance = distance,
            tugChairData =
                TugChairData(
                    standing = standing,
                    sitting = sitting,
                ),
            duration = duration,
        )
    }

    // ===== Local helper functions for core types =====
    // These bridge core SDK types to KMP utility function equivalents,
    // since the KMP utils expect KMP model types.

    /**
     * Rounds this Float using OSTParameterMetadata, matching the behavior
     * of the KMP roundValue that takes OSTParameterMetadata.
     */
    private fun Float.roundValue(
        metadata: OSTParameterMetadata,
        preferenceManager: PreferencesBridge,
    ): Float {
        val expandRounding = (useImperialSystem(preferenceManager) && metadata.units == CM_UNITS)
        val roundDigits = when {
            expandRounding -> metadata.roundDigits?.plus(1)
            else -> metadata.roundDigits
        }
        return when (roundDigits) {
            0f -> toInt().toFloat()
            else -> {
                var multiplier = 1f
                repeat(roundDigits?.toInt() ?: 0) { multiplier *= 10 }
                kotlin.math.round(this * multiplier) / multiplier
            }
        }
    }

    /**
     * Converts an OSTNorm to imperial units, matching the behavior
     * of the KMP OSTNorm?.toImperial extension.
     */
    private fun OSTNorm?.toImperial(value: Float): ConversionResult {
        val units = this?.units
        return when (units) {
            co.onestep.kmp.uikit.utils.METERS_PER_SECOND_UNITS -> {
                val factor = METERS_TO_FEET_RATIO
                ConversionResult(
                    value * factor,
                    this?.copy(
                        parts = this.parts?.map { part ->
                            OSTNormPart(
                                start = part.start * factor,
                                end = part.end * factor,
                                color = part.color,
                                includeStart = part.includeStart,
                                includeEnd = part.includeEnd,
                            )
                        },
                    ),
                )
            }

            CM_UNITS -> {
                val factor = co.onestep.kmp.uikit.utils.CM_TO_INCHES_RATIO
                ConversionResult(
                    value * factor,
                    this?.copy(
                        parts = this.parts?.map { part ->
                            OSTNormPart(
                                start = part.start * factor,
                                end = part.end * factor,
                                color = part.color,
                                includeStart = part.includeStart,
                                includeEnd = part.includeEnd,
                            )
                        },
                    ),
                )
            }

            co.onestep.kmp.uikit.utils.METERS_UNITS, co.onestep.kmp.uikit.utils.M_UNITS -> {
                val factor = METERS_TO_FEET_RATIO
                ConversionResult(
                    value * factor,
                    this?.copy(
                        parts = this.parts?.map { part ->
                            OSTNormPart(
                                start = part.start * factor,
                                end = part.end * factor,
                                color = part.color,
                                includeStart = part.includeStart,
                                includeEnd = part.includeEnd,
                            )
                        },
                    ),
                )
            }

            else -> ConversionResult(value, this)
        }
    }

    /**
     * Converts a Float to imperial units based on unit string,
     * matching the KMP Float.toImperial(units: String?) extension.
     */
    private fun Float.toImperial(units: String?): Float =
        when (units) {
            co.onestep.kmp.uikit.utils.METERS_PER_SECOND_UNITS -> this * METERS_TO_FEET_RATIO
            CM_UNITS -> this / co.onestep.kmp.uikit.utils.CM_TO_INCHES_RATIO
            co.onestep.kmp.uikit.utils.METERS_UNITS -> this * METERS_TO_FEET_RATIO
            co.onestep.kmp.uikit.utils.M_UNITS -> this * METERS_TO_FEET_RATIO
            else -> this
        }
}
