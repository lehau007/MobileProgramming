package com.lehau007.todolist.notification

import android.content.Context
import android.os.Build
import androidx.work.*
import com.lehau007.todolist.domain.model.Task
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for scheduling task reminder notifications.
 * 
 * Uses WorkManager with enhanced reliability:
 * - Expedited work for notifications due soon
 * - High priority constraints
 * - Handles device reboots
 * - Cancel notifications when tasks are completed/deleted
 */
@Singleton
class TaskNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)
    
    /**
     * Schedule a notification for a task.
     * 
     * @param task The task to schedule notification for
     */
    fun scheduleNotification(task: Task) {
        // Calculate reminder time
        val reminderTime = task.dueDateTime - (task.reminderMinutesBefore * 60 * 1000)
        val currentTime = System.currentTimeMillis()
        
        // Don't schedule if reminder time is in the past
        if (reminderTime <= currentTime) {
            return
        }
        
        val delay = reminderTime - currentTime
        
        // Use expedited work for notifications due within 15 minutes
        val isUrgent = delay < TimeUnit.MINUTES.toMillis(15)
        
        // Create work request with high priority
        val notificationWorkBuilder = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    TaskReminderWorker.KEY_TASK_ID to task.id
                )
            )
            .addTag(NOTIFICATION_TAG)
            .addTag(getNotificationTag(task.id))
        
        // For Android 12+, try to use expedited work for urgent notifications
        if (isUrgent && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            notificationWorkBuilder.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        }
        
        val notificationWork = notificationWorkBuilder.build()
        
        // Schedule work, replacing any existing work for this task
        workManager.enqueueUniqueWork(
            getNotificationWorkName(task.id),
            ExistingWorkPolicy.REPLACE,
            notificationWork
        )
    }
    
    /**
     * Cancel notification for a task.
     * 
     * @param taskId The ID of the task
     */
    fun cancelNotification(taskId: String) {
        workManager.cancelUniqueWork(getNotificationWorkName(taskId))
    }
    
    /**
     * Cancel all scheduled notifications.
     */
    fun cancelAllNotifications() {
        workManager.cancelAllWorkByTag(NOTIFICATION_TAG)
    }
    
    /**
     * Reschedule all pending notifications.
     * Call this after boot or when app settings change.
     */
    suspend fun rescheduleAllNotifications(tasks: List<Task>) {
        // Cancel all existing and reschedule
        tasks.forEach { task ->
            if (!task.isCompleted) {
                scheduleNotification(task)
            }
        }
    }
    
    private fun getNotificationWorkName(taskId: String): String {
        return "task_reminder_$taskId"
    }
    
    private fun getNotificationTag(taskId: String): String {
        return "task_reminder_tag_$taskId"
    }
    
    companion object {
        private const val NOTIFICATION_TAG = "task_notifications"
    }
}
