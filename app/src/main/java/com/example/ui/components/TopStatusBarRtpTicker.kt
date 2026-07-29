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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sandbox.BetChangeDirection
import com.example.sandbox.BetRecommendation
import com.example.sandbox.RtpStats
import com.example.ui.theme.SlotCyanAccent
import com.example.ui.theme.SlotEmeraldGreen
import com.example.ui.theme.SlotGoldPrimary

@Composable
fun TopStatusBarRtpTicker(
    rtpStats: RtpStats,
    onSimulateSpin: () -> Unit,
    onResetSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    val liveRtp = rtpStats.liveRtpPercent
    val shortTermRtp = rtpStats.shortTermRtpPercent

    // Unified Sync Color when user bet matches recommendation
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

    // Top Status Bar Bar Container (Anchored at the top system area)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp)
            .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
            .background(Color(0xEE090D16)) // Ultra-dark translucent bar
            .border(
                width = if (rtpStats.isSynced) 1.5.dp else 0.8.dp,
                color = if (rtpStats.isSynced) unifiedSyncColor else rtpTrendColor.copy(alpha = 0.8f),
                shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
            )
            .padding(horizontal = 10.dp, vertical = 2.dp)
            .testTag("top_status_bar_rtp_ticker"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Status Bar Title & Live RTP % + Trend Arrow
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1.3f)
        ) {
            Icon(
                imageVector = if (rtpStats.isSynced) Icons.Default.Verified else Icons.Default.Analytics,
                contentDescription = null,
                tint = if (rtpStats.isSynced) unifiedSyncColor else rtpTrendColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (rtpStats.spinCount > 0) String.format("RTP: %.1f%%", liveRtp) else "RTP: --%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = rtpTrendColor,
                        modifier = Modifier.testTag("top_status_bar_rtp_value")
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = trendArrowSymbol,
                        fontSize = 10.sp
                    )
                }
                Text(
                    text = if (rtpStats.spinCount > 0) String.format("5-Spin: %.1f%%", shortTermRtp) else "Gathering Data",
                    fontSize = 8.sp,
                    color = Color.LightGray
                )
            }
        }

        // 2. Smart Bet Advisor Recommendation & Unified Sync Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (rtpStats.isSynced) unifiedSyncColor else Color(0xFF1E2838))
                .padding(horizontal = 8.dp, vertical = 3.dp)
                .testTag("status_bar_sync_badge")
        ) {
            Text(
                text = if (rtpStats.isSynced) {
                    "SYNCED ⚡"
                } else {
                    when (rtpStats.recommendation) {
                        BetRecommendation.INCREASE_BET -> "RAISE ⬆️"
                        BetRecommendation.DECREASE_BET -> "LOWER ⬇️"
                        BetRecommendation.HOLD_STABLE -> "STABLE ➡️"
                    }
                },
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = if (rtpStats.isSynced) Color.Black else Color.White
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // 3. Current Bet & Change Direction
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (rtpStats.lastSpinBet > 0) String.format("Bet: $%.1f", rtpStats.lastSpinBet) else "Bet: --",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = betIndicatorColor,
                        modifier = Modifier.testTag("top_status_bar_bet_value")
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = betArrowSymbol,
                        fontSize = 9.sp
                    )
                }
                Text(
                    text = String.format("Win: $%.1f", rtpStats.totalWin),
                    fontSize = 8.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // 4. Compact Status Bar Controls: Pulse Spin & Reset
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(SlotEmeraldGreen)
                    .clickable { onSimulateSpin() }
                    .padding(horizontal = 7.dp, vertical = 4.dp)
                    .testTag("status_bar_spin_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Simulate Spin",
                        tint = Color.Black,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "SPIN",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                }
            }

            IconButton(
                onClick = onResetSession,
                modifier = Modifier.size(22.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset RTP Session",
                    tint = Color.Gray,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}
