package com.lehau007.todolist.domain.usecase

import com.lehau007.todolist.domain.repository.TaskRepository
import com.lehau007.todolist.notification.TaskNotificationManager
import javax.inject.Inject

/**
 * Use case for deleting a task.
 * Also cancels scheduled notifications.
 */
class DeleteTaskUseCase @Inject constructor(
    private val repository: TaskRepository,
    private val notificationManager: TaskNotificationManager
) {
    suspend operator fun invoke(taskId: String) {
        repository.deleteTaskById(taskId)
        notificationManager.cancelNotification(taskId)
    }
}
