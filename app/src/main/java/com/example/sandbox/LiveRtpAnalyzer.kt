package com.example.sandbox

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.random.Random

enum class BetRecommendation {
    INCREASE_BET, // ⬆️ Surging trend
    DECREASE_BET, // ⬇️ Crashing trend
    HOLD_STABLE   // ➡️ Neutral / Steady trend
}

enum class BetChangeDirection {
    INCREASED, // ⬆️
    DECREASED, // ⬇️
    UNCHANGED  // ➡️
}

data class SpinRecord(
    val spinId: Int,
    val betAmount: Double,
    val winAmount: Double,
    val timestampMs: Long = System.currentTimeMillis()
)

data class RtpStats(
    val totalBet: Double = 0.0,
    val totalWin: Double = 0.0,
    val spinCount: Int = 0,
    val liveRtpPercent: Double = 0.0,
    val shortTermRtpPercent: Double = 0.0,
    val isHudVisible: Boolean = true,
    val lastSpinBet: Double = 0.0,
    val previousSpinBet: Double = 0.0,
    val lastSpinWin: Double = 0.0,
    val recommendation: BetRecommendation = BetRecommendation.HOLD_STABLE,
    val betChangeDirection: BetChangeDirection = BetChangeDirection.UNCHANGED,
    val isSynced: Boolean = false,
    val advisorMessage: String = "Analyzing RTP Trend...",
    val recentSpins: List<SpinRecord> = emptyList()
)

class LiveRtpAnalyzer(private val engine: SandboxEngine) {

    companion object {
        private const val TAG = "LiveRtpAnalyzer"
    }

    private val notificationManager by lazy { RtpNotificationManager(engine.context) }
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _rtpStats = MutableStateFlow(RtpStats())
    val rtpStats: StateFlow<RtpStats> = _rtpStats.asStateFlow()

    fun toggleHudVisibility(visible: Boolean? = null) {
        val next = visible ?: !_rtpStats.value.isHudVisible
        _rtpStats.value = _rtpStats.value.copy(isHudVisible = next)
        engine.addLog("RTP_HUD", "Live Session RTP Floating Overlay state: ${if (next) "VISIBLE" else "HIDDEN"}", LogLevel.INFO)
    }

    /**
     * Inspects HTTP/WebSocket JSON payloads for bet and win fields.
     */
    fun parseNetworkPayload(jsonPayload: String) {
        try {
            val json = JSONObject(jsonPayload)
            var extractedBet: Double? = null
            var extractedWin: Double? = null

            // Outgoing spin request keys
            listOf("bet", "stake", "bet_amount", "amount", "total_bet").forEach { key ->
                if (json.has(key)) {
                    extractedBet = json.optDouble(key, 0.0)
                }
            }

            // Incoming win response keys
            listOf("win", "payout", "win_amount", "total_win", "reward").forEach { key ->
                if (json.has(key)) {
                    extractedWin = json.optDouble(key, 0.0)
                }
            }

            if (extractedBet != null || extractedWin != null) {
                recordSpin(
                    bet = extractedBet ?: 0.0,
                    win = extractedWin ?: 0.0,
                    source = "NETWORK_INTERCEPTOR"
                )
            }
        } catch (e: Exception) {
            // Not a JSON payload or missing fields
        }
    }

    /**
     * Updates totalBet, totalWin, spinCount, live RTP percentage, and Smart Bet Advisor trend.
     */
    fun recordSpin(bet: Double, win: Double, source: String = "GAME_PROCESS") {
        if (bet <= 0.0 && win <= 0.0) return

        val current = _rtpStats.value
        val newBet = current.totalBet + bet
        val newWin = current.totalWin + win
        val newCount = if (bet > 0) current.spinCount + 1 else current.spinCount

        val rtp = if (newBet > 0.0) (newWin / newBet) * 100.0 else 0.0

        val newRecord = SpinRecord(
            spinId = newCount,
            betAmount = bet,
            winAmount = win
        )

        val updatedRecent = (listOf(newRecord) + current.recentSpins).take(10)

        // 1. Short-term RTP calculation (last 5 spins)
        val last5Spins = updatedRecent.take(5)
        val shortTermBetSum = last5Spins.sumOf { it.betAmount }
        val shortTermWinSum = last5Spins.sumOf { it.winAmount }
        val shortTermRtp = if (shortTermBetSum > 0.0) (shortTermWinSum / shortTermBetSum) * 100.0 else rtp

        // 2. Action Recommendation Engine
        val recommendation = when {
            newCount < 2 -> BetRecommendation.HOLD_STABLE
            shortTermRtp >= 105.0 || (rtp >= 96.0 && shortTermRtp >= 98.0) -> BetRecommendation.INCREASE_BET
            shortTermRtp <= 85.0 || (rtp < 94.0 && shortTermRtp <= 90.0) -> BetRecommendation.DECREASE_BET
            else -> BetRecommendation.HOLD_STABLE
        }

        // 3. User actual bet change tracking
        val prevBet = current.lastSpinBet
        val currBet = if (bet > 0.0) bet else prevBet
        val betDir = when {
            prevBet <= 0.0 || currBet == prevBet -> BetChangeDirection.UNCHANGED
            currBet > prevBet -> BetChangeDirection.INCREASED
            else -> BetChangeDirection.DECREASED
        }

        // 4. Sync Validation Logic
        val isSynced = when {
            newCount < 2 -> false
            recommendation == BetRecommendation.INCREASE_BET && betDir == BetChangeDirection.INCREASED -> true
            recommendation == BetRecommendation.DECREASE_BET && betDir == BetChangeDirection.DECREASED -> true
            recommendation == BetRecommendation.HOLD_STABLE && betDir == BetChangeDirection.UNCHANGED -> true
            else -> false
        }

        // 5. Advisor Message
        val advisorMsg = when {
            newCount < 2 -> "Gathering initial spin telemetry..."
            isSynced && recommendation == BetRecommendation.INCREASE_BET -> "PERFECT SYNC: Bet raised on RTP surge! 🚀"
            isSynced && recommendation == BetRecommendation.DECREASE_BET -> "PERFECT SYNC: Stake reduced on RTP drop! 🛡️"
            isSynced -> "ALGORITHM SYNCED: Stake optimal for current RTP zone. ⚡"
            recommendation == BetRecommendation.INCREASE_BET -> "ADVISOR: RTP Surging (${String.format("%.1f", shortTermRtp)}%)! Increase Bet ⬆️"
            recommendation == BetRecommendation.DECREASE_BET -> "ADVISOR: RTP Dropping (${String.format("%.1f", shortTermRtp)}%)! Lower Bet ⬇️"
            else -> "ADVISOR: RTP Steady (${String.format("%.1f", shortTermRtp)}%). Maintain stake ➡️"
        }

        _rtpStats.value = current.copy(
            totalBet = newBet,
            totalWin = newWin,
            spinCount = newCount,
            liveRtpPercent = rtp,
            shortTermRtpPercent = shortTermRtp,
            previousSpinBet = prevBet,
            lastSpinBet = currBet,
            lastSpinWin = win,
            recommendation = recommendation,
            betChangeDirection = betDir,
            isSynced = isSynced,
            advisorMessage = advisorMsg,
            recentSpins = updatedRecent
        )

        try {
            notificationManager.updateNotification(_rtpStats.value)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating status bar notification: ${e.message}")
        }

        engine.addLog(
            "RTP_ADVISOR",
            "[$source] Spin #${newCount}: Bet=$${String.format("%.2f", currBet)} (${betDir}) | LiveRTP=${String.format("%.1f", rtp)}% | 5-SpinRTP=${String.format("%.1f", shortTermRtp)}% | Rec=${recommendation} | Synced=${isSynced}",
            LogLevel.NET
        )
    }

    /**
     * Generates simulated spin packet to test real-time RTP analysis and advisor sync.
     */
    fun simulateSpinPulse() {
        val current = _rtpStats.value
        val prevBet = if (current.lastSpinBet > 0) current.lastSpinBet else 10.0

        // Determine bet size based on recommendation or smart variation to test synced/unsynced state
        val bet = when (current.recommendation) {
            BetRecommendation.INCREASE_BET -> {
                if (Random.nextBoolean()) prevBet * 2.0 else prevBet // 50% chance to follow recommendation
            }
            BetRecommendation.DECREASE_BET -> {
                if (Random.nextBoolean()) (prevBet / 2.0).coerceAtLeast(2.0) else prevBet
            }
            BetRecommendation.HOLD_STABLE -> {
                listOf(2.0, 5.0, 10.0, 20.0, 50.0).random()
            }
        }

        // Win multiplier variation to simulate RTP trends
        val winMultiplier = when (Random.nextInt(100)) {
            in 0..35 -> 0.0 // Loss
            in 36..70 -> Random.nextDouble(0.2, 1.1) // Small payback
            in 71..88 -> Random.nextDouble(1.5, 5.0) // Big Win
            else -> Random.nextDouble(10.0, 30.0) // Jackpot Surging RTP
        }

        val win = (bet * winMultiplier)

        val simulatedJsonReq = """{"action":"spin","bet":$bet,"lines":25,"currency":"USD"}"""
        val simulatedJsonResp = """{"event":"spin_result","win":${String.format("%.2f", win)},"multiplier":${String.format("%.2f", winMultiplier)}}"""

        parseNetworkPayload(simulatedJsonReq)
        parseNetworkPayload(simulatedJsonResp)
    }

    fun resetSession() {
        _rtpStats.value = RtpStats(isHudVisible = _rtpStats.value.isHudVisible)
        engine.addLog("RTP_RESET", "Live Session RTP statistics reset to 0.", LogLevel.INFO)
    }
}

