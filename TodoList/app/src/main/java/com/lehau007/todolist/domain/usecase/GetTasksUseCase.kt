package com.lehau007.todolist.domain.usecase

import com.lehau007.todolist.domain.model.Task
import com.lehau007.todolist.domain.model.TaskFilter
import com.lehau007.todolist.domain.model.TaskSortOrder
import com.lehau007.todolist.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving tasks with sorting and filtering.
 * 
 * Encapsulates the business logic for getting tasks.
 */
class GetTasksUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    operator fun invoke(
        sortOrder: TaskSortOrder = TaskSortOrder.BY_DATE,
        filter: TaskFilter = TaskFilter.ALL
    ): Flow<List<Task>> {
        return repository.getTasks(sortOrder, filter)
    }
}
