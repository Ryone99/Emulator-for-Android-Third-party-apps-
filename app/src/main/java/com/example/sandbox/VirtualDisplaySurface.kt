package com.example.sandbox

import android.util.Log

class VirtualDisplaySurface(private val engine: SandboxEngine) {

    companion object {
        private const val TAG = "VirtualDisplaySurface"
    }

    private var activePackage: String? = null
    private var isSurfaceBound = false
    private var targetFps = 120
    private var renderPassThrough = true

    fun bindSurface(packageName: String, fpsTarget: Int, passThrough: Boolean) {
        this.activePackage = packageName
        this.targetFps = fpsTarget
        this.renderPassThrough = passThrough
        this.isSurfaceBound = true

        engine.addLog(
            "GPU_SURFACE",
            "Bound VirtualDisplaySurface [1080x2400 @ ${fpsTarget}Hz] for $packageName (RenderBypass=$passThrough)",
            LogLevel.GPU
        )

        if (SandboxNativeBridge.isNativeLoaded) {
            try {
                val nativeBridge = SandboxNativeBridge()
                nativeBridge.setGpuPassThrough(fpsTarget, passThrough)
            } catch (e: Exception) {
                Log.e(TAG, "Native setGpuPassThrough error", e)
            }
        }
    }

    fun unbindSurface() {
        val pkg = activePackage
        activePackage = null
        isSurfaceBound = false

        if (pkg != null) {
            engine.addLog("GPU_SURFACE", "Unbound VirtualDisplaySurface for $pkg. EGL context released.", LogLevel.GPU)
        }
    }

    fun isBound(): Boolean = isSurfaceBound
    fun getActivePackage(): String? = activePackage
    fun getTargetFps(): Int = targetFps
}
