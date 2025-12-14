package com.lehau007.todolist.data.local.dao

import androidx.room.*
import com.lehau007.todolist.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for task operations.
 * 
 * Uses Flow for reactive data updates - UI will automatically update
 * when database changes occur.
 */
@Dao
interface TaskDao {
    
    /**
     * Get all tasks ordered by due date (ascending).
     * Returns a Flow for reactive updates.
     */
    @Query("SELECT * FROM tasks ORDER BY dueDateTime ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>
    
    /**
     * Get tasks sorted by priority (High to Low).
     */
    @Query("SELECT * FROM tasks ORDER BY priority DESC, dueDateTime ASC")
    fun getTasksByPriority(): Flow<List<TaskEntity>>
    
    /**
     * Get only pending (not completed) tasks.
     */
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY dueDateTime ASC")
    fun getPendingTasks(): Flow<List<TaskEntity>>
    
    /**
     * Get only completed tasks.
     */
    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY dueDateTime DESC")
    fun getCompletedTasks(): Flow<List<TaskEntity>>
    
    /**
     * Get tasks by category.
     */
    @Query("SELECT * FROM tasks WHERE categoryId = :categoryId ORDER BY dueDateTime ASC")
    fun getTasksByCategory(categoryId: String): Flow<List<TaskEntity>>
    
    /**
     * Get pending tasks by category.
     */
    @Query("SELECT * FROM tasks WHERE categoryId = :categoryId AND isCompleted = 0 ORDER BY dueDateTime ASC")
    fun getPendingTasksByCategory(categoryId: String): Flow<List<TaskEntity>>
    
    /**
     * Get a single task by ID.
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun getTaskById(taskId: String): Flow<TaskEntity?>
    
    /**
     * Get a single task by ID (suspend function for one-time read).
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskByIdOnce(taskId: String): TaskEntity?
    
    /**
     * Insert a new task.
     * Returns the row ID of the inserted task.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long
    
    /**
     * Update an existing task.
     */
    @Update
    suspend fun updateTask(task: TaskEntity)
    
    /**
     * Delete a task.
     */
    @Delete
    suspend fun deleteTask(task: TaskEntity)
    
    /**
     * Delete a task by ID.
     */
    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)
    
    /**
     * Delete all completed tasks.
     */
    @Query("DELETE FROM tasks WHERE isCompleted = 1")
    suspend fun deleteCompletedTasks()
    
    /**
     * Mark a task as completed or uncompleted.
     */
    @Query("UPDATE tasks SET isCompleted = :isCompleted, updatedAt = :updatedAt WHERE id = :taskId")
    suspend fun updateTaskCompletionStatus(taskId: String, isCompleted: Boolean, updatedAt: Long)
    
    /**
     * Get tasks for a specific date range (start of day to end of day).
     */
    @Query("SELECT * FROM tasks WHERE dueDateTime >= :startOfDay AND dueDateTime < :endOfDay ORDER BY dueDateTime ASC")
    fun getTasksByDateRange(startOfDay: Long, endOfDay: Long): Flow<List<TaskEntity>>
    
    /**
     * Get all pending tasks (one-time read for boot receiver).
     */
    @Query("SELECT * FROM tasks WHERE isCompleted = 0")
    suspend fun getPendingTasksOnce(): List<TaskEntity>
    
    /**
     * Update focus time for a task.
     */
    @Query("UPDATE tasks SET totalFocusTimeSeconds = totalFocusTimeSeconds + :additionalSeconds, updatedAt = :updatedAt WHERE id = :taskId")
    suspend fun addFocusTime(taskId: String, additionalSeconds: Long, updatedAt: Long)
    
    // Statistics queries
    
    /**
     * Get total completed tasks count.
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 1")
    fun getCompletedTasksCount(): Flow<Int>
    
    /**
     * Get total pending tasks count.
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 0")
    fun getPendingTasksCount(): Flow<Int>
    
    /**
     * Get completed tasks in a date range.
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 1 AND updatedAt >= :startTime AND updatedAt < :endTime")
    suspend fun getCompletedTasksInRange(startTime: Long, endTime: Long): Int
    
    /**
     * Get tasks due in the next N days.
     */
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND dueDateTime >= :now AND dueDateTime < :endTime ORDER BY dueDateTime ASC")
    fun getUpcomingTasks(now: Long, endTime: Long): Flow<List<TaskEntity>>
    
    /**
     * Update category for tasks when category is deleted.
     */
    @Query("UPDATE tasks SET categoryId = NULL WHERE categoryId = :categoryId")
    suspend fun clearCategoryFromTasks(categoryId: String)
}
