package com.example.domain

sealed interface ReportedValue<out T> {
    data class Available<T>(
        val value: T,
        val reportedAtEpochMillis: Long
    ) : ReportedValue<T>

    data class Unavailable(
        val reason: String
    ) : ReportedValue<Nothing>

    val isAvailable: Boolean get() = this is Available
    fun valueOrNull(): T? = (this as? Available)?.value
}

data class BatteryState(
    val left: ReportedValue<Int>,
    val right: ReportedValue<Int>,
    val case: ReportedValue<Int>,
    val isCharging: ReportedValue<Boolean> = ReportedValue.Unavailable("Charging state not reported")
) {
    companion object {
        fun unavailable(reason: String = "Not connected or not reported"): BatteryState = BatteryState(
            left = ReportedValue.Unavailable(reason),
            right = ReportedValue.Unavailable(reason),
            case = ReportedValue.Unavailable(reason),
            isCharging = ReportedValue.Unavailable(reason)
        )

        fun createSafe(
            leftPercent: Int?,
            rightPercent: Int?,
            casePercent: Int?,
            timestamp: Long,
            isCharging: Boolean? = null
        ): BatteryState {
            fun sanitize(percent: Int?, label: String): ReportedValue<Int> {
                return if (percent != null && percent in 0..100) {
                    ReportedValue.Available(percent, timestamp)
                } else {
                    ReportedValue.Unavailable("$label battery reading not available")
                }
            }

            val chargingVal: ReportedValue<Boolean> = if (isCharging != null) {
                ReportedValue.Available(isCharging, timestamp)
            } else {
                ReportedValue.Unavailable("Charging indicator not reported")
            }

            return BatteryState(
                left = sanitize(leftPercent, "Left earbud"),
                right = sanitize(rightPercent, "Right earbud"),
                case = sanitize(casePercent, "Case"),
                isCharging = chargingVal
            )
        }
    }
}
