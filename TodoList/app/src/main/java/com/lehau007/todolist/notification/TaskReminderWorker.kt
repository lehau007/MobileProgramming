package com.lehau007.todolist.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lehau007.todolist.MainActivity
import com.lehau007.todolist.R
import com.lehau007.todolist.data.local.dao.TaskDao
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.text.SimpleDateFormat
import java.util.*

/**
 * Worker for displaying task reminder notifications.
 * 
 * Enhanced to work reliably when app is closed:
 * - Uses high priority notification channel
 * - Adds sound and vibration
 * - Uses heads-up notification display
 */
class TaskReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface TaskReminderWorkerEntryPoint {
        fun taskDao(): TaskDao
    }
    
    override suspend fun doWork(): Result {
        val taskId = inputData.getString(KEY_TASK_ID) ?: return Result.failure()
        
        // Get TaskDao using Hilt EntryPoint
        val entryPoint = EntryPointAccessors.fromApplication(
            context,
            TaskReminderWorkerEntryPoint::class.java
        )
        val taskDao = entryPoint.taskDao()
        
        // Get task from database
        val task = taskDao.getTaskByIdOnce(taskId) ?: return Result.failure()
        
        // Don't show notification if task is completed
        if (task.isCompleted) {
            return Result.success()
        }
        
        // Create notification channel (required for Android 8.0+)
        createNotificationChannel()
        
        // Get default notification sound
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        // Build notification with enhanced visibility
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(task.title)
            .setContentText(
                context.getString(R.string.notification_task_due) + ": " +
                        formatDateTime(task.dueDateTime)
            )
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(buildNotificationText(task.title, task.description, task.dueDateTime)))
            .setPriority(NotificationCompat.PRIORITY_MAX) // Maximum priority for heads-up
            .setCategory(NotificationCompat.CATEGORY_REMINDER) // Categorize as reminder
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Show on lock screen
            .setAutoCancel(true)
            .setSound(defaultSoundUri) // Add sound
            .setVibrate(longArrayOf(0, 500, 200, 500)) // Vibration pattern
            .setDefaults(NotificationCompat.DEFAULT_LIGHTS) // LED lights
            .setContentIntent(createContentIntent(taskId))
            .setFullScreenIntent(createContentIntent(taskId), true) // High priority intent
            .addAction(
                R.drawable.ic_launcher_foreground,
                context.getString(R.string.notification_action_open),
                createContentIntent(taskId)
            )
            .build()
        
        // Show notification (check permission for Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(context)
                    .notify(taskId.hashCode(), notification)
            }
        } else {
            NotificationManagerCompat.from(context)
                .notify(taskId.hashCode(), notification)
        }
        
        return Result.success()
    }
    
    private fun buildNotificationText(title: String, description: String?, dueDateTime: Long): String {
        val dueText = context.getString(R.string.notification_task_due) + ": " + formatDateTime(dueDateTime)
        return if (!description.isNullOrBlank()) {
            "$dueText\n$description"
        } else {
            dueText
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH // High importance for heads-up
            ).apply {
                description = context.getString(R.string.notification_channel_description)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                enableLights(true)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setSound(
                    defaultSoundUri,
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
                    as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createContentIntent(taskId: String): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_TASK_ID, taskId)
        }
        
        return PendingIntent.getActivity(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    
    companion object {
        const val CHANNEL_ID = "task_reminders"
        const val KEY_TASK_ID = "task_id"
        const val EXTRA_TASK_ID = "extra_task_id"
    }
}
