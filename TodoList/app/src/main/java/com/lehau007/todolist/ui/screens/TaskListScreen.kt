package com.lehau007.todolist.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Task
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lehau007.todolist.R
import com.lehau007.todolist.domain.model.Category
import com.lehau007.todolist.domain.model.Task
import com.lehau007.todolist.ui.components.PriorityIconBadge
import com.lehau007.todolist.ui.theme.GradientEnd
import com.lehau007.todolist.ui.theme.GradientStart
import com.lehau007.todolist.ui.viewmodel.TaskListViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    selectedCategory: Category? = null,
    onNavigateToAddTask: () -> Unit,
    onNavigateToEditTask: (String) -> Unit,
    onNavigateToFocusMode: (String) -> Unit = {},
    viewModel: TaskListViewModel = hiltViewModel()
) {
    LaunchedEffect(selectedCategory) {
        viewModel.setSelectedCategory(selectedCategory?.id)
    }

    val groupedTasks by viewModel.groupedTasks.collectAsStateWithLifecycle()
    val completedToday by viewModel.completedToday.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    // val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle() // Removed local observation
    
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }
    var previousExpanded by remember { mutableStateOf(true) }
    var futureExpanded by remember { mutableStateOf(true) }
    var completedExpanded by remember { mutableStateOf(true) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Category Filter Chips
            item {
                CategoryFilterRow(
                    categories = categories,
                    selectedCategory = selectedCategory?.id,
                    onCategorySelected = { viewModel.setSelectedCategory(it) }
                )
            }
            
            // Previous Tasks Section
            if (groupedTasks.previous.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = stringResource(R.string.section_previous),
                        isExpanded = previousExpanded,
                        onToggle = { previousExpanded = !previousExpanded }
                    )
                }
                
                if (previousExpanded) {
                    items(groupedTasks.previous, key = { it.id }) { task ->
                        TaskListItem(
                            task = task,
                            onTaskClick = { onNavigateToEditTask(task.id) },
                            onCheckedChange = { viewModel.toggleTaskCompletion(task.id) },
                            onDeleteClick = { showDeleteDialog = task.id },
                            onFocusModeClick = { onNavigateToFocusMode(task.id) },
                            isOverdue = true
                        )
                    }
                }
            }
            
            // Today Tasks Section (always show if has tasks)
            if (groupedTasks.today.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = stringResource(R.string.section_today),
                        isExpanded = true,
                        showToggle = false,
                        onToggle = {}
                    )
                }
                
                items(groupedTasks.today, key = { it.id }) { task ->
                    TaskListItem(
                        task = task,
                        onTaskClick = { onNavigateToEditTask(task.id) },
                        onCheckedChange = { viewModel.toggleTaskCompletion(task.id) },
                        onDeleteClick = { showDeleteDialog = task.id },
                        onFocusModeClick = { onNavigateToFocusMode(task.id) }
                    )
                }
            }
            
            // Future Tasks Section
            if (groupedTasks.future.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = stringResource(R.string.section_future),
                        isExpanded = futureExpanded,
                        onToggle = { futureExpanded = !futureExpanded }
                    )
                }
                
                if (futureExpanded) {
                    items(groupedTasks.future, key = { it.id }) { task ->
                        TaskListItem(
                            task = task,
                            onTaskClick = { onNavigateToEditTask(task.id) },
                            onCheckedChange = { viewModel.toggleTaskCompletion(task.id) },
                            onDeleteClick = { showDeleteDialog = task.id },
                            onFocusModeClick = { onNavigateToFocusMode(task.id) }
                        )
                    }
                }
            }
            
            // Completed Today Section
            if (completedToday.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = stringResource(R.string.section_completed_today),
                        isExpanded = completedExpanded,
                        onToggle = { completedExpanded = !completedExpanded }
                    )
                }
                
                if (completedExpanded) {
                    items(completedToday, key = { it.id }) { task ->
                        TaskListItem(
                            task = task,
                            onTaskClick = { onNavigateToEditTask(task.id) },
                            onCheckedChange = { viewModel.toggleTaskCompletion(task.id) },
                            onDeleteClick = { showDeleteDialog = task.id },
                            onFocusModeClick = { onNavigateToFocusMode(task.id) },
                            isCompleted = true
                        )
                    }
                }
            }
            
            // Empty state
            if (groupedTasks.previous.isEmpty() && 
                groupedTasks.today.isEmpty() && 
                groupedTasks.future.isEmpty() &&
                completedToday.isEmpty()) {
                item {
                    EmptyTasksState()
                }
            }
        }
        
        // FAB
        FloatingActionButton(
            onClick = onNavigateToAddTask,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.add_task),
                modifier = Modifier.size(28.dp)
            )
        }
    }
    
    // Delete Dialog
    showDeleteDialog?.let { taskId ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            icon = { 
                Icon(
                    Icons.Default.Delete, 
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                ) 
            },
            title = { Text(stringResource(R.string.delete_task_confirm)) },
            text = { Text(stringResource(R.string.delete_task_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTask(taskId)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun CategoryFilterRow(
    categories: List<Category>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        // All filter
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text(stringResource(R.string.filter_all)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White
                )
            )
        }
        
        items(categories) { category ->
            FilterChip(
                selected = selectedCategory == category.id,
                onClick = { onCategorySelected(category.id) },
                label = { Text(category.name) },
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(category.color))
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(category.color).copy(alpha = 0.2f),
                    selectedLabelColor = Color(category.color)
                )
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    isExpanded: Boolean,
    showToggle: Boolean = true,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = showToggle, onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
        if (showToggle) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TaskListItem(
    task: Task,
    onTaskClick: () -> Unit,
    onCheckedChange: () -> Unit,
    onDeleteClick: () -> Unit,
    onFocusModeClick: () -> Unit = {},
    isOverdue: Boolean = false,
    isCompleted: Boolean = false
) {
    var showMenu by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("MM-dd", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            IconButton(
                onClick = onCheckedChange,
                modifier = Modifier.size(32.dp)
            ) {
                if (isCompleted || task.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(4.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .border(
                                width = 2.dp,
                                color = Color.Gray,
                                shape = CircleShape
                            )
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (isCompleted || task.isCompleted) 
                            TextDecoration.LineThrough else TextDecoration.None
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isCompleted || task.isCompleted) 
                        MaterialTheme.colorScheme.onSurfaceVariant 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = dateFormat.format(Date(task.dueDateTime)),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isOverdue) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (task.hasTime) {
                        Text(
                            text = timeFormat.format(Date(task.dueDateTime)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    if (task.isRecurring) {
                        Icon(
                            imageVector = Icons.Outlined.Repeat,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Priority Icon
            PriorityIconBadge(icon = task.priorityIcon)
            
            // 3-dot menu
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.edit_task)) },
                        onClick = {
                            showMenu = false
                            onTaskClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.focus_mode)) },
                        onClick = {
                            showMenu = false
                            onFocusModeClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete)) },
                        onClick = {
                            showMenu = false
                            onDeleteClick()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyTasksState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Task,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = stringResource(R.string.no_tasks),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.no_tasks_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}