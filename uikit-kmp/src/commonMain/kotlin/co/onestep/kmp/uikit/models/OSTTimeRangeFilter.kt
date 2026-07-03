package co.onestep.kmp.uikit.models

sealed class OSTTimeRangeSlicer {
    data object HOUR : OSTTimeRangeSlicer()
    data object DAY : OSTTimeRangeSlicer()
}

class OSTTimeRangeFilter(
    private val startTime: Long?,
    private val endTime: Long?,
    private val window: OSTTimeRangeSlicer? = null,
) {
    fun getStartTime() = startTime
    fun getEndTime() = endTime
    fun getWindow() = window

    companion object {
        fun between(
            startTime: Long,
            endTime: Long,
        ) = OSTTimeRangeFilter(startTime, endTime)

        fun after(
            startTime: Long,
            window: OSTTimeRangeSlicer? = null,
        ) = OSTTimeRangeFilter(startTime, null, window)

        fun before(
            endTime: Long,
            window: OSTTimeRangeSlicer? = null,
        ) = OSTTimeRangeFilter(null, endTime, window)
    }

    override fun toString(): String =
        "OSTTimeRangeFilter(startTime=$startTime, endTime=$endTime, window=$window)"
}

interface OSTDataRequest {
    val limit: Int?
    val order: OSTOrder?
    val activityType: String?
}

interface OSTTimedDataRequest : OSTDataRequest {
    val timeRangeFilter: OSTTimeRangeFilter?
}

data class OSTTimeRangedDataRequest(
    override val limit: Int? = null,
    override val order: OSTOrder? = null,
    override val timeRangeFilter: OSTTimeRangeFilter? = null,
    override val activityType: String? = null,
) : OSTTimedDataRequest
