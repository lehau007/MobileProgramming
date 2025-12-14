package com.lehau007.todolist

import android.app.Application
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class with Hilt setup.
 * 
 * @HiltAndroidApp triggers Hilt's code generation and sets up
 * the application-level dependency container.
 */
@HiltAndroidApp
class TodoApplication : Application()
