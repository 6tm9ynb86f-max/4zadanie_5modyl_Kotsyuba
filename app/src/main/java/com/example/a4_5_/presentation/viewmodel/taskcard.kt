package com.example.a4_5_.presentation.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.a4_5_.domain.model.TodoSettings
import com.example.a4_5_.domain.model.TodoTask

@Composable
fun TaskCard(
    task: TodoTask,
    settings: TodoSettings,
    enabled: Boolean,
    onCompleted: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val highlighted = task.isCompleted && settings.highlightCompleted
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (highlighted) Color(settings.completedColor.toInt())
            else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (highlighted) Color.Black
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(task.isCompleted, onCompleted, enabled = enabled)
                Column(Modifier.weight(1f)) {
                    Text(
                        task.title,
                        style = MaterialTheme.typography.titleMedium,
                        textDecoration = if (task.isCompleted)
                            TextDecoration.LineThrough else TextDecoration.None
                    )
                    if (task.description.isNotBlank()) Text(task.description)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onEdit, enabled = enabled) { Text("Изменить") }
                TextButton(onClick = onDelete, enabled = enabled) { Text("Удалить") }
            }
        }
    }
}

@Composable
fun CompletedColorDialog(onSelect: (Long) -> Unit, onDismiss: () -> Unit) {
    val colors = listOf(
        "Зелёный" to 0xFFE2F0D9L,
        "Голубой" to 0xFFDCEEFBL,
        "Розовый" to 0xFFFCE4ECL,
        "Сиреневый" to 0xFFEADCF8L
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Цвет завершённых задач") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                colors.forEach { (name, color) ->
                    Button(
                        onClick = { onSelect(color) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(color.toInt()),
                            contentColor = Color.Black
                        )
                    ) { Text(name) }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть") }
        }
    )
}
