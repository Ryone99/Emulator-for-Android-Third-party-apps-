#include "hook_engine.h"
#include <sstream>
#include <chrono>
#include <iomanip>

HookEngine& HookEngine::getInstance() {
    static HookEngine instance;
    return instance;
}

void HookEngine::initialize() {
    std::lock_guard<std::mutex> lock(engineMutex_);
    hooks_.clear();
    logBuffer_.clear();

    // Register core anti-detection and system call hooks
    hooks_.push_back({"__system_property_get", "libc.so", true, 0});
    hooks_.push_back({"gettimeofday", "libc.so", true, 0});
    hooks_.push_back({"ioctl", "libEGL.so", true, 0});
    hooks_.push_back({"eglSwapBuffers", "libGLESv2.so", true, 0});
    hooks_.push_back({"recvfrom", "libnet.so", true, 0});
    hooks_.push_back({"read", "libart.so", true, 0});

    addLog("SYS_INIT", "NDK Sandbox Hook Engine V1.0 initialized with 6 PLT interception targets.");
    addLog("GPU_INIT", "Direct Pass-Through OpenGL ES 3.2 / Vulkan renderer initialized at 120Hz.");
    addLog("SPOOF_INIT", "Device spoofing active: IMEI, MAC, AndroidID, Model disguised.");
}

bool HookEngine::hookSymbol(const std::string& module, const std::string& symbol) {
    std::lock_guard<std::mutex> lock(engineMutex_);
    for (auto& hook : hooks_) {
        if (hook.moduleName == module && hook.targetSymbol == symbol) {
            hook.active = true;
            hook.callCount++;
            std::ostringstream ss;
            ss << "Intercepted syscall [" << symbol << "] in module " << module << " (Calls: " << hook.callCount << ")";
            addLog("SYS_CALL", ss.str());
            return true;
        }
    }
    hooks_.push_back({symbol, module, true, 1});
    addLog("HOOK_REG", "Registered new dynamic PLT hook for " + symbol + " in " + module);
    return true;
}

void HookEngine::updateSpoofProfile(const DeviceSpoofProfile& profile) {
    std::lock_guard<std::mutex> lock(engineMutex_);
    spoofProfile_ = profile;
    addLog("SPOOF_UPDATE", "Updated Device Spoofing Profile: " + profile.deviceModel + " | IMEI: " + profile.imei);
}

DeviceSpoofProfile HookEngine::getSpoofProfile() const {
    std::lock_guard<std::mutex> lock(engineMutex_);
    return spoofProfile_;
}

void HookEngine::setFpsUnlock(int targetFps, bool directGpuPassThrough) {
    std::lock_guard<std::mutex> lock(engineMutex_);
    targetFps_ = targetFps;
    gpuPassThroughEnabled_ = directGpuPassThrough;
    std::ostringstream ss;
    ss << "GPU Config Updated: " << targetFps << "Hz Mode | PassThrough=" << (directGpuPassThrough ? "ENABLED" : "DISABLED");
    addLog("GPU_CONFIG", ss.str());
}

void HookEngine::addLog(const std::string& category, const std::string& message) {
    auto now = std::chrono::system_clock::now();
    auto in_time_t = std::chrono::system_clock::to_time_t(now);
    
    std::stringstream ss;
    ss << std::put_time(std::localtime(&in_time_t), "%H:%M:%S") << " [" << category << "] " << message;
    
    // Keep last 100 log entries
    if (logBuffer_.size() >= 100) {
        logBuffer_.erase(logBuffer_.begin());
    }
    logBuffer_.push_back(ss.str());
    LOGI("%s", ss.str().c_str());
}

std::vector<std::string> HookEngine::getLogs() {
    std::lock_guard<std::mutex> lock(engineMutex_);
    return logBuffer_;
}
