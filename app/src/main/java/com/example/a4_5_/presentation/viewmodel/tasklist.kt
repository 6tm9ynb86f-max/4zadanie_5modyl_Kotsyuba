package com.example.a4_5_.presentation.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.a4_5_.presentation.ui.component.CompletedColorDialog
import com.example.a4_5_.presentation.ui.component.TaskCard
import com.example.a4_5_.presentation.viewmodel.TodoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TodoViewModel,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val enabled = !state.isLoading && !state.isBusy
    var showColors by rememberSaveable { mutableStateOf(false) }
    var deleteId by rememberSaveable { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мои задачи") },
                actions = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Цвет завершенных", style = MaterialTheme.typography.labelSmall)
                        Switch(
                            checked = state.settings.highlightCompleted,
                            onCheckedChange = viewModel::setHighlightCompleted,
                            enabled = enabled
                        )
                    }
                    TextButton(
                        onClick = { showColors = true },
                        enabled = enabled && state.settings.highlightCompleted
                    ) { Text("Цвет") }
                }
            )
        },
        floatingActionButton = {
            Button(onClick = onAdd, enabled = enabled) { Text("+ Задача") }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (state.isLoading || state.isBusy) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }
            state.error?.let { message ->
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(message, color = MaterialTheme.colorScheme.error)
                    Row {
                        TextButton(onClick = viewModel::retryLoading, enabled = enabled) {
                            Text("Повторить")
                        }
                        TextButton(onClick = viewModel::clearError) { Text("Закрыть") }
                    }
                }
            }
            if (state.tasks.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        when {
                            state.isLoading -> "Загрузка задач…"
                            state.error != null -> "Не удалось загрузить список"
                            else -> "Задач пока нет. Нажми «+ Задача»."
                        }
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.tasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            settings = state.settings,
                            enabled = enabled,
                            onCompleted = { viewModel.setTaskCompleted(task.id, it) },
                            onEdit = { onEdit(task.id) },
                            onDelete = { deleteId = task.id }
                        )
                    }
                }
            }
        }
    }

    if (showColors) {
        CompletedColorDialog(
            onSelect = {
                viewModel.setCompletedColor(it)
                showColors = false
            },
            onDismiss = { showColors = false }
        )
    }
    val taskToDelete = state.tasks.firstOrNull { it.id == deleteId }
    if (taskToDelete != null) {
        AlertDialog(
            onDismissRequest = { deleteId = null },
            title = { Text("Удалить задачу?") },
            text = { Text(taskToDelete.title) },
            confirmButton = {
                TextButton(
                    enabled = enabled,
                    onClick = {
                        viewModel.deleteTask(taskToDelete.id)
                        deleteId = null
                    }
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { deleteId = null }) { Text("Отмена") }
            }
        )
    }
}