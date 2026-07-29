package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SlotCyanAccent
import com.example.ui.theme.SlotEmeraldGreen
import com.example.ui.theme.SlotGoldPrimary

@Composable
fun EngineMetricsHeader(
    fpsTarget: Int,
    isGpuPassThrough: Boolean,
    isNativeLoaded: Boolean,
    activeHooksCount: Int,
    pingLatencyMs: Int = 14,
    activeTcpSockets: Int = 8,
    primaryDns: String = "1.1.1.1 (Cloudflare)",
    isLowLatencyActive: Boolean = true,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .testTag("engine_metrics_header"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val statusColor by animateColorAsState(
                        targetValue = if (isNativeLoaded) SlotEmeraldGreen else SlotGoldPrimary,
                        label = "statusColor"
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isNativeLoaded) "NDK HARDWARE ACCELERATED" else "VIRTUAL CONTAINER RUNTIME",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "LXC USERSPACE",
                    fontSize = 10.sp,
                    color = SlotCyanAccent,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2x2 Grid of Engine Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricItem(
                    icon = Icons.Default.Speed,
                    label = "GPU Pass-Through",
                    value = "${fpsTarget}Hz Active",
                    subValue = if (isGpuPassThrough) "Vulkan 1.3 Direct" else "OpenGL ES 3.2",
                    accentColor = SlotCyanAccent,
                    modifier = Modifier.weight(1f)
                )

                MetricItem(
                    icon = Icons.Default.Security,
                    label = "Anti-Detection",
                    value = "Spoofing Active",
                    subValue = "$activeHooksCount C++ PLT Hooks",
                    accentColor = SlotEmeraldGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricItem(
                    icon = Icons.Default.NetworkCheck,
                    label = "Net Interceptor",
                    value = "${pingLatencyMs}ms Latency",
                    subValue = "Route: $primaryDns",
                    accentColor = SlotGoldPrimary,
                    modifier = Modifier.weight(1f)
                )

                MetricItem(
                    icon = Icons.Default.Lan,
                    label = "Keep-Alive Sockets",
                    value = "$activeTcpSockets TCP Active",
                    subValue = if (isLowLatencyActive) "0ms Heartbeat Tunnel" else "Standard Relay",
                    accentColor = SlotCyanAccent,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricItem(
    icon: ImageVector,
    label: String,
    value: String,
    subValue: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subValue,
                fontSize = 10.sp,
                color = accentColor.copy(alpha = 0.85f),
                maxLines = 1
            )
        }
    }
}
