package com.lehau007.todolist.domain.repository

import com.lehau007.todolist.domain.model.Category
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for category operations.
 */
interface CategoryRepository {
    
    /**
     * Get all categories with task counts.
     */
    fun getAllCategories(): Flow<List<Category>>
    
    /**
     * Get a category by ID.
     */
    suspend fun getCategoryById(id: String): Category?
    
    /**
     * Get a category by ID as Flow.
     */
    fun getCategoryByIdFlow(id: String): Flow<Category?>
    
    /**
     * Insert or update a category.
     */
    suspend fun upsertCategory(category: Category)
    
    /**
     * Delete a category by ID.
     * Returns true if deleted, false if it's a default category.
     */
    suspend fun deleteCategory(id: String): Boolean
    
    /**
     * Create a new category with generated ID.
     */
    suspend fun createCategory(name: String, color: Long, iconName: String): Category
}
