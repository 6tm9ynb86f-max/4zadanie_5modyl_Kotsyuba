package com.example.a4_5_.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.a4_5_.domain.model.TodoSettings
import com.example.a4_5_.domain.model.TodoTask
import com.example.a4_5_.domain.repository.SettingsRepository
import com.example.a4_5_.domain.repository.TaskRepository
import com.example.a4_5_.domain.usecase.ObserveTasksUseCase
import com.example.a4_5_.domain.usecase.SaveTaskUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TodoUiState(
    val tasks: List<TodoTask> = emptyList(),
    val settings: TodoSettings = TodoSettings(),
    val isLoading: Boolean = true,
    val isBusy: Boolean = false,
    val error: String? = null
)

data class TaskEditorState(
    val task: TodoTask = TodoTask(title = ""),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

class TodoViewModel(
    private val taskRepository: TaskRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val observeTasks = ObserveTasksUseCase(taskRepository)
    private val saveTask = SaveTaskUseCase(taskRepository)

    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState = _uiState.asStateFlow()

    private val _editorState = MutableStateFlow(TaskEditorState())
    val editorState = _editorState.asStateFlow()

    private var loadingJob: Job? = null
    private var editorJob: Job? = null
    private var currentEditorId: Long? = null

    init {
        retryLoading()

        viewModelScope.launch {
            try {
                settingsRepository.settings.collect { settings ->
                    _uiState.update { it.copy(settings = settings) }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        error = error.message
                            ?: "Не удалось прочитать настройки"
                    )
                }
            }
        }
    }

    fun retryLoading() {
        loadingJob?.cancel()
        _uiState.update { it.copy(isLoading = true, error = null) }

        loadingJob = viewModelScope.launch {
            try {
                taskRepository.importInitialTasks()

                observeTasks().collect { tasks ->
                    _uiState.update {
                        it.copy(tasks = tasks, isLoading = false)
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.message
                            ?: "Не удалось загрузить задачи"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun setTaskCompleted(id: Long, completed: Boolean) = runAction {
        taskRepository.setTaskCompleted(id, completed)
    }

    fun deleteTask(id: Long) = runAction {
        taskRepository.deleteTask(id)
    }

    fun setHighlightCompleted(enabled: Boolean) = runAction {
        settingsRepository.setHighlightCompleted(enabled)
    }

    fun setCompletedColor(color: Long) = runAction {
        settingsRepository.setCompletedColor(color)
    }

    private fun runAction(action: suspend () -> Unit) {
        if (_uiState.value.isBusy || _uiState.value.isLoading) return

        _uiState.update { it.copy(isBusy = true, error = null) }

        viewModelScope.launch {
            try {
                action()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        error = error.message
                            ?: "Не удалось выполнить действие"
                    )
                }
            } finally {
                _uiState.update { it.copy(isBusy = false) }
            }
        }
    }

    fun openEditor(id: Long, force: Boolean = false) {
        if (!force && currentEditorId == id) return

        editorJob?.cancel()
        currentEditorId = id

        _editorState.value = TaskEditorState(
            task = TodoTask(id = id, title = ""),
            isLoading = id != 0L
        )

        if (id == 0L) return

        editorJob = viewModelScope.launch {
            try {
                val task = taskRepository.getTaskById(id)
                    ?: error("Задача не найдена")

                _editorState.value = TaskEditorState(task = task)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _editorState.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Ошибка загрузки"
                    )
                }
            }
        }
    }

    fun changeTitle(title: String) {
        _editorState.update {
            it.copy(
                task = it.task.copy(title = title),
                error = null
            )
        }
    }

    fun changeDescription(description: String) {
        _editorState.update {
            it.copy(
                task = it.task.copy(description = description),
                error = null
            )
        }
    }

    fun changeEditorCompleted(completed: Boolean) {
        _editorState.update {
            it.copy(task = it.task.copy(isCompleted = completed))
        }
    }

    fun saveEditor() {
        val state = _editorState.value
        if (state.isLoading || state.isSaving || state.isSaved) return

        _editorState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {
                val id = saveTask(state.task)

                _editorState.update {
                    it.copy(
                        task = state.task.copy(id = id),
                        isSaving = false,
                        isSaved = true
                    )
                }
            } catch (error: CancellationException) {
                _editorState.update { it.copy(isSaving = false) }
                throw error
            } catch (error: Exception) {
                _editorState.update {
                    it.copy(
                        isSaving = false,
                        error = error.message
                            ?: "Не удалось сохранить задачу"
                    )
                }
            }
        }
    }

    fun acknowledgeEditorSaved() {
        _editorState.update { it.copy(isSaved = false) }
    }

    companion object {
        fun factory(
            taskRepository: TaskRepository,
            settingsRepository: SettingsRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {

            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass.isAssignableFrom(TodoViewModel::class.java))

                return TodoViewModel(
                    taskRepository,
                    settingsRepository
                ) as T
            }
        }
    }
}