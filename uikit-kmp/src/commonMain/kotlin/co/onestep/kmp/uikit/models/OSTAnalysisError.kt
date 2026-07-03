package co.onestep.kmp.uikit.models

enum class OSTAnalysisError(
    val code: Int,
) {
    Static(1),
    Short(2),
    Position(3),
    Curvy(4),
    Other(5),
    StairsFlat(6),
    NotAccordingToDefinition(7),
    NonRepetitiveMovement(8),
    WrongDuration(9),
}

fun Int?.toAnalysisError(): OSTAnalysisError =
    when (this) {
        1 -> OSTAnalysisError.Static
        2 -> OSTAnalysisError.Short
        3 -> OSTAnalysisError.Position
        4 -> OSTAnalysisError.Curvy
        5 -> OSTAnalysisError.Other
        6 -> OSTAnalysisError.StairsFlat
        7 -> OSTAnalysisError.NotAccordingToDefinition
        8 -> OSTAnalysisError.NonRepetitiveMovement
        9 -> OSTAnalysisError.WrongDuration
        null -> OSTAnalysisError.Other
        else -> OSTAnalysisError.Other
    }
