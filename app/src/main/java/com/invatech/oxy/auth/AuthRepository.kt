package com.invatech.oxy.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.invatech.oxy.R
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class OxyUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val semester: String? = null,
    val department: String? = null,
    val rollNumber: String? = null,
    val attendanceStartDate: String? = null,
    val createdAt: Timestamp? = null
) {
    val hasCompletedProfile: Boolean
        get() = !semester.isNullOrBlank() && !attendanceStartDate.isNullOrBlank()
}

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun addAuthStateListener(listener: FirebaseAuth.AuthStateListener) {
        auth.addAuthStateListener(listener)
    }

    fun removeAuthStateListener(listener: FirebaseAuth.AuthStateListener) {
        auth.removeAuthStateListener(listener)
    }

    suspend fun signInWithGoogle(context: Context): OxyUser {
        val webClientId = context.defaultWebClientId()
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credential = try {
            CredentialManager.create(context).getCredential(context, request).credential
        } catch (exception: GetCredentialException) {
            throw IllegalStateException(exception.message ?: "Google sign-in was cancelled or unavailable.", exception)
        }

        val googleCredential = try {
            GoogleIdTokenCredential.createFrom(credential.data)
        } catch (exception: GoogleIdTokenParsingException) {
            throw IllegalStateException("Google returned an invalid ID token.", exception)
        }

        val firebaseCredential = GoogleAuthProvider.getCredential(googleCredential.idToken, null)
        val authResult = auth.signInWithCredential(firebaseCredential).await()
        val firebaseUser = authResult.user ?: throw IllegalStateException("Firebase sign-in completed without a user.")

        return ensureUserDocument(firebaseUser)
    }

    suspend fun loadSignedInUser(): OxyUser? {
        val firebaseUser = currentUser ?: return null
        return ensureUserDocument(firebaseUser)
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun completeProfile(
        semester: String,
        department: String,
        rollNumber: String,
        attendanceStartDate: String
    ): OxyUser {
        val firebaseUser = currentUser ?: throw IllegalStateException("You must be signed in to complete your profile.")
        firestore.collection(USERS_COLLECTION)
            .document(firebaseUser.uid)
            .set(
                mapOf(
                    "semester" to semester.trim(),
                    "department" to department.trim(),
                    "rollNumber" to rollNumber.trim(),
                    "attendanceStartDate" to attendanceStartDate,
                    "profileComplete" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
            .await()

        return ensureUserDocument(firebaseUser)
    }

    private suspend fun ensureUserDocument(firebaseUser: FirebaseUser): OxyUser {
        val userRef = firestore.collection(USERS_COLLECTION).document(firebaseUser.uid)
        val snapshot = userRef.get().await()
        val existingCreatedAt = snapshot.getTimestamp("createdAt")

        val userData = mutableMapOf<String, Any?>(
            "uid" to firebaseUser.uid,
            "email" to firebaseUser.email,
            "displayName" to firebaseUser.displayName,
            "photoUrl" to firebaseUser.photoUrl?.toString(),
            "provider" to "google.com",
            "lastLoginAt" to FieldValue.serverTimestamp()
        )

        if (!snapshot.exists()) {
            userData["createdAt"] = FieldValue.serverTimestamp()
            userData["role"] = "student"
            userData["profileComplete"] = false
        }

        userRef.set(userData, SetOptions.merge()).await()

        return OxyUser(
            uid = firebaseUser.uid,
            email = firebaseUser.email,
            displayName = firebaseUser.displayName,
            photoUrl = snapshot.getString("photoUrl") ?: firebaseUser.photoUrl?.toString(),
            semester = snapshot.getString("semester"),
            department = snapshot.getString("department"),
            rollNumber = snapshot.getString("rollNumber"),
            attendanceStartDate = snapshot.getString("attendanceStartDate"),
            createdAt = existingCreatedAt
        )
    }

    private fun Context.defaultWebClientId(): String {
        val generatedResourceId = resources.getIdentifier("default_web_client_id", "string", packageName)
        val webClientId = if (generatedResourceId != 0) {
            getString(generatedResourceId)
        } else {
            getString(R.string.google_web_client_id)
        }

        if (webClientId.isBlank()) {
            throw IllegalStateException(
                "Missing Google web client ID. Add a valid OAuth web client ID in Firebase Console."
            )
        }
        return webClientId
    }

    private companion object {
        const val USERS_COLLECTION = "users"
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
