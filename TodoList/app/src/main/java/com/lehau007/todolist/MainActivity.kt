package com.lehau007.todolist

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.lehau007.todolist.navigation.Screen
import com.lehau007.todolist.navigation.TodoNavGraph
import com.lehau007.todolist.notification.TaskReminderWorker
import com.lehau007.todolist.ui.theme.TodoListTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity for the To-Do List app.
 * 
 * Responsibilities:
 * - Setup Compose UI
 * - Handle navigation
 * - Request notification permissions (Android 13+)
 * - Handle deep links from notifications
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Handle permission result
        if (!isGranted) {
            // Could show a dialog explaining why the permission is needed
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request notification permission for Android 13+
        requestNotificationPermission()
        
        // Get task ID from notification intent if present
        val taskId = intent.getStringExtra(TaskReminderWorker.EXTRA_TASK_ID)
        
        enableEdgeToEdge()
        setContent {
            TodoListTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    // Navigate to task detail if opened from notification
                    taskId?.let {
                        navController.navigate(Screen.EditTask.createRoute(it))
                    }
                    
                    TodoNavGraph(navController = navController)
                }
            }
        }
    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Show explanation and request permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // Request permission directly
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}