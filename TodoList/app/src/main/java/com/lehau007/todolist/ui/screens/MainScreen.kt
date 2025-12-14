package com.lehau007.todolist.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.lehau007.todolist.domain.model.AppSettings
import com.lehau007.todolist.domain.model.Category
import com.lehau007.todolist.ui.components.BottomNavBar
import com.lehau007.todolist.ui.components.BottomNavDestination
import com.lehau007.todolist.ui.components.DrawerMenu
import com.lehau007.todolist.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

/**
 * Main screen that wraps content with bottom navigation and drawer.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    viewModel: MainViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    var currentDestination by remember { mutableStateOf(BottomNavDestination.TASKS) }
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerMenu(
                    categories = categories,
                    currentLanguage = currentLanguage,
                    onCategoryClick = { category ->
                        scope.launch { drawerState.close() }
                        viewModel.selectCategory(category)
                        currentDestination = BottomNavDestination.TASKS
                    },
                    onCreateCategoryClick = {
                        scope.launch { drawerState.close() }
                        // Navigate to create category
                    },
                    onLanguageChange = { language ->
                        viewModel.setLanguage(language)
                    }
                )
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                BottomNavBar(
                    currentDestination = currentDestination,
                    onDestinationSelected = { destination ->
                        if (destination == BottomNavDestination.MENU) {
                            scope.launch { drawerState.open() }
                        } else {
                            currentDestination = destination
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentDestination) {
                    BottomNavDestination.MENU -> {
                        // Menu is handled by drawer
                    }
                    BottomNavDestination.TASKS -> {
                        TaskListScreen(
                            selectedCategory = viewModel.selectedCategory.collectAsStateWithLifecycle().value,
                            onNavigateToAddTask = {
                                navController.navigate("add_task")
                            },
                            onNavigateToEditTask = { taskId ->
                                navController.navigate("edit_task/$taskId")
                            },
                            onNavigateToFocusMode = { taskId ->
                                navController.navigate("focus_mode/$taskId")
                            }
                        )
                    }
                    BottomNavDestination.CALENDAR -> {
                        CalendarScreen(
                            onNavigateToEditTask = { taskId ->
                                navController.navigate("edit_task/$taskId")
                            },
                            onNavigateToAddTask = {
                                navController.navigate("add_task")
                            }
                        )
                    }
                    BottomNavDestination.PROFILE -> {
                        ProfileScreen()
                    }
                }
            }
        }
    }
}
