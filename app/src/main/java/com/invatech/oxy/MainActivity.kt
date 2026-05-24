package com.invatech.oxy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.invatech.oxy.auth.AuthUiState
import com.invatech.oxy.auth.AuthViewModel
import com.invatech.oxy.screens.AuthScreen
import com.invatech.oxy.screens.MainScreen
import com.invatech.oxy.screens.ProfileSetupScreen
import com.invatech.oxy.ui.theme.OxyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OxyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val authViewModel: AuthViewModel = viewModel()
                    val uiState by authViewModel.uiState.collectAsState()
                    val context = LocalContext.current

                    when (val state = uiState) {
                        is AuthUiState.SignedIn -> {
                            if (state.user.hasCompletedProfile) {
                                MainScreen(user = state.user, onSignOut = authViewModel::signOut)
                            } else {
                                ProfileSetupScreen(
                                    user = state.user,
                                    isSaving = false,
                                    onSave = { semester, department, rollNumber, startDate ->
                                        authViewModel.completeProfile(
                                            user = state.user,
                                            semester = semester,
                                            department = department,
                                            rollNumber = rollNumber,
                                            attendanceStartDate = startDate
                                        )
                                    },
                                    onSignOut = authViewModel::signOut
                                )
                            }
                        }

                        is AuthUiState.CompletingProfile -> {
                            ProfileSetupScreen(
                                user = state.user,
                                isSaving = true,
                                onSave = { _, _, _, _ -> },
                                onSignOut = authViewModel::signOut
                            )
                        }

                        else -> {
                            AuthScreen(
                                uiState = state,
                                onGoogleSignIn = { authViewModel.signInWithGoogle(context) },
                                onDismissError = authViewModel::clearError
                            )
                        }
                    }
                }
            }
        }
    }
}
