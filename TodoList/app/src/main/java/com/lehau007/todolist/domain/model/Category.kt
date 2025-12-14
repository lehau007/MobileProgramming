package com.lehau007.todolist.domain.model

/**
 * Domain model representing a task category.
 * 
 * Categories help organize tasks into groups like Work, Personal, etc.
 * Users can create custom categories with their own colors and icons.
 */
data class Category(
    val id: String,
    val name: String,
    val color: Long, // Color as ARGB long value
    val iconName: String, // Material icon name
    val isDefault: Boolean = false, // Default categories can't be deleted
    val taskCount: Int = 0, // Number of tasks in this category (computed)
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        // Default category IDs
        const val UNCATEGORIZED_ID = "uncategorized"
        const val WORK_ID = "work"
        const val PERSONAL_ID = "personal"
        const val SHOPPING_ID = "shopping"
        const val HEALTH_ID = "health"
    }
}
