package com.example.a4_5_.domain.repository

import com.example.a4_5_.domain.model.TodoSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<TodoSettings>

    suspend fun setHighlightCompleted(enabled: Boolean)

    suspend fun setCompletedColor(color: Long)
}