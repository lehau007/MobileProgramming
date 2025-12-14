package com.lehau007.todolist.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lehau007.todolist.domain.model.Task
import com.lehau007.todolist.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {
    
    val completedTasksCount: StateFlow<Int> = taskRepository.getCompletedTasksCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
    val pendingTasksCount: StateFlow<Int> = taskRepository.getPendingTasksCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
    val upcomingTasks: StateFlow<List<Task>> = taskRepository.getUpcomingTasks(7)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    private val _currentWeekOffset = MutableStateFlow(0)
    
    val currentWeekRange: StateFlow<String> = _currentWeekOffset.map { offset ->
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.WEEK_OF_YEAR, offset)
        calendar.firstDayOfWeek = Calendar.SUNDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        val startDate = calendar.time
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        val endDate = calendar.time
        
        val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
        "${dateFormat.format(startDate)}-${dateFormat.format(endDate)}"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    
    val weeklyCompletionStats: StateFlow<List<Int>> = _currentWeekOffset
        .map { offset -> loadWeeklyStats(offset) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), List(7) { 0 })
    
    private suspend fun loadWeeklyStats(weekOffset: Int): List<Int> {
        val stats = mutableListOf<Int>()
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.WEEK_OF_YEAR, weekOffset)
        calendar.firstDayOfWeek = Calendar.SUNDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        for (i in 0..6) {
            val dayStart = calendar.timeInMillis
            val count = taskRepository.getCompletedTasksForDay(dayStart)
            stats.add(count)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        return stats
    }
    
    fun previousWeek() {
        _currentWeekOffset.value -= 1
    }
    
    fun nextWeek() {
        _currentWeekOffset.value += 1
    }
}
