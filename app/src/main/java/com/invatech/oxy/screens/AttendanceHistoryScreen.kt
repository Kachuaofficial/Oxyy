package com.invatech.oxy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.invatech.oxy.auth.OxyUser
import com.invatech.oxy.data.AttendanceViewModel
import com.invatech.oxy.data.calculateAttendanceSummary
import com.invatech.oxy.ui.theme.OxyTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AttendanceHistoryScreen(
    user: OxyUser,
    attendanceViewModel: AttendanceViewModel = viewModel()
) {
    val attendanceState by attendanceViewModel.uiState.collectAsState()
    val startDate = user.attendanceStartDate
        ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        ?: LocalDate.now()
    val summary = calculateAttendanceSummary(attendanceState.records, startDate)
    val historyItems = attendanceState.records
        .mapNotNull { (date, status) ->
            runCatching { LocalDate.parse(date) to status }.getOrNull()
        }
        .filter { (date, _) -> !date.isBefore(startDate) }
        .sortedByDescending { (date, _) -> date }

    LaunchedEffect(user.uid) {
        attendanceViewModel.load(user.uid)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Attendance History",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        if (attendanceState.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        attendanceState.error?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HistoryStatCard("Tracked", summary.trackedDays.toString(), Icons.Default.CalendarMonth, Modifier.weight(1f))
            HistoryStatCard("Present", summary.present.toString(), Icons.Default.CheckCircle, Modifier.weight(1f), Color(0xFF2E7D32))
            HistoryStatCard("Absent", summary.absent.toString(), Icons.Default.EventBusy, Modifier.weight(1f), Color(0xFFD32F2F))
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "All Records",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (historyItems.isEmpty() && !attendanceState.isLoading) {
                    Text(
                        text = "No attendance records yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    historyItems.forEach { (date, status) ->
                        HistoryRow(date = date, status = status)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color)
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun HistoryRow(date: LocalDate, status: AttendanceStatus) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(statusColor(status).copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = statusIcon(status),
                contentDescription = null,
                tint = statusColor(status)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = date.format(DateTimeFormatter.ofPattern("EEEE")),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Surface(
            shape = RoundedCornerShape(50),
            color = statusColor(status).copy(alpha = 0.14f)
        ) {
            Text(
                text = statusLabel(status),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                color = statusColor(status),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

private fun statusLabel(status: AttendanceStatus): String {
    return when (status) {
        AttendanceStatus.PRESENT -> "Present"
        AttendanceStatus.ABSENT -> "Absent"
        AttendanceStatus.HOLIDAY -> "Holiday"
        AttendanceStatus.NOT_MARKED -> "Not marked"
    }
}

private fun statusColor(status: AttendanceStatus): Color {
    return when (status) {
        AttendanceStatus.PRESENT -> Color(0xFF2E7D32)
        AttendanceStatus.ABSENT -> Color(0xFFD32F2F)
        AttendanceStatus.HOLIDAY -> Color(0xFF6D6D6D)
        AttendanceStatus.NOT_MARKED -> Color(0xFF757575)
    }
}

private fun statusIcon(status: AttendanceStatus): ImageVector {
    return when (status) {
        AttendanceStatus.PRESENT -> Icons.Default.CheckCircle
        AttendanceStatus.ABSENT -> Icons.Default.EventBusy
        AttendanceStatus.HOLIDAY -> Icons.Default.WbSunny
        AttendanceStatus.NOT_MARKED -> Icons.Default.CalendarMonth
    }
}

@Preview(showBackground = true)
@Composable
fun AttendanceHistoryScreenPreview() {
    OxyTheme {
        AttendanceHistoryScreen(
            user = OxyUser(
                uid = "demo",
                email = "student@example.com",
                displayName = "Student",
                photoUrl = null,
                semester = "5",
                department = "Computer Science",
                rollNumber = "OXY-24-CS-118",
                attendanceStartDate = "2026-05-25"
            )
        )
    }
}
