package com.example.a4_5_.presentation.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.a4_5_.presentation.viewmodel.TodoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditorScreen(id: Long, viewModel: TodoViewModel, onBack: () -> Unit) {
    val state by viewModel.editorState.collectAsStateWithLifecycle()
    val enabled = !state.isLoading && !state.isSaving && !state.isSaved

    LaunchedEffect(id) { viewModel.openEditor(id) }
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            viewModel.acknowledgeEditorSaved()
            onBack()
        }
    }
    BackHandler(enabled = state.isSaving) { }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (id == 0L) "Новая задача" else "Изменить задачу") },
                navigationIcon = {
                    TextButton(onClick = onBack, enabled = !state.isSaving) { Text("Назад") }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).imePadding()
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.isLoading || state.isSaving) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }
            val message = state.error
            if (!state.isLoading) {
                OutlinedTextField(
                    value = state.task.title,
                    onValueChange = viewModel::changeTitle,
                    label = { Text("Название задачи") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = enabled
                )
                OutlinedTextField(
                    value = state.task.description,
                    onValueChange = viewModel::changeDescription,
                    label = { Text("Описание") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    enabled = enabled
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = state.task.isCompleted,
                        onCheckedChange = viewModel::changeEditorCompleted,
                        enabled = enabled
                    )
                    Text("Задача выполнена")
                }
                if (message != null) Text(message, color = MaterialTheme.colorScheme.error)
                if (id != 0L && message != null && state.task.title.isEmpty()) {
                    TextButton(onClick = { viewModel.openEditor(id, force = true) }) {
                        Text("Повторить загрузку")
                    }
                }
                Button(
                    onClick = viewModel::saveEditor,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled
                ) { Text(if (state.isSaving) "Сохранение…" else "Сохранить") }
            }
        }
    }
}
