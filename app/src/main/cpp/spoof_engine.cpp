#include "spoof_engine.h"
#include "hook_engine.h"
#include <android/log.h>
#include <cstring>
#include <algorithm>

SpoofEngine& SpoofEngine::getInstance() {
    static SpoofEngine instance;
    return instance;
}

void SpoofEngine::initialize() {
    std::lock_guard<std::mutex> lock(spoofMutex_);
    
    blockedPathPatterns_ = {
        "/system/xbin/su",
        "/system/bin/su",
        "/sbin/su",
        "/system/app/Superuser.apk",
        "/data/adb/magisk",
        "/data/local/tmp/frida",
        "/dev/socket/qemud",
        "/sys/qemu_trace",
        "/system/lib/libc_malloc_debug_qemu.so",
        "/sys/module/vboxguest",
        "/dev/vboxuser",
        "/system/etc/init.goldfish.rc"
    };

    updateProfile(currentProfile_);
    HookEngine::getInstance().addLog("SPOOF_NDK", "Native Spoofing Engine bound to __system_property_get & access() syscalls.");
}

void SpoofEngine::updateProfile(const NativeSpoofProfile& profile) {
    std::lock_guard<std::mutex> lock(spoofMutex_);
    currentProfile_ = profile;

    propertyOverrides_["ro.product.model"] = profile.deviceModel;
    propertyOverrides_["ro.product.brand"] = "samsung";
    propertyOverrides_["ro.product.manufacturer"] = "samsung";
    propertyOverrides_["ro.product.name"] = "q2q";
    propertyOverrides_["ro.build.fingerprint"] = profile.buildFingerprint;
    propertyOverrides_["ro.build.tags"] = "release-keys";
    propertyOverrides_["ro.build.type"] = "user";
    propertyOverrides_["ro.debuggable"] = "0";
    propertyOverrides_["ro.secure"] = "1";
    propertyOverrides_["ro.kernel.qemu"] = "0";
    propertyOverrides_["ro.hardware"] = "qcom";
    propertyOverrides_["ro.product.cpu.abi"] = profile.cpuAbi;

    HookEngine::getInstance().addLog("SPOOF_IDENTITY", "Spoofed Identity Active: " + profile.deviceModel + " [MAC: " + profile.macAddress + " | IMEI: " + profile.imei + "]");
}

NativeSpoofProfile SpoofEngine::getProfile() const {
    std::lock_guard<std::mutex> lock(spoofMutex_);
    return currentProfile_;
}

int SpoofEngine::interceptSystemPropertyGet(const char* name, char* value) {
    std::lock_guard<std::mutex> lock(spoofMutex_);
    if (!name || !value) return 0;

    std::string propName(name);
    auto it = propertyOverrides_.find(propName);
    if (it != propertyOverrides_.end()) {
        std::strncpy(value, it->second.c_str(), 91);
        value[91] = '\0';
        LOGD("Intercepted __system_property_get(%s) -> Spoofed: %s", name, value);
        return (int)it->second.length();
    }
    return 0;
}

bool SpoofEngine::isPathBlocked(const char* path) {
    if (!path) return false;
    std::lock_guard<std::mutex> lock(spoofMutex_);
    if (!currentProfile_.hideRoot && !currentProfile_.hideEmulator) {
        return false;
    }

    std::string pathStr(path);
    for (const auto& pattern : blockedPathPatterns_) {
        if (pathStr.find(pattern) != std::string::npos) {
            LOGI("Intercepted file access to %s -> Blocked/Hidden for anti-detection", path);
            HookEngine::getInstance().addLog("ANTI_DETECT", "Blocked root/emulator probe on path: " + pathStr);
            return true;
        }
    }
    return false;
}

void SpoofEngine::getSpoofedBatteryInfo(int* level, int* status, float* temp) {
    std::lock_guard<std::mutex> lock(spoofMutex_);
    if (level) *level = currentProfile_.batteryLevel;
    if (status) *status = 2;
    if (temp) *temp = currentProfile_.cpuTempCelsius;
}
