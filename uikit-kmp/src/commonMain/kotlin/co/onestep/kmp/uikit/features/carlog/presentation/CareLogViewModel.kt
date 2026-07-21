package co.onestep.kmp.uikit.features.carlog.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.onestep.kmp.uikit.bridge.MotionDataBridge
import co.onestep.kmp.uikit.bridge.OSTSDKBridge
import co.onestep.kmp.uikit.bridge.PreferencesBridge
import co.onestep.kmp.uikit.bridge.RecorderBridge
import co.onestep.kmp.uikit.features.carlog.models.BackgroundLogItemData
import co.onestep.kmp.uikit.features.carlog.models.BackgroundScreenState
import co.onestep.kmp.uikit.features.carlog.models.CarLogItemData
import co.onestep.kmp.uikit.features.carlog.models.CarLogScreenState
import co.onestep.kmp.uikit.features.carlog.models.InAppScreenState
import co.onestep.kmp.uikit.features.carlog.models.InfoData
import co.onestep.kmp.uikit.features.carlog.models.MeasurementItemData
import co.onestep.kmp.uikit.features.carlog.models.NavigationCommand
import co.onestep.kmp.uikit.features.carlog.models.NoticeCardData
import co.onestep.kmp.uikit.features.carlog.models.NoticeCardType
import co.onestep.kmp.uikit.features.carlog.models.PendingMeasurementItemData
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTRecordingConfiguration
import co.onestep.kmp.uikit.features.recordFlow.screensData.PrimaryButtonData
import co.onestep.kmp.uikit.features.recordFlow.screensData.IconData
import co.onestep.kmp.uikit.features.recordFlow.screensData.TextData
import co.onestep.kmp.uikit.features.recordFlow.screensData.ToolBarData
import co.onestep.kmp.uikit.models.OSTActivityType
import co.onestep.kmp.uikit.models.OSTDailyBackgroundMeasurement
import co.onestep.kmp.uikit.models.OSTMotionMeasurement
import co.onestep.kmp.uikit.models.OSTOrder
import co.onestep.kmp.uikit.models.OSTParamName
import co.onestep.kmp.uikit.models.OSTState
import co.onestep.kmp.uikit.models.OSTTimeRangeFilter
import co.onestep.kmp.uikit.models.OSTTimeRangedDataRequest
import co.onestep.kmp.uikit.models.OSTTimeRangeSlicer
import co.onestep.kmp.sdk.currentTimeMillis
import co.onestep.kmp.uikit.utils.ResourceProvider
import co.onestep.kmp.uikit.utils.toColor
import co.onestep.kmp.uikit.utils.toColorDescription
import co.onestep.kmp.uikit.utils.toDeviceTimeString
import co.onestep.kmp.uikit.utils.toLocalizedTimeString
import co.onestep.kmp.uikit.utils.toPartColor
import co.onestep.kmp.uikit.utils.toFormattedDuration
import co.onestep.kmp.uikit.utils.toRoundedImperialString
import co.onestep.kmp.uikit.utils.toStringOrDefault
import co.onestep.kmp.uikit.utils.units
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.angle_degrees
import co.onestep.kmp.uikit_kmp.generated.resources.background_recordings_are_measurements_taken_automatically_without_actively_accessing_the_onestep_app_and_only_after_permissions_are_approved
import co.onestep.kmp.uikit_kmp.generated.resources.distance
import co.onestep.kmp.uikit_kmp.generated.resources.distance_default_unit
import co.onestep.kmp.uikit_kmp.generated.resources.duration_sec
import co.onestep.kmp.uikit_kmp.generated.resources.enable_background_monitoring
import co.onestep.kmp.uikit_kmp.generated.resources.enable_permissions
import co.onestep.kmp.uikit_kmp.generated.resources.ic_6minwalk
import co.onestep.kmp.uikit_kmp.generated.resources.ic_close
import co.onestep.kmp.uikit_kmp.generated.resources.ic_dual_task
import co.onestep.kmp.uikit_kmp.generated.resources.ic_knee_extention
import co.onestep.kmp.uikit_kmp.generated.resources.ic_location_services
import co.onestep.kmp.uikit_kmp.generated.resources.ic_sts
import co.onestep.kmp.uikit_kmp.generated.resources.ic_tug
import co.onestep.kmp.uikit_kmp.generated.resources.ic_walks
import co.onestep.kmp.uikit_kmp.generated.resources.no_background_data_collected
import co.onestep.kmp.uikit_kmp.generated.resources.no_background_permission_notice_text
import co.onestep.kmp.uikit_kmp.generated.resources.no_recorded_walks
import co.onestep.kmp.uikit_kmp.generated.resources.number_of_reps_reps
import co.onestep.kmp.uikit_kmp.generated.resources.number_of_reps_reps_null
import co.onestep.kmp.uikit_kmp.generated.resources.once_a_walk_is_recorded_you_can_view_it_here
import co.onestep.kmp.uikit_kmp.generated.resources.record_a_walk
import co.onestep.kmp.uikit_kmp.generated.resources.score_with_colons_and_variable
import co.onestep.kmp.uikit_kmp.generated.resources.unavailable
import co.onestep.kmp.uikit_kmp.generated.resources.walk_score_daily_average
import co.onestep.kmp.uikit_kmp.generated.resources.your_background_monitoring_is_currently_off
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class CareLogViewModel(
    private val resourceProvider: ResourceProvider,
    private val preferencesBridge: PreferencesBridge,
    private val recorderBridge: RecorderBridge,
    private val motionDataBridge: MotionDataBridge,
    private val sdkBridge: OSTSDKBridge,
) : ViewModel() {

    private val _navigationCommand = MutableStateFlow<NavigationCommand?>(null)
    val navigationCommand: StateFlow<NavigationCommand?> get() = _navigationCommand

    private var requirePermission: Boolean = true
    private var includeBackgroundData: Boolean = true
    var recordingConfiguration: OSTRecordingConfiguration? = null
    private var showCloseButton: Boolean = true

    private var _inAppState = MutableStateFlow<CarLogScreenState>(CarLogScreenState.Loading)
    val inAppState: StateFlow<CarLogScreenState> =
        _inAppState
            .onStart { fetchCareLog() }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000L),
                CarLogScreenState.Loading,
            )

    private var _backgroundScreenState = MutableStateFlow<CarLogScreenState?>(null)
    val backgroundScreenState: StateFlow<CarLogScreenState?> =
        _backgroundScreenState
            .onStart {
                if (!includeBackgroundData) {
                    _backgroundScreenState.value = null
                }
                fetchBackground()
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000L),
                null,
            )

    val toolbarState = mutableStateOf<ToolBarData?>(ToolBarData())

    fun setupToolBar(endAction: (() -> Unit)? = null) {
        toolbarState.value =
            if (showCloseButton) {
                ToolBarData(
                    endIcons = listOf(
                        IconData(
                            icon = Res.drawable.ic_close,
                            action = endAction,
                        ),
                    ),
                )
            } else {
                null
            }
    }

    fun setPermissionState(require: Boolean) {
        requirePermission = require
    }

    private fun startRecording() {
        _navigationCommand.value = NavigationCommand.ToRecordingFlow(recordingConfiguration)
    }

    fun goToSummary(measurementId: String) {
        _navigationCommand.value = NavigationCommand.ToSummary(measurementId)
    }

    private fun goToPermissionFlow() {
        _navigationCommand.value = NavigationCommand.ToPermissionFlow
    }

    fun clearNavigationCommand() {
        _navigationCommand.value = null
    }

    fun setConfiguration(recordingConfiguration: OSTRecordingConfiguration?) {
        this.recordingConfiguration = recordingConfiguration
    }

    fun showCloseButton(showCloseButton: Boolean) {
        this.showCloseButton = showCloseButton
    }

    fun fetchBackground() {
        viewModelScope.launch {
            val records = sdkBridge.getDailySummaries()

            val noticeCards = mutableListOf<NoticeCardData>().apply {
                add(
                    NoticeCardData(
                        type = NoticeCardType.Permissions,
                        textData = TextData(
                            text = resourceProvider.getString(Res.string.no_background_permission_notice_text),
                            textSize = 18.sp,
                            fontWeight = FontWeight.W400,
                        ),
                        button = PrimaryButtonData(
                            text = TextData(
                                text = resourceProvider.getString(Res.string.enable_permissions),
                                textSize = 18.sp,
                                fontWeight = FontWeight.W600,
                            ),
                            action = { goToPermissionFlow() },
                        ),
                    ).apply {
                        isVisible = requirePermission
                    },
                )
                add(
                    NoticeCardData(
                        type = NoticeCardType.BackgroundMonitoring,
                        textData = TextData(
                            text = resourceProvider.getString(Res.string.your_background_monitoring_is_currently_off),
                            textSize = 18.sp,
                            fontWeight = FontWeight.W400,
                        ),
                        button = PrimaryButtonData(
                            text = TextData(
                                text = resourceProvider.getString(Res.string.enable_background_monitoring),
                                textSize = 18.sp,
                                fontWeight = FontWeight.W600,
                            ),
                            action = { sdkBridge.optInToMonitoring() },
                        ),
                    ).apply {
                        isVisible = when {
                            requirePermission -> false
                            sdkBridge.sdkState.value is OSTState.Identified -> !sdkBridge.isMonitoringActive
                            else -> false
                        }
                    },
                )
            }

            _backgroundScreenState.value =
                if (!includeBackgroundData) {
                    null
                } else if (records.isEmpty()) {
                    CarLogScreenState.Empty(
                        iconData = IconData(icon = Res.drawable.ic_location_services),
                        title = TextData(
                            text = resourceProvider.getString(Res.string.no_background_data_collected),
                            textSize = 28.sp,
                            fontWeight = FontWeight.W700,
                        ),
                        noticeCards = noticeCards,
                    )
                } else {
                    val ostNorm = motionDataBridge.getNormByName(OSTParamName.WALKING_WALK_SCORE)
                    BackgroundScreenState.Content(
                        records.toBackgroundCarLogItems(),
                        noticeCards,
                        infoData = InfoData(
                            title = resourceProvider.getString(Res.string.walk_score_daily_average),
                            subtitle = resourceProvider.getString(Res.string.background_recordings_are_measurements_taken_automatically_without_actively_accessing_the_onestep_app_and_only_after_permissions_are_approved),
                            infos = ostNorm?.parts?.map { part ->
                                InfoData.Info(
                                    text = "${part.start.toInt()} - ${part.end.toInt()} ${part.color.toColorDescription(resourceProvider)}",
                                    color = part.color.toPartColor(),
                                )
                            } ?: emptyList(),
                        ),
                    )
                }
        }
    }

    fun fetchCareLog() {
        viewModelScope.launch {
            val request = OSTTimeRangedDataRequest(
                order = OSTOrder.DESCENDING,
                timeRangeFilter = OSTTimeRangeFilter.before(
                    currentTimeMillis(),
                    OSTTimeRangeSlicer.DAY,
                ),
            )
            val measurements = recorderBridge.readMotionMeasurements(request)

            _inAppState.value =
                if (measurements.isEmpty()) {
                    CarLogScreenState.Empty(
                        iconData = IconData(icon = Res.drawable.ic_walks),
                        title = TextData(
                            text = resourceProvider.getString(Res.string.no_recorded_walks),
                            textSize = 28.sp,
                            fontWeight = FontWeight.W700,
                        ),
                        subtitle = TextData(
                            text = resourceProvider.getString(Res.string.once_a_walk_is_recorded_you_can_view_it_here),
                            textSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                        ),
                        buttonData = PrimaryButtonData(
                            text = TextData(
                                text = resourceProvider.getString(Res.string.record_a_walk),
                                textSize = 20.sp,
                                fontWeight = FontWeight.W600,
                            ),
                            action = { startRecording() },
                        ),
                        noticeCards = mutableListOf(),
                    )
                } else {
                    InAppScreenState.Content(measurements.toCarLogItems(), mutableListOf())
                }
        }
    }

    fun includeBackgroundData(shouldIncludeBackgroundData: Boolean) {
        includeBackgroundData = shouldIncludeBackgroundData
        _backgroundScreenState.value =
            if (shouldIncludeBackgroundData) {
                CarLogScreenState.Loading
            } else {
                null
            }
    }

    private fun List<OSTMotionMeasurement>.toCarLogItems(): List<CarLogItemData> =
        this.map { measurement ->
            when (measurement.status) {
                OSTMotionMeasurement.MotionMeasurementStatus.ANALYZED -> measurement.toMeasurementItemData()
                else -> measurement.toPendingMeasurementItemData()
            }
        }

    private fun OSTMotionMeasurement.toPendingMeasurementItemData() =
        PendingMeasurementItemData(
            id = id,
            day = timestamp.toDateString(),
            type = type,
            title = type.displayNameKey,
            duration = "${metadata.seconds.toFormattedDuration(padMinutes = false)} min",
            time = timestamp.toDeviceTimeString(),
            icon = Res.drawable.ic_walks,
        )

    private fun OSTMotionMeasurement.toMeasurementItemData(): MeasurementItemData {
        val icon = type.toLogIcon()

        val mainParam = when (type) {
            OSTActivityType.WALK, OSTActivityType.DUAL_TASK_WALK_SUBTRACT ->
                resourceProvider.getString(
                    Res.string.score_with_colons_and_variable,
                    motionDataBridge.mainParam(this)?.second
                        .toStringOrDefault(resourceProvider.getString(Res.string.unavailable)),
                )

            OSTActivityType.STS -> {
                val quantity = params[OSTParamName.STS_REPETITION_COUNT.columnName]?.toInt()
                quantity?.let {
                    resourceProvider.getQuantityString(Res.plurals.number_of_reps_reps, quantity)
                } ?: resourceProvider.getString(Res.string.number_of_reps_reps_null)
            }

            OSTActivityType.TUG -> {
                val mainParamKey = motionDataBridge.mainParam(this)?.first ?: OSTParamName.TUG_DURATION_SECONDS
                val parameterMetadata = motionDataBridge.getParameterMetadata(mainParamKey)
                resourceProvider.getString(
                    Res.string.duration_sec,
                    params[mainParamKey.columnName]
                        .toRoundedImperialString(
                            resourceProvider.getString(Res.string.unavailable),
                            preferencesBridge,
                            parameterMetadata,
                        ),
                )
            }

            OSTActivityType.ROM_KNEE_FLEX ->
                resourceProvider.getString(
                    Res.string.angle_degrees,
                    params[OSTParamName.RANGE_OF_MOTION_ANGLE.columnName]
                        ?.toInt()?.toString()
                        ?: resourceProvider.getString(Res.string.unavailable),
                )

            OSTActivityType.ROM_KNEE_EXT ->
                resourceProvider.getString(
                    Res.string.angle_degrees,
                    params[OSTParamName.KNEE_EXT_RANGE_OF_MOTION_ANGLE.columnName]
                        ?.toInt()?.toString()
                        ?: resourceProvider.getString(Res.string.unavailable),
                )

            OSTActivityType.SIX_MINUTE_WALK, OSTActivityType.TWO_MINUTE_WALK -> {
                val defaultKey = if (type == OSTActivityType.SIX_MINUTE_WALK)
                    OSTParamName.SIX_MINUTE_WALK_DISTANCE_METERS
                else
                    OSTParamName.TWO_MINUTE_WALK_DISTANCE_METERS
                val mainParamKey = motionDataBridge.mainParam(this)?.first ?: defaultKey
                val parameterMetadata = motionDataBridge.getParameterMetadata(mainParamKey)
                val unitsStr = parameterMetadata.units(preferencesBridge)
                resourceProvider.getString(
                    Res.string.distance,
                    params[mainParamKey.columnName]
                        .toRoundedImperialString(
                            resourceProvider.getString(Res.string.unavailable),
                            preferencesBridge,
                            parameterMetadata,
                        ),
                    unitsStr ?: resourceProvider.getString(Res.string.distance_default_unit),
                )
            }

            else -> null
        }

        return MeasurementItemData(
            id = id,
            day = timestamp.toDateString(),
            type = type,
            title = type.displayNameKey,
            duration = if (type == OSTActivityType.TUG) null else "${metadata.seconds.toFormattedDuration(padMinutes = false)} min",
            time = timestamp.toDeviceTimeString().uppercase(),
            icon = icon,
            mainParam = mainParam.orEmpty(),
            tags = metadata.tags.joinToString { it },
            assistiveDevice = metadata.assistiveDevice?.let { "Device $it" },
            levelOfAssistance = metadata.levelOfAssistance?.let { "Level $it" },
            note = metadata.note,
        )
    }

    private fun List<OSTDailyBackgroundMeasurement>.toBackgroundCarLogItems(): Map<String, List<BackgroundLogItemData>> {
        val dateToValue = mutableMapOf<String, Float>()
        val dateToMonth = mutableMapOf<String, String>()

        for (record in this) {
            val dateLocal = record.dateLocal
            val value = record.parameters[OSTParamName.WALKING_WALK_SCORE.columnName] ?: 0f
            val existingValue = dateToValue[dateLocal] ?: 0f
            dateToValue[dateLocal] = maxOf(existingValue, value)

            val parts = dateLocal.split("-")
            if (parts.size >= 2) {
                val monthKey = "${parts[0]}-${parts[1]}"
                dateToMonth[dateLocal] = monthKey
            }
        }

        val result = mutableMapOf<String, MutableList<BackgroundLogItemData>>()
        for ((dateLocal, value) in dateToValue.entries.sortedByDescending { it.key }) {
            val monthKey = dateToMonth[dateLocal] ?: continue
            val parts = dateLocal.split("-")
            val day = parts.getOrNull(2)?.trimStart('0') ?: continue
            val color = value.toColor(motionDataBridge)

            result.getOrPut(monthKey) { mutableListOf() }.add(
                BackgroundLogItemData(
                    day = day,
                    value = value,
                    color = color,
                )
            )
        }
        return result
    }
}

private fun Long.toDateString(): String {
    val full = this.toLocalizedTimeString()
    return full.split("|").firstOrNull()?.trim() ?: full
}

private fun OSTActivityType?.toLogIcon() = when (this) {
    OSTActivityType.TUG -> Res.drawable.ic_tug
    OSTActivityType.STS -> Res.drawable.ic_sts
    OSTActivityType.ROM_KNEE_FLEX, OSTActivityType.ROM_KNEE_EXT -> Res.drawable.ic_knee_extention
    OSTActivityType.DUAL_TASK_WALK_SUBTRACT -> Res.drawable.ic_dual_task
    OSTActivityType.SIX_MINUTE_WALK, OSTActivityType.TWO_MINUTE_WALK -> Res.drawable.ic_6minwalk
    else -> Res.drawable.ic_walks
}
