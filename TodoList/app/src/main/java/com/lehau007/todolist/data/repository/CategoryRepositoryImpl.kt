package com.lehau007.todolist.data.repository

import com.lehau007.todolist.data.local.dao.CategoryDao
import com.lehau007.todolist.data.local.dao.TaskDao
import com.lehau007.todolist.data.mapper.toDomain
import com.lehau007.todolist.data.mapper.toEntity
import com.lehau007.todolist.domain.model.Category
import com.lehau007.todolist.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of CategoryRepository using Room database.
 */
@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    private val taskDao: TaskDao
) : CategoryRepository {
    
    override fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getCategoriesWithTaskCount().map { list ->
            list.map { it.toDomain() }
        }
    }
    
    override suspend fun getCategoryById(id: String): Category? {
        return categoryDao.getCategoryById(id)?.toDomain()
    }
    
    override fun getCategoryByIdFlow(id: String): Flow<Category?> {
        return categoryDao.getCategoryByIdFlow(id).map { it?.toDomain() }
    }
    
    override suspend fun upsertCategory(category: Category) {
        categoryDao.insertCategory(category.toEntity())
    }
    
    override suspend fun deleteCategory(id: String): Boolean {
        // Clear category from all tasks first
        taskDao.clearCategoryFromTasks(id)
        // Delete the category (only non-default)
        val deleted = categoryDao.deleteCategoryById(id)
        return deleted > 0
    }
    
    override suspend fun createCategory(name: String, color: Long, iconName: String): Category {
        val category = Category(
            id = UUID.randomUUID().toString(),
            name = name,
            color = color,
            iconName = iconName,
            isDefault = false,
            createdAt = System.currentTimeMillis()
        )
        categoryDao.insertCategory(category.toEntity())
        return category
    }
}
