package com.lehau007.todolist.ui.viewmodel

import android.os.Vibrator
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lehau007.todolist.domain.model.Task
import com.lehau007.todolist.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class FocusState {
    IDLE, RUNNING, PAUSED, COMPLETED
}

@HiltViewModel
class FocusModeViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val taskId: String = savedStateHandle.get<String>("taskId") ?: ""
    
    val task: StateFlow<Task?> = taskRepository.getTaskById(taskId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    
    private val _focusState = MutableStateFlow(FocusState.IDLE)
    val focusState: StateFlow<FocusState> = _focusState.asStateFlow()
    
    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()
    
    private val _targetMinutes = MutableStateFlow(0)
    val targetMinutes: StateFlow<Int> = _targetMinutes.asStateFlow()
    
    private val _showTimePicker = MutableStateFlow(false)
    val showTimePicker: StateFlow<Boolean> = _showTimePicker.asStateFlow()
    
    private var timerJob: Job? = null
    
    fun setTargetMinutes(minutes: Int) {
        _targetMinutes.value = minutes
    }
    
    fun showTimePicker() {
        _showTimePicker.value = true
    }
    
    fun hideTimePicker() {
        _showTimePicker.value = false
    }
    
    fun startTimer() {
        _focusState.value = FocusState.RUNNING
        timerJob = viewModelScope.launch {
            while (_focusState.value == FocusState.RUNNING) {
                delay(1000)
                _elapsedSeconds.value += 1
                
                // Check if target reached
                val targetSeconds = _targetMinutes.value * 60L
                if (targetSeconds > 0 && _elapsedSeconds.value >= targetSeconds) {
                    _focusState.value = FocusState.COMPLETED
                    vibrateCompletion()
                }
            }
        }
    }
    
    fun pauseTimer() {
        _focusState.value = FocusState.PAUSED
        timerJob?.cancel()
    }
    
    fun resumeTimer() {
        _focusState.value = FocusState.RUNNING
        timerJob = viewModelScope.launch {
            while (_focusState.value == FocusState.RUNNING) {
                delay(1000)
                _elapsedSeconds.value += 1
            }
        }
    }
    
    fun stopTimer() {
        _focusState.value = FocusState.IDLE
        timerJob?.cancel()
    }
    
    fun resetTimer() {
        stopTimer()
        _elapsedSeconds.value = 0
    }
    
    fun stopAndSave() {
        timerJob?.cancel()
        viewModelScope.launch {
            if (_elapsedSeconds.value > 0 && taskId.isNotEmpty()) {
                taskRepository.addFocusTime(taskId, _elapsedSeconds.value)
            }
        }
        _focusState.value = FocusState.IDLE
    }
    
    private fun vibrateCompletion() {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.vibrate(android.os.VibrationEffect.createOneShot(500, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (e: Exception) {
            // Ignore vibration errors
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
