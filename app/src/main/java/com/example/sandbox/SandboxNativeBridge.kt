package com.example.sandbox

import android.util.Log

class SandboxNativeBridge {

    companion object {
        private const val TAG = "SandboxNativeBridge"
        var isNativeLoaded = false
            private set

        init {
            try {
                System.loadLibrary("sandbox_core")
                isNativeLoaded = true
                Log.i(TAG, "Native C++ sandbox_core library successfully loaded.")
            } catch (e: UnsatisfiedLinkError) {
                isNativeLoaded = false
                Log.w(TAG, "Native library sandbox_core not found or failed to load. Falling back to Kotlin Virtual Engine simulation.", e)
            }
        }
    }

    // JNI Native methods
    external fun initSandboxEngine()
    external fun loadApkContainer(apkPath: String, packageName: String): Boolean
    external fun interceptSysCall(module: String, syscall: String)
    external fun spoofDeviceIdentifier(imei: String, mac: String, androidId: String, model: String, hideRoot: Boolean)
    external fun setGpuPassThrough(targetFps: Int, enablePassThrough: Boolean)
    external fun enforceCustomDns(primary: String, secondary: String, lowLatencyMode: Boolean)
    external fun getNetworkStats(): Array<String>
    external fun getEngineStatusLogs(): Array<String>
}
