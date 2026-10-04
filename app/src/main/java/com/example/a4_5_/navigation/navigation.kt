package com.example.a4_5_.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.a4_5_.presentation.ui.screen.TaskEditorScreen
import com.example.a4_5_.presentation.ui.screen.TaskListScreen
import com.example.a4_5_.presentation.viewmodel.TodoViewModel

@Composable
fun TodoNavigation(viewModel: TodoViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "tasks"
    ) {
        composable("tasks") {
            TaskListScreen(
                viewModel = viewModel,
                onAdd = {
                    viewModel.openEditor(0L, force = true)
                    navController.navigate("editor/0") {
                        launchSingleTop = true
                    }
                },
                onEdit = { id ->
                    viewModel.openEditor(id, force = true)
                    navController.navigate("editor/$id") {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = "editor/{id}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.LongType
                }
            )
        ) { entry ->
            val id = entry.arguments?.getLong("id") ?: 0L

            TaskEditorScreen(
                id = id,
                viewModel = viewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}