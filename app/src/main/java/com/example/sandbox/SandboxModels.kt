package com.example.sandbox

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LogLevel {
    INFO, DEBUG, WARN, ERROR, SYSCALL, HOOK, GPU, NET
}

data class SandboxLog(
    val timestamp: String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()),
    val category: String,
    val message: String,
    val level: LogLevel = LogLevel.INFO
)

data class ApkPackageInfo(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val apkSizeBytes: Long,
    val isRunningInSandbox: Boolean = false,
    val targetFps: Int = 120,
    val iconResId: Int? = null,
    val iconPath: String? = null,
    val apkPath: String? = null,
    val category: String = "Slot Game",
    val antiDetectEnabled: Boolean = true
)

data class AntiDetectionProfile(
    val imei: String = "867543029108234",
    val macAddress: String = "02:00:00:4A:8B:11",
    val androidId: String = "9774d56d682e549c",
    val deviceModel: String = "Samsung Galaxy S24 Ultra (SM-S928B)",
    val buildFingerprint: String = "samsung/e2s/e2s:14/UP1A.231005.007/S928BXXU1AXB5:user/release-keys",
    val batteryLevel: Int = 98,
    val batteryStatus: String = "Discharging (98% Physical)",
    val thermalTempCelsius: Float = 36.5f,
    val cpuArchitecture: String = "arm64-v8a (Qualcomm Snapdragon 8 Gen 3)",
    val isRootHidden: Boolean = true,
    val isVirtualBoxSpoofed: Boolean = true,
    val activeHooksCount: Int = 38
)

data class GpuEngineConfig(
    val refreshRateHz: Int = 120,
    val gpuApi: String = "Vulkan 1.3 / Direct Pass-Through",
    val lowLatencyNetEnabled: Boolean = true,
    val cpuAffinityCores: String = "Cores 4-7 (High Performance)",
    val renderBypassActive: Boolean = true
)

data class NetworkMetricsState(
    val pingMs: Int = 14,
    val activeTcpSockets: Int = 8,
    val activeUdpSockets: Int = 2,
    val primaryDns: String = "1.1.1.1 (Cloudflare)",
    val secondaryDns: String = "8.8.8.8 (Google)",
    val isLowLatencyActive: Boolean = true,
    val isKeepAliveRunning: Boolean = true,
    val totalInterceptedBytes: Long = 14285760L
)

