package co.onestep.kmp.uikit.features.recordFlow.configurations

import androidx.compose.runtime.Composable
import co.onestep.kmp.uikit.features.summary.models.OSTSummaryOptions
import co.onestep.kmp.uikit.models.OSTActivityType
import co.onestep.kmp.uikit_kmp.generated.resources.*
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource

/**
 * Configuration for the recording flow.
 *
 * @param activityType The activity type to record.
 * @param instructions The instructions to show to the user.
 * @param duration The duration of the recording (in Seconds).
 * @param isCountingDown Whether to count up or down.
 * @param prepareScreenData Data for prepare screen, can be duration or tts.
 * @param playVoiceOver Whether to play the voice over during the recording.
 * @param showPhonePositionScreen Whether to show the choose phone position screen.
 * @param preRecordingQuestions The tags to show to the user.
 * @param shouldRecordGeoLocation Whether to record the geo location.
 * @param showSummaryScreen Whether to show a summary screen or just a completion notice.
 * @param postTaggingData Contains the post summary tagging behaviour and optional tagging options.
 * @param showPreRecordingAssistiveDeviceSelection When true, a screen asking the user to pick an
 *        [co.onestep.kmp.uikit.models.OSTAssistiveDevice] is shown before the recording starts. The
 *        selection is attached to the resulting measurement's metadata and pre-fills the
 *        post-tagging assistive-device row if that screen is also enabled. The user must pick a
 *        device (including `NONE`) to proceed.
 * @param showPreRecordingFootwearSelection When true, a screen asking the user to pick a
 *        [co.onestep.kmp.uikit.features.tagging.models.Footwear] is shown before the recording
 *        starts. The selection is attached to the resulting measurement as a tag and pre-fills the
 *        post-tagging footwear row if that screen is also enabled. The user must pick a footwear
 *        option (including `NONE`) to proceed; `NONE` does not add a tag.
 */
@Serializable
data class OSTRecordingConfiguration(
    val uuid: String = randomUUID(),
    val activityType: OSTActivityType,
    val instructions: OSTMeasurementInstructionsData? = null,
    val duration: Int? = null,
    val isCountingDown: Boolean,
    val prepareScreenData: OSTPrepareData? = null,
    val playVoiceOver: Boolean,
    val showPhonePositionScreen: Boolean,
    val preRecordingQuestions: List<OSTRecordingQuestionData>? = null,
    var shouldRecordGeoLocation: Boolean = false,
    var showSummaryScreen: OSTSummaryOptions = OSTSummaryOptions.Full,
    val showPermissionExplanationScreen: Boolean = true,
    val readyForAnalysisUiAssist: Boolean = false,
    val sensorEnhancedMode: Boolean = false,
    val postTaggingData: OSTPostTaggingData = OSTPostTaggingData.default(),
    val showPreRecordingAssistiveDeviceSelection: Boolean = false,
    val showPreRecordingFootwearSelection: Boolean = false,
    /**
     * Static Balance Test condition schema (OS-15960). Non-null only for the
     * [OSTActivityType.STATIC_BALANCE] flow, where it drives the Condition Setup screen;
     * defaults to all options when the host supplies none. Ignored by every other activity.
     */
    val balance: OSTBalance? = null,
) {
    companion object {
        fun defaultWalk(
            walkInstructions: OSTMeasurementInstructionsData? = null,
        ) = OSTRecordingConfiguration(
            instructions = walkInstructions,
            activityType = OSTActivityType.WALK,
            duration = null,
            isCountingDown = true,
            prepareScreenData = OSTPrepareData.default(),
            playVoiceOver = true,
            showPhonePositionScreen = true,
            shouldRecordGeoLocation = false,
            showSummaryScreen = OSTSummaryOptions.Full,
            postTaggingData = OSTPostTaggingData.default(),
        )

        fun balanceTest(
            instructions: OSTMeasurementInstructionsData? = null,
        ) = OSTRecordingConfiguration(
            activityType = OSTActivityType.BALANCE_TEST,
            instructions = instructions,
            duration = 150,
            isCountingDown = false,
            prepareScreenData = OSTPrepareData.Duration(
                prepareDuration = OSTPrepareDuration.NONE,
            ),
            playVoiceOver = true,
            showPhonePositionScreen = false,
            showSummaryScreen = OSTSummaryOptions.MINIMAL,
            postTaggingData = OSTPostTaggingData.default(),
        )

        /** Fixed per-condition recording cap for the Static Balance Test (Phase 1). */
        const val STATIC_BALANCE_CONDITION_DURATION_SEC = 30

        /**
         * Static Balance Test (OS-15960): clinician-operated postural-sway assessment.
         *
         * A session consists of one or more conditions ([OSTBalanceCondition]), each
         * recorded as a separate 30-second perception and tagged with its condition
         * configuration plus a session-scoped `session_uuid`. The session summary is
         * web-only ([OSTSummaryOptions.WEB]) — the flow finishes with the measurement id and
         * the host app opens the web summary; no native summary screen is shown.
         */
        fun staticBalance(
            instructions: OSTMeasurementInstructionsData? = null,
            balance: OSTBalance = OSTBalance(),
        ) = OSTRecordingConfiguration(
            activityType = OSTActivityType.STATIC_BALANCE,
            instructions = instructions,
            duration = STATIC_BALANCE_CONDITION_DURATION_SEC,
            balance = balance,
            isCountingDown = true,
            prepareScreenData = OSTPrepareData.Duration(
                prepareDuration = OSTPrepareDuration.TEN_SECONDS,
            ),
            playVoiceOver = true,
            showPhonePositionScreen = false,
            showSummaryScreen = OSTSummaryOptions.WEB,
            // Post-recording notes are handled by the dedicated Static Balance
            // "Recording saved" screen, not the generic post-tagging screen.
            postTaggingData = OSTPostTaggingData.OSTPostTaggingScreen(
                questions = null,
                assistiveDeviceTag = false,
                levelOfAssistanceTag = false,
                footwearTag = false,
                note = true,
            ),
        )

        fun sts(
            instructions: OSTMeasurementInstructionsData? = null,
            preRecordingQuestions: List<OSTRecordingQuestionData>? = null,
            postRecordingQuestions: List<OSTRecordingQuestionData>? = null,
            didYouUseHandsTitle: String = "Did you use hands for support?",
            useOfHandsDescription: String = "Use of hands",
            usedHands: String = "Used hands",
            didNotUseHands: String = "Did not use hands",
        ) = OSTRecordingConfiguration(
            instructions = instructions,
            activityType = OSTActivityType.STS,
            duration = 30,
            isCountingDown = true,
            prepareScreenData = OSTPrepareData.Duration(
                prepareDuration = OSTPrepareDuration.TEN_SECONDS,
            ),
            playVoiceOver = true,
            preRecordingQuestions = preRecordingQuestions,
            showPhonePositionScreen = true,
            postTaggingData = OSTPostTaggingData.OSTPostTaggingScreen(
                postRecordingQuestions ?: listOf(
                    OSTRecordingQuestionData(
                        title = didYouUseHandsTitle,
                        description = useOfHandsDescription,
                        tagsValues = listOf(usedHands, didNotUseHands),
                        isMultiSelect = false,
                    ),
                ),
            ),
        )

        fun tug(
            instructions: OSTMeasurementInstructionsData? = null,
            preRecordingQuestions: List<OSTRecordingQuestionData>? = null,
            postRecordingQuestions: List<OSTRecordingQuestionData>? = null,
            didYouUseHandsTitle: String = "Did you use hands for support?",
            useOfHandsDescription: String = "Use of hands",
            usedHands: String = "Used hands",
            didNotUseHands: String = "Did not use hands",
        ) = OSTRecordingConfiguration(
            instructions = instructions,
            activityType = OSTActivityType.TUG,
            duration = 30 * 6,
            isCountingDown = false,
            prepareScreenData = OSTPrepareData.Duration(
                prepareDuration = OSTPrepareDuration.TEN_SECONDS,
            ),
            playVoiceOver = true,
            showPhonePositionScreen = true,
            preRecordingQuestions = preRecordingQuestions,
            postTaggingData = OSTPostTaggingData.OSTPostTaggingScreen(
                postRecordingQuestions ?: listOf(
                    OSTRecordingQuestionData(
                        title = didYouUseHandsTitle,
                        description = useOfHandsDescription,
                        tagsValues = listOf(usedHands, didNotUseHands),
                        isMultiSelect = false,
                    ),
                ),
            ),
        )

        fun romExt(
            instructions: OSTMeasurementInstructionsData? = null,
        ) = OSTRecordingConfiguration(
            duration = 60 * 3,
            instructions = instructions,
            activityType = OSTActivityType.ROM_KNEE_EXT,
            isCountingDown = false,
            prepareScreenData = OSTPrepareData.Duration(
                prepareDuration = OSTPrepareDuration.TEN_SECONDS,
            ),
            playVoiceOver = true,
            showPhonePositionScreen = false,
            postTaggingData = OSTPostTaggingData.OSTPostTaggingScreen(
                questions = emptyList(),
                assistiveDeviceTag = false,
                footwearTag = false,
            ),
        )

        fun stairs(
            instructions: OSTMeasurementInstructionsData? = null,
        ) = OSTRecordingConfiguration(
            duration = 30 * 60,
            instructions = instructions,
            activityType = OSTActivityType.STAIRS,
            isCountingDown = false,
            prepareScreenData = OSTPrepareData.Duration(
                prepareDuration = OSTPrepareDuration.TEN_SECONDS,
            ),
            playVoiceOver = true,
            showPhonePositionScreen = false,
            postTaggingData = OSTPostTaggingData.OSTPostTaggingScreen(
                questions = emptyList(),
                assistiveDeviceTag = false,
                footwearTag = false,
            ),
        )

        fun sixMinuteWalk(
            instructions: OSTMeasurementInstructionsData? = null,
        ) = OSTRecordingConfiguration(
            instructions = instructions,
            activityType = OSTActivityType.SIX_MINUTE_WALK,
            duration = 6 * 60,
            isCountingDown = true,
            prepareScreenData = OSTPrepareData.default().copy(
                prepareDuration = OSTPrepareDuration.TEN_SECONDS,
            ),
            playVoiceOver = true,
            showPhonePositionScreen = true,
            shouldRecordGeoLocation = false,
            showSummaryScreen = OSTSummaryOptions.Full,
            postTaggingData = OSTPostTaggingData.default(),
        )

        fun twoMinuteWalk(
            instructions: OSTMeasurementInstructionsData? = null,
        ) = OSTRecordingConfiguration(
            instructions = instructions,
            activityType = OSTActivityType.TWO_MINUTE_WALK,
            duration = 2 * 60,
            isCountingDown = true,
            prepareScreenData = OSTPrepareData.default().copy(
                prepareDuration = OSTPrepareDuration.TEN_SECONDS,
            ),
            playVoiceOver = true,
            showPhonePositionScreen = true,
            shouldRecordGeoLocation = false,
            showSummaryScreen = OSTSummaryOptions.Full,
            postTaggingData = OSTPostTaggingData.default(),
        )

        fun dualTaskSubtract(
            instructions: OSTMeasurementInstructionsData? = null,
            showSummaryScreen: OSTSummaryOptions = OSTSummaryOptions.Full,
            showInstructions: Boolean = true,
            ttsSpeechText: String,
            postRecordingQuestions: List<OSTRecordingQuestionData>? = null,
            postTaggingData: OSTPostTaggingData? = null,
        ) = OSTRecordingConfiguration(
            instructions = instructions,
            activityType = OSTActivityType.DUAL_TASK_WALK_SUBTRACT,
            duration = 60,
            isCountingDown = true,
            prepareScreenData = OSTPrepareData.Tts(
                ttsSpeechText = ttsSpeechText,
                showInstructions = showInstructions,
            ),
            playVoiceOver = true,
            showPhonePositionScreen = true,
            showSummaryScreen = showSummaryScreen,
            postTaggingData = postTaggingData ?: OSTPostTaggingData.OSTPostTaggingQuestionsFlow(
                postRecordingQuestions ?: emptyList(),
            ),
        )
    }
}

internal expect fun randomUUID(): String

/**
 * [OSTPostTaggingData] adjusted so the footwear and assistive-device rows are not shown again on the
 * post-recording review screen when the same selection was already collected in the pre-recording
 * flow. Re-showing them there re-states the value: footwear (stored as a tag) is submitted a second
 * time and the assistive device is re-sent, which is the duplicate-tag behaviour from OS-16020. Only
 * [OSTPostTaggingData.OSTPostTaggingScreen] carries these rows; other variants are returned unchanged.
 */
fun OSTRecordingConfiguration.effectivePostTaggingData(): OSTPostTaggingData {
    val screen =
        postTaggingData as? OSTPostTaggingData.OSTPostTaggingScreen ?: return postTaggingData
    return screen.copy(
        footwearTag = if (showPreRecordingFootwearSelection) false else screen.footwearTag,
        assistiveDeviceTag = if (showPreRecordingAssistiveDeviceSelection) false else screen.assistiveDeviceTag,
    )
}

@Composable
fun OSTRecordingConfiguration.defaultInstructions(): OSTMeasurementInstructionsData =
    when (activityType) {
        OSTActivityType.WALK -> OSTMeasurementInstructionsData(
            activityDisplayName = stringResource(Res.string.walk),
            instructions = listOf(
                stringResource(Res.string.walk_instructions_1),
                stringResource(Res.string.walk_instructions_2),
                stringResource(Res.string.walk_instructions_3),
                stringResource(Res.string.walk_instructions_4),
                stringResource(Res.string.walk_instructions_5),
            ),
            gifResourceKey = "walk_instruction",
        )

        OSTActivityType.STS -> OSTMeasurementInstructionsData(
            activityDisplayName = stringResource(Res.string.sit_to_stand),
            instructions = listOf(
                stringResource(Res.string.sit_on_a_chair_with_both_feet_on_the_floor),
                stringResource(Res.string.cross_your_arms_against_your_chest_in_an_x_shape),
                stringResource(Res.string.stand_up_and_sit_down_as_many_times_as_you_can_in_30_seconds),
            ),
            videoUrl = "https://res.cloudinary.com/dujaj7bp2/video/upload/v1749023565/STS_with_phone_in_pocket.mp4_h3ek6k.mov",
        )

        OSTActivityType.TUG -> OSTMeasurementInstructionsData(
            activityDisplayName = stringResource(Res.string.timed_up_and_go),
            instructions = listOf(
                stringResource(Res.string.sit_in_a_chair_with_your_back_against_the_back_of_the_chair),
                stringResource(Res.string.stand_up),
                stringResource(Res.string.walk_towards_the_marker_and_go_around_it),
                stringResource(Res.string.walk_back_towards_the_chair_and_sit_down),
                stringResource(Res.string.wait_3_seconds_while_sitting_down),
                stringResource(Res.string.then_slide_the_stop_button),
            ),
            videoUrl = "https://res.cloudinary.com/dujaj7bp2/video/upload/c_crop,h_853,w_853/v1749022870/TUG_phone_in_pocket_lq3ph9.mp4",
        )

        OSTActivityType.ROM_KNEE_FLEX, OSTActivityType.ROM_KNEE_EXT -> OSTMeasurementInstructionsData(
            activityDisplayName = stringResource(Res.string.knee_extension),
            instructions = listOf(
                stringResource(Res.string.sit_on_a_firm_chair),
                stringResource(Res.string.move_to_the_edge_of_the_chair_for_better_positioning),
                stringResource(Res.string.straighten_the_measured_leg_to_be_out_in_front_of_you_as_much_as_possible),
                stringResource(Res.string.keep_your_heel_resting_on_the_ground_toes_pointing_up),
                stringResource(Res.string.place_the_phone_on_the_front_of_your_mid_thigh),
                stringResource(Res.string.slide_it_back_and_forth_gently_from_mid_thigh_to_just_below_the_knee_joint_taking_care_not_to_rub_over_any_scars),
                stringResource(Res.string.repeat_10_times),
            ),
        )

        // Static Balance (OS-15960) reuses the balance-test instruction copy for its
        // "View instructions" bottom sheet; the per-condition guidance lives on the recording
        // Get Ready screen (pocket/strap vs. chest).
        OSTActivityType.STATIC_BALANCE,
        OSTActivityType.BALANCE_TEST -> OSTMeasurementInstructionsData(
            activityDisplayName = stringResource(Res.string.balance_test),
            instructions = listOf(
                stringResource(Res.string.calibration_walk_30s),
                stringResource(Res.string.stand_still_with_eyes_opened_30s),
                stringResource(Res.string.stand_still_with_eyes_closed_30s),
                stringResource(Res.string.single_leg_eyes_opened_30s),
                stringResource(Res.string.single_leg_eyes_closed_30s),
            ),
            hints = listOf(
                stringResource(Res.string.ensure_the_patient_keeps_the_phone_securely_in_their_pocket_for_the_whole_test),
                stringResource(Res.string.the_test_is_guided_by_voice_cues_verify_the_volume_is_high_enough_and_the_environment_is_quite),
            ),
            gifUrl = "https://res.cloudinary.com/dujaj7bp2/image/upload/v1740501179/balance_test_generated_rtrmsm.gif",
        )

        OSTActivityType.DUAL_TASK_WALK_SUBTRACT -> OSTMeasurementInstructionsData(
            activityDisplayName = stringResource(Res.string.dual_task_activity_display_name),
            instructions = listOf(
                stringResource(Res.string.this_activity_will_ask_you_to_walk_for_60_seconds_at_your_normal_comfortable_pace_and_at_the_same_time_count_backwards_by_3_from_a_number_we_provide),
                stringResource(Res.string.please_stop_immediately_if_you_begin_to_feel_uncomfortable_at_any_time),
                stringResource(Res.string.we_will_tell_you_when_to_start_and_stop),
                stringResource(Res.string.please_wear_a_comfortable_pair_of_walking_shoes),
                stringResource(Res.string.find_a_flat_smooth_surface_for_walking_the_straighter_this_path_the_better),
                stringResource(Res.string.try_to_walk_continuously_throughout_each_trial_by_turning_at_the_ends_of_your_path_as_if_you_are_walking_around_a_cone),
                stringResource(Res.string.importantly_walk_at_your_normal_pace_you_do_not_need_to_walk_faster_than_usual),
                stringResource(Res.string.press_on_the_start_button_and_put_the_phone_in_your_pocket),
                stringResource(Res.string.we_will_verbally_guide_you_through_the_rest_of_this_activity),
            ),
        )

        OSTActivityType.SIX_MINUTE_WALK -> OSTMeasurementInstructionsData(
            activityDisplayName = stringResource(Res.string._6_minute_walk),
            instructions = listOf(
                stringResource(Res.string.tap_start_to_begin_the_test),
                stringResource(Res.string.place_your_phone_securely_in_your_pocket_or_flat_against_your_thigh),
                stringResource(Res.string.walk_at_a_steady_pace_back_and_forth_along_a_flat_path_like_a_hallway),
                stringResource(Res.string.turn_around_at_each_end_of_the_hallway_or_marked_walking_area),
                stringResource(Res.string.keep_the_phone_in_place_throughout_do_not_remove_or_adjust_it),
                stringResource(Res.string.the_app_will_let_you_know_when_6_minutes_are_over_no_need_to_track_time),
            ),
        )

        OSTActivityType.TWO_MINUTE_WALK -> OSTMeasurementInstructionsData(
            activityDisplayName = stringResource(Res.string._2_minute_walk),
            instructions = listOf(
                stringResource(Res.string.tap_start_to_begin_the_test),
                stringResource(Res.string.place_your_phone_securely_in_your_pocket_or_flat_against_your_thigh),
                stringResource(Res.string.walk_at_a_steady_pace_back_and_forth_along_a_flat_path_like_a_hallway),
                stringResource(Res.string.turn_around_at_each_end_of_the_hallway_or_marked_walking_area),
                stringResource(Res.string.keep_the_phone_in_place_throughout_do_not_remove_or_adjust_it),
                stringResource(Res.string.the_app_will_let_you_know_when_2_minutes_are_over_no_need_to_track_time),
            ),
        )

        OSTActivityType.STAIRS -> OSTMeasurementInstructionsData(
            activityDisplayName = stringResource(Res.string.walk),
            instructions = listOf(
                stringResource(Res.string.walk_instructions_1),
                stringResource(Res.string.walk_instructions_2),
                stringResource(Res.string.walk_instructions_3),
                stringResource(Res.string.walk_instructions_4),
                stringResource(Res.string.walk_instructions_5),
            ),
        )
    }
