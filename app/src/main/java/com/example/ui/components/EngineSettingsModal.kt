package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sandbox.GpuEngineConfig
import com.example.sandbox.NetworkMetricsState
import com.example.ui.theme.SlotCyanAccent
import com.example.ui.theme.SlotGoldPrimary

@Composable
fun EngineSettingsModal(
    config: GpuEngineConfig,
    netMetrics: NetworkMetricsState,
    onDismiss: () -> Unit,
    onSaveConfig: (fps: Int, passThrough: Boolean, primaryDns: String, ultraLowLatency: Boolean) -> Unit
) {
    var selectedFps by remember { mutableStateOf(config.refreshRateHz) }
    var passThroughActive by remember { mutableStateOf(config.renderBypassActive) }
    var selectedDns by remember { mutableStateOf(netMetrics.primaryDns) }
    var ultraLowLatencyActive by remember { mutableStateOf(netMetrics.isLowLatencyActive) }

    val fpsOptions = listOf(60, 90, 120)
    val dnsOptions = listOf("1.1.1.1 (Cloudflare)", "8.8.8.8 (Google)", "9.9.9.9 (Quad9)")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Engine & Network Optimization",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.testTag("settings_modal_title")
            )
        },
        text = {
            Column {
                Text(
                    text = "Configure rendering refresh rates, DNS routing, & socket latency pass-through:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Display Frame Rate Target:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlotCyanAccent
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    fpsOptions.forEach { fps ->
                        val isSelected = selectedFps == fps
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFps = fps },
                            label = { Text(text = "${fps} FPS", fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SlotCyanAccent,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Enforced Custom DNS Route:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlotGoldPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    dnsOptions.forEach { dns ->
                        val isSelected = selectedDns == dns
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedDns = dns },
                            label = { Text(text = dns, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SlotGoldPrimary,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Direct GPU Pass-Through",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Bypasses virtual surface composition for zero input lag.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = passThroughActive,
                        onCheckedChange = { passThroughActive = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SlotCyanAccent,
                            checkedTrackColor = SlotCyanAccent.copy(alpha = 0.3f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ultra-Low Latency Tunnel",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Optimizes TCP socket buffers & reduces ping latency.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = ultraLowLatencyActive,
                        onCheckedChange = { ultraLowLatencyActive = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SlotGoldPrimary,
                            checkedTrackColor = SlotGoldPrimary.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val secDns = if (selectedDns.contains("1.1.1.1")) "1.0.0.1" else "8.8.4.4"
                    onSaveConfig(selectedFps, passThroughActive, selectedDns, ultraLowLatencyActive)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SlotGoldPrimary)
            ) {
                Text(text = "Save Configuration", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        }
    )
}
