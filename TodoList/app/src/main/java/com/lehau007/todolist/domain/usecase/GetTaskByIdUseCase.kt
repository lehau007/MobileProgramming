package com.lehau007.todolist.domain.usecase

import com.lehau007.todolist.domain.model.Task
import com.lehau007.todolist.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving a single task by ID.
 */
class GetTaskByIdUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    operator fun invoke(taskId: String): Flow<Task?> {
        return repository.getTaskById(taskId)
    }
}
