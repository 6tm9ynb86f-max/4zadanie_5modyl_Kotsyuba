package com.example.a4_5_.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "import_state")
data class ImportStateEntity(
    @PrimaryKey
    val source: String,
    val completedAt: Long = System.currentTimeMillis()
)