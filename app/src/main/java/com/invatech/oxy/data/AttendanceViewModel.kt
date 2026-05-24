package com.invatech.oxy.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.invatech.oxy.screens.AttendanceStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AttendanceUiState(
    val isLoading: Boolean = true,
    val records: Map<String, AttendanceStatus> = emptyMap(),
    val error: String? = null
)

class AttendanceViewModel(
    private val repository: AttendanceRepository = AttendanceRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(AttendanceUiState())
    val uiState: StateFlow<AttendanceUiState> = _uiState.asStateFlow()

    fun load(uid: String) {
        viewModelScope.launch {
            _uiState.value = AttendanceUiState(isLoading = true)
            _uiState.value = try {
                AttendanceUiState(isLoading = false, records = repository.loadAttendance(uid))
            } catch (exception: Exception) {
                AttendanceUiState(isLoading = false, error = exception.message ?: "Unable to load attendance.")
            }
        }
    }

    fun mark(uid: String, date: String, status: AttendanceStatus, reason: String = "") {
        _uiState.update { state ->
            state.copy(records = state.records + (date to status), error = null)
        }

        viewModelScope.launch {
            try {
                repository.saveAttendance(uid, AttendanceRecord(date, status, reason))
            } catch (exception: Exception) {
                _uiState.update { state ->
                    state.copy(error = exception.message ?: "Unable to save attendance.")
                }
            }
        }
    }
}
