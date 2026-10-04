package com.example.a4_5_.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.a4_5_.data.model.ImportStateEntity
import com.example.a4_5_.data.model.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query(
        "SELECT * FROM tasks " +
                "ORDER BY isCompleted ASC, createdAt DESC, id DESC"
    )
    fun observeTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Insert
    suspend fun insertTask(task: TaskEntity): Long

    @Insert
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity): Int

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: Long): Int

    @Query("UPDATE tasks SET isCompleted = :completed WHERE id = :id")
    suspend fun setTaskCompleted(id: Long, completed: Boolean): Int

    @Query(
        "SELECT EXISTS(" +
                "SELECT 1 FROM import_state WHERE source = :source)"
    )
    suspend fun isImported(source: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun markImported(state: ImportStateEntity)
}