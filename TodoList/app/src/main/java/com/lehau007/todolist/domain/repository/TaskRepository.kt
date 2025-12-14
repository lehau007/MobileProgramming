package com.lehau007.todolist.domain.repository

import com.lehau007.todolist.domain.model.Task
import com.lehau007.todolist.domain.model.TaskFilter
import com.lehau007.todolist.domain.model.TaskSortOrder
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface defining task data operations.
 * 
 * This abstraction allows the domain layer to be independent of data sources.
 * The actual implementation can use Room, remote API, or any other data source.
 */
interface TaskRepository {
    
    /**
     * Get all tasks with specified sorting and filtering.
     */
    fun getTasks(sortOrder: TaskSortOrder, filter: TaskFilter): Flow<List<Task>>
    
    /**
     * Get tasks by category.
     */
    fun getTasksByCategory(categoryId: String): Flow<List<Task>>
    
    /**
     * Get a single task by ID.
     */
    fun getTaskById(taskId: String): Flow<Task?>
    
    /**
     * Get a single task by ID (one-time read).
     */
    suspend fun getTaskByIdOnce(taskId: String): Task?
    
    /**
     * Insert or update a task.
     */
    suspend fun upsertTask(task: Task)
    
    /**
     * Delete a task.
     */
    suspend fun deleteTask(task: Task)
    
    /**
     * Delete a task by ID.
     */
    suspend fun deleteTaskById(taskId: String)
    
    /**
     * Toggle task completion status.
     */
    suspend fun toggleTaskCompletion(taskId: String)
    
    /**
     * Delete all completed tasks.
     */
    suspend fun deleteCompletedTasks()
    
    /**
     * Add focus time to a task.
     */
    suspend fun addFocusTime(taskId: String, seconds: Long)
    
    // Statistics
    
    /**
     * Get completed tasks count.
     */
    fun getCompletedTasksCount(): Flow<Int>
    
    /**
     * Get pending tasks count.
     */
    fun getPendingTasksCount(): Flow<Int>
    
    /**
     * Get tasks due in the next N days.
     */
    fun getUpcomingTasks(days: Int): Flow<List<Task>>
    
    /**
     * Get completed tasks count for a specific day.
     */
    suspend fun getCompletedTasksForDay(dayStartMillis: Long): Int
}
