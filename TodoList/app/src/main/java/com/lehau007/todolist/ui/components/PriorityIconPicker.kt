package com.lehau007.todolist.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lehau007.todolist.R
import com.lehau007.todolist.domain.model.Task

/**
 * Priority icon types for the picker.
 */
enum class PriorityIconType {
    FLAG, EMOJI, PROGRESS
}

/**
 * Available flag colors.
 */
val flagColors = listOf(
    0xFFEF4444L to "Red",      // Red
    0xFFF97316L to "Orange",   // Orange
    0xFFFACC15L to "Yellow",   // Yellow
    0xFF22C55EL to "Green",    // Green
    0xFF3B82F6L to "Blue",     // Blue
    0xFF8B5CF6L to "Purple"    // Purple
)

/**
 * Available emojis for priority.
 */
val priorityEmojis = listOf(
    "🔥", "⭐", "❤️", "🚀", "⚠️", "⏰", 
    "✅", "😊", "😢", "🤔", "💪", "🎯",
    "📌", "💡", "🔔", "⚡", "🌟", "❗"
)

/**
 * Priority icon picker dialog.
 */
@Composable
fun PriorityIconPickerDialog(
    currentIcon: Task.PriorityIcon,
    onIconSelected: (Task.PriorityIcon) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedType by remember { 
        mutableStateOf(
            when (currentIcon) {
                is Task.PriorityIcon.Flag -> PriorityIconType.FLAG
                is Task.PriorityIcon.Emoji -> PriorityIconType.EMOJI
                is Task.PriorityIcon.Progress -> PriorityIconType.PROGRESS
                is Task.PriorityIcon.Standard -> PriorityIconType.FLAG
            }
        )
    }
    
    var selectedFlag by remember { 
        mutableStateOf(
            if (currentIcon is Task.PriorityIcon.Flag) currentIcon.color else 0xFFEF4444L
        )
    }
    
    var selectedEmoji by remember { 
        mutableStateOf(
            if (currentIcon is Task.PriorityIcon.Emoji) currentIcon.emoji else "🔥"
        )
    }
    
    var progressValue by remember { 
        mutableIntStateOf(
            if (currentIcon is Task.PriorityIcon.Progress) currentIcon.percentage else 50
        )
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                stringResource(R.string.priority_icon),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Type selector tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PriorityIconType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { 
                                Text(
                                    when (type) {
                                        PriorityIconType.FLAG -> "Flag"
                                        PriorityIconType.EMOJI -> "Emoji"
                                        PriorityIconType.PROGRESS -> "Progress"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                
                HorizontalDivider()
                
                // Content based on selected type
                when (selectedType) {
                    PriorityIconType.FLAG -> {
                        FlagColorPicker(
                            selectedColor = selectedFlag,
                            onColorSelected = { selectedFlag = it }
                        )
                    }
                    PriorityIconType.EMOJI -> {
                        EmojiPicker(
                            selectedEmoji = selectedEmoji,
                            onEmojiSelected = { selectedEmoji = it }
                        )
                    }
                    PriorityIconType.PROGRESS -> {
                        ProgressPicker(
                            value = progressValue,
                            onValueChange = { progressValue = it }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val icon = when (selectedType) {
                        PriorityIconType.FLAG -> Task.PriorityIcon.Flag(selectedFlag)
                        PriorityIconType.EMOJI -> Task.PriorityIcon.Emoji(selectedEmoji)
                        PriorityIconType.PROGRESS -> Task.PriorityIcon.Progress(progressValue)
                    }
                    onIconSelected(icon)
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun FlagColorPicker(
    selectedColor: Long,
    onColorSelected: (Long) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Select Flag Color",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(120.dp)
        ) {
            items(flagColors) { (color, name) ->
                FlagColorItem(
                    color = color,
                    name = name,
                    isSelected = selectedColor == color,
                    onClick = { onColorSelected(color) }
                )
            }
        }
    }
}

@Composable
private fun FlagColorItem(
    color: Long,
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(
                if (isSelected) 
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else 
                    Color.Transparent
            )
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(color)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Flag,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmojiPicker(
    selectedEmoji: String,
    onEmojiSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Select Emoji",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(150.dp)
        ) {
            items(priorityEmojis) { emoji ->
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (selectedEmoji == emoji)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        .clickable { onEmojiSelected(emoji) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emoji,
                        fontSize = 22.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressPicker(
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Set Progress Percentage",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // Progress display circle
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$value%",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // Slider
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..100f,
            steps = 9,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Quick buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(0, 25, 50, 75, 100).forEach { percent ->
                FilterChip(
                    selected = value == percent,
                    onClick = { onValueChange(percent) },
                    label = { Text("$percent%") }
                )
            }
        }
    }
}

/**
 * Small priority icon display for task items.
 */
@Composable
fun PriorityIconBadge(
    icon: Task.PriorityIcon,
    modifier: Modifier = Modifier
) {
    when (icon) {
        is Task.PriorityIcon.Flag -> {
            Box(
                modifier = modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(icon.color).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Flag,
                    contentDescription = null,
                    tint = Color(icon.color),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        is Task.PriorityIcon.Emoji -> {
            Text(
                text = icon.emoji,
                fontSize = 20.sp,
                modifier = modifier
            )
        }
        is Task.PriorityIcon.Progress -> {
            Box(
                modifier = modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${icon.percentage}%",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        is Task.PriorityIcon.Standard -> {
            val color = when (icon.priority) {
                Task.Priority.HIGH -> 0xFFEF4444L
                Task.Priority.MEDIUM -> 0xFFF59E0BL
                Task.Priority.LOW -> 0xFF22C55EL
            }
            Box(
                modifier = modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(color).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Flag,
                    contentDescription = null,
                    tint = Color(color),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
