package com.lehau007.todolist.data.mapper

import com.lehau007.todolist.data.local.dao.CategoryWithTaskCount
import com.lehau007.todolist.data.local.entity.CategoryEntity
import com.lehau007.todolist.data.local.entity.TaskEntity
import com.lehau007.todolist.domain.model.Category
import com.lehau007.todolist.domain.model.Task

/**
 * Mapper functions to convert between data layer entities and domain models.
 * 
 * This separation allows the domain layer to remain independent of database implementation.
 */

// Task Mappers

fun TaskEntity.toDomain(): Task {
    val priority = Task.Priority.fromValue(this.priority)
    return Task(
        id = id,
        title = title,
        description = description,
        dueDateTime = dueDateTime,
        hasTime = hasTime,
        priority = priority,
        priorityIcon = Task.PriorityIcon.fromStorageString(priorityIconData, priority),
        isCompleted = isCompleted,
        categoryId = categoryId,
        reminderMinutesBefore = reminderMinutesBefore,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isRecurring = isRecurring,
        recurrencePeriod = Task.RecurrencePeriod.fromString(recurrencePeriod),
        recurrenceEndDate = recurrenceEndDate,
        totalFocusTimeSeconds = totalFocusTimeSeconds
    )
}

fun Task.toEntity(): TaskEntity {
    return TaskEntity(
        id = id,
        title = title,
        description = description,
        dueDateTime = dueDateTime,
        hasTime = hasTime,
        priority = priority.value,
        priorityIconData = priorityIcon.toStorageString(),
        isCompleted = isCompleted,
        categoryId = categoryId,
        reminderMinutesBefore = reminderMinutesBefore,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isRecurring = isRecurring,
        recurrencePeriod = recurrencePeriod.name,
        recurrenceEndDate = recurrenceEndDate,
        totalFocusTimeSeconds = totalFocusTimeSeconds
    )
}

// Category Mappers

fun CategoryEntity.toDomain(taskCount: Int = 0): Category {
    return Category(
        id = id,
        name = name,
        color = color,
        iconName = iconName,
        isDefault = isDefault,
        taskCount = taskCount,
        createdAt = createdAt
    )
}

fun Category.toEntity(): CategoryEntity {
    return CategoryEntity(
        id = id,
        name = name,
        color = color,
        iconName = iconName,
        isDefault = isDefault,
        createdAt = createdAt
    )
}

fun CategoryWithTaskCount.toDomain(): Category {
    return Category(
        id = id,
        name = name,
        color = color,
        iconName = iconName,
        isDefault = isDefault,
        taskCount = taskCount,
        createdAt = createdAt
    )
}
