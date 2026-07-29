#include <jni.h>
#include <string>
#include <vector>
#include "hook_engine.h"
#include "network_interceptor.h"
#include "spoof_engine.h"

extern "C" JNIEXPORT void JNICALL
Java_com_example_sandbox_SandboxNativeBridge_initSandboxEngine(
        JNIEnv* env,
        jobject /* this */) {
    HookEngine::getInstance().initialize();
    NetworkInterceptor::getInstance().initialize();
    SpoofEngine::getInstance().initialize();
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_sandbox_SandboxNativeBridge_loadApkContainer(
        JNIEnv* env,
        jobject /* this */,
        jstring jApkPath,
        jstring jPackageName) {
    const char* apkPath = env->GetStringUTFChars(jApkPath, nullptr);
    const char* packageName = env->GetStringUTFChars(jPackageName, nullptr);

    std::string pathStr(apkPath);
    std::string pkgStr(packageName);

    HookEngine::getInstance().addLog("LXC_SANDBOX", "Isolated user-space container instantiated for APK: " + pkgStr);
    HookEngine::getInstance().addLog("APK_STAGING", "Parsed APK manifest & assets from path: " + pathStr);
    HookEngine::getInstance().hookSymbol("libart.so", "LoadNativeLibrary");
    HookEngine::getInstance().hookSymbol("libc.so", "__system_property_get");

    env->ReleaseStringUTFChars(jApkPath, apkPath);
    env->ReleaseStringUTFChars(jPackageName, packageName);

    return JNI_TRUE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_sandbox_SandboxNativeBridge_interceptSysCall(
        JNIEnv* env,
        jobject /* this */,
        jstring jModule,
        jstring jSyscall) {
    const char* moduleName = env->GetStringUTFChars(jModule, nullptr);
    const char* syscallName = env->GetStringUTFChars(jSyscall, nullptr);

    HookEngine::getInstance().hookSymbol(moduleName, syscallName);

    env->ReleaseStringUTFChars(jModule, moduleName);
    env->ReleaseStringUTFChars(jSyscall, syscallName);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_sandbox_SandboxNativeBridge_spoofDeviceIdentifier(
        JNIEnv* env,
        jobject /* this */,
        jstring jImei,
        jstring jMac,
        jstring jAndroidId,
        jstring jModel,
        jboolean jHideRoot) {
    const char* imei = env->GetStringUTFChars(jImei, nullptr);
    const char* mac = env->GetStringUTFChars(jMac, nullptr);
    const char* androidId = env->GetStringUTFChars(jAndroidId, nullptr);
    const char* model = env->GetStringUTFChars(jModel, nullptr);

    DeviceSpoofProfile profile;
    profile.imei = imei;
    profile.macAddress = mac;
    profile.androidId = androidId;
    profile.deviceModel = model;
    profile.isRootHidden = jHideRoot;

    HookEngine::getInstance().updateSpoofProfile(profile);

    NativeSpoofProfile nativeProfile;
    nativeProfile.deviceModel = model;
    nativeProfile.imei = imei;
    nativeProfile.macAddress = mac;
    nativeProfile.androidId = androidId;
    nativeProfile.hideRoot = jHideRoot;
    nativeProfile.hideEmulator = true;
    SpoofEngine::getInstance().updateProfile(nativeProfile);

    env->ReleaseStringUTFChars(jImei, imei);
    env->ReleaseStringUTFChars(jMac, mac);
    env->ReleaseStringUTFChars(jAndroidId, androidId);
    env->ReleaseStringUTFChars(jModel, model);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_sandbox_SandboxNativeBridge_setGpuPassThrough(
        JNIEnv* env,
        jobject /* this */,
        jint targetFps,
        jboolean enablePassThrough) {
    HookEngine::getInstance().setFpsUnlock(targetFps, enablePassThrough);
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_example_sandbox_SandboxNativeBridge_getEngineStatusLogs(
        JNIEnv* env,
        jobject /* this */) {
    std::vector<std::string> logs = HookEngine::getInstance().getLogs();

    jclass stringClass = env->FindClass("java/lang/String");
    jobjectArray result = env->NewObjectArray((jsize)logs.size(), stringClass, nullptr);

    for (size_t i = 0; i < logs.size(); i++) {
        jstring logStr = env->NewStringUTF(logs[i].c_str());
        env->SetObjectArrayElement(result, (jsize)i, logStr);
        env->DeleteLocalRef(logStr);
    }

    return result;
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_sandbox_SandboxNativeBridge_enforceCustomDns(
        JNIEnv* env,
        jobject /* this */,
        jstring jPrimary,
        jstring jSecondary,
        jboolean jLowLatencyMode) {
    const char* primary = env->GetStringUTFChars(jPrimary, nullptr);
    const char* secondary = env->GetStringUTFChars(jSecondary, nullptr);

    NetworkInterceptor::getInstance().setCustomDns(primary, secondary, jLowLatencyMode);

    env->ReleaseStringUTFChars(jPrimary, primary);
    env->ReleaseStringUTFChars(jSecondary, secondary);
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_example_sandbox_SandboxNativeBridge_getNetworkStats(
        JNIEnv* env,
        jobject /* this */) {
    NetworkInterceptorStats stats = NetworkInterceptor::getInstance().getStats();

    jclass stringClass = env->FindClass("java/lang/String");
    jobjectArray result = env->NewObjectArray(5, stringClass, nullptr);

    env->SetObjectArrayElement(result, 0, env->NewStringUTF(std::to_string(stats.pingLatencyMs).c_str()));
    env->SetObjectArrayElement(result, 1, env->NewStringUTF(std::to_string(stats.activeTcpSockets).c_str()));
    env->SetObjectArrayElement(result, 2, env->NewStringUTF(stats.primaryDns.c_str()));
    env->SetObjectArrayElement(result, 3, env->NewStringUTF(stats.isLowLatencyActive ? "1" : "0"));
    env->SetObjectArrayElement(result, 4, env->NewStringUTF(std::to_string(stats.totalBytesIntercepted).c_str()));

    return result;
}

