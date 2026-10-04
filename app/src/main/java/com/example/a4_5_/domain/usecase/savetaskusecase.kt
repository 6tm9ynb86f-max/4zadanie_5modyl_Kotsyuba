package com.example.a4_5_.domain.usecase

import com.example.a4_5_.domain.model.TodoTask
import com.example.a4_5_.domain.repository.TaskRepository

class SaveTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(task: TodoTask): Long {
        val title = task.title.trim()
        require(title.isNotBlank()) { "Введите название задачи" }
        return repository.saveTask(
            task.copy(title = title, description = task.description.trim())
        )
    }
}
