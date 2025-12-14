package com.lehau007.todolist.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lehau007.todolist.R
import com.lehau007.todolist.domain.model.Task
import com.lehau007.todolist.ui.theme.GradientEnd
import com.lehau007.todolist.ui.theme.GradientStart
import com.lehau007.todolist.ui.viewmodel.CalendarViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigateToEditTask: (String) -> Unit = {},
    onNavigateToAddTask: () -> Unit = {},
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val currentMonth by viewModel.currentMonth.collectAsStateWithLifecycle()
    val tasksForSelectedDate by viewModel.tasksForSelectedDate.collectAsStateWithLifecycle()
    val isMonthlyView by viewModel.isMonthlyView.collectAsStateWithLifecycle()
    
    val listState = rememberLazyListState()
    
    // Detect scroll to switch between monthly and weekly
    LaunchedEffect(listState.firstVisibleItemIndex) {
        if (listState.firstVisibleItemIndex > 0 && isMonthlyView) {
            viewModel.setMonthlyView(false)
        } else if (listState.firstVisibleItemIndex == 0 && !isMonthlyView) {
            viewModel.setMonthlyView(true)
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Calendar Header
            item {
                CalendarHeader(
                    currentMonth = currentMonth,
                    onPreviousMonth = { viewModel.previousMonth() },
                    onNextMonth = { viewModel.nextMonth() },
                    isMonthlyView = isMonthlyView,
                    onToggleView = { viewModel.toggleView() }
                )
            }
            
            // Calendar Grid
            item {
                CalendarGrid(
                    currentMonth = currentMonth,
                    selectedDate = selectedDate,
                    isMonthlyView = isMonthlyView,
                    taskDates = viewModel.getTaskDates(),
                    onDateSelected = { viewModel.selectDate(it) }
                )
            }
            
            // Tasks for selected date
            item {
                Spacer(modifier = Modifier.height(16.dp))
                if (tasksForSelectedDate.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tasks for this date",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            items(tasksForSelectedDate) { task ->
                CalendarTaskItem(
                    task = task,
                    onClick = { onNavigateToEditTask(task.id) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
        
        // FAB
        FloatingActionButton(
            onClick = onNavigateToAddTask,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Task")
        }
    }
}

@Composable
private fun CalendarHeader(
    currentMonth: Calendar,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    isMonthlyView: Boolean,
    onToggleView: () -> Unit
) {
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous")
        }
        
        Text(
            text = monthFormat.format(currentMonth.time).uppercase(),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
        
        Row {
            IconButton(onClick = onNextMonth) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next")
            }
            IconButton(onClick = onToggleView) {
                Icon(
                    if (isMonthlyView) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Toggle View"
                )
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    currentMonth: Calendar,
    selectedDate: Long,
    isMonthlyView: Boolean,
    taskDates: Set<Long>,
    onDateSelected: (Long) -> Unit
) {
    val calendar = currentMonth.clone() as Calendar
    val today = Calendar.getInstance()
    
    // Day headers
    val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(horizontal = 8.dp)
    ) {
        // Day names row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            dayNames.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (isMonthlyView) {
            // Monthly view - show all weeks
            MonthlyCalendarGrid(
                calendar = calendar,
                today = today,
                selectedDate = selectedDate,
                taskDates = taskDates,
                onDateSelected = onDateSelected
            )
        } else {
            // Weekly view - show only selected week
            WeeklyCalendarGrid(
                selectedDate = selectedDate,
                today = today,
                taskDates = taskDates,
                onDateSelected = onDateSelected
            )
        }
    }
}

@Composable
private fun MonthlyCalendarGrid(
    calendar: Calendar,
    today: Calendar,
    selectedDate: Long,
    taskDates: Set<Long>,
    onDateSelected: (Long) -> Unit
) {
    val tempCal = calendar.clone() as Calendar
    tempCal.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) - 1
    val daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    val totalCells = firstDayOfWeek + daysInMonth
    val weeks = (totalCells + 6) / 7
    
    for (week in 0 until weeks) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            for (dayOfWeek in 0 until 7) {
                val cellIndex = week * 7 + dayOfWeek
                val dayNumber = cellIndex - firstDayOfWeek + 1
                
                if (dayNumber in 1..daysInMonth) {
                    tempCal.set(Calendar.DAY_OF_MONTH, dayNumber)
                    val dateMillis = tempCal.timeInMillis
                    val isSelected = isSameDay(dateMillis, selectedDate)
                    val isToday = isSameDay(dateMillis, today.timeInMillis)
                    val hasTask = taskDates.any { isSameDay(it, dateMillis) }
                    
                    CalendarDayCell(
                        day = dayNumber,
                        isSelected = isSelected,
                        isToday = isToday,
                        hasTask = hasTask,
                        onClick = { onDateSelected(dateMillis) },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun WeeklyCalendarGrid(
    selectedDate: Long,
    today: Calendar,
    taskDates: Set<Long>,
    onDateSelected: (Long) -> Unit
) {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = selectedDate
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        for (i in 0 until 7) {
            val dateMillis = calendar.timeInMillis
            val isSelected = isSameDay(dateMillis, selectedDate)
            val isToday = isSameDay(dateMillis, today.timeInMillis)
            val hasTask = taskDates.any { isSameDay(it, dateMillis) }
            
            CalendarDayCell(
                day = calendar.get(Calendar.DAY_OF_MONTH),
                isSelected = isSelected,
                isToday = isToday,
                hasTask = hasTask,
                onClick = { onDateSelected(dateMillis) },
                modifier = Modifier.weight(1f)
            )
            
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    hasTask: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primaryContainer
                    else -> Color.Transparent
                }
            )
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = when {
                isSelected -> Color.White
                isToday -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurface
            },
            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
        )
        
        if (hasTask && !isSelected) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
private fun CalendarTaskItem(
    task: Task,
    onClick: () -> Unit
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Priority indicator
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        when (task.priority) {
                            Task.Priority.HIGH -> Color(0xFFEF4444)
                            Task.Priority.MEDIUM -> Color(0xFFF59E0B)
                            Task.Priority.LOW -> Color(0xFF22C55E)
                        }
                    )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
                if (task.hasTime) {
                    Text(
                        text = timeFormat.format(Date(task.dueDateTime)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Priority icon placeholder
            PriorityIconDisplay(task.priorityIcon)
        }
    }
}

@Composable
private fun PriorityIconDisplay(priorityIcon: Task.PriorityIcon) {
    when (priorityIcon) {
        is Task.PriorityIcon.Flag -> {
            Text(
                text = "🚩",
                fontSize = 20.sp,
                color = Color(priorityIcon.color)
            )
        }
        is Task.PriorityIcon.Emoji -> {
            Text(
                text = priorityIcon.emoji,
                fontSize = 20.sp
            )
        }
        is Task.PriorityIcon.Progress -> {
            // Progress circle would go here
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${priorityIcon.percentage}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.sp
                )
            }
        }
        is Task.PriorityIcon.Standard -> {
            // Default flag icon
            Text(
                text = "🏴",
                fontSize = 20.sp
            )
        }
    }
}

private fun isSameDay(date1: Long, date2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = date1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = date2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
