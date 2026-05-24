package com.invatech.oxy.data

import com.invatech.oxy.screens.AttendanceStatus
import java.time.LocalDate
import kotlin.math.ceil
import kotlin.math.roundToInt

data class AttendanceSummary(
    val present: Int,
    val absent: Int,
    val trackedDays: Int,
    val percentage: Int,
    val target: Int = 75,
    val safeBunks: Int = 0,
    val requiredPresents: Int = 0
)

fun calculateAttendanceSummary(
    records: Map<String, AttendanceStatus>,
    trackingStartDate: LocalDate,
    target: Int = 75
): AttendanceSummary {
    val trackedRecords = records.filterKeys { date ->
        runCatching { !LocalDate.parse(date).isBefore(trackingStartDate) }.getOrDefault(false)
    }
    val present = trackedRecords.values.count { it == AttendanceStatus.PRESENT }
    val absent = trackedRecords.values.count { it == AttendanceStatus.ABSENT }
    val trackedDays = present + absent
    val percentage = if (trackedDays == 0) 0 else ((present.toFloat() / trackedDays.toFloat()) * 100).roundToInt()

    return AttendanceSummary(
        present = present,
        absent = absent,
        trackedDays = trackedDays,
        percentage = percentage,
        target = target,
        safeBunks = calculateSafeBunks(present, trackedDays, target),
        requiredPresents = calculateRequiredPresents(present, trackedDays, target)
    )
}

fun calculateAttendanceStreak(
    records: Map<String, AttendanceStatus>,
    trackingStartDate: LocalDate
): Int {
    var streak = 0
    var currentDate = LocalDate.now()

    while (!currentDate.isBefore(trackingStartDate)) {
        when (records[currentDate.toString()]) {
            AttendanceStatus.PRESENT -> streak++
            AttendanceStatus.HOLIDAY,
            AttendanceStatus.NOT_MARKED,
            null -> Unit
            AttendanceStatus.ABSENT -> return streak
        }
        currentDate = currentDate.minusDays(1)
    }

    return streak
}

private fun calculateSafeBunks(present: Int, trackedDays: Int, target: Int): Int {
    if (trackedDays == 0) return 0
    var safeBunks = 0
    while (true) {
        val nextTotal = trackedDays + safeBunks + 1
        val nextPercentage = (present.toFloat() / nextTotal.toFloat()) * 100
        if (nextPercentage < target) return safeBunks
        safeBunks++
    }
}

private fun calculateRequiredPresents(present: Int, trackedDays: Int, target: Int): Int {
    if (trackedDays == 0) return 1
    val currentPercentage = (present.toFloat() / trackedDays.toFloat()) * 100
    if (currentPercentage >= target) return 0

    val targetRatio = target / 100f
    return ceil(((targetRatio * trackedDays) - present) / (1f - targetRatio)).toInt().coerceAtLeast(1)
}
