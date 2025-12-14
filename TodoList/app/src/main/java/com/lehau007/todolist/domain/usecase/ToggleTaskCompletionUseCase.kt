package com.lehau007.todolist.domain.usecase

import com.lehau007.todolist.domain.repository.TaskRepository
import javax.inject.Inject

/**
 * Use case for toggling task completion status.
 */
class ToggleTaskCompletionUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: String) {
        repository.toggleTaskCompletion(taskId)
    }
}
