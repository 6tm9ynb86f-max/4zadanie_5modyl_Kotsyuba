package com.example.a4_5_.data.repository

import com.example.a4_5_.data.local.TaskJsonImporter
import com.example.a4_5_.data.local.TodoDatabase
import com.example.a4_5_.data.model.TaskEntity
import com.example.a4_5_.domain.model.TodoTask
import com.example.a4_5_.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomTaskRepository(
    database: TodoDatabase,
    private val importer: TaskJsonImporter
) : TaskRepository {
    private val dao = database.taskDao()

    override fun observeTasks(): Flow<List<TodoTask>> {
        return dao.observeTasks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTaskById(id: Long): TodoTask? {
        return dao.getTaskById(id)?.toDomain()
    }

    override suspend fun saveTask(task: TodoTask): Long {
        val entity = task.toEntity()

        return if (task.id == 0L) {
            dao.insertTask(entity)
        } else {
            check(dao.updateTask(entity) == 1) {
                "Задача не найдена. Возможно, она уже удалена."
            }
            task.id
        }
    }

    override suspend fun deleteTask(id: Long) {
        check(dao.deleteTask(id) == 1) {
            "Задача не найдена. Возможно, она уже удалена."
        }
    }

    override suspend fun setTaskCompleted(id: Long, completed: Boolean) {
        check(dao.setTaskCompleted(id, completed) == 1) {
            "Задача не найдена. Возможно, она уже удалена."
        }
    }

    override suspend fun importInitialTasks(): Int {
        return importer.importOnce()
    }
}

private fun TaskEntity.toDomain(): TodoTask = TodoTask(
    id = id,
    title = title,
    description = description,
    isCompleted = isCompleted,
    createdAt = createdAt
)

private fun TodoTask.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    description = description,
    isCompleted = isCompleted,
    createdAt = createdAt
)