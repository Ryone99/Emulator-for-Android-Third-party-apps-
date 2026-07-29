package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sandbox.BetChangeDirection
import com.example.sandbox.BetRecommendation
import com.example.sandbox.RtpStats
import com.example.ui.theme.SlotCyanAccent
import com.example.ui.theme.SlotEmeraldGreen
import com.example.ui.theme.SlotGoldPrimary
import kotlin.math.roundToInt

@Composable
fun LiveRtpFloatingHud(
    rtpStats: RtpStats,
    onSimulateSpin: () -> Unit,
    onResetSession: () -> Unit,
    onCloseHud: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var isExpanded by remember { mutableStateOf(true) }

    val liveRtp = rtpStats.liveRtpPercent
    val shortTermRtp = rtpStats.shortTermRtpPercent

    // Unified Sync Color when algorithm recommendation matches user bet move
    val unifiedSyncColor = SlotGoldPrimary

    val rtpTrendColor = when {
        rtpStats.isSynced -> unifiedSyncColor
        rtpStats.recommendation == BetRecommendation.INCREASE_BET -> SlotEmeraldGreen
        rtpStats.recommendation == BetRecommendation.DECREASE_BET -> Color(0xFFFF5252)
        else -> SlotCyanAccent
    }

    val betIndicatorColor = when {
        rtpStats.isSynced -> unifiedSyncColor
        rtpStats.betChangeDirection == BetChangeDirection.INCREASED -> SlotEmeraldGreen
        rtpStats.betChangeDirection == BetChangeDirection.DECREASED -> Color(0xFFFF5252)
        else -> Color.White
    }

    val trendArrowSymbol = when (rtpStats.recommendation) {
        BetRecommendation.INCREASE_BET -> "⬆️"
        BetRecommendation.DECREASE_BET -> "⬇️"
        BetRecommendation.HOLD_STABLE -> "➡️"
    }

    val betArrowSymbol = when (rtpStats.betChangeDirection) {
        BetChangeDirection.INCREASED -> "⬆️"
        BetChangeDirection.DECREASED -> "⬇️"
        BetChangeDirection.UNCHANGED -> "➡️"
    }

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
            .testTag("live_rtp_floating_hud")
    ) {
        Card(
            modifier = Modifier
                .width(if (isExpanded) 300.dp else 210.dp)
                .border(
                    width = if (rtpStats.isSynced) 2.dp else 1.2.dp,
                    color = if (rtpStats.isSynced) unifiedSyncColor else rtpTrendColor,
                    shape = RoundedCornerShape(16.dp)
                ),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xEE0E1420) // High-contrast dark translucent glass canvas
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Header / Drag handle & Sync State Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DragHandle,
                            contentDescription = "Drag HUD",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            tint = if (rtpStats.isSynced) unifiedSyncColor else SlotGoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SMART BET ADVISOR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { isExpanded = !isExpanded },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Toggle expand",
                                tint = Color.LightGray
                            )
                        }
                        Spacer(modifier = Modifier.width(2.dp))
                        IconButton(
                            onClick = onCloseHud,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close HUD",
                                tint = Color.LightGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Prominent Live RTP & Short-Term Trend Card Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(rtpTrendColor.copy(alpha = 0.15f))
                        .border(
                            width = if (rtpStats.isSynced) 1.dp else 0.5.dp,
                            color = rtpTrendColor.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "LIVE RTP",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.LightGray
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "($trendArrowSymbol)",
                                fontSize = 11.sp,
                                color = rtpTrendColor
                            )
                        }
                        Text(
                            text = if (rtpStats.spinCount > 0) String.format("%.1f%%", liveRtp) else "-- %",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = rtpTrendColor,
                            modifier = Modifier.testTag("live_rtp_value")
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "5-SPIN TREND",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.LightGray
                        )
                        Text(
                            text = if (rtpStats.spinCount > 0) String.format("%.1f%%", shortTermRtp) else "--",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = rtpTrendColor
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Unified Sync Badge
                        if (rtpStats.isSynced) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(unifiedSyncColor)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "SYNCED ⚡",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF2A3447))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = when (rtpStats.recommendation) {
                                        BetRecommendation.INCREASE_BET -> "RAISE ⬆️"
                                        BetRecommendation.DECREASE_BET -> "LOWER ⬇️"
                                        BetRecommendation.HOLD_STABLE -> "HOLD ➡️"
                                    },
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                AnimatedVisibility(visible = isExpanded) {
                    Column(modifier = Modifier.padding(top = 10.dp)) {

                        // Advisor Banner Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (rtpStats.isSynced) unifiedSyncColor.copy(alpha = 0.2f) else Color(0xFF161E2E))
                                .border(
                                    width = 1.dp,
                                    color = if (rtpStats.isSynced) unifiedSyncColor else Color(0xFF2B3A52),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (rtpStats.isSynced) Icons.Default.Verified else Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = if (rtpStats.isSynced) unifiedSyncColor else SlotGoldPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = rtpStats.advisorMessage,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (rtpStats.isSynced) unifiedSyncColor else Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Current Bet Display with Direction Arrow & Unified Sync Glow
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF131B29))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Current Stake / Bet",
                                    fontSize = 9.sp,
                                    color = Color.Gray
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (rtpStats.lastSpinBet > 0) String.format("$%.2f", rtpStats.lastSpinBet) else "--",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = betIndicatorColor,
                                        modifier = Modifier.testTag("current_bet_value")
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = betArrowSymbol,
                                        fontSize = 12.sp,
                                        color = betIndicatorColor
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Total Session Win",
                                    fontSize = 9.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = String.format("$%.2f", rtpStats.totalWin),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SlotEmeraldGreen,
                                    modifier = Modifier.testTag("total_win_value")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Stats Summary Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Spins Monitored: ${rtpStats.spinCount}",
                                fontSize = 10.sp,
                                color = SlotCyanAccent,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = "Total Bet: $${String.format("%.1f", rtpStats.totalBet)}",
                                fontSize = 9.sp,
                                color = Color.LightGray
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Actions: Trigger Spin Pulse & Reset
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = onSimulateSpin,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = SlotGoldPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Spin Pulse",
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = onResetSession,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Reset",
                                    fontSize = 10.sp,
                                    color = Color.LightGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

