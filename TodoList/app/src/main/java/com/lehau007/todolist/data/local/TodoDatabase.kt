package com.lehau007.todolist.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lehau007.todolist.data.local.dao.CategoryDao
import com.lehau007.todolist.data.local.dao.TaskDao
import com.lehau007.todolist.data.local.entity.CategoryEntity
import com.lehau007.todolist.data.local.entity.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Room Database configuration.
 * 
 * Database version 3 - added categories, custom priority icons, focus mode
 */
@Database(
    entities = [TaskEntity::class, CategoryEntity::class],
    version = 3,
    exportSchema = true
)
abstract class TodoDatabase : RoomDatabase() {
    
    abstract fun taskDao(): TaskDao
    abstract fun categoryDao(): CategoryDao
    
    companion object {
        const val DATABASE_NAME = "todo_database"
        
        @Volatile
        private var INSTANCE: TodoDatabase? = null
        
        /**
         * Migration from version 1 to version 2
         * Adds recurring task fields
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN isRecurring INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE tasks ADD COLUMN recurrencePeriod TEXT NOT NULL DEFAULT 'NONE'")
                db.execSQL("ALTER TABLE tasks ADD COLUMN recurrenceEndDate INTEGER")
            }
        }
        
        /**
         * Migration from version 2 to version 3
         * Adds categories table and new task fields
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create categories table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS categories (
                        id TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        color INTEGER NOT NULL,
                        iconName TEXT NOT NULL DEFAULT 'Category',
                        isDefault INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL DEFAULT 0
                    )
                """)
                
                // Add new columns to tasks table
                db.execSQL("ALTER TABLE tasks ADD COLUMN hasTime INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE tasks ADD COLUMN priorityIconData TEXT NOT NULL DEFAULT 'STANDARD:1'")
                db.execSQL("ALTER TABLE tasks ADD COLUMN categoryId TEXT")
                db.execSQL("ALTER TABLE tasks ADD COLUMN totalFocusTimeSeconds INTEGER NOT NULL DEFAULT 0")
                
                // Create index for categoryId
                db.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_categoryId ON tasks(categoryId)")
                
                // Insert default categories
                val currentTime = System.currentTimeMillis()
                db.execSQL("""
                    INSERT INTO categories (id, name, color, iconName, isDefault, createdAt) VALUES
                    ('work', 'Work', ${0xFF3B82F6}, 'Work', 1, $currentTime),
                    ('personal', 'Personal', ${0xFF22C55E}, 'Person', 1, $currentTime),
                    ('shopping', 'Shopping', ${0xFFF59E0B}, 'ShoppingCart', 1, $currentTime),
                    ('health', 'Health', ${0xFFEF4444}, 'FavoriteBorder', 1, $currentTime)
                """)
            }
        }
        
        fun getInstance(context: Context): TodoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TodoDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * Callback to populate default data on first creation
         */
        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Default categories are inserted in migration for existing users
                // For new users (fresh install), insert them here
                val currentTime = System.currentTimeMillis()
                db.execSQL("""
                    INSERT OR IGNORE INTO categories (id, name, color, iconName, isDefault, createdAt) VALUES
                    ('work', 'Work', ${0xFF3B82F6}, 'Work', 1, $currentTime),
                    ('personal', 'Personal', ${0xFF22C55E}, 'Person', 1, $currentTime),
                    ('shopping', 'Shopping', ${0xFFF59E0B}, 'ShoppingCart', 1, $currentTime),
                    ('health', 'Health', ${0xFFEF4444}, 'FavoriteBorder', 1, $currentTime)
                """)
            }
        }
    }
}
