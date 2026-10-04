package com.example.a4_5_

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.a4_5_.data.local.TaskJsonImporter
import com.example.a4_5_.data.local.TodoDatabase
import com.example.a4_5_.data.preferences.DataStoreSettingsRepository
import com.example.a4_5_.data.repository.RoomTaskRepository
import com.example.a4_5_.navigation.TodoNavigation
import com.example.a4_5_.presentation.viewmodel.TodoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = TodoDatabase.getInstance(applicationContext)

        val taskRepository = RoomTaskRepository(
            database = database,
            importer = TaskJsonImporter(
                applicationContext,
                database
            )
        )

        val settingsRepository = DataStoreSettingsRepository(
            applicationContext
        )

        setContent {
            val factory = remember {
                TodoViewModel.factory(
                    taskRepository,
                    settingsRepository
                )
            }

            val todoViewModel: TodoViewModel = viewModel(
                factory = factory
            )

            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF386641),
                    secondary = Color(0xFF6A994E)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    TodoNavigation(
                        viewModel = todoViewModel
                    )
                }
            }
        }
    }
}