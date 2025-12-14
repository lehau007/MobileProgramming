package com.lehau007.todolist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room entity representing a category in the database.
 * 
 * Categories are used to organize tasks into groups.
 * Default categories are pre-created and cannot be deleted.
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    val name: String,
    
    // Color stored as ARGB long value
    val color: Long,
    
    // Material icon name (e.g., "Work", "Person", "ShoppingCart")
    val iconName: String = "Category",
    
    // Default categories cannot be deleted
    val isDefault: Boolean = false,
    
    val createdAt: Long = System.currentTimeMillis()
)
