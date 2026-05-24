package com.invatech.oxy.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.invatech.oxy.auth.OxyUser
import com.invatech.oxy.data.AttendanceViewModel
import com.invatech.oxy.data.calculateAttendanceStreak
import com.invatech.oxy.data.calculateAttendanceSummary
import com.invatech.oxy.ui.theme.OxyTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    user: OxyUser,
    attendanceViewModel: AttendanceViewModel = viewModel()
) {
    val attendanceState by attendanceViewModel.uiState.collectAsState()
    val startDate = user.attendanceStartDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: LocalDate.now()
    val summary = calculateAttendanceSummary(attendanceState.records, startDate)
    val streak = calculateAttendanceStreak(attendanceState.records, startDate)

    LaunchedEffect(user.uid) {
        attendanceViewModel.load(user.uid)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        HomeHeader(user = user)
        AttendanceCard(
            percentage = summary.percentage,
            target = summary.target,
            safeBunks = summary.safeBunks,
            requiredPresents = summary.requiredPresents
        )
        QuickStatsSection(
            trackedDays = summary.trackedDays,
            present = summary.present,
            absent = summary.absent,
            streak = streak
        )
        LeavePlannerCard(
            attendanceMap = attendanceState.records,
            trackingStartDate = startDate
        )
    }
}

@Composable
fun HomeHeader(user: OxyUser) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(
                    text = "Good Morning!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Light
                )

                Text(
                    text = user.displayName ?: "Student",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.weight(1f))

            UserProfileImage(
                photoUrl = user.photoUrl,
                displayName = user.displayName,
                size = 48.dp
            )
        }
    }
}

@Composable
fun AttendanceCard(
    percentage: Int,
    target: Int,
    safeBunks: Int,
    requiredPresents: Int
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Overall Attendance",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (percentage >= target) "On track for the $target% target." else "Below the $target% target.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (percentage >= target) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (percentage >= target) {
                        "$safeBunks safe absent days before you drop below $target%."
                    } else {
                        "Attend next $requiredPresents tracked days to reach $target%."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { percentage / 100f },
                    modifier = Modifier.size(100.dp),
                    strokeWidth = 10.dp,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun QuickStatsSection(trackedDays: Int, present: Int, absent: Int, streak: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Quick Stats",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard("Tracked", trackedDays.toString(), Icons.Default.CalendarMonth, Modifier.weight(1f))
            StatCard("Present", present.toString(), Icons.Default.CheckCircle, Modifier.weight(1f))
            StatCard("Absent", absent.toString(), Icons.Default.EventBusy, Modifier.weight(1f))
            StatCard("Streak", "$streak days", Icons.Default.LocalFireDepartment, Modifier.weight(1f))
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 16.dp, horizontal = 8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun LeavePlannerCard(
    attendanceMap: Map<String, AttendanceStatus>,
    trackingStartDate: LocalDate
) {
    val selectedLeaveDates = remember { mutableStateListOf<String>() }
    val futureDates = remember(trackingStartDate) {
        currentMonthPlanningDates(trackingStartDate)
    }
    val nextLeaveDate = futureDates.firstOrNull { date ->
        attendanceMap[date.toString()] != AttendanceStatus.ABSENT
    }
    val currentSummary = calculateAttendanceSummary(attendanceMap, trackingStartDate)
    val oneLeaveSummary = calculateAttendanceSummary(
        if (nextLeaveDate == null) attendanceMap else attendanceMap + (nextLeaveDate.toString() to AttendanceStatus.ABSENT),
        trackingStartDate
    )
    val selectedDates = selectedLeaveDates.map { LocalDate.parse(it) }
    val monthPlanSummary = calculateAttendanceSummary(
        attendanceMap + selectedDates.associate { it.toString() to AttendanceStatus.ABSENT },
        trackingStartDate
    )

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EventBusy, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Leave Planner",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = if (nextLeaveDate == null) {
                    "No upcoming tracked days left this month."
                } else {
                    "If you take one more leave on ${nextLeaveDate.format(DateTimeFormatter.ofPattern("dd MMM"))}, attendance becomes ${oneLeaveSummary.percentage}%."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Check monthly leave plan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Tap the days you may take leave. Sundays are skipped.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        futureDates.forEach { date ->
                            val key = date.toString()
                            FilterChip(
                                selected = key in selectedLeaveDates,
                                onClick = {
                                    if (key in selectedLeaveDates) {
                                        selectedLeaveDates.remove(key)
                                    } else {
                                        selectedLeaveDates.add(key)
                                    }
                                },
                                label = { Text(date.format(DateTimeFormatter.ofPattern("dd MMM"))) },
                                enabled = attendanceMap[key] != AttendanceStatus.HOLIDAY
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${selectedLeaveDates.size} planned leave days",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Current ${currentSummary.percentage}% -> Plan ${monthPlanSummary.percentage}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (monthPlanSummary.percentage < currentSummary.target) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    Color(0xFF2E7D32)
                                }
                            )
                        }
                        TextButton(onClick = { selectedLeaveDates.clear() }) {
                            Text("Clear")
                        }
                    }
                }
            }
        }
    }
}

private fun currentMonthPlanningDates(trackingStartDate: LocalDate): List<LocalDate> {
    val today = LocalDate.now()
    val start = maxOf(today, trackingStartDate)
    val end = today.withDayOfMonth(today.lengthOfMonth())
    val dates = mutableListOf<LocalDate>()
    var current = start

    while (!current.isAfter(end)) {
        if (current.dayOfWeek.value != 7) {
            dates += current
        }
        current = current.plusDays(1)
    }

    return dates
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    OxyTheme {
        HomeScreen(
            user = OxyUser(
                uid = "demo",
                email = "student@example.com",
                displayName = "Student",
                photoUrl = null,
                semester = "5",
                department = "Computer Science",
                rollNumber = "OXY-24-CS-118",
                attendanceStartDate = LocalDate.now().toString()
            )
        )
    }
}
