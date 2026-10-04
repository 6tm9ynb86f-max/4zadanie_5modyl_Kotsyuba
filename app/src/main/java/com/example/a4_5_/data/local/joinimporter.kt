package com.example.a4_5_.data.local

import android.content.Context
import androidx.room.withTransaction
import com.example.a4_5_.data.model.ImportStateEntity
import com.example.a4_5_.data.model.TaskEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

class TaskJsonImporter(
    context: Context,
    private val database: TodoDatabase
) {
    private val appContext = context.applicationContext

    suspend fun importOnce(): Int = withContext(Dispatchers.IO) {
        val dao = database.taskDao()

        if (dao.isImported(SOURCE)) {
            return@withContext 0
        }

        val jsonText = appContext.assets.open(SOURCE)
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }

        val array = JSONArray(jsonText)
        val now = System.currentTimeMillis()

        val tasks = List(array.length()) { index ->
            val item = array.getJSONObject(index)
            val title = item.getString("title").trim()

            require(title.isNotBlank()) {
                "В JSON найдена задача без названия: ${index + 1}"
            }

            TaskEntity(
                title = title,
                description = item.optString("description", "").trim(),
                isCompleted = if (item.has("isCompleted")) {
                    item.getBoolean("isCompleted")
                } else {
                    false
                },
                createdAt = item.optLong("createdAt", now - index)
            )
        }

        database.withTransaction {
            if (dao.isImported(SOURCE)) {
                return@withTransaction 0
            }

            dao.insertTasks(tasks)
            dao.markImported(ImportStateEntity(source = SOURCE))
            tasks.size
        }
    }

    companion object {
        private const val SOURCE = "tasks.json"
    }
}