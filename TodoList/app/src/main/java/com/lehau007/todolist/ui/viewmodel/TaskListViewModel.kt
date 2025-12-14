package com.lehau007.todolist.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lehau007.todolist.domain.model.Category
import com.lehau007.todolist.domain.model.Task
import com.lehau007.todolist.domain.model.TaskFilter
import com.lehau007.todolist.domain.model.TaskSortOrder
import com.lehau007.todolist.domain.repository.CategoryRepository
import com.lehau007.todolist.domain.usecase.DeleteTaskUseCase
import com.lehau007.todolist.domain.usecase.GetTasksUseCase
import com.lehau007.todolist.domain.usecase.ToggleTaskCompletionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * Grouped tasks for display in TaskListScreen.
 */
data class GroupedTasks(
    val previous: List<Task> = emptyList(),
    val today: List<Task> = emptyList(),
    val future: List<Task> = emptyList()
)

/**
 * ViewModel for the task list screen.
 * 
 * Manages:
 * - Task list state with grouping (Previous/Today/Future)
 * - Category filtering
 * - Task completion and deletion
 */
@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val getTasksUseCase: GetTasksUseCase,
    private val toggleTaskCompletionUseCase: ToggleTaskCompletionUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    
    // Selected category filter (null = all)
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()
    
    // Categories list
    val categories: StateFlow<List<Category>> = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // All pending tasks
    private val allPendingTasks: Flow<List<Task>> = getTasksUseCase(TaskSortOrder.BY_DATE, TaskFilter.PENDING)
    
    // Grouped tasks (Previous, Today, Future)
    val groupedTasks: StateFlow<GroupedTasks> = combine(
        allPendingTasks,
        _selectedCategory
    ) { tasks, categoryId ->
        val filteredTasks = if (categoryId != null) {
            tasks.filter { it.categoryId == categoryId }
        } else {
            tasks
        }
        
        val today = getStartOfDay(System.currentTimeMillis())
        val todayEnd = getEndOfDay(System.currentTimeMillis())
        
        val previous = filteredTasks.filter { 
            getStartOfDay(it.dueDateTime) < today 
        }.sortedByDescending { it.dueDateTime }
        
        val todayTasks = filteredTasks.filter { task ->
            val taskDay = getStartOfDay(task.dueDateTime)
            taskDay == today
        }.sortedBy { it.dueDateTime }
        
        val future = filteredTasks.filter { 
            getStartOfDay(it.dueDateTime) > today 
        }.sortedBy { it.dueDateTime }
        
        GroupedTasks(previous = previous, today = todayTasks, future = future)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GroupedTasks())
    
    // Completed today
    val completedToday: StateFlow<List<Task>> = combine(
        getTasksUseCase(TaskSortOrder.BY_DATE, TaskFilter.COMPLETED),
        _selectedCategory
    ) { tasks, categoryId ->
        val today = getStartOfDay(System.currentTimeMillis())
        val filtered = if (categoryId != null) {
            tasks.filter { it.categoryId == categoryId }
        } else {
            tasks
        }
        // Show tasks completed today (based on updatedAt timestamp)
        filtered.filter { 
            getStartOfDay(it.updatedAt) == today 
        }.sortedByDescending { it.updatedAt }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // Legacy support for existing code
    private val _filter = MutableStateFlow(TaskFilter.ALL)
    val filter: StateFlow<TaskFilter> = _filter.asStateFlow()
    
    private val _selectedDate = MutableStateFlow<Long?>(getTodayStartTimestamp())
    val selectedDate: StateFlow<Long?> = _selectedDate.asStateFlow()
    
    val tasks: StateFlow<List<Task>> = allPendingTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    private val _uiState = MutableStateFlow<TaskListUiState>(TaskListUiState.Success)
    val uiState: StateFlow<TaskListUiState> = _uiState.asStateFlow()
    
    fun setSelectedCategory(categoryId: String?) {
        _selectedCategory.value = categoryId
    }
    
    fun setFilter(filter: TaskFilter) {
        _filter.value = filter
    }
    
    fun toggleTaskCompletion(taskId: String) {
        viewModelScope.launch {
            try {
                toggleTaskCompletionUseCase(taskId)
            } catch (e: Exception) {
                _uiState.value = TaskListUiState.Error(e.message ?: "Failed to update task")
            }
        }
    }
    
    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            try {
                deleteTaskUseCase(taskId)
            } catch (e: Exception) {
                _uiState.value = TaskListUiState.Error(e.message ?: "Failed to delete task")
            }
        }
    }
    
    fun clearError() {
        _uiState.value = TaskListUiState.Success
    }
    
    fun setSelectedDate(date: Long?) {
        _selectedDate.value = date
    }
    
    private fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    private fun getEndOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
    
    private fun getTodayStartTimestamp(): Long {
        return getStartOfDay(System.currentTimeMillis())
    }
}

/**
 * UI state for task list screen.
 */
sealed class TaskListUiState {
    data object Success : TaskListUiState()
    data class Error(val message: String) : TaskListUiState()
}
