package com.lehau007.todolist.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.lehau007.todolist.domain.model.Task
import com.lehau007.todolist.domain.model.TaskFilter
import com.lehau007.todolist.domain.model.TaskSortOrder
import com.lehau007.todolist.domain.usecase.DeleteTaskUseCase
import com.lehau007.todolist.domain.usecase.GetTasksUseCase
import com.lehau007.todolist.domain.usecase.ToggleTaskCompletionUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for TaskListViewModel.
 * 
 * Tests ViewModel logic including:
 * - Task list management
 * - Sorting and filtering
 * - User interactions
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TaskListViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var getTasksUseCase: GetTasksUseCase
    private lateinit var toggleTaskCompletionUseCase: ToggleTaskCompletionUseCase
    private lateinit var deleteTaskUseCase: DeleteTaskUseCase
    private lateinit var viewModel: TaskListViewModel
    
    private val testTask = Task(
        id = "1",
        title = "Test Task",
        description = "Test Description",
        dueDateTime = System.currentTimeMillis(),
        priority = Task.Priority.MEDIUM,
        isCompleted = false,
        reminderMinutesBefore = 30,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        getTasksUseCase = mockk()
        toggleTaskCompletionUseCase = mockk(relaxed = true)
        deleteTaskUseCase = mockk(relaxed = true)
        
        coEvery { 
            getTasksUseCase(any(), any())
        } returns flowOf(listOf(testTask))
        
        viewModel = TaskListViewModel(
            getTasksUseCase,
            toggleTaskCompletionUseCase,
            deleteTaskUseCase
        )
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    

    @Test
    fun `setFilter updates filter`() {
        // When
        viewModel.setFilter(TaskFilter.PENDING)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertEquals(TaskFilter.PENDING, viewModel.filter.value)
    }
    
    @Test
    fun `toggleTaskCompletion calls use case`() = runTest {
        // When
        viewModel.toggleTaskCompletion("1")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        coVerify { toggleTaskCompletionUseCase("1") }
    }
    
    @Test
    fun `deleteTask calls use case`() = runTest {
        // When
        viewModel.deleteTask("1")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        coVerify { deleteTaskUseCase("1") }
    }
    
    @Test
    fun `initial ui state is Success`() {
        assertEquals(TaskListUiState.Success, viewModel.uiState.value)
    }
}
