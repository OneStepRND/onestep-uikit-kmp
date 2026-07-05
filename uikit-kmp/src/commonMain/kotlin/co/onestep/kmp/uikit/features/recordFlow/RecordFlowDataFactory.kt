package co.onestep.kmp.uikit.features.recordFlow

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.kmp.uikit.features.recordFlow.configurations.OSTRecordingQuestionData
import co.onestep.kmp.uikit.features.tagging.models.Footwear
import co.onestep.kmp.uikit.models.OSTAssistiveDevice
import co.onestep.kmp.uikit.models.displayName
import co.onestep.kmp.uikit.models.icon
import co.onestep.kmp.uikit.data.PocketLocation
import co.onestep.kmp.uikit.features.recordFlow.screensData.PrimaryButtonData
import co.onestep.kmp.uikit.features.recordFlow.screensData.IconData
import co.onestep.kmp.uikit.features.recordFlow.screensData.MainButtonData
import co.onestep.kmp.uikit.features.recordFlow.screensData.SecondaryButtonData
import co.onestep.kmp.uikit.features.recordFlow.screensData.SelectionItemData
import co.onestep.kmp.uikit.features.recordFlow.screensData.SelectionListData
import co.onestep.kmp.uikit.features.recordFlow.screensData.TextData
import co.onestep.kmp.uikit.features.recordFlow.screensData.NoteBannerData
import co.onestep.kmp.uikit.features.recordFlow.screensData.UiKitScreenData
import co.onestep.kmp.uikit.features.permissions.PermissionScreenData
import co.onestep.kmp.uikit.models.OSTActivityType
import co.onestep.kmp.uikit.ui.components.InstructionContent
import co.onestep.kmp.uikit.ui.components.StyledSegment
import co.onestep.kmp.uikit.utils.ResourceProvider
import co.onestep.kmp.uikit_kmp.generated.resources.Res
import co.onestep.kmp.uikit_kmp.generated.resources.Reload
import co.onestep.kmp.uikit_kmp.generated.resources.choose_your_assistive_device
import co.onestep.kmp.uikit_kmp.generated.resources.choose_your_footwear
import co.onestep.kmp.uikit_kmp.generated.resources.a_connection_issue
import co.onestep.kmp.uikit_kmp.generated.resources.allow
import co.onestep.kmp.uikit_kmp.generated.resources.continue_camel_case
import co.onestep.kmp.uikit_kmp.generated.resources.finish
import co.onestep.kmp.uikit_kmp.generated.resources.five_minutes
import co.onestep.kmp.uikit_kmp.generated.resources.for_a_successful_analysis_you_need_to_walk_in_a_straight_line
import co.onestep.kmp.uikit_kmp.generated.resources.go_to_settings
import co.onestep.kmp.uikit_kmp.generated.resources.go_to_the_device_settings_and_then_toggle_on_microphone
import co.onestep.kmp.uikit_kmp.generated.resources.how_long_do_you_want_to_walk_today
import co.onestep.kmp.uikit_kmp.generated.resources.ic_clock_red_stars
import co.onestep.kmp.uikit_kmp.generated.resources.ic_connectivity_red_stars
import co.onestep.kmp.uikit_kmp.generated.resources.ic_edit
import co.onestep.kmp.uikit_kmp.generated.resources.ic_info_circle
import co.onestep.kmp.uikit_kmp.generated.resources.ic_knee_red_stars
import co.onestep.kmp.uikit_kmp.generated.resources.ic_microphone_stars
import co.onestep.kmp.uikit_kmp.generated.resources.ic_motion_red_stars
import co.onestep.kmp.uikit_kmp.generated.resources.ic_routes_red_stars
import co.onestep.kmp.uikit_kmp.generated.resources.ic_server_red_stars
import co.onestep.kmp.uikit_kmp.generated.resources.ic_sts_red_stars
import co.onestep.kmp.uikit_kmp.generated.resources.ic_timeout
import co.onestep.kmp.uikit_kmp.generated.resources.ic_tug_red_stars
import co.onestep.kmp.uikit_kmp.generated.resources.ic_phone_orientation
import co.onestep.kmp.uikit_kmp.generated.resources.ic_volume_stars
import co.onestep.kmp.uikit_kmp.generated.resources.ic_walk_red_stars
import co.onestep.kmp.uikit_kmp.generated.resources.ic_warning_red_stars
import co.onestep.kmp.uikit_kmp.generated.resources.static_balance_banner_body
import co.onestep.kmp.uikit_kmp.generated.resources.static_balance_banner_title
import co.onestep.kmp.uikit_kmp.generated.resources.it_seems_you_have_internet_connection_issues_check_your_internet_connection_and_press_reload_the_measurement_will_be_uploaded_and_analyzed_once_we_get_internet_connection
import co.onestep.kmp.uikit_kmp.generated.resources.long_walk
import co.onestep.kmp.uikit_kmp.generated.resources.long_walk_description
import co.onestep.kmp.uikit_kmp.generated.resources.make_sure_your_phone_is_not_on_silent_mode_and_the_volume_is_set_to_maximum
import co.onestep.kmp.uikit_kmp.generated.resources.one_minute
import co.onestep.kmp.uikit_kmp.generated.resources.oops
import co.onestep.kmp.uikit_kmp.generated.resources.please_make_sure_is_placed_snugly_against_your_thigh_and_follow_the_measurement_instructions
import co.onestep.kmp.uikit_kmp.generated.resources.rom_error_position_description
import co.onestep.kmp.uikit_kmp.generated.resources.rom_static_error_description
import co.onestep.kmp.uikit_kmp.generated.resources.skip
import co.onestep.kmp.uikit_kmp.generated.resources.start
import co.onestep.kmp.uikit_kmp.generated.resources.static_balance_too_short_error_description
import co.onestep.kmp.uikit_kmp.generated.resources.sts_enter_results_manually
import co.onestep.kmp.uikit_kmp.generated.resources.sts_position_error_description
import co.onestep.kmp.uikit_kmp.generated.resources.sts_static_error_description
import co.onestep.kmp.uikit_kmp.generated.resources.sts_too_short_error_description
import co.onestep.kmp.uikit_kmp.generated.resources.there_was_a_problem_connecting_to_the_server_please_try_again_later
import co.onestep.kmp.uikit_kmp.generated.resources.this_measurement_s_recording_was_too_short
import co.onestep.kmp.uikit_kmp.generated.resources.three_minutes
import co.onestep.kmp.uikit_kmp.generated.resources.timeout_screen_description
import co.onestep.kmp.uikit_kmp.generated.resources.timeout_screen_title
import co.onestep.kmp.uikit_kmp.generated.resources.to_perform_this_test_grant_access_to_the_microphone
import co.onestep.kmp.uikit_kmp.generated.resources.try_again
import co.onestep.kmp.uikit_kmp.generated.resources.tug_position_error_description
import co.onestep.kmp.uikit_kmp.generated.resources.tug_static_error_description
import co.onestep.kmp.uikit_kmp.generated.resources.tug_too_short_error_description
import co.onestep.kmp.uikit_kmp.generated.resources.view_instructions
import co.onestep.kmp.uikit_kmp.generated.resources.walk_position_error_description
import co.onestep.kmp.uikit_kmp.generated.resources.walk_repetetive_movement_error_description
import co.onestep.kmp.uikit_kmp.generated.resources.walk_static_error_description
import co.onestep.kmp.uikit_kmp.generated.resources.walk_too_short_error_description
import co.onestep.kmp.uikit_kmp.generated.resources.we_couldn_t_capture_your_steps
import co.onestep.kmp.uikit_kmp.generated.resources.we_didn_t_detect_any_movement
import co.onestep.kmp.uikit_kmp.generated.resources.we_didnt_detect_any_repetitions
import co.onestep.kmp.uikit_kmp.generated.resources.we_need_additional_data_in_order_to_offer_you_meaningful_insights_this_again
import co.onestep.kmp.uikit_kmp.generated.resources.when_prompted_select
import co.onestep.kmp.uikit_kmp.generated.resources.where_will_the_phone_be_placed
import co.onestep.kmp.uikit_kmp.generated.resources.your_walk_had_too_many_turns
import org.jetbrains.compose.resources.stringResource

/**
 * Slim factory for building record flow screen data.
 * Extracted from the original DataFactory (97KB) to only include record-flow-related methods.
 */
internal object RecordFlowDataFactory {

    @Composable
    fun customTagsScreenData(
        tagsData: OSTRecordingQuestionData,
        onSelection: (OSTRecordingQuestionData) -> Unit,
    ) = UiKitScreenData(
        mainIcon = null,
        title = TextData(
            tagsData.title,
            28.sp,
            FontWeight.W700,
            textAlign = TextAlign.Start,
        ),
        selectionList = SelectionListData(
            items = tagsData.tagsValues.map {
                SelectionItemData(
                    text = TextData(
                        it,
                        20.sp,
                        FontWeight.W400,
                    ),
                )
            },
            isMultiSelect = tagsData.isMultiSelect,
        ) { selectedIndexes ->
            onSelection(
                tagsData.apply {
                    selectedAnswers = tagsData.tagsValues.filterIndexed { index, _ ->
                        selectedIndexes.contains(index)
                    }
                },
            )
        },
        brandButton = if (tagsData.isMultiSelect) {
            PrimaryButtonData(
                text = TextData(
                    stringResource(Res.string.continue_camel_case),
                    24.sp,
                    FontWeight.W600,
                ),
                action = { },
            )
        } else {
            null
        },
    )

    /**
     * Builds the [UiKitScreenData] for a specific [RecordFlowError] — the KMP port of uikit's
     * per-error `DataFactory.*ScreenData(...)` methods. Title / subtitle / icon / CTA are copied
     * verbatim from the matching uikit error screen so the two flows are text-identical.
     *
     * [onRetry] drives the primary CTA ("Try again" / "Reload" / "Continue"); [onSecondaryAction]
     * (when non-null) drives the outline secondary CTA ("View instructions", or "Finish" for the
     * Static Balance short error). Errors whose uikit screen has no secondary button ignore it.
     */
    fun errorScreenData(
        error: RecordFlowError,
        resourceProvider: ResourceProvider,
        onRetry: () -> Unit,
        onSecondaryAction: (() -> Unit)? = null,
        // STS manual-report entry (OS-15960 sibling): when non-null, the STS Short/Static/Position
        // error screens replace their "View instructions" secondary with "Enter results manually"
        // (pen icon). Wired only when the STS_MANUAL_REPORT flag is on; ignored for non-STS errors.
        onEnterResultsManually: (() -> Unit)? = null,
    ): UiKitScreenData {
        fun primary(res: org.jetbrains.compose.resources.StringResource) =
            PrimaryButtonData(
                text = TextData(resourceProvider.getString(res), textSize = 24.sp, fontWeight = FontWeight.W600),
                action = { onRetry() },
            )

        val tryAgain = primary(Res.string.try_again)

        // "View instructions" secondary — matches uikit's outlineBrandButton on analysis-error
        // screens. Present only when the caller supplies a secondary action.
        val viewInstructions: SecondaryButtonData? =
            onSecondaryAction?.let {
                SecondaryButtonData(
                    iconData = IconData(Res.drawable.ic_info_circle),
                    text = TextData(
                        resourceProvider.getString(Res.string.view_instructions),
                        textSize = 24.sp,
                        fontWeight = FontWeight.W600,
                    ),
                    action = it,
                )
            }

        // "Enter results manually" secondary — on STS error screens, replaces "View instructions"
        // when the STS manual-report flag is on (mirrors uikit's onEnterResultsManually swap).
        val enterResultsManually: SecondaryButtonData? =
            onEnterResultsManually?.let {
                SecondaryButtonData(
                    iconData = IconData(Res.drawable.ic_edit),
                    text = TextData(
                        resourceProvider.getString(Res.string.sts_enter_results_manually),
                        textSize = 24.sp,
                        fontWeight = FontWeight.W600,
                    ),
                    action = it,
                )
            }

        // For the three STS error screens, the manual-report affordance replaces "View
        // instructions" when present; otherwise fall back to the instructions secondary.
        val stsSecondary: SecondaryButtonData? = enterResultsManually ?: viewInstructions

        return when (error) {
            RecordFlowError.StaticWalk -> analysisError(resourceProvider,
                Res.string.we_didn_t_detect_any_movement, Res.string.walk_static_error_description,
                Res.drawable.ic_motion_red_stars, tryAgain, viewInstructions)
            RecordFlowError.StaticSts -> analysisError(resourceProvider,
                Res.string.we_didnt_detect_any_repetitions, Res.string.sts_static_error_description,
                Res.drawable.ic_sts_red_stars, tryAgain, stsSecondary)
            RecordFlowError.StaticTug -> analysisError(resourceProvider,
                Res.string.we_didn_t_detect_any_movement, Res.string.tug_static_error_description,
                Res.drawable.ic_tug_red_stars, tryAgain, viewInstructions)
            RecordFlowError.StaticRom -> analysisError(resourceProvider,
                Res.string.we_didn_t_detect_any_movement, Res.string.rom_static_error_description,
                Res.drawable.ic_knee_red_stars, tryAgain, viewInstructions)
            RecordFlowError.WalkPosition -> analysisError(resourceProvider,
                Res.string.we_couldn_t_capture_your_steps, Res.string.walk_position_error_description,
                Res.drawable.ic_walk_red_stars, tryAgain, viewInstructions)
            RecordFlowError.StsPosition -> analysisError(resourceProvider,
                Res.string.we_didnt_detect_any_repetitions, Res.string.sts_position_error_description,
                Res.drawable.ic_warning_red_stars, tryAgain, stsSecondary)
            RecordFlowError.TugPosition -> analysisError(resourceProvider,
                Res.string.we_couldn_t_capture_your_steps, Res.string.tug_position_error_description,
                Res.drawable.ic_warning_red_stars, tryAgain, viewInstructions)
            RecordFlowError.RomPosition -> analysisError(resourceProvider,
                Res.string.we_didn_t_detect_any_movement, Res.string.rom_error_position_description,
                Res.drawable.ic_motion_red_stars, tryAgain, viewInstructions)
            RecordFlowError.WalkShort -> analysisError(resourceProvider,
                Res.string.this_measurement_s_recording_was_too_short, Res.string.walk_too_short_error_description,
                Res.drawable.ic_clock_red_stars, tryAgain, viewInstructions)
            RecordFlowError.StsShort -> analysisError(resourceProvider,
                Res.string.this_measurement_s_recording_was_too_short, Res.string.sts_too_short_error_description,
                Res.drawable.ic_clock_red_stars, tryAgain, stsSecondary)
            RecordFlowError.TugShort -> analysisError(resourceProvider,
                Res.string.this_measurement_s_recording_was_too_short, Res.string.tug_too_short_error_description,
                Res.drawable.ic_clock_red_stars, tryAgain, viewInstructions)
            // uikit's romShortErrorScreenData intentionally reuses sts_too_short_error_description.
            RecordFlowError.RomShort -> analysisError(resourceProvider,
                Res.string.this_measurement_s_recording_was_too_short, Res.string.sts_too_short_error_description,
                Res.drawable.ic_clock_red_stars, tryAgain, viewInstructions)
            // Static Balance short: secondary is "Finish", not "View instructions".
            RecordFlowError.StaticBalanceShort -> UiKitScreenData(
                title = TextData(resourceProvider.getString(Res.string.this_measurement_s_recording_was_too_short), 28.sp, FontWeight.Bold),
                subtitle = TextData(resourceProvider.getString(Res.string.static_balance_too_short_error_description), 20.sp, FontWeight.Normal),
                mainIcon = IconData(Res.drawable.ic_clock_red_stars),
                brandButton = tryAgain,
                outlineBrandButton = onSecondaryAction?.let {
                    SecondaryButtonData(
                        text = TextData(resourceProvider.getString(Res.string.finish), textSize = 24.sp, fontWeight = FontWeight.W600),
                        action = it,
                    )
                },
            )
            RecordFlowError.Curvy -> analysisError(resourceProvider,
                Res.string.your_walk_had_too_many_turns,
                Res.string.for_a_successful_analysis_you_need_to_walk_in_a_straight_line,
                Res.drawable.ic_routes_red_stars, tryAgain, viewInstructions)
            RecordFlowError.WalkNonRepetitive -> analysisError(resourceProvider,
                Res.string.we_couldn_t_capture_your_steps, Res.string.walk_repetetive_movement_error_description,
                Res.drawable.ic_walk_red_stars, tryAgain, viewInstructions)
            // Technical errors — no "View instructions" secondary in uikit.
            RecordFlowError.Timeout -> UiKitScreenData(
                title = TextData(resourceProvider.getString(Res.string.timeout_screen_title), 28.sp, FontWeight.Bold),
                subtitle = TextData(resourceProvider.getString(Res.string.timeout_screen_description), 20.sp, FontWeight.Normal),
                mainIcon = IconData(Res.drawable.ic_timeout),
                brandButton = primary(Res.string.continue_camel_case),
            )
            RecordFlowError.ServerIssue -> analysisError(resourceProvider,
                Res.string.oops,
                Res.string.there_was_a_problem_connecting_to_the_server_please_try_again_later,
                Res.drawable.ic_server_red_stars, tryAgain)
            RecordFlowError.Connectivity -> UiKitScreenData(
                title = TextData(resourceProvider.getString(Res.string.a_connection_issue), 28.sp, FontWeight.Bold),
                subtitle = TextData(resourceProvider.getString(Res.string.it_seems_you_have_internet_connection_issues_check_your_internet_connection_and_press_reload_the_measurement_will_be_uploaded_and_analyzed_once_we_get_internet_connection), 20.sp, FontWeight.Normal),
                mainIcon = IconData(Res.drawable.ic_connectivity_red_stars),
                brandButton = primary(Res.string.Reload),
            )
            RecordFlowError.General -> analysisError(resourceProvider,
                Res.string.we_need_additional_data_in_order_to_offer_you_meaningful_insights_this_again,
                Res.string.please_make_sure_is_placed_snugly_against_your_thigh_and_follow_the_measurement_instructions,
                Res.drawable.ic_warning_red_stars, tryAgain, viewInstructions)
        }
    }

    @Composable
    fun walkDurationSelectionScreenData(
        recordingLimit: String,
        onSelection: (Int) -> Unit,
    ) = UiKitScreenData(
        title = TextData(
            stringResource(Res.string.how_long_do_you_want_to_walk_today),
            28.sp,
            FontWeight.Bold,
            textAlign = TextAlign.Start,
        ),
        selectionList = SelectionListData(
            listOf(
                SelectionItemData(
                    itemHeight = 80.dp,
                    text = TextData(
                        stringResource(Res.string.one_minute),
                        24.sp,
                        FontWeight.W400,
                    ),
                ),
                SelectionItemData(
                    itemHeight = 80.dp,
                    text = TextData(
                        stringResource(Res.string.three_minutes),
                        24.sp,
                        FontWeight.W400,
                    ),
                ),
                SelectionItemData(
                    itemHeight = 80.dp,
                    text = TextData(
                        stringResource(Res.string.five_minutes),
                        24.sp,
                        FontWeight.W400,
                    ),
                ),
                SelectionItemData(
                    itemHeight = 80.dp,
                    text = TextData(
                        stringResource(Res.string.long_walk),
                        24.sp,
                        FontWeight.W400,
                    ),
                    description = TextData(
                        stringResource(Res.string.long_walk_description, recordingLimit),
                        16.sp,
                        FontWeight.Normal,
                    ),
                ),
            ),
        ) {
            onSelection(it.first())
        },
    )

    @Composable
    fun choosePlacementScreenData(
        onSelection: (Int) -> Unit,
    ) = UiKitScreenData(
        mainIcon = null,
        title = TextData(
            stringResource(Res.string.where_will_the_phone_be_placed),
            28.sp,
            FontWeight.W700,
            textAlign = TextAlign.Start,
        ),
        selectionList = SelectionListData(
            items = PocketLocation.values.map {
                SelectionItemData(
                    text = TextData(
                        it.displayTitle(),
                        20.sp,
                        FontWeight.W400,
                    ),
                    icon = IconData(it.imageResource(), iconSize = 70.dp),
                )
            },
        ) {
            onSelection(it.first())
        },
    )

    /** Builds a standard analysis-error screen. Used by the 16 error cases that share the same
     *  title/subtitle/icon/primary/secondary layout in [errorScreenData]. */
    private fun analysisError(
        resourceProvider: ResourceProvider,
        titleRes: org.jetbrains.compose.resources.StringResource,
        subtitleRes: org.jetbrains.compose.resources.StringResource,
        iconRes: org.jetbrains.compose.resources.DrawableResource,
        brandButton: PrimaryButtonData,
        outlineBrandButton: SecondaryButtonData? = null,
    ) = UiKitScreenData(
        title = TextData(resourceProvider.getString(titleRes), 28.sp, FontWeight.Bold),
        subtitle = TextData(resourceProvider.getString(subtitleRes), 20.sp, FontWeight.Normal),
        mainIcon = IconData(iconRes),
        brandButton = brandButton,
        outlineBrandButton = outlineBrandButton,
    )

    /**
     * Ordered assistive-device options with `NONE` pinned first, mirroring the Android uikit
     * `buildAssistiveDeviceList()`. The selected index maps back through this same list.
     */
    private fun assistiveDeviceOptions(): List<OSTAssistiveDevice> =
        buildList {
            add(OSTAssistiveDevice.NONE)
            addAll(OSTAssistiveDevice.entries.filter { it != OSTAssistiveDevice.NONE })
        }

    /**
     * Ordered footwear options with `NONE` pinned first, mirroring the Android uikit
     * `buildFootwearList()`. The selected index maps back through this same list.
     */
    private fun footwearOptions(): List<Footwear> =
        buildList {
            add(Footwear.NONE)
            addAll(Footwear.entries.filter { it != Footwear.NONE })
        }

    @Composable
    fun selectAssistiveDeviceScreenData(
        onSelection: (OSTAssistiveDevice) -> Unit,
    ) = UiKitScreenData(
        mainIcon = null,
        title = TextData(
            stringResource(Res.string.choose_your_assistive_device),
            28.sp,
            FontWeight.W700,
            textAlign = TextAlign.Start,
        ),
        selectionList = SelectionListData(
            items = assistiveDeviceOptions().map { device ->
                SelectionItemData(
                    text = TextData(
                        device.displayName(),
                        20.sp,
                        FontWeight.W400,
                    ),
                    itemHeight = 80.dp,
                    icon = IconData(device.icon, iconSize = 40.dp),
                )
            },
        ) { selectedIndexes ->
            onSelection(assistiveDeviceOptions()[selectedIndexes.first()])
        },
    )

    @Composable
    fun selectFootwearScreenData(
        onSelection: (Footwear) -> Unit,
    ) = UiKitScreenData(
        mainIcon = null,
        title = TextData(
            stringResource(Res.string.choose_your_footwear),
            28.sp,
            FontWeight.W700,
            textAlign = TextAlign.Start,
        ),
        selectionList = SelectionListData(
            items = footwearOptions().map { footwear ->
                SelectionItemData(
                    text = TextData(
                        footwear.displayName(),
                        20.sp,
                        FontWeight.W400,
                    ),
                    itemHeight = 80.dp,
                    icon = IconData(footwear.icon, iconSize = 40.dp),
                )
            },
        ) { selectedIndexes ->
            onSelection(footwearOptions()[selectedIndexes.first()])
        },
    )

    @Composable
    fun soundInstructionData(
        onSelection: () -> Unit,
    ) = UiKitScreenData(
        mainIcon = IconData(
            icon = Res.drawable.ic_volume_stars,
        ),
        title = TextData(
            stringResource(Res.string.make_sure_your_phone_is_not_on_silent_mode_and_the_volume_is_set_to_maximum),
            textSize = 28.sp,
            fontWeight = FontWeight.Bold,
        ),
        brandButton = PrimaryButtonData(
            text = TextData(
                stringResource(Res.string.continue_camel_case),
                24.sp,
                FontWeight.W600,
            ),
            action = onSelection,
        ),
    )

    @Composable
    fun startRecordData(
        activityType: OSTActivityType,
        onMainButton: () -> Unit,
        onBottomButton: () -> Unit,
    ) = UiKitScreenData(
        noteBanner = NoteBannerData(
            icon = Res.drawable.ic_phone_orientation,
            title = stringResource(Res.string.static_balance_banner_title),
            body = stringResource(Res.string.static_balance_banner_body),
        ).takeIf { activityType == OSTActivityType.STATIC_BALANCE },
        mainButton = MainButtonData(
            text = TextData(
                stringResource(Res.string.start),
                28.sp,
                FontWeight.Bold,
            ),
            action = onMainButton,
            topSpace = 140.dp,
        ),
        outlineBrandButton = SecondaryButtonData(
            iconData = IconData(Res.drawable.ic_info_circle),
            text = TextData(
                stringResource(Res.string.view_instructions),
                24.sp,
                FontWeight.Bold,
            ),
            action = onBottomButton,
        ),
    )

    fun soundPermissionData(
        resourceProvider: ResourceProvider,
        onSelection: () -> Unit,
        onSkip: () -> Unit,
    ) = PermissionScreenData(
        mainIcon = IconData(
            icon = Res.drawable.ic_microphone_stars,
        ),
        title = TextData(
            resourceProvider.getString(Res.string.to_perform_this_test_grant_access_to_the_microphone),
            textSize = 28.sp,
            fontWeight = FontWeight.Bold,
        ),
        content = InstructionContent.Paragraph(
            listOf(
                StyledSegment(
                    resourceProvider.getString(Res.string.when_prompted_select),
                    color = Color(0xFF716D69),
                ),
                StyledSegment(
                    " '${resourceProvider.getString(Res.string.allow)}'",
                    color = Color(0xFF716D69),
                    fontWeight = FontWeight.W700,
                ),
            ),
        ),
        brandButton = PrimaryButtonData(
            text = TextData(
                resourceProvider.getString(Res.string.allow),
                24.sp,
                FontWeight.W600,
            ),
            action = onSelection,
        ),
        outlineBrandButton = SecondaryButtonData(
            text = TextData(
                resourceProvider.getString(Res.string.skip),
                18.sp,
                FontWeight.W700,
            ),
            action = onSkip,
        ),
    )

    fun soundPermissionDeniedAlwaysData(
        resourceProvider: ResourceProvider,
        onGoToSettings: () -> Unit,
        onSkip: () -> Unit,
    ) = PermissionScreenData(
        mainIcon = IconData(
            icon = Res.drawable.ic_microphone_stars,
        ),
        title = TextData(
            text = resourceProvider.getString(Res.string.to_perform_this_test_grant_access_to_the_microphone),
            textSize = 28.sp,
            fontWeight = FontWeight.Bold,
        ),
        content = InstructionContent.Paragraph(
            listOf(
                StyledSegment(
                    resourceProvider.getString(Res.string.go_to_the_device_settings_and_then_toggle_on_microphone),
                    color = Color(0xFF716D69),
                ),
            ),
        ),
        outlineBrandButton = SecondaryButtonData(
            text = TextData(
                resourceProvider.getString(Res.string.skip),
                20.sp,
                FontWeight.W700,
            ),
            action = onSkip,
        ),
        brandButton = PrimaryButtonData(
            text = TextData(
                resourceProvider.getString(Res.string.go_to_settings),
                20.sp,
                FontWeight.W700,
            ),
            action = onGoToSettings,
        ),
    )

}
