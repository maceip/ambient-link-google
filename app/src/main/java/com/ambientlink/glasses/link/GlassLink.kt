package com.ambientlink.glasses.link

import kotlinx.coroutines.flow.StateFlow

/**
 * Copy of the canonical contract in ambient-link-core/contracts/GlassLink.kt.
 * Kept in-repo until the shared :core-android library lands (TODO(shared)).
 *
 * Shape extracted from the recovered Cosmo CosmoGlassManager (glasses_link.md).
 */
interface GlassLink {
    val connected: StateFlow<Boolean>
    val bound: StateFlow<Boolean>

    suspend fun bind()
    fun unbind()

    fun setupImageCapture(onFrame: (Frame) -> Unit)
    fun startImageCapture()
    fun stopImageCapture()

    fun startAudioCapture(onBytes: (ByteArray, Int) -> Unit)
    fun stopAudioCapture()

    fun clear()

    data class Frame(val width: Int, val height: Int, val pixels: ByteArray, val tsMillis: Long)

    companion object {
        /** Cosmo: FRAME_PROCESS_INTERVAL_MS = 10_000 ms, GLASS_CAMERA_TARGET_FPS = 0.1. */
        const val DEFAULT_FRAME_INTERVAL_MS: Long = 10_000
    }
}
