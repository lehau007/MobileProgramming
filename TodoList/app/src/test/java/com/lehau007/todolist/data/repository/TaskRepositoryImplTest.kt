package com.lehau007.todolist.data.repository

import com.lehau007.todolist.data.local.dao.TaskDao
import com.lehau007.todolist.data.local.entity.TaskEntity
import com.lehau007.todolist.domain.model.Task
import com.lehau007.todolist.domain.model.TaskFilter
import com.lehau007.todolist.domain.model.TaskSortOrder
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for TaskRepositoryImpl.
 * 
 * Tests the repository layer's data transformation and DAO interactions.
 */
class TaskRepositoryImplTest {
    
    private lateinit var taskDao: TaskDao
    private lateinit var repository: TaskRepositoryImpl
    
    private val testTaskEntity = TaskEntity(
        id = "1",
        title = "Test Task",
        description = "Test Description",
        dueDateTime = System.currentTimeMillis(),
        priority = 1,
        isCompleted = false,
        reminderMinutesBefore = 30,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
    
    @Before
    fun setup() {
        taskDao = mockk(relaxed = true)
        repository = TaskRepositoryImpl(taskDao)
    }
    
    @Test
    fun `getTasks returns all tasks when filter is ALL`() = runTest {
        // Given
        coEvery { taskDao.getTasksByPriority() } returns flowOf(listOf(testTaskEntity))
        
        // When
        val result = repository.getTasks(TaskSortOrder.BY_DATE, TaskFilter.ALL).first()
        
        // Then
        assertEquals(1, result.size)
        assertEquals("Test Task", result[0].title)
        coVerify { taskDao.getTasksByPriority() }
    }
    
    @Test
    fun `getTasks returns pending tasks when filter is PENDING`() = runTest {
        // Given
        coEvery { taskDao.getPendingTasks() } returns flowOf(listOf(testTaskEntity))
        
        // When
        val result = repository.getTasks(TaskSortOrder.BY_DATE, TaskFilter.PENDING).first()
        
        // Then
        assertEquals(1, result.size)
        assertEquals(false, result[0].isCompleted)
        coVerify { taskDao.getPendingTasks() }
    }
    
    @Test
    fun `upsertTask inserts task entity`() = runTest {
        // Given
        val task = Task(
            id = "1",
            title = "New Task",
            description = "Description",
            dueDateTime = System.currentTimeMillis(),
            priority = Task.Priority.HIGH,
            isCompleted = false,
            reminderMinutesBefore = 30,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        coEvery { taskDao.insertTask(any()) } returns 1L
        
        // When
        repository.upsertTask(task)
        
        // Then
        coVerify { taskDao.insertTask(any()) }
    }
    
    @Test
    fun `deleteTaskById deletes task`() = runTest {
        // Given
        val taskId = "1"
        
        // When
        repository.deleteTaskById(taskId)
        
        // Then
        coVerify { taskDao.deleteTaskById(taskId) }
    }
    
    @Test
    fun `toggleTaskCompletion updates completion status`() = runTest {
        // Given
        val taskId = "1"
        coEvery { taskDao.getTaskByIdOnce(taskId) } returns testTaskEntity
        
        // When
        repository.toggleTaskCompletion(taskId)
        
        // Then
        coVerify { 
            taskDao.updateTaskCompletionStatus(
                taskId = taskId,
                isCompleted = true, // Should toggle from false to true
                updatedAt = any()
            )
        }
    }
}
