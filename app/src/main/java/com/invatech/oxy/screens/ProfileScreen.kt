package com.invatech.oxy.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.invatech.oxy.auth.OxyUser
import com.invatech.oxy.ui.theme.OxyTheme

@Composable
fun ProfileScreen(
    user: OxyUser,
    onSignOut: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProfileHeader(user = user)
        ProfileInfoCard(user = user)
        Button(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Text(text = " Sign out")
        }
    }
}

@Composable
private fun ProfileHeader(user: OxyUser) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserProfileImage(
                photoUrl = user.photoUrl,
                displayName = user.displayName,
                size = 64.dp
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.displayName ?: "Student",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = user.email.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f)
                )
            }
        }
    }
}

@Composable
private fun ProfileInfoCard(user: OxyUser) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Academic Details", style = MaterialTheme.typography.titleMedium)
            ProfileInfoRow(Icons.Default.School, "Department", user.department.orEmpty(), Color(0xFF1976D2))
            ProfileInfoRow(Icons.Default.CalendarMonth, "Semester", user.semester.orEmpty(), Color(0xFF2E7D32))
            ProfileInfoRow(Icons.Default.Badge, "Roll number", user.rollNumber.orEmpty(), Color(0xFF7B1FA2))
            ProfileInfoRow(Icons.Default.Email, "Tracking start", user.attendanceStartDate.orEmpty(), Color(0xFFEF6C00))
        }
    }
}

@Composable
private fun ProfileInfoRow(icon: ImageVector, label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(42.dp),
            shape = RoundedCornerShape(12.dp),
            color = color.copy(alpha = 0.14f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.padding(10.dp),
                tint = color
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value.ifBlank { "Not set" }, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    OxyTheme {
        ProfileScreen(
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
