#ifndef HOOK_ENGINE_H
#define HOOK_ENGINE_H

#include <string>
#include <vector>
#include <mutex>
#include <android/log.h>

#define LOG_TAG "SlotSandboxNDK"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

struct HookEntry {
    std::string targetSymbol;
    std::string moduleName;
    bool active;
    uint32_t callCount;
};

struct DeviceSpoofProfile {
    std::string imei;
    std::string macAddress;
    std::string androidId;
    std::string deviceModel;
    std::string buildFingerprint;
    bool isRootHidden;
};

class HookEngine {
public:
    static HookEngine& getInstance();

    void initialize();
    bool hookSymbol(const std::string& module, const std::string& symbol);
    void updateSpoofProfile(const DeviceSpoofProfile& profile);
    DeviceSpoofProfile getSpoofProfile() const;

    void setFpsUnlock(int targetFps, bool directGpuPassThrough);
    int getTargetFps() const { return targetFps_; }
    bool isGpuPassThroughEnabled() const { return gpuPassThroughEnabled_; }

    void addLog(const std::string& category, const std::string& message);
    std::vector<std::string> getLogs();

    const std::vector<HookEntry>& getActiveHooks() const { return hooks_; }

private:
    HookEngine() = default;
    ~HookEngine() = default;

    mutable std::mutex engineMutex_;
    DeviceSpoofProfile spoofProfile_{
        "867543029108234",
        "02:00:00:4A:8B:11",
        "9774d56d682e549c",
        "Samsung Galaxy S24 Ultra (Sandbox Virtual)",
        "google/raven/raven:14/UP1A.231105.003/11018593:user/release-keys",
        true
    };
    std::vector<HookEntry> hooks_;
    std::vector<std::string> logBuffer_;
    int targetFps_ = 120;
    bool gpuPassThroughEnabled_ = true;
};

#endif // HOOK_ENGINE_H
