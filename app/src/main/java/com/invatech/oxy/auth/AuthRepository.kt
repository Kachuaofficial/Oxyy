package com.invatech.oxy.auth

import android.content.Context
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
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
        val credentialManager = CredentialManager.create(context)
        val credential = getGoogleCredential(context, credentialManager, webClientId)

        val googleCredential = try {
            GoogleIdTokenCredential.createFrom(credential.data)
        } catch (exception: GoogleIdTokenParsingException) {
            throw IllegalStateException("Google returned an invalid ID token.", exception)
        }

        val firebaseCredential = GoogleAuthProvider.getCredential(googleCredential.idToken, null)
        val authResult = try {
            auth.signInWithCredential(firebaseCredential).await()
        } catch (exception: Exception) {
            throw IllegalStateException(exception.toFirebaseAuthMessage(), exception)
        }
        val firebaseUser = authResult.user ?: throw IllegalStateException("Firebase sign-in completed without a user.")

        return ensureUserDocument(firebaseUser)
    }

    private suspend fun getGoogleCredential(
        context: Context,
        credentialManager: CredentialManager,
        webClientId: String
    ): Credential {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            credentialManager.getCredential(context, request).credential
        } catch (exception: NoCredentialException) {
            getExplicitGoogleCredential(context, credentialManager, webClientId)
        } catch (exception: GetCredentialException) {
            if (exception.message?.contains("No credentials", ignoreCase = true) == true) {
                getExplicitGoogleCredential(context, credentialManager, webClientId)
            } else {
                throw IllegalStateException(exception.toGoogleSignInMessage(), exception)
            }
        }
    }

    private suspend fun getExplicitGoogleCredential(
        context: Context,
        credentialManager: CredentialManager,
        webClientId: String
    ): Credential {
        val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(webClientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        return try {
            credentialManager.getCredential(context, request).credential
        } catch (exception: NoCredentialException) {
            throw IllegalStateException(
                "No Google account is available on this device. Add a Google account and try again.",
                exception
            )
        } catch (exception: GetCredentialException) {
            throw IllegalStateException(exception.toGoogleSignInMessage(), exception)
        }
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

    private fun GetCredentialException.toGoogleSignInMessage(): String {
        val rawMessage = message.orEmpty()

        if (
            rawMessage.contains("account reauth failed", ignoreCase = true) ||
            rawMessage.contains("[16]", ignoreCase = true)
        ) {
            return "Google sign-in is not configured for this app build. Add this build's SHA-1 and SHA-256 fingerprints to the Android app in Firebase, confirm the package name is com.invatech.oxy, enable Google sign-in, then download the updated google-services.json."
        }

        return rawMessage.takeIf { it.isNotBlank() }
            ?: "Google sign-in was cancelled or unavailable. Please try again."
    }

    private fun Exception.toFirebaseAuthMessage(): String {
        val authException = this as? FirebaseAuthException
        val rawMessage = message.orEmpty()

        if (
            authException?.errorCode == "ERROR_APP_NOT_AUTHORIZED" ||
            rawMessage.contains("not authorized", ignoreCase = true) ||
            rawMessage.contains("SHA", ignoreCase = true)
        ) {
            return "This release build is not authorized for Firebase Auth. Add the release SHA-1 and SHA-256 fingerprints in Firebase Console, then download the updated google-services.json."
        }

        return rawMessage.takeIf { it.isNotBlank() }
            ?: "Firebase could not complete Google sign-in. Please try again."
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
