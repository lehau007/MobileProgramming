package com.lehau007.todolist.di

import android.content.Context
import androidx.room.Room
import com.lehau007.todolist.data.local.TodoDatabase
import com.lehau007.todolist.data.local.dao.CategoryDao
import com.lehau007.todolist.data.local.dao.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing database-related dependencies.
 * 
 * @InstallIn(SingletonComponent::class) makes these dependencies available
 * throughout the app's lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    /**
     * Provides the Room database instance.
     * Uses @Singleton to ensure only one instance exists.
     */
    @Provides
    @Singleton
    fun provideTodoDatabase(
        @ApplicationContext context: Context
    ): TodoDatabase {
        return Room.databaseBuilder(
            context,
            TodoDatabase::class.java,
            TodoDatabase.DATABASE_NAME
        )
            .addMigrations(TodoDatabase.MIGRATION_1_2, TodoDatabase.MIGRATION_2_3)
            .build()
    }
    
    /**
     * Provides the TaskDao from the database.
     */
    @Provides
    @Singleton
    fun provideTaskDao(database: TodoDatabase): TaskDao {
        return database.taskDao()
    }
    
    /**
     * Provides the CategoryDao from the database.
     */
    @Provides
    @Singleton
    fun provideCategoryDao(database: TodoDatabase): CategoryDao {
        return database.categoryDao()
    }
}
