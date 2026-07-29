#ifndef SPOOF_ENGINE_H
#define SPOOF_ENGINE_H

#include <string>
#include <vector>
#include <mutex>
#include <unordered_map>

struct NativeSpoofProfile {
    std::string deviceModel;
    std::string imei;
    std::string macAddress;
    std::string androidId;
    std::string buildFingerprint;
    std::string cpuAbi;
    int batteryLevel;
    float cpuTempCelsius;
    bool hideRoot;
    bool hideEmulator;
    int activeHooksCount;
};

class SpoofEngine {
public:
    static SpoofEngine& getInstance();

    void initialize();
    void updateProfile(const NativeSpoofProfile& profile);
    NativeSpoofProfile getProfile() const;

    int interceptSystemPropertyGet(const char* name, char* value);
    bool isPathBlocked(const char* path);
    void getSpoofedBatteryInfo(int* level, int* status, float* temp);

private:
    SpoofEngine() = default;
    ~SpoofEngine() = default;

    mutable std::mutex spoofMutex_;
    NativeSpoofProfile currentProfile_{
        "Samsung Galaxy S24 Ultra (SM-S928B)",
        "867543029108234",
        "02:00:00:4A:8B:11",
        "9774d56d682e549c",
        "google/raven/raven:14/UP1A.231105.003/11018593:user/release-keys",
        "arm64-v8a",
        98,
        36.5f,
        true,
        true,
        38
    };

    std::unordered_map<std::string, std::string> propertyOverrides_;
    std::vector<std::string> blockedPathPatterns_;
};

#endif // SPOOF_ENGINE_H
