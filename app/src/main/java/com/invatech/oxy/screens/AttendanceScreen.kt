package com.invatech.oxy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.invatech.oxy.auth.OxyUser
import com.invatech.oxy.data.AttendanceViewModel
import com.invatech.oxy.data.calculateAttendanceSummary
import com.invatech.oxy.ui.theme.OxyTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class AttendanceStatus {
    PRESENT, ABSENT, HOLIDAY, NOT_MARKED
}

@Composable
fun AttendanceScreen(
    user: OxyUser,
    attendanceViewModel: AttendanceViewModel = viewModel()
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showMarkDialog by remember { mutableStateOf(false) }
    var showFutureDateAlert by remember { mutableStateOf(false) }
    val attendanceState by attendanceViewModel.uiState.collectAsState()
    val startDate = remember(user.attendanceStartDate) {
        user.attendanceStartDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: LocalDate.now()
    }
    val attendanceMap = attendanceState.records
    val isTodaySunday = LocalDate.now().dayOfWeek.value == 7

    LaunchedEffect(user.uid) {
        attendanceViewModel.load(user.uid)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Attendance",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Tracking from ${startDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (attendanceState.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        attendanceState.error?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        AttendanceCalendar(
            selectedDate = selectedDate,
            attendanceMap = attendanceMap,
            trackingStartDate = startDate,
            onDateSelected = { date ->
                if (date.isAfter(LocalDate.now())) {
                    showFutureDateAlert = true
                } else {
                    selectedDate = date
                    showMarkDialog = true
                }
            }
        )

        AttendanceStats(attendanceMap = attendanceMap, trackingStartDate = startDate)

        QuickActions(
            isTodaySunday = isTodaySunday,
            onPresentToday = {
                attendanceViewModel.mark(user.uid, LocalDate.now().toString(), AttendanceStatus.PRESENT)
            },
            onLeaveToday = {
                attendanceViewModel.mark(user.uid, LocalDate.now().toString(), AttendanceStatus.ABSENT, "Leave")
            }
        )
    }

    if (showMarkDialog) {
        MarkAttendanceDialog(
            date = selectedDate,
            currentStatus = attendanceMap[selectedDate.toString()] ?: AttendanceStatus.NOT_MARKED,
            onDismiss = { showMarkDialog = false },
            onSave = { status, reason ->
                attendanceViewModel.mark(user.uid, selectedDate.toString(), status, reason)
                showMarkDialog = false
            }
        )
    }

    if (showFutureDateAlert) {
        AlertDialog(
            onDismissRequest = { showFutureDateAlert = false },
            title = { Text("Future date") },
            text = { Text("You can mark attendance only for today or past dates.") },
            confirmButton = {
                TextButton(onClick = { showFutureDateAlert = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun AttendanceCalendar(
    selectedDate: LocalDate,
    attendanceMap: Map<String, AttendanceStatus>,
    trackingStartDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val monthYear = selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
            Text(
                text = monthYear,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                text = "Tap any tracked date to mark present, absent, or holiday.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Weekdays Header
            Row(modifier = Modifier.fillMaxWidth()) {
                val days = listOf("M", "T", "W", "T", "F", "S", "S")
                days.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Simplified Calendar Grid (Current Month Logic)
            val firstDayOfMonth = selectedDate.withDayOfMonth(1)
            val dayOfWeekOffset = firstDayOfMonth.dayOfWeek.value - 1
            val daysInMonth = selectedDate.lengthOfMonth()

            var currentDay = 1
            for (week in 0..5) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (dayIndex in 0..6) {
                        val isDayInMonth = (week == 0 && dayIndex >= dayOfWeekOffset) || 
                                           (week > 0 && currentDay <= daysInMonth)
                        
                        if (isDayInMonth && currentDay <= daysInMonth) {
                            val date = selectedDate.withDayOfMonth(currentDay)
                            CalendarDay(
                                day = currentDay,
                                date = date,
                                status = attendanceMap[date.toString()] ?: AttendanceStatus.NOT_MARKED,
                                isSelected = date == selectedDate,
                                isEnabled = !date.isBefore(trackingStartDate),
                                isFutureDate = date.isAfter(LocalDate.now()),
                                modifier = Modifier.weight(1f),
                                onClick = { onDateSelected(date) }
                            )
                            currentDay++
                        } else {
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                        }
                    }
                }
                if (currentDay > daysInMonth) break
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                LegendItem("Present", Color(0xFF2E7D32))
                LegendItem("Absent", Color(0xFFD32F2F))
                LegendItem("Holiday", Color.White)
            }
        }
    }
}

@Composable
fun CalendarDay(
    day: Int,
    date: LocalDate,
    status: AttendanceStatus,
    isSelected: Boolean,
    isEnabled: Boolean,
    isFutureDate: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor = when (status) {
        AttendanceStatus.PRESENT -> Color(0xFF2E7D32)
        AttendanceStatus.ABSENT -> Color(0xFFD32F2F)
        AttendanceStatus.HOLIDAY -> Color.White
        AttendanceStatus.NOT_MARKED -> Color.Transparent
    }
    val isSunday = date.dayOfWeek.value == 7
    val contentColor = when (status) {
        AttendanceStatus.PRESENT -> Color.White
        AttendanceStatus.ABSENT -> Color.White
        AttendanceStatus.HOLIDAY -> MaterialTheme.colorScheme.onSurface
        AttendanceStatus.NOT_MARKED -> if (isSunday) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
    }
    val isUnavailable = !isEnabled || isFutureDate
    val dayBackground = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        isUnavailable -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)
        else -> bgColor.copy(alpha = if (isEnabled) 1f else 0.24f)
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(dayBackground)
            .clickable(enabled = isEnabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isUnavailable) {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.72f)
            } else if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                contentColor
            },
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun AttendanceStats(attendanceMap: Map<String, AttendanceStatus>, trackingStartDate: LocalDate) {
    val summary = calculateAttendanceSummary(attendanceMap, trackingStartDate)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard("Tracked", summary.trackedDays.toString(), Modifier.weight(1f))
        StatCard("Present", summary.present.toString(), Modifier.weight(1f), color = Color(0xFF2E7D32))
        StatCard("Absent", summary.absent.toString(), Modifier.weight(1f), color = Color(0xFFD32F2F))
        StatCard("%", "${summary.percentage}%", Modifier.weight(1f), color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurface) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall)
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun QuickActions(
    isTodaySunday: Boolean,
    onPresentToday: () -> Unit,
    onLeaveToday: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onPresentToday,
                enabled = !isTodaySunday,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Present Today")
            }
            OutlinedButton(
                onClick = onLeaveToday,
                enabled = !isTodaySunday,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.EventBusy, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Mark Leave")
            }
        }
        if (isTodaySunday) {
            Text(
                text = "Sunday is ignored by default. Tap the date in the calendar only if you need to mark it.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MarkAttendanceDialog(
    date: LocalDate,
    currentStatus: AttendanceStatus,
    onDismiss: () -> Unit,
    onSave: (AttendanceStatus, String) -> Unit
) {
    var status by remember { mutableStateOf(currentStatus) }
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mark Attendance - ${date.format(DateTimeFormatter.ofPattern("dd MMM"))}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (date.dayOfWeek.value == 7) {
                    Text(
                        text = "Sunday is not counted unless you save a status for it.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text("Select Status:")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = status == AttendanceStatus.PRESENT,
                        onClick = { status = AttendanceStatus.PRESENT },
                        label = { Text("Present") }
                    )
                    FilterChip(
                        selected = status == AttendanceStatus.ABSENT,
                        onClick = { status = AttendanceStatus.ABSENT },
                        label = { Text("Absent") }
                    )
                    FilterChip(
                        selected = status == AttendanceStatus.HOLIDAY,
                        onClick = { status = AttendanceStatus.HOLIDAY },
                        label = { Text("Holiday") }
                    )
                }
                
                if (status == AttendanceStatus.ABSENT) {
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason for absence") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Sick, Personal work, etc.") }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(status, reason) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun AttendanceScreenPreview() {
    OxyTheme {
        AttendanceScreen(
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
