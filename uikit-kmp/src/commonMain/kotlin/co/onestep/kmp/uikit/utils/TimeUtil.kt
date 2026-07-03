package co.onestep.kmp.uikit.utils

internal expect fun Long.toMainParamTitle(showDuration: Boolean = true): String

internal expect fun Int.toDisplayTime(): String

fun String.toDisplayTime(countdown: Boolean? = false): String {
    val parts = this.split(":")
    if (parts.size != 2) return ""

    var minutes = parts[0].toInt()
    var seconds = parts[1].toInt()

    if (countdown == true) {
        if (seconds == 0 && minutes > 0) {
            minutes -= 1
            seconds = 59
        } else if (seconds > 0) {
            seconds -= 1
        }
    } else {
        seconds += 1
        if (seconds >= 60) {
            seconds = 0
            minutes += 1
        }
    }

    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}
