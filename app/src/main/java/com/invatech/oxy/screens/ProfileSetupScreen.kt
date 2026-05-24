package com.invatech.oxy.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.invatech.oxy.auth.OxyUser
import com.invatech.oxy.ui.theme.OxyTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    user: OxyUser,
    isSaving: Boolean,
    onSave: (semester: String, department: String, rollNumber: String, startDate: String) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    var semester by remember { mutableStateOf(user.semester.orEmpty()) }
    var department by remember { mutableStateOf(user.department.orEmpty()) }
    var rollNumber by remember { mutableStateOf(user.rollNumber.orEmpty()) }
    var attendanceStartDate by remember { mutableStateOf(user.attendanceStartDate ?: LocalDate.now().toString()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val canSave = semester.isNotBlank() && department.isNotBlank() && rollNumber.isNotBlank() && attendanceStartDate.isNotBlank()
    val semesterOptions = listOf("1", "2", "3", "4", "5", "6", "7", "8")
    val departmentOptions = listOf(
        "Computer Science",
        "Information Technology",
        "Electronics",
        "Mechanical",
        "Civil"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        Box(
            modifier = Modifier.size(58.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {}
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Set up attendance tracking",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Choose fixed academic details so attendance stays consistent across devices.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            ChipSelector(
                title = "Semester",
                options = semesterOptions,
                selectedOption = semester,
                onOptionSelected = { semester = it }
            )
            ChipSelector(
                title = "Department",
                options = departmentOptions,
                selectedOption = department,
                onOptionSelected = { department = it }
            )
            OutlinedTextField(
                value = rollNumber,
                onValueChange = { rollNumber = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Roll number") },
                placeholder = { Text("OXY-24-CS-118") },
                singleLine = true
            )
            OutlinedTextField(
                value = formatDate(attendanceStartDate),
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                label = { Text("Attendance tracking starts from") },
                leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                trailingIcon = {
                    TextButton(onClick = { showDatePicker = true }) {
                        Text("Change")
                    }
                }
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { onSave(semester, department, rollNumber, attendanceStartDate) },
                enabled = canSave && !isSaving,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(if (isSaving) "Saving..." else "Start attendance tracking")
            }
            TextButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
                Text("Use another account")
            }
        }
    }

    if (showDatePicker) {
        val initialMillis = LocalDate.parse(attendanceStartDate)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            attendanceStartDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                                .toString()
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Select")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun ChipSelector(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                FilterChip(
                    selected = selectedOption == option,
                    onClick = { onOptionSelected(option) },
                    label = { Text(option) }
                )
            }
        }
    }
}

private fun formatDate(value: String): String {
    return runCatching {
        LocalDate.parse(value).format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
    }.getOrDefault(value)
}

@Preview(showBackground = true)
@Composable
fun ProfileSetupScreenPreview() {
    OxyTheme {
        ProfileSetupScreen(
            user = OxyUser(
                uid = "demo",
                email = "student@example.com",
                displayName = "Student",
                photoUrl = null
            ),
            isSaving = false,
            onSave = { _, _, _, _ -> },
            onSignOut = {}
        )
    }
}
