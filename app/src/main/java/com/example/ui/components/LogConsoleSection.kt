package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sandbox.LogLevel
import com.example.sandbox.SandboxLog
import com.example.ui.theme.SlotCyanAccent
import com.example.ui.theme.SlotEmeraldGreen
import com.example.ui.theme.SlotGoldPrimary
import com.example.ui.theme.TerminalBackground
import com.example.ui.theme.TerminalCyanText
import com.example.ui.theme.TerminalGoldText
import com.example.ui.theme.TerminalGreenText
import com.example.ui.theme.TextPrimaryDark

@Composable
fun LogConsoleSection(
    logs: List<SandboxLog>,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") }
    val filters = listOf("ALL", "SYS CALLS", "HOOKS", "GPU", "NET")
    val listState = rememberLazyListState()

    val filteredLogs = remember(logs, selectedFilter) {
        when (selectedFilter) {
            "SYS CALLS" -> logs.filter { it.level == LogLevel.SYSCALL || it.category.contains("SYS") }
            "HOOKS" -> logs.filter { it.level == LogLevel.HOOK || it.category.contains("HOOK") || it.category.contains("SPOOF") }
            "GPU" -> logs.filter { it.level == LogLevel.GPU || it.category.contains("GPU") }
            "NET" -> logs.filter { it.level == LogLevel.NET || it.category.contains("NET") }
            else -> logs
        }
    }

    // Auto scroll to bottom on new logs
    LaunchedEffect(filteredLogs.size) {
        if (filteredLogs.isNotEmpty()) {
            listState.animateScrollToItem(filteredLogs.size - 1)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = TerminalBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(SlotCyanAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Console",
                            tint = SlotCyanAccent,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sandbox Execution Log Output",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${filteredLogs.size} lines",
                        fontSize = 11.sp,
                        color = SlotCyanAccent,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onClearLogs, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear logs",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                filters.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) SlotCyanAccent else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = filter,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) TerminalBackground else TextPrimaryDark,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Log Console Terminal Screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                if (filteredLogs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No log output recorded yet. Load or trigger an APK to see live syscall trace.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredLogs) { log ->
                            LogLineItem(log = log)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LogLineItem(log: SandboxLog) {
    val categoryColor = when (log.level) {
        LogLevel.SYSCALL -> TerminalGoldText
        LogLevel.HOOK -> TerminalGreenText
        LogLevel.GPU -> TerminalCyanText
        LogLevel.NET -> SlotCyanAccent
        LogLevel.WARN -> SlotGoldPrimary
        LogLevel.ERROR -> MaterialTheme.colorScheme.error
        else -> TextPrimaryDark
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "${log.timestamp} ",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = "[${log.category}] ",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = categoryColor,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = log.message,
            fontSize = 10.sp,
            color = TextPrimaryDark,
            fontFamily = FontFamily.Monospace
        )
    }
}
