package com.example.sandbox

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

class RtpNotificationManager(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "rtp_smart_advisor_channel"
        private const val CHANNEL_NAME = "Smart Bet Advisor Status Bar Notifications"
        private const val NOTIFICATION_ID = 7771
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Displays live RTP % and Smart Bet Advisor status in the top status bar"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun updateNotification(rtpStats: RtpStats) {
        val trendArrow = when (rtpStats.recommendation) {
            BetRecommendation.INCREASE_BET -> "⬆️"
            BetRecommendation.DECREASE_BET -> "⬇️"
            BetRecommendation.HOLD_STABLE -> "➡️"
        }

        val betArrow = when (rtpStats.betChangeDirection) {
            BetChangeDirection.INCREASED -> "⬆️"
            BetChangeDirection.DECREASED -> "⬇️"
            BetChangeDirection.UNCHANGED -> "➡️"
        }

        val syncStatusText = if (rtpStats.isSynced) "⚡ PERFECT SYNC" else "ADVISING"
        val rtpStr = if (rtpStats.spinCount > 0) String.format("%.1f%%", rtpStats.liveRtpPercent) else "--%"
        val trendRtpStr = if (rtpStats.spinCount > 0) String.format("%.1f%%", rtpStats.shortTermRtpPercent) else "--%"

        val title = "RTP Advisor: Live $rtpStr $trendArrow | 5-Spin: $trendRtpStr"
        val contentText = "[$syncStatusText] Bet: $${String.format("%.1f", rtpStats.lastSpinBet)} $betArrow | ${rtpStats.advisorMessage}"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "$contentText\nTotal Bet: $${String.format("%.2f", rtpStats.totalBet)} | Win: $${String.format("%.2f", rtpStats.totalWin)}"
                )
            )
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)

        try {
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        } catch (e: Exception) {
            // Catch security exception if notification permission is denied on Android 13+
        }
    }

    fun cancelNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
