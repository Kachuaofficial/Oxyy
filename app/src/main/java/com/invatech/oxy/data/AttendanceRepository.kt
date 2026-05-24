package com.invatech.oxy.data

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.invatech.oxy.screens.AttendanceStatus
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class AttendanceRecord(
    val date: String,
    val status: AttendanceStatus,
    val reason: String = ""
)

class AttendanceRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun loadAttendance(uid: String): Map<String, AttendanceStatus> {
        val snapshot = firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(ATTENDANCE_COLLECTION)
            .get()
            .await()

        return snapshot.documents.mapNotNull { document ->
            val status = document.getString("status")?.let { value ->
                runCatching { AttendanceStatus.valueOf(value) }.getOrNull()
            } ?: return@mapNotNull null
            document.id to status
        }.toMap()
    }

    suspend fun saveAttendance(uid: String, record: AttendanceRecord) {
        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(ATTENDANCE_COLLECTION)
            .document(record.date)
            .set(
                mapOf(
                    "date" to record.date,
                    "status" to record.status.name,
                    "reason" to record.reason,
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
            .await()
    }

    private companion object {
        const val USERS_COLLECTION = "users"
        const val ATTENDANCE_COLLECTION = "attendance"
    }
}

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) {
            continuation.resume(task.result)
        } else {
            continuation.resumeWithException(task.exception ?: IllegalStateException("Firebase task failed."))
        }
    }
}
