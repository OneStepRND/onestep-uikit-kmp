package co.onestep.kmp.uikit.features.recordFlow

/**
 * The concrete error screen the record flow should present.
 *
 * Ported 1:1 from the Android `uikit` module's per-error destinations in
 * `features/recordFlow/screens/flowScreens/errors`. Each variant corresponds to exactly
 * one uikit error destination and is rendered by [RecordFlowDataFactory.errorScreenData] with
 * the same title / subtitle / icon / CTA. [ResultHandler] maps a measurement (or a technical
 * analyser error) to one of these variants using the same dispatch rules as uikit's
 * `ResultHandler`.
 *
 * `errorType` mirrors the analytics `ERROR_TYPE` constant on the matching uikit destination so
 * that error tracking can be wired later without re-deriving it.
 */
enum class RecordFlowError(val errorType: String) {
    // Analysis errors (empty / partial analysis) — from ResultHandler.handleMeasurementResult
    StaticWalk("static_walk"),
    StaticSts("sts_static"),
    StaticTug("tug_static"),
    StaticRom("rom_static"),

    WalkPosition("walk_position"),
    StsPosition("sts_position"),
    TugPosition("tug_position"),
    RomPosition("rom_position"),

    WalkShort("walk_short"),
    StsShort("sts_short"),
    TugShort("tug_short"),
    RomShort("rom_short"),
    StaticBalanceShort("static_balance_short"),

    Curvy("curvy"),
    WalkNonRepetitive("walk_non_repetitive"),

    // Technical analyser errors — from ResultHandler.onAnalyseError
    Timeout("time_out"),
    ServerIssue("server_issue"),
    Connectivity("connectivity"),

    // Fallback
    General("general"),
}
