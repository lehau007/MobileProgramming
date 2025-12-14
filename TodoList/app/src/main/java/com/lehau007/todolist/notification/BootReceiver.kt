package com.lehau007.todolist.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lehau007.todolist.data.local.TodoDatabase
import com.lehau007.todolist.data.mapper.toDomain
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Broadcast receiver to reschedule all pending task notifications
 * after device reboot.
 * 
 * This ensures that notifications survive device reboots and continue
 * to work even when the app is closed.
 * 
 * Note: This receiver is registered in the manifest and cannot use @AndroidEntryPoint.
 * Instead, it uses Hilt's EntryPoint to manually access dependencies.
 */
class BootReceiver : BroadcastReceiver() {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Get dependencies manually since @AndroidEntryPoint can't be used with manifest receivers
            val database = TodoDatabase.getInstance(context)
            val taskDao = database.taskDao()
            val notificationManager = TaskNotificationManager(context)
            
            // Reschedule all pending task notifications
            scope.launch {
                val tasks = taskDao.getPendingTasksOnce()
                tasks.forEach { taskEntity ->
                    // Convert to domain model and reschedule
                    val task = taskEntity.toDomain()
                    notificationManager.scheduleNotification(task)
                }
            }
        }
    }
}
