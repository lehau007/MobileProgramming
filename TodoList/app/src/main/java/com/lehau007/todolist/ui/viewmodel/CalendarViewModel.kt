package com.lehau007.todolist.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lehau007.todolist.domain.model.Task
import com.lehau007.todolist.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {
    
    private val _currentMonth = MutableStateFlow(Calendar.getInstance())
    val currentMonth: StateFlow<Calendar> = _currentMonth.asStateFlow()
    
    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()
    
    private val _isMonthlyView = MutableStateFlow(true)
    val isMonthlyView: StateFlow<Boolean> = _isMonthlyView.asStateFlow()
    
    private val allTasks: StateFlow<List<Task>> = taskRepository.getTasks(
        com.lehau007.todolist.domain.model.TaskSortOrder.BY_DATE,
        com.lehau007.todolist.domain.model.TaskFilter.ALL
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val tasksForSelectedDate: StateFlow<List<Task>> = combine(
        allTasks,
        _selectedDate
    ) { tasks, selectedDate ->
        tasks.filter { isSameDay(it.dueDateTime, selectedDate) && !it.isCompleted }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    fun getTaskDates(): Set<Long> {
        return allTasks.value.filter { !it.isCompleted }.map { it.dueDateTime }.toSet()
    }
    
    fun selectDate(date: Long) {
        _selectedDate.value = date
    }
    
    fun previousMonth() {
        val cal = _currentMonth.value.clone() as Calendar
        cal.add(Calendar.MONTH, -1)
        _currentMonth.value = cal
    }
    
    fun nextMonth() {
        val cal = _currentMonth.value.clone() as Calendar
        cal.add(Calendar.MONTH, 1)
        _currentMonth.value = cal
    }
    
    fun setMonthlyView(isMonthly: Boolean) {
        _isMonthlyView.value = isMonthly
    }
    
    fun toggleView() {
        _isMonthlyView.value = !_isMonthlyView.value
    }
    
    private fun isSameDay(date1: Long, date2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = date1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
