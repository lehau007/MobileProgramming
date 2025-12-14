package com.lehau007.todolist.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.lehau007.todolist.ui.screens.AddEditTaskScreen
import com.lehau007.todolist.ui.screens.CategoryManagementScreen
import com.lehau007.todolist.ui.screens.FocusModeScreen
import com.lehau007.todolist.ui.screens.MainScreen

/**
 * Navigation routes for the app.
 */
sealed class Screen(val route: String) {
    data object Main : Screen("main")
    data object AddTask : Screen("add_task")
    data object EditTask : Screen("edit_task/{taskId}") {
        fun createRoute(taskId: String) = "edit_task/$taskId"
    }
    data object FocusMode : Screen("focus_mode/{taskId}") {
        fun createRoute(taskId: String) = "focus_mode/$taskId"
    }
    data object CategoryManagement : Screen("category_management")
}

/**
 * Main navigation graph for the app.
 * 
 * Defines all navigation routes and their destinations.
 */
@Composable
fun TodoNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Main.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Main Screen with bottom navigation
        composable(Screen.Main.route) {
            MainScreen(navController = navController)
        }
        
        // Add Task Screen
        composable(Screen.AddTask.route) {
            AddEditTaskScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToFocusMode = { taskId ->
                    navController.navigate(Screen.FocusMode.createRoute(taskId))
                }
            )
        }
        
        // Edit Task Screen
        composable(
            route = Screen.EditTask.route,
            arguments = listOf(
                navArgument("taskId") {
                    type = NavType.StringType
                }
            )
        ) {
            AddEditTaskScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToFocusMode = { taskId ->
                    navController.navigate(Screen.FocusMode.createRoute(taskId))
                }
            )
        }
        
        // Focus Mode Screen
        composable(
            route = Screen.FocusMode.route,
            arguments = listOf(
                navArgument("taskId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            FocusModeScreen(
                taskId = taskId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Category Management Screen
        composable(Screen.CategoryManagement.route) {
            CategoryManagementScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
