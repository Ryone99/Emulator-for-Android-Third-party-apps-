#ifndef NETWORK_INTERCEPTOR_H
#define NETWORK_INTERCEPTOR_H

#include <string>
#include <vector>
#include <mutex>

struct NetworkInterceptorStats {
    int pingLatencyMs;
    int activeTcpSockets;
    int activeUdpSockets;
    std::string primaryDns;
    std::string secondaryDns;
    bool isLowLatencyActive;
    uint64_t totalBytesIntercepted;
};

class NetworkInterceptor {
public:
    static NetworkInterceptor& getInstance();

    void initialize();
    void setCustomDns(const std::string& primary, const std::string& secondary, bool lowLatencyMode);
    void simulateTraffic(int activeSocketsDelta, int pingMs);
    NetworkInterceptorStats getStats() const;

    bool interceptSocketConnect(int socketFd, const char* remoteIp, int port);
    std::string resolveDomainWithCustomDns(const char* domainName);

private:
    NetworkInterceptor() = default;
    ~NetworkInterceptor() = default;

    mutable std::mutex netMutex_;
    std::string primaryDns_ = "1.1.1.1";
    std::string secondaryDns_ = "8.8.8.8";
    bool lowLatencyActive_ = true;
    int pingLatencyMs_ = 14;
    int activeTcpSockets_ = 6;
    int activeUdpSockets_ = 2;
    uint64_t totalBytes_ = 10485760;
};

#endif // NETWORK_INTERCEPTOR_H
