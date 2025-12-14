package com.lehau007.todolist.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lehau007.todolist.domain.model.Category
import com.lehau007.todolist.domain.model.Task
import com.lehau007.todolist.domain.repository.CategoryRepository
import com.lehau007.todolist.domain.usecase.GetTaskByIdUseCase
import com.lehau007.todolist.domain.usecase.UpsertTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for add/edit task screen.
 */
@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    private val upsertTaskUseCase: UpsertTaskUseCase,
    private val getTaskByIdUseCase: GetTaskByIdUseCase,
    private val categoryRepository: CategoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val taskId: String? = savedStateHandle.get<String>("taskId")
    
    private val _taskState = MutableStateFlow(TaskFormState())
    val taskState: StateFlow<TaskFormState> = _taskState.asStateFlow()
    
    private val _uiState = MutableStateFlow<AddEditTaskUiState>(AddEditTaskUiState.Idle)
    val uiState: StateFlow<AddEditTaskUiState> = _uiState.asStateFlow()
    
    // Categories for picker
    val categories: StateFlow<List<Category>> = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // Priority icon picker state
    private val _showPriorityPicker = MutableStateFlow(false)
    val showPriorityPicker: StateFlow<Boolean> = _showPriorityPicker.asStateFlow()
    
    init {
        if (taskId != null) {
            loadTask(taskId)
        }
    }
    
    private fun loadTask(id: String) {
        viewModelScope.launch {
            getTaskByIdUseCase(id).collect { task ->
                if (task != null) {
                    _taskState.value = TaskFormState(
                        id = task.id,
                        title = task.title,
                        description = task.description ?: "",
                        dueDateTime = task.dueDateTime,
                        priority = task.priority,
                        priorityIcon = task.priorityIcon,
                        categoryId = task.categoryId,
                        hasTime = task.hasTime,
                        reminderMinutesBefore = task.reminderMinutesBefore,
                        createdAt = task.createdAt,
                        isRecurring = task.isRecurring,
                        recurrencePeriod = task.recurrencePeriod,
                        recurrenceEndDate = task.recurrenceEndDate,
                        totalFocusTimeSeconds = task.totalFocusTimeSeconds
                    )
                }
            }
        }
    }
    
    fun updateTitle(title: String) {
        _taskState.value = _taskState.value.copy(title = title)
    }
    
    fun updateDescription(description: String) {
        _taskState.value = _taskState.value.copy(description = description)
    }
    
    fun updateDueDateTime(dateTime: Long) {
        _taskState.value = _taskState.value.copy(dueDateTime = dateTime)
    }
    
    fun updatePriority(priority: Task.Priority) {
        _taskState.value = _taskState.value.copy(
            priority = priority,
            priorityIcon = Task.PriorityIcon.Standard(priority)
        )
    }
    
    fun updatePriorityIcon(icon: Task.PriorityIcon) {
        _taskState.value = _taskState.value.copy(priorityIcon = icon)
    }
    
    fun updateCategoryId(categoryId: String?) {
        _taskState.value = _taskState.value.copy(categoryId = categoryId)
    }
    
    fun updateHasTime(hasTime: Boolean) {
        _taskState.value = _taskState.value.copy(hasTime = hasTime)
    }
    
    fun updateReminderMinutes(minutes: Int) {
        _taskState.value = _taskState.value.copy(reminderMinutesBefore = minutes)
    }
    
    fun updateIsRecurring(isRecurring: Boolean) {
        _taskState.value = _taskState.value.copy(isRecurring = isRecurring)
    }
    
    fun updateRecurrencePeriod(period: Task.RecurrencePeriod) {
        _taskState.value = _taskState.value.copy(recurrencePeriod = period)
    }
    
    fun updateRecurrenceEndDate(endDate: Long?) {
        _taskState.value = _taskState.value.copy(recurrenceEndDate = endDate)
    }
    
    fun showPriorityPicker() {
        _showPriorityPicker.value = true
    }
    
    fun hidePriorityPicker() {
        _showPriorityPicker.value = false
    }
    
    /**
     * Save the task (create or update).
     */
    fun saveTask() {
        val state = _taskState.value
        
        if (state.title.isBlank()) {
            _uiState.value = AddEditTaskUiState.Error("Title cannot be empty")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = AddEditTaskUiState.Saving
            
            val task = Task(
                id = state.id,
                title = state.title,
                description = state.description.ifBlank { null },
                dueDateTime = state.dueDateTime,
                priority = state.priority,
                priorityIcon = state.priorityIcon,
                isCompleted = false,
                categoryId = state.categoryId,
                hasTime = state.hasTime,
                reminderMinutesBefore = state.reminderMinutesBefore,
                createdAt = state.createdAt,
                updatedAt = System.currentTimeMillis(),
                isRecurring = state.isRecurring,
                recurrencePeriod = state.recurrencePeriod,
                recurrenceEndDate = state.recurrenceEndDate,
                totalFocusTimeSeconds = state.totalFocusTimeSeconds
            )
            
            val result = upsertTaskUseCase(task)
            
            if (result.isSuccess) {
                _uiState.value = AddEditTaskUiState.Success
            } else {
                _uiState.value = AddEditTaskUiState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to save task"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = AddEditTaskUiState.Idle
    }
}

/**
 * Form state for task creation/editing.
 */
data class TaskFormState(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val description: String = "",
    val dueDateTime: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000), // Tomorrow
    val priority: Task.Priority = Task.Priority.MEDIUM,
    val priorityIcon: Task.PriorityIcon = Task.PriorityIcon.Standard(Task.Priority.MEDIUM),
    val categoryId: String? = null,
    val hasTime: Boolean = false,
    val reminderMinutesBefore: Int = 30,
    val createdAt: Long = System.currentTimeMillis(),
    val isRecurring: Boolean = false,
    val recurrencePeriod: Task.RecurrencePeriod = Task.RecurrencePeriod.NONE,
    val recurrenceEndDate: Long? = null,
    val totalFocusTimeSeconds: Long = 0
)

/**
 * UI state for add/edit screen.
 */
sealed class AddEditTaskUiState {
    data object Idle : AddEditTaskUiState()
    data object Saving : AddEditTaskUiState()
    data object Success : AddEditTaskUiState()
    data class Error(val message: String) : AddEditTaskUiState()
}
