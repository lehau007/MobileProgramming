package com.lehau007.todolist.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Domain model representing a task.
 * 
 * This is the business logic representation, independent of database implementation.
 * Includes priority icons, categories, and focus mode tracking.
 */
data class Task(
    val id: String,
    val title: String,
    val description: String?,
    val dueDateTime: Long,
    val hasTime: Boolean = true, // If false, only date matters (no notification)
    val priority: Priority,
    val priorityIcon: PriorityIcon = PriorityIcon.Standard(priority),
    val isCompleted: Boolean,
    val categoryId: String? = null,
    val reminderMinutesBefore: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val isRecurring: Boolean = false,
    val recurrencePeriod: RecurrencePeriod = RecurrencePeriod.NONE,
    val recurrenceEndDate: Long? = null,
    val totalFocusTimeSeconds: Long = 0 // Accumulated focus time
) {
    /**
     * Task priority levels.
     */
    enum class Priority(val value: Int, val displayName: String) {
        LOW(0, "Low"),
        MEDIUM(1, "Medium"),
        HIGH(2, "High");
        
        companion object {
            fun fromValue(value: Int): Priority {
                return entries.find { it.value == value } ?: MEDIUM
            }
        }
    }
    
    /**
     * Priority icon types for visual customization.
     */
    sealed class PriorityIcon {
        // Standard text-based priority
        data class Standard(val priority: Priority) : PriorityIcon()
        
        // Colored flag icon
        data class Flag(val color: Long) : PriorityIcon() {
            companion object {
                val RED = Flag(0xFFEF4444)
                val ORANGE = Flag(0xFFF97316)
                val YELLOW = Flag(0xFFEAB308)
                val GREEN = Flag(0xFF22C55E)
                val BLUE = Flag(0xFF3B82F6)
                val PURPLE = Flag(0xFF8B5CF6)
            }
        }
        
        // Emoji icon
        data class Emoji(val emoji: String) : PriorityIcon() {
            companion object {
                val FIRE = Emoji("🔥")
                val STAR = Emoji("⭐")
                val HEART = Emoji("❤️")
                val ROCKET = Emoji("🚀")
                val WARNING = Emoji("⚠️")
                val CLOCK = Emoji("⏰")
                val CHECK = Emoji("✅")
                val SMILE = Emoji("😊")
                val CRY = Emoji("😢")
                val THINKING = Emoji("🤔")
            }
        }
        
        // Progress circle (0-100%)
        data class Progress(val percentage: Int) : PriorityIcon() {
            init {
                require(percentage in 0..100) { "Percentage must be between 0 and 100" }
            }
        }
        
        fun toStorageString(): String {
            return when (this) {
                is Standard -> "STANDARD:${priority.value}"
                is Flag -> "FLAG:$color"
                is Emoji -> "EMOJI:$emoji"
                is Progress -> "PROGRESS:$percentage"
            }
        }
        
        companion object {
            fun fromStorageString(value: String, defaultPriority: Priority = Priority.MEDIUM): PriorityIcon {
                val parts = value.split(":", limit = 2)
                if (parts.size != 2) return Standard(defaultPriority)
                
                return when (parts[0]) {
                    "STANDARD" -> Standard(Priority.fromValue(parts[1].toIntOrNull() ?: 1))
                    "FLAG" -> Flag(parts[1].toLongOrNull() ?: 0xFFEF4444)
                    "EMOJI" -> Emoji(parts[1])
                    "PROGRESS" -> Progress(parts[1].toIntOrNull()?.coerceIn(0, 100) ?: 0)
                    else -> Standard(defaultPriority)
                }
            }
        }
    }
    
    /**
     * Recurrence period for repeating tasks.
     */
    enum class RecurrencePeriod(val displayName: String) {
        NONE("Does not repeat"),
        DAILY("Daily"),
        WEEKLY("Weekly"),
        MONTHLY("Monthly");
        
        companion object {
            fun fromString(value: String): RecurrencePeriod {
                return entries.find { it.name == value } ?: NONE
            }
        }
    }
}
