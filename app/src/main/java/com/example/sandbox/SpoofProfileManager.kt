package com.example.sandbox

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

data class DeviceArchetype(
    val model: String,
    val brand: String,
    val cpuArch: String,
    val fingerprint: String
)

class SpoofProfileManager(private val engine: SandboxEngine) {

    companion object {
        private const val TAG = "SpoofProfileManager"

        private val DEVICE_ARCHETYPES = listOf(
            DeviceArchetype(
                model = "Samsung Galaxy S24 Ultra (SM-S928B)",
                brand = "Samsung",
                cpuArch = "arm64-v8a (Qualcomm Snapdragon 8 Gen 3)",
                fingerprint = "samsung/e2s/e2s:14/UP1A.231005.007/S928BXXU1AXB5:user/release-keys"
            ),
            DeviceArchetype(
                model = "Google Pixel 8 Pro (GC3VE)",
                brand = "Google",
                cpuArch = "arm64-v8a (Google Tensor G3)",
                fingerprint = "google/husky/husky:14/UD1A.230803.041/10812391:user/release-keys"
            ),
            DeviceArchetype(
                model = "OnePlus 12 (CPH2581)",
                brand = "OnePlus",
                cpuArch = "arm64-v8a (Qualcomm Snapdragon 8 Gen 3)",
                fingerprint = "oneplus/CPH2581/OP592DL1:14/UKQ1.230924.001/17012391:user/release-keys"
            ),
            DeviceArchetype(
                model = "Xiaomi 14 Pro (23116PN5BC)",
                brand = "Xiaomi",
                cpuArch = "arm64-v8a (Qualcomm Snapdragon 8 Gen 3)",
                fingerprint = "xiaomi/shen農/shennong:14/UKQ1.230804.001/V816.0.4.0:user/release-keys"
            ),
            DeviceArchetype(
                model = "Asus ROG Phone 8 Pro (AI2401)",
                brand = "Asus",
                cpuArch = "arm64-v8a (Qualcomm Snapdragon 8 Gen 3 Overclocked)",
                fingerprint = "asus/WW_AI2401/ASUS_AI2401:14/UKQ1.230917.001/34.1410.1410.51:user/release-keys"
            )
        )
    }

    private val _currentProfile = MutableStateFlow(AntiDetectionProfile())
    val currentProfile: StateFlow<AntiDetectionProfile> = _currentProfile.asStateFlow()

    fun updateProfile(newProfile: AntiDetectionProfile) {
        _currentProfile.value = newProfile
        applyToNativeBridge(newProfile)

        engine.addLog(
            "SPOOF_UPDATE",
            "Hardware Profile Applied: ${newProfile.deviceModel} | IMEI: ${newProfile.imei} | MAC: ${newProfile.macAddress}",
            LogLevel.HOOK
        )
    }

    fun generateRandomProfile(): AntiDetectionProfile {
        val archetype = DEVICE_ARCHETYPES.random()
        val randomImei = generateRandomImei()
        val randomMac = generateRandomMac()
        val randomAndroidId = generateRandomAndroidId()
        val randomBatteryLevel = Random.nextInt(88, 100)
        val randomTemp = (340 + Random.nextInt(0, 45)) / 10.0f
        val hooksCount = Random.nextInt(36, 44)

        val newProfile = AntiDetectionProfile(
            imei = randomImei,
            macAddress = randomMac,
            androidId = randomAndroidId,
            deviceModel = archetype.model,
            buildFingerprint = archetype.fingerprint,
            batteryLevel = randomBatteryLevel,
            batteryStatus = "Discharging (${randomBatteryLevel}% Physical Battery)",
            thermalTempCelsius = randomTemp,
            cpuArchitecture = archetype.cpuArch,
            isRootHidden = true,
            isVirtualBoxSpoofed = true,
            activeHooksCount = hooksCount
        )

        _currentProfile.value = newProfile
        applyToNativeBridge(newProfile)

        engine.addLog(
            "SPOOF_RANDOM",
            "Generated New Device Identity -> ${newProfile.deviceModel} | AndroidID: ${newProfile.androidId}",
            LogLevel.HOOK
        )
        engine.addLog(
            "ANTI_EMULATOR",
            "Hardware Sensors Spoofed: Battery ${randomBatteryLevel}% | Temp ${randomTemp}°C Nominal | CPU ${archetype.cpuArch}",
            LogLevel.HOOK
        )

        return newProfile
    }

    private fun applyToNativeBridge(profile: AntiDetectionProfile) {
        if (SandboxNativeBridge.isNativeLoaded) {
            try {
                val bridge = SandboxNativeBridge()
                bridge.spoofDeviceIdentifier(
                    imei = profile.imei,
                    mac = profile.macAddress,
                    androidId = profile.androidId,
                    model = profile.deviceModel,
                    hideRoot = profile.isRootHidden
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error invoking NDK spoofDeviceIdentifier", e)
            }
        }
    }

    private fun generateRandomImei(): String {
        // TAC prefix for Samsung/Google flagship + random serial + Luhn
        val prefix = "86" + Random.nextInt(100000, 999999).toString()
        val serial = Random.nextInt(100000, 999999).toString()
        val base = prefix + serial
        return base + calculateLuhnCheckDigit(base)
    }

    private fun calculateLuhnCheckDigit(number: String): Int {
        var sum = 0
        var alternate = true
        for (i in number.length - 1 downTo 0) {
            var n = number[i] - '0'
            if (alternate) {
                n *= 2
                if (n > 9) n -= 9
            }
            sum += n
            alternate = !alternate
        }
        return (10 - (sum % 10)) % 10
    }

    private fun generateRandomMac(): String {
        val bytes = ByteArray(4)
        Random.nextBytes(bytes)
        val b1 = String.format("%02X", bytes[0])
        val b2 = String.format("%02X", bytes[1])
        val b3 = String.format("%02X", bytes[2])
        val b4 = String.format("%02X", bytes[3])
        return "02:00:00:$b1:$b2:$b3"
    }

    private fun generateRandomAndroidId(): String {
        val chars = "0123456789abcdef"
        return (1..16).map { chars.random() }.joinToString("")
    }
}
