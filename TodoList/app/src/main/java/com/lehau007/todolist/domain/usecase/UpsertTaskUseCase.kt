package com.lehau007.todolist.domain.usecase

import com.lehau007.todolist.domain.model.Task
import com.lehau007.todolist.domain.repository.TaskRepository
import com.lehau007.todolist.notification.TaskNotificationManager
import javax.inject.Inject

/**
 * Use case for creating or updating a task.
 * 
 * Includes validation logic to ensure task data is valid.
 * Also schedules notifications for the task.
 */
class UpsertTaskUseCase @Inject constructor(
    private val repository: TaskRepository,
    private val notificationManager: TaskNotificationManager
) {
    suspend operator fun invoke(task: Task): Result<Unit> {
        // Validate task
        if (task.title.isBlank()) {
            return Result.failure(Exception("Task title cannot be empty"))
        }
        
        if (task.dueDateTime < System.currentTimeMillis()) {
            // Allow past dates but could add warning
        }
        
        return try {
            repository.upsertTask(task)
            
            // Schedule notification for the task
            if (!task.isCompleted) {
                notificationManager.scheduleNotification(task)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
