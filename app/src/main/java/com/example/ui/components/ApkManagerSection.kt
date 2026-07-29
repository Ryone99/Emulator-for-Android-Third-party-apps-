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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sandbox.AntiDetectionProfile
import com.example.sandbox.ApkPackageInfo
import com.example.sandbox.RtpStats
import com.example.ui.theme.SlotCyanAccent
import com.example.ui.theme.SlotEmeraldGreen
import com.example.ui.theme.SlotGoldPrimary

@Composable
fun ApkManagerSection(
    apks: List<ApkPackageInfo>,
    spoofProfile: AntiDetectionProfile,
    rtpStats: RtpStats,
    onLoadApkClick: () -> Unit,
    onCustomApkLoaded: (appName: String, pkgName: String, sizeMb: Double) -> Unit,
    onLaunchApk: (ApkPackageInfo) -> Unit,
    onStopApk: (String) -> Unit,
    onSimulateSpin: () -> Unit,
    onResetRtpSession: () -> Unit,
    onToggleRtpHud: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showLoadApkDialog by remember { mutableStateOf(false) }
    var activeVirtualGameApk by remember { mutableStateOf<ApkPackageInfo?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Virtual Slot Containers",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${apks.size} Slot APKs isolated in sandbox",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // PRIMARY BUTTON REQUIRED BY PROMPT: "Load Slot Game APK"
            Button(
                onClick = { showLoadApkDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SlotGoldPrimary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("load_slot_apk_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Load Slot Game APK",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (apks.isEmpty()) {
            EmptyApkCard(onLoadClick = { showLoadApkDialog = true })
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                apks.forEach { apk ->
                    ApkCardItem(
                        apk = apk,
                        onLaunch = {
                            onLaunchApk(apk)
                            activeVirtualGameApk = apk
                        },
                        onStop = { onStopApk(apk.packageName) },
                        onOpenSurface = { activeVirtualGameApk = apk }
                    )
                }
            }
        }
    }

    if (showLoadApkDialog) {
        LoadSlotApkModal(
            onDismiss = { showLoadApkDialog = false },
            onPickStorageApk = {
                showLoadApkDialog = false
                onLoadApkClick()
            },
            onConfirmLoad = { appName, pkgName, sizeMb ->
                onCustomApkLoaded(appName, pkgName, sizeMb)
                showLoadApkDialog = false
            }
        )
    }

    activeVirtualGameApk?.let { activeApk ->
        VirtualGameSurfaceDialog(
            apk = activeApk,
            spoofProfile = spoofProfile,
            rtpStats = rtpStats,
            onSimulateSpin = onSimulateSpin,
            onResetSession = onResetRtpSession,
            onToggleHud = onToggleRtpHud,
            onDismiss = { activeVirtualGameApk = null },
            onStopContainer = {
                onStopApk(activeApk.packageName)
                activeVirtualGameApk = null
            }
        )
    }
}

@Composable
private fun ApkCardItem(
    apk: ApkPackageInfo,
    onLaunch: () -> Unit,
    onStop: () -> Unit,
    onOpenSurface: () -> Unit
) {
    val isRunning = apk.isRunningInSandbox
    val sizeMb = (apk.apkSizeBytes / (1024 * 1024.0))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isRunning) 1.5.dp else 0.5.dp,
                color = if (isRunning) SlotEmeraldGreen else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable {
                if (isRunning) onOpenSurface() else onLaunch()
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isRunning) SlotEmeraldGreen.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isRunning) SlotEmeraldGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Casino,
                    contentDescription = null,
                    tint = if (isRunning) SlotEmeraldGreen else SlotGoldPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = apk.appName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(SlotCyanAccent.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${apk.targetFps}Hz",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlotCyanAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${apk.packageName} • ${String.format("%.1f", sizeMb)} MB",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isRunning) SlotEmeraldGreen else SlotGoldPrimary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isRunning) "CONTAINER ACTIVE (PID: ${Math.abs(apk.packageName.hashCode() % 8000 + 1000)})" else "SANDBOX STAGED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isRunning) SlotEmeraldGreen else SlotGoldPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isRunning) {
                Button(
                    onClick = onStop,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Stop, contentDescription = "Stop", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Stop", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onLaunch,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SlotEmeraldGreen,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("start_game_button")
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Start Game", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Start Game", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun EmptyApkCard(onLoadClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Casino,
                contentDescription = null,
                tint = SlotGoldPrimary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No Virtual Slot APKs Loaded",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap 'Load Slot Game APK' above to import an APK into the container.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LoadSlotApkModal(
    onDismiss: () -> Unit,
    onPickStorageApk: () -> Unit,
    onConfirmLoad: (appName: String, pkgName: String, sizeMb: Double) -> Unit
) {
    var customAppName by remember { mutableStateOf("CyberSlots Deluxe 777") }
    var customPkgName by remember { mutableStateOf("com.slotgame.cyberslots.v777") }
    var customSizeMb by remember { mutableStateOf("52.4") }

    var selectedSampleIndex by remember { mutableStateOf(0) }
    val sampleSlotApks = remember {
        listOf(
            Triple("CyberSlots Deluxe 777", "com.slotgame.cyberslots.v777", 52.4),
            Triple("Dragon King Megaways", "com.oriental.dragon.slots", 78.1),
            Triple("Golden Pharaoh 100x", "com.egypt.pharaoh.wilds", 41.5)
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Load Slot Game APK into Sandbox",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                Text(
                    text = "Choose an option to stage a Slot Game APK into the isolated virtual container:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // SAF File Picker Direct Option
                Button(
                    onClick = onPickStorageApk,
                    colors = ButtonDefaults.buttonColors(containerColor = SlotEmeraldGreen),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Select .APK from Device Storage (SAF)", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Or Stage Sample Slot APK Preset:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlotGoldPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                sampleSlotApks.forEachIndexed { index, triple ->
                    val isSelected = selectedSampleIndex == index
                    Card(
                        onClick = {
                            selectedSampleIndex = index
                            customAppName = triple.first
                            customPkgName = triple.second
                            customSizeMb = triple.third.toString()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(
                                width = if (isSelected) 1.5.dp else 0.5.dp,
                                color = if (isSelected) SlotGoldPrimary else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) SlotGoldPrimary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = triple.first,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = triple.second,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${triple.third} MB",
                                fontSize = 11.sp,
                                color = SlotCyanAccent,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = customAppName,
                    onValueChange = { customAppName = it },
                    label = { Text("App Display Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SlotGoldPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customPkgName,
                    onValueChange = { customPkgName = it },
                    label = { Text("Package Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SlotGoldPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val size = customSizeMb.toDoubleOrNull() ?: 50.0
                    onConfirmLoad(customAppName, customPkgName, size)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SlotGoldPrimary)
            ) {
                Text(text = "Stage Preset APK into Container", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
