package com.lehau007.todolist.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lehau007.todolist.domain.model.Task
import com.lehau007.todolist.ui.theme.PriorityHigh
import com.lehau007.todolist.ui.theme.PriorityLow
import com.lehau007.todolist.ui.theme.PriorityMedium
import com.lehau007.todolist.ui.theme.SuccessColor
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TaskItem(
    task: Task,
    onCheckedChange: (Boolean) -> Unit,
    onTaskClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val priorityColor = getPriorityColor(task.priority)
    val checkScale by animateFloatAsState(
        targetValue = if (task.isCompleted) 1.1f else 1f,
        animationSpec = tween(200),
        label = "checkScale"
    )
    val cardAlpha by animateFloatAsState(
        targetValue = if (task.isCompleted) 0.7f else 1f,
        animationSpec = tween(300),
        label = "cardAlpha"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (task.isCompleted) 2.dp else 6.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = priorityColor.copy(alpha = 0.1f),
                spotColor = priorityColor.copy(alpha = 0.15f)
            )
            .clickable(onClick = onTaskClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = cardAlpha)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Priority indicator stripe
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                priorityColor,
                                priorityColor.copy(alpha = 0.6f)
                            )
                        )
                    )
            )
            
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Custom Animated Checkbox
                IconButton(
                    onClick = { onCheckedChange(!task.isCompleted) },
                    modifier = Modifier.scale(checkScale)
                ) {
                    Icon(
                        imageVector = if (task.isCompleted) 
                            Icons.Outlined.CheckCircle 
                        else 
                            Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = if (task.isCompleted) "Mark incomplete" else "Mark complete",
                        tint = if (task.isCompleted) 
                            SuccessColor 
                        else 
                            MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Task Details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.SemiBold
                        ),
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (task.isCompleted) 
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        else 
                            MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (!task.description.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = if (task.isCompleted) 0.5f else 0.8f
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Priority Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = priorityColor.copy(alpha = 0.12f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(priorityColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = task.priority.displayName,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = priorityColor
                                )
                            }
                        }

                        // Date & Time
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (isOverdue(task)) 
                                    MaterialTheme.colorScheme.error 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formatDateTime(task.dueDateTime),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isOverdue(task)) 
                                    MaterialTheme.colorScheme.error
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                
                // Delete Button
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete task",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun getPriorityColor(priority: Task.Priority): Color {
    return when (priority) {
        Task.Priority.LOW -> PriorityLow
        Task.Priority.MEDIUM -> PriorityMedium
        Task.Priority.HIGH -> PriorityHigh
    }
}

private fun formatDateTime(timestamp: Long): String {
    val now = Calendar.getInstance()
    val date = Calendar.getInstance().apply { timeInMillis = timestamp }
    
    val isToday = now.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                  now.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)
    
    val isTomorrow = now.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                     now.get(Calendar.DAY_OF_YEAR) + 1 == date.get(Calendar.DAY_OF_YEAR)

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeStr = timeFormat.format(Date(timestamp))

    return when {
        isToday -> "Today, $timeStr"
        isTomorrow -> "Tomorrow, $timeStr"
        else -> {
            val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            "${dateFormat.format(Date(timestamp))}, $timeStr"
        }
    }
}

private fun isOverdue(task: Task): Boolean {
    return !task.isCompleted && task.dueDateTime < System.currentTimeMillis()
}
