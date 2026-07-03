package co.onestep.kmp.uikit.models

enum class OSTParamName(
    val columnName: String,
) {
    // Walking parameters
    WALKING_STEPS("steps"),
    WALKING_CADENCE("cadence"),
    WALKING_VELOCITY("velocity"),
    WALKING_DOUBLE_SUPPORT("double_support"),
    WALKING_STANCE("stance"),
    WALKING_STANCE_ASYMMETRY("stance_asymmetry"),
    WALKING_STRIDE_LENGTH("stride_length"),
    WALKING_STEP_LENGTH("step_length"),
    WALKING_STEP_LENGTH_LEFT("step_length_left"),
    WALKING_STEP_LENGTH_RIGHT("step_length_right"),
    WALKING_STEP_LENGTH_DIFF("step_length_diff"),
    WALKING_STEP_LENGTH_ASYMMETRY("step_length_asymmetry"),
    WALKING_CONSISTENCY("consistency"),
    WALKING_HIP_RANGE("hip_range"),
    WALKING_BASE_WIDTH("base_width"),
    WALKING_DOUBLE_SUPPORT_ASYMMETRY("double_support_asymmetry"),
    WALKING_SINGLE_SUPPORT_RIGHT("single_support_right"),
    WALKING_SINGLE_SUPPORT_LEFT("single_support_left"),
    WALKING_STANCE_RIGHT("stance_right"),
    WALKING_STANCE_LEFT("stance_left"),
    WALKING_WALK_SCORE("walk_score"),
    WALKING_DISTANCE("distance"),
    WALKING_CADENCE_VARIABILITY("cadence_variability"),
    WALKING_VELOCITY_VARIABILITY("velocity_variability"),

    // Balance parameters
    BALANCE_SENSORY_COMPOSITE_SCORE("balance_sensory_composite_score"),
    BALANCE_SENSORY_SINGLE_EC_SCORE("balance_sensory_single_ec_score"),
    BALANCE_SENSORY_SINGLE_EO_SCORE("balance_sensory_single_eo_score"),
    BALANCE_SENSORY_STABLE_EC_SCORE("balance_sensory_stable_ec_score"),
    BALANCE_SENSORY_STABLE_EO_SCORE("balance_sensory_stable_eo_score"),

    // TUG parameters
    TUG_DURATION_SECONDS("tug_duration_seconds"),
    TUG_FORWARD_SECONDS("tug_forward_seconds"),
    TUG_BACKWARD_SECONDS("tug_backward_seconds"),
    TUG_SITTING_SECONDS("tug_sitting_seconds"),
    TUG_STANDING_SECONDS("tug_standing_seconds"),
    TUG_TURNING_SECONDS("tug_turning_seconds"),
    TUG_TURNING_TO_CHAIR_SECONDS("tug_turning_to_chair_seconds"),
    TUG_DISTANCE_METERS("tug_distance_meters"),

    // STS parameters
    STS_REPETITION_COUNT("sts_repetition_count"),
    STS_REPETITION_TIME("sts_repetition_time"),
    STS_REPETITION_VAR("sts_repetition_var"),
    STS_FATIGUE("sts_fatigue"),
    STS_ANGLE("sts_angle"),

    // ROM parameters
    RANGE_OF_MOTION_ANGLE("range_of_motion_angle"),
    HIP_EXT_RANGE_OF_MOTION_ANGLE("hip_ext_range_of_motion_angle"),
    HIP_FLEX_RANGE_OF_MOTION_ANGLE("hip_flex_range_of_motion_angle"),
    HIP_ABD_RANGE_OF_MOTION_ANGLE("hip_abd_range_of_motion_angle"),
    HIP_ADD_RANGE_OF_MOTION_ANGLE("hip_add_range_of_motion_angle"),
    KNEE_FLEX_RANGE_OF_MOTION_ANGLE("knee_flex_range_of_motion_angle"),
    KNEE_EXT_RANGE_OF_MOTION_ANGLE("knee_ext_range_of_motion_angle"),
    KNEE_FLEX_PASSIVE_RANGE_OF_MOTION_ANGLE("knee_flex_passive_range_of_motion_angle"),

    // Timed walk tests
    SIX_MINUTE_WALK_DISTANCE_METERS("six_minute_walk_distance_meters"),
    SIX_MINUTE_WALK_LAPS("six_minute_walk_laps"),
    TWO_MINUTE_WALK_DISTANCE_METERS("two_minute_walk_distance_meters"),
}

fun Map<String, Float>.toParamName(): Map<OSTParamName, Float> {
    val paramNameLookup = OSTParamName.entries.associateBy { it.columnName }
    return mapNotNull { (key, value) ->
        paramNameLookup[key]?.let { paramName ->
            paramName to value
        }
    }.toMap()
}

fun String.toParamName(): OSTParamName? =
    OSTParamName.entries.find { it.columnName == this }
