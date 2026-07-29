package com.example

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sandbox.SandboxEngine
import com.example.sandbox.SandboxNativeBridge
import com.example.ui.components.AntiDetectPanel
import com.example.ui.components.ApkManagerSection
import com.example.ui.components.EngineMetricsHeader
import com.example.ui.components.EngineSettingsModal
import com.example.ui.components.LogConsoleSection
import com.example.ui.components.TopStatusBarRtpTicker
import com.example.ui.theme.SlotCyanAccent
import com.example.ui.theme.SlotEngineTheme
import com.example.ui.theme.SlotGoldPrimary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SlotEngineTheme {
                SlotEngineMainApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotEngineMainApp() {
    val context = LocalContext.current
    val engine = remember { SandboxEngine.getInstance(context) }

    val logs by engine.logs.collectAsState()
    val installedApks by engine.installedApks.collectAsState()
    val spoofProfile by engine.spoofProfile.collectAsState()
    val gpuConfig by engine.gpuConfig.collectAsState()
    val netMetrics by engine.networkMetrics.collectAsState()
    val rtpStats by engine.rtpStats.collectAsState()

    var showSettingsModal by remember { mutableStateOf(false) }

    val apkPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            engine.installApkFromUri(it, context)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SlotGoldPrimary.copy(alpha = 0.2f))
                                .border(1.dp, SlotGoldPrimary, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Casino,
                                contentDescription = null,
                                tint = SlotGoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            // TITLE EXPLICITLY REQUESTED BY PROMPT: "Slot Game Emulator Engine"
                            Text(
                                text = "Slot Game Emulator Engine",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.testTag("app_title")
                            )
                            Text(
                                text = "High-Performance Container Sandbox",
                                fontSize = 10.sp,
                                color = SlotCyanAccent
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        engine.rtpAnalyzer.toggleHudVisibility()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "Toggle Live RTP HUD",
                            tint = if (rtpStats.isHudVisible) SlotGoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = {
                        engine.interceptSysCall("libart.so", "RuntimeHookCheck")
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Trigger Sycall Intercept",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { showSettingsModal = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Engine Settings",
                            tint = SlotGoldPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (rtpStats.isHudVisible) {
                TopStatusBarRtpTicker(
                    rtpStats = rtpStats,
                    onSimulateSpin = { engine.rtpAnalyzer.simulateSpinPulse() },
                    onResetSession = { engine.rtpAnalyzer.resetSession() }
                )
            } else {
                Spacer(modifier = Modifier.height(2.dp))
            }

            // 1. Engine Hardware Metrics Header with Real-Time Network & GPU Metrics
            EngineMetricsHeader(
                fpsTarget = gpuConfig.refreshRateHz,
                isGpuPassThrough = gpuConfig.renderBypassActive,
                isNativeLoaded = SandboxNativeBridge.isNativeLoaded,
                activeHooksCount = spoofProfile.activeHooksCount,
                pingLatencyMs = netMetrics.pingMs,
                activeTcpSockets = netMetrics.activeTcpSockets,
                primaryDns = netMetrics.primaryDns,
                isLowLatencyActive = netMetrics.isLowLatencyActive
            )

            // 2. APK Manager Section with "Load Slot Game APK" Button & APK list
            ApkManagerSection(
                apks = installedApks,
                spoofProfile = spoofProfile,
                rtpStats = rtpStats,
                onLoadApkClick = {
                    apkPickerLauncher.launch("*/*")
                },
                onCustomApkLoaded = { appName, pkgName, sizeMb ->
                    engine.loadCustomApk(appName, pkgName, sizeMb)
                },
                onLaunchApk = { apk ->
                    engine.launchApkInSandbox(apk)
                },
                onStopApk = { pkgName ->
                    engine.stopApkInSandbox(pkgName)
                },
                onSimulateSpin = {
                    engine.rtpAnalyzer.simulateSpinPulse()
                },
                onResetRtpSession = {
                    engine.rtpAnalyzer.resetSession()
                },
                onToggleRtpHud = {
                    engine.rtpAnalyzer.toggleHudVisibility()
                }
            )

            // 3. Anti-Detection & Device Spoofing Panel
            AntiDetectPanel(
                profile = spoofProfile,
                onUpdateProfile = { newProfile ->
                    engine.updateSpoofProfile(newProfile)
                },
                onRandomizeIdentity = {
                    engine.generateRandomSpoofProfile()
                }
            )

            // 4. Sandbox Execution Console / Log Viewer
            LogConsoleSection(
                logs = logs,
                onClearLogs = { engine.clearLogs() }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showSettingsModal) {
        EngineSettingsModal(
            config = gpuConfig,
            netMetrics = netMetrics,
            onDismiss = { showSettingsModal = false },
            onSaveConfig = { fps, passThrough, primaryDns, ultraLowLatency ->
                engine.updateGpuConfig(fps, passThrough)
                engine.updateDnsRouting(primaryDns, "8.8.4.4", ultraLowLatency)
            }
        )
    }
}
