package com.example.a4_5_.domain.usecase

import com.example.a4_5_.domain.model.TodoTask
import com.example.a4_5_.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow

class ObserveTasksUseCase(private val repository: TaskRepository) {
    operator fun invoke(): Flow<List<TodoTask>> = repository.observeTasks()
}