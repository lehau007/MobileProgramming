package com.lehau007.todolist.data.local.dao

import androidx.room.*
import com.lehau007.todolist.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Category operations.
 */
@Dao
interface CategoryDao {
    
    @Query("SELECT * FROM categories ORDER BY isDefault DESC, name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>
    
    @Query("SELECT * FROM categories ORDER BY isDefault DESC, name ASC")
    suspend fun getAllCategoriesOnce(): List<CategoryEntity>
    
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: String): CategoryEntity?
    
    @Query("SELECT * FROM categories WHERE id = :id")
    fun getCategoryByIdFlow(id: String): Flow<CategoryEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)
    
    @Update
    suspend fun updateCategory(category: CategoryEntity)
    
    @Delete
    suspend fun deleteCategory(category: CategoryEntity)
    
    @Query("DELETE FROM categories WHERE id = :id AND isDefault = 0")
    suspend fun deleteCategoryById(id: String): Int
    
    @Query("SELECT COUNT(*) FROM tasks WHERE categoryId = :categoryId")
    fun getTaskCountForCategory(categoryId: String): Flow<Int>
    
    @Query("""
        SELECT c.*, COUNT(t.id) as taskCount 
        FROM categories c 
        LEFT JOIN tasks t ON c.id = t.categoryId 
        GROUP BY c.id 
        ORDER BY c.isDefault DESC, c.name ASC
    """)
    fun getCategoriesWithTaskCount(): Flow<List<CategoryWithTaskCount>>
}

/**
 * Data class for category with task count query result.
 */
data class CategoryWithTaskCount(
    val id: String,
    val name: String,
    val color: Long,
    val iconName: String,
    val isDefault: Boolean,
    val createdAt: Long,
    val taskCount: Int
)
