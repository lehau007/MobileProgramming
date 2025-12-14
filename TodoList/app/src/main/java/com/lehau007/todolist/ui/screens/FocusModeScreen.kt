package com.lehau007.todolist.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lehau007.todolist.R
import com.lehau007.todolist.ui.theme.GradientEnd
import com.lehau007.todolist.ui.theme.GradientStart
import com.lehau007.todolist.ui.viewmodel.FocusModeViewModel
import com.lehau007.todolist.ui.viewmodel.FocusState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusModeScreen(
    taskId: String,
    onNavigateBack: () -> Unit,
    viewModel: FocusModeViewModel = hiltViewModel()
) {
    val task by viewModel.task.collectAsStateWithLifecycle()
    val focusState by viewModel.focusState.collectAsStateWithLifecycle()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsStateWithLifecycle()
    val targetMinutes by viewModel.targetMinutes.collectAsStateWithLifecycle()
    val showTimePicker by viewModel.showTimePicker.collectAsStateWithLifecycle()
    
    // Keep screen on during focus
    val context = LocalContext.current
    DisposableEffect(focusState) {
        if (focusState == FocusState.RUNNING) {
            (context as? android.app.Activity)?.window?.addFlags(
                android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        onDispose {
            (context as? android.app.Activity)?.window?.clearFlags(
                android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GradientStart,
                        GradientEnd
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    viewModel.stopTimer()
                    onNavigateBack()
                }) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
                Text(
                    text = stringResource(R.string.focus_mode),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(48.dp))
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Task title
            task?.let {
                Text(
                    text = it.title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Timer Circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(280.dp)
            ) {
                val progress = if (targetMinutes > 0) {
                    (elapsedSeconds.toFloat() / (targetMinutes * 60)).coerceIn(0f, 1f)
                } else 0f
                
                val animatedProgress by animateFloatAsState(
                    targetValue = progress,
                    animationSpec = tween(300),
                    label = "progress"
                )
                
                // Background circle
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 12.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    
                    // Track
                    drawCircle(
                        color = Color.White.copy(alpha = 0.2f),
                        radius = radius,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    
                    // Progress arc
                    if (targetMinutes > 0) {
                        drawArc(
                            color = Color.White,
                            startAngle = -90f,
                            sweepAngle = 360f * animatedProgress,
                            useCenter = false,
                            topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                            size = Size(size.width - strokeWidth, size.height - strokeWidth),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }
                
                // Time display
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = formatTime(elapsedSeconds),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 56.sp
                        ),
                        color = Color.White
                    )
                    if (targetMinutes > 0) {
                        Text(
                            text = "/ ${targetMinutes} min",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset button
                if (focusState != FocusState.IDLE) {
                    FloatingActionButton(
                        onClick = { viewModel.resetTimer() },
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset")
                    }
                }
                
                // Play/Pause button
                FloatingActionButton(
                    onClick = {
                        when (focusState) {
                            FocusState.IDLE -> {
                                if (targetMinutes > 0) {
                                    viewModel.startTimer()
                                } else {
                                    viewModel.showTimePicker()
                                }
                            }
                            FocusState.RUNNING -> viewModel.pauseTimer()
                            FocusState.PAUSED -> viewModel.resumeTimer()
                            FocusState.COMPLETED -> viewModel.resetTimer()
                        }
                    },
                    containerColor = Color.White,
                    contentColor = GradientStart,
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = when (focusState) {
                            FocusState.RUNNING -> Icons.Default.Pause
                            FocusState.COMPLETED -> Icons.Default.Refresh
                            else -> Icons.Default.PlayArrow
                        },
                        contentDescription = "Start/Pause",
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                // Stop/Save button
                if (focusState != FocusState.IDLE) {
                    FloatingActionButton(
                        onClick = {
                            viewModel.stopAndSave()
                            onNavigateBack()
                        },
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Finish")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Set time button (when idle)
            if (focusState == FocusState.IDLE) {
                TextButton(
                    onClick = { viewModel.showTimePicker() }
                ) {
                    Text(
                        text = if (targetMinutes > 0) 
                            stringResource(R.string.change_target_time) 
                        else 
                            stringResource(R.string.set_target_time),
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
    
    // Time picker dialog
    if (showTimePicker) {
        TimePickerDialog(
            currentMinutes = targetMinutes,
            onDismiss = { viewModel.hideTimePicker() },
            onConfirm = { minutes ->
                viewModel.setTargetMinutes(minutes)
                viewModel.hideTimePicker()
            }
        )
    }
}

@Composable
private fun TimePickerDialog(
    currentMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var selectedMinutes by remember { mutableIntStateOf(if (currentMinutes > 0) currentMinutes else 25) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.set_focus_duration)) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IconButton(
                        onClick = { if (selectedMinutes > 5) selectedMinutes -= 5 }
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                    }
                    
                    Text(
                        text = "$selectedMinutes",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    
                    IconButton(
                        onClick = { if (selectedMinutes < 180) selectedMinutes += 5 }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase")
                    }
                }
                
                Text(
                    text = stringResource(R.string.minutes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Quick select buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(15, 25, 45, 60).forEach { minutes ->
                        FilterChip(
                            selected = selectedMinutes == minutes,
                            onClick = { selectedMinutes = minutes },
                            label = { Text("$minutes") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedMinutes) }) {
                Text(stringResource(R.string.start))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

private fun formatTime(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%02d:%02d".format(mins, secs)
}
