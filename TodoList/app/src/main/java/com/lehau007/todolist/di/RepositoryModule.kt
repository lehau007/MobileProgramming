package com.lehau007.todolist.di

import com.lehau007.todolist.data.repository.CategoryRepositoryImpl
import com.lehau007.todolist.data.repository.TaskRepositoryImpl
import com.lehau007.todolist.domain.repository.CategoryRepository
import com.lehau007.todolist.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for binding repository implementations.
 * 
 * Uses @Binds instead of @Provides for interface binding,
 * which is more efficient.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    /**
     * Binds TaskRepositoryImpl as the implementation of TaskRepository.
     */
    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        taskRepositoryImpl: TaskRepositoryImpl
    ): TaskRepository
    
    /**
     * Binds CategoryRepositoryImpl as the implementation of CategoryRepository.
     */
    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        categoryRepositoryImpl: CategoryRepositoryImpl
    ): CategoryRepository
}
