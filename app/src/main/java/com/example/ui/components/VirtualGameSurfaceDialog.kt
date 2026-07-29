package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.sandbox.AntiDetectionProfile
import com.example.sandbox.ApkPackageInfo
import com.example.sandbox.RtpStats
import com.example.ui.theme.SlotCyanAccent
import com.example.ui.theme.SlotEmeraldGreen
import com.example.ui.theme.SlotGoldPrimary

@Composable
fun VirtualGameSurfaceDialog(
    apk: ApkPackageInfo,
    spoofProfile: AntiDetectionProfile,
    rtpStats: RtpStats,
    onSimulateSpin: () -> Unit,
    onResetSession: () -> Unit,
    onToggleHud: () -> Unit,
    onDismiss: () -> Unit,
    onStopContainer: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "slot_reel")
    val rotationAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "reel_spin"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("virtual_game_surface_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F141C),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, SlotEmeraldGreen)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 12.dp)
                ) {
                    // Persistent Top Status Bar / Notification Bar Ticker
                    if (rtpStats.isHudVisible) {
                        TopStatusBarRtpTicker(
                            rtpStats = rtpStats,
                            onSimulateSpin = onSimulateSpin,
                            onResetSession = onResetSession,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // Top Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(SlotEmeraldGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = apk.appName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "CONTAINER RUNNING | ${apk.packageName}",
                                    fontSize = 11.sp,
                                    color = SlotEmeraldGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onToggleHud) {
                                Icon(
                                    imageVector = Icons.Default.Analytics,
                                    contentDescription = "Toggle RTP HUD",
                                    tint = if (rtpStats.isHudVisible) SlotGoldPrimary else Color.Gray
                                )
                            }
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close overlay",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Active Specs Banner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1B2433))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = SlotCyanAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "120.0 FPS Vulkan Pass-Through", fontSize = 11.sp, color = SlotCyanAccent, fontWeight = FontWeight.Bold)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Analytics, contentDescription = null, tint = SlotGoldPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Live RTP: ${if (rtpStats.spinCount > 0) String.format("%.1f%%", rtpStats.liveRtpPercent) else "N/A"}",
                                fontSize = 11.sp,
                                color = SlotGoldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = SlotEmeraldGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Spoofed: ${spoofProfile.deviceModel.take(12)}...", fontSize = 11.sp, color = SlotEmeraldGreen, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Simulated Direct GPU Render Surface (Canvas)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF0D121B),
                                        Color(0xFF1A122B),
                                        Color(0xFF0A1C28)
                                    )
                                )
                            )
                            .border(1.dp, SlotGoldPrimary.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val centerX = size.width / 2f
                            val centerY = size.height / 2f

                            // Draw Grid lines simulating 3D Vulkan Render Surface
                            for (i in -4..4) {
                                drawLine(
                                    color = Color(0x334DEEEA),
                                    start = Offset(0f, centerY + i * 40f),
                                    end = Offset(size.width, centerY + i * 40f),
                                    strokeWidth = 1f
                                )
                                drawLine(
                                    color = Color(0x334DEEEA),
                                    start = Offset(centerX + i * 50f, 0f),
                                    end = Offset(centerX + i * 50f, size.height),
                                    strokeWidth = 1f
                                )
                            }

                            // Slot Reels Animation Frame
                            drawCircle(
                                color = Color(0xFFD4AF37),
                                radius = 120f,
                                center = Offset(centerX, centerY),
                                style = Stroke(width = 6f)
                            )

                            drawArc(
                                color = Color(0xFF00E676),
                                startAngle = rotationAnim,
                                sweepAngle = 140f,
                                useCenter = false,
                                topLeft = Offset(centerX - 100f, centerY - 100f),
                                size = Size(200f, 200f),
                                style = Stroke(width = 10f)
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "🎰 VIRTUAL SLOT REEL SURFACE ACTIVE 🎰",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SlotGoldPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Direct Pass-through EGLContext Bound",
                                fontSize = 11.sp,
                                color = Color.LightGray
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(SlotEmeraldGreen)
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "777 MEGA JACKPOT RENDERING",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Bottom Control Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = "Minimize Container", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                onStopContainer()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Stop Game Process", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
