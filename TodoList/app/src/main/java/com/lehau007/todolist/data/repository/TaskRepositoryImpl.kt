package com.lehau007.todolist.data.repository

import com.lehau007.todolist.data.local.dao.TaskDao
import com.lehau007.todolist.data.mapper.toDomain
import com.lehau007.todolist.data.mapper.toEntity
import com.lehau007.todolist.domain.model.Task
import com.lehau007.todolist.domain.model.TaskFilter
import com.lehau007.todolist.domain.model.TaskSortOrder
import com.lehau007.todolist.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of TaskRepository using Room database.
 * 
 * This class handles:
 * - Converting between entity and domain models
 * - Delegating database operations to DAO
 * - Applying sorting and filtering logic
 */
@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {
    
    override fun getTasks(sortOrder: TaskSortOrder, filter: TaskFilter): Flow<List<Task>> {
        // Always sort by priority (High -> Medium -> Low), then by due date
        val flow = when (filter) {
            TaskFilter.PENDING -> taskDao.getPendingTasks()
            TaskFilter.COMPLETED -> taskDao.getCompletedTasks()
            TaskFilter.ALL -> taskDao.getTasksByPriority()
        }
        
        return flow.map { entities -> 
            val tasks = entities.map { it.toDomain() }
            // Sort by priority (High=2, Medium=1, Low=0 descending), then by due date
            tasks.sortedWith(
                compareByDescending<Task> { it.priority.ordinal }
                    .thenBy { it.dueDateTime }
            )
        }
    }
    
    override fun getTasksByCategory(categoryId: String): Flow<List<Task>> {
        return taskDao.getTasksByCategory(categoryId).map { entities ->
            entities.map { it.toDomain() }
                .sortedWith(
                    compareByDescending<Task> { it.priority.ordinal }
                        .thenBy { it.dueDateTime }
                )
        }
    }
    
    override fun getTaskById(taskId: String): Flow<Task?> {
        return taskDao.getTaskById(taskId).map { it?.toDomain() }
    }
    
    override suspend fun getTaskByIdOnce(taskId: String): Task? {
        return taskDao.getTaskByIdOnce(taskId)?.toDomain()
    }
    
    override suspend fun upsertTask(task: Task) {
        taskDao.insertTask(task.toEntity())
    }
    
    override suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task.toEntity())
    }
    
    override suspend fun deleteTaskById(taskId: String) {
        taskDao.deleteTaskById(taskId)
    }
    
    override suspend fun toggleTaskCompletion(taskId: String) {
        val task = taskDao.getTaskByIdOnce(taskId)
        if (task != null) {
            // Handle recurring tasks specially when marking as complete
            if (task.isRecurring && !task.isCompleted && task.recurrencePeriod != "NONE") {
                // Calculate the next occurrence date
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = task.dueDateTime
                
                when (task.recurrencePeriod) {
                    "DAILY" -> calendar.add(Calendar.DAY_OF_YEAR, 1)
                    "WEEKLY" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
                    "MONTHLY" -> calendar.add(Calendar.MONTH, 1)
                }
                
                val nextDueDateTime = calendar.timeInMillis
                
                // Check if next occurrence is within the recurrence end date (if set)
                val shouldContinueRecurrence = task.recurrenceEndDate == null || 
                    nextDueDateTime <= task.recurrenceEndDate
                
                if (shouldContinueRecurrence) {
                    // Move to next occurrence instead of marking as complete
                    taskDao.updateTaskDueDateTime(
                        taskId = taskId,
                        dueDateTime = nextDueDateTime,
                        updatedAt = System.currentTimeMillis()
                    )
                } else {
                    // Past the end date, mark as completed
                    taskDao.updateTaskCompletionStatus(
                        taskId = taskId,
                        isCompleted = true,
                        updatedAt = System.currentTimeMillis()
                    )
                }
            } else {
                // Non-recurring task or unchecking a completed task
                taskDao.updateTaskCompletionStatus(
                    taskId = taskId,
                    isCompleted = !task.isCompleted,
                    updatedAt = System.currentTimeMillis()
                )
            }
        }
    }
    
    override suspend fun deleteCompletedTasks() {
        taskDao.deleteCompletedTasks()
    }
    
    override suspend fun addFocusTime(taskId: String, seconds: Long) {
        taskDao.addFocusTime(taskId, seconds, System.currentTimeMillis())
    }
    
    // Statistics
    
    override fun getCompletedTasksCount(): Flow<Int> {
        return taskDao.getCompletedTasksCount()
    }
    
    override fun getPendingTasksCount(): Flow<Int> {
        return taskDao.getPendingTasksCount()
    }
    
    override fun getUpcomingTasks(days: Int): Flow<List<Task>> {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = now
        calendar.add(Calendar.DAY_OF_YEAR, days)
        val endTime = calendar.timeInMillis
        
        return taskDao.getUpcomingTasks(now, endTime).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getCompletedTasksForDay(dayStartMillis: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = dayStartMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val endOfDay = calendar.timeInMillis
        
        return taskDao.getCompletedTasksInRange(startOfDay, endOfDay)
    }
}
