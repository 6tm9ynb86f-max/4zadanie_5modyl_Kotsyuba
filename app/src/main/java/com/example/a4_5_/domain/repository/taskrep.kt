package com.example.a4_5_.domain.repository

import com.example.a4_5_.domain.model.TodoTask
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun observeTasks(): Flow<List<TodoTask>>

    suspend fun getTaskById(id: Long): TodoTask?

    suspend fun saveTask(task: TodoTask): Long

    suspend fun deleteTask(id: Long)

    suspend fun setTaskCompleted(id: Long, completed: Boolean)

    suspend fun importInitialTasks(): Int
}