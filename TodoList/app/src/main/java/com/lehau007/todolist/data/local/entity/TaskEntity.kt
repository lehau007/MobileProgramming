package com.lehau007.todolist.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room entity representing a task in the database.
 * 
 * This entity stores all task information including:
 * - Basic info (title, description)
 * - Scheduling (due date/time, reminder time)
 * - Priority level and custom priority icon
 * - Category assignment
 * - Status (completed or not)
 * - Timestamps (creation and last update)
 * - Recurrence (repeating tasks)
 * - Focus mode tracking
 */
@Entity(
    tableName = "tasks",
    indices = [Index("categoryId")]
)
data class TaskEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    val title: String,
    
    val description: String? = null,
    
    // Timestamp in milliseconds
    val dueDateTime: Long,
    
    // If false, only date matters - no time-based notification
    val hasTime: Boolean = true,
    
    // Priority: 0 = Low, 1 = Medium, 2 = High
    val priority: Int,
    
    // Custom priority icon stored as "TYPE:VALUE" string
    // e.g., "STANDARD:1", "FLAG:0xFFEF4444", "EMOJI:🔥", "PROGRESS:50"
    val priorityIconData: String = "STANDARD:1",
    
    val isCompleted: Boolean = false,
    
    // Category ID (nullable for uncategorized tasks)
    val categoryId: String? = null,
    
    // Reminder time in minutes before due time (e.g., 5, 30, 60)
    // Only used when hasTime = true
    val reminderMinutesBefore: Int = 30,
    
    val createdAt: Long = System.currentTimeMillis(),
    
    val updatedAt: Long = System.currentTimeMillis(),
    
    // Recurring task fields
    val isRecurring: Boolean = false,
    
    // Recurrence period: "NONE", "DAILY", "WEEKLY", "MONTHLY"
    val recurrencePeriod: String = "NONE",
    
    // End date for recurring tasks (null = no end)
    val recurrenceEndDate: Long? = null,
    
    // Total time spent in focus mode (in seconds)
    val totalFocusTimeSeconds: Long = 0
)
