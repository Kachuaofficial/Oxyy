package com.invatech.oxy.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object Checking : AuthUiState
    data object SignedOut : AuthUiState
    data object SigningIn : AuthUiState
    data class CompletingProfile(val user: OxyUser) : AuthUiState
    data class SignedIn(val user: OxyUser) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Checking)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener {
        viewModelScope.launch {
            _uiState.value = repository.loadSignedInUser()?.let(AuthUiState::SignedIn) ?: AuthUiState.SignedOut
        }
    }

    init {
        repository.addAuthStateListener(authStateListener)
    }

    fun signInWithGoogle(context: Context) {
        _uiState.value = AuthUiState.SigningIn
        viewModelScope.launch {
            _uiState.value = try {
                AuthUiState.SignedIn(repository.signInWithGoogle(context))
            } catch (exception: Exception) {
                AuthUiState.Error(exception.message ?: "Unable to sign in with Google.")
            }
        }
    }

    fun signOut() {
        repository.signOut()
        _uiState.value = AuthUiState.SignedOut
    }

    fun completeProfile(
        user: OxyUser,
        semester: String,
        department: String,
        rollNumber: String,
        attendanceStartDate: String
    ) {
        _uiState.value = AuthUiState.CompletingProfile(user)
        viewModelScope.launch {
            _uiState.value = try {
                AuthUiState.SignedIn(
                    repository.completeProfile(
                        semester = semester,
                        department = department,
                        rollNumber = rollNumber,
                        attendanceStartDate = attendanceStartDate
                    )
                )
            } catch (exception: Exception) {
                AuthUiState.Error(exception.message ?: "Unable to save profile details.")
            }
        }
    }

    fun clearError() {
        _uiState.value = AuthUiState.SignedOut
    }

    override fun onCleared() {
        repository.removeAuthStateListener(authStateListener)
        super.onCleared()
    }
}
