package com.lehau007.todolist.domain.model

/**
 * Sort options for task list.
 */
enum class TaskSortOrder {
    BY_DATE,
    BY_PRIORITY
}

/**
 * Filter options for task list.
 */
enum class TaskFilter {
    ALL,
    PENDING,
    COMPLETED
}
