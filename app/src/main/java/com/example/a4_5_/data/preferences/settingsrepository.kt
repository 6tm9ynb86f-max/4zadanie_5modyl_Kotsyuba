package com.example.a4_5_.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.a4_5_.domain.model.TodoSettings
import com.example.a4_5_.domain.repository.SettingsRepository
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.todoDataStore by preferencesDataStore(
    name = "todo_settings"
)

class DataStoreSettingsRepository(context: Context) : SettingsRepository {
    private val dataStore = context.applicationContext.todoDataStore

    override val settings: Flow<TodoSettings> = dataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences ->
            TodoSettings(
                highlightCompleted = preferences[HIGHLIGHT_COMPLETED] ?: true,
                completedColor = preferences[COMPLETED_COLOR]
                    ?: TodoSettings.DEFAULT_COMPLETED_COLOR
            )
        }

    override suspend fun setHighlightCompleted(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[HIGHLIGHT_COMPLETED] = enabled
        }
    }

    override suspend fun setCompletedColor(color: Long) {
        require(color in 0L..0xFFFFFFFFL) {
            "Некорректное значение цвета"
        }

        dataStore.edit { preferences ->
            preferences[COMPLETED_COLOR] = color
        }
    }

    private companion object {
        val HIGHLIGHT_COMPLETED = booleanPreferencesKey("highlight_completed")
        val COMPLETED_COLOR = longPreferencesKey("completed_color")
    }
}