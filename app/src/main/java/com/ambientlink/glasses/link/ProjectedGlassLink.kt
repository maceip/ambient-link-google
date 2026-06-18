package com.ambientlink.glasses.link

import android.content.Context
import android.graphics.Bitmap
import com.ambientlink.core.EphemeralBuffer
import com.ambientlink.core.GlassLink
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Android XR projected-glasses implementation of [GlassLink].
 *
 * This is the reimplementation target from glasses_link.md §"Practical
 * Reimplementation Targets", modeled on Cosmo's CosmoGlassManager. The vendor
 * transport sits below `ProjectedGlassCaptureService`; this class only owns the
 * stable boundary above it: reactive gates, idempotent binding, throttled frames
 * into an ephemeral buffer, and audio passthrough to the shared STT sink.
 *
 * TODO(xr): wire the bracketed calls to the real Jetpack XR / Google Glasses SDK:
 *   - androidx.xr.projected.ProjectedContext.isProjectedDeviceConnected(ctx)
 *   - androidx.xr.projected.ProjectedContext.createProjectedActivityOptions(ctx)
 *   - bindService(ProjectedGlassCaptureService) -> LocalBinder.getService()
 *   - service.setupImageCapture / startImageCapture / stopImageCapture
 *   - service.startAudioCapture(audioConfig, listener) / stopAudioCapture
 */
class ProjectedGlassLink(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    private val frameIntervalMs: Long = GlassLink.DEFAULT_FRAME_INTERVAL_MS,
    /** Per-link settings gate (Cosmo: isConnectToGlassEnabled). */
    private val isLinkEnabled: () -> Boolean = { true },
) : GlassLink {

    private val _connected = MutableStateFlow(false)
    override val connected: StateFlow<Boolean> = _connected.asStateFlow()

    private val _bound = MutableStateFlow(false)
    override val bound: StateFlow<Boolean> = _bound.asStateFlow()

    /** Mirrors @GlassesCameraEphemeralBuffer InMemoryEphemeralBuffer<Bitmap>. */
    val cameraBuffer = EphemeralBuffer<Bitmap>(ttlMillis = 60_000)

    private val bindMutex = Mutex()
    private var bindJob: Job? = null

    private var frameSink: ((GlassLink.Frame) -> Unit)? = null
    private var lastFrameAt = 0L
    private var imageCaptureOn = false
    private var audioSink: ((ByteArray, Int) -> Unit)? = null

    /** Idempotent: no-op if already bound or a bind is in flight (Cosmo guard). */
    override suspend fun bind() {
        if (!isLinkEnabled()) return
        if (_bound.value) return
        bindMutex.withLock {
            if (_bound.value || bindJob?.isActive == true) return
            // TODO(xr): _connected.value = ProjectedContext.isProjectedDeviceConnected(context)
            if (!_connected.value) return
            // TODO(xr): launch projected CosmoGlassActivity with createProjectedActivityOptions,
            //           bindService(ProjectedGlassCaptureService), await LocalBinder, then:
            _bound.value = true
        }
    }

    override fun unbind() {
        // TODO(xr): context.unbindService(connection); stop projected activity (finish_activity=true)
        _bound.value = false
        bindJob?.cancel()
        bindJob = null
    }

    override fun setupImageCapture(onFrame: (GlassLink.Frame) -> Unit) {
        frameSink = onFrame
        // TODO(xr): service.setupImageCapture { proxy -> onProxy(proxy) }
    }

    override fun startImageCapture() {
        imageCaptureOn = true
        // TODO(xr): service.startImageCapture()
    }

    override fun stopImageCapture() {
        imageCaptureOn = false
        // TODO(xr): service.stopImageCapture()
    }

    /**
     * Frame ingress from the capture service. Throttled to one frame per
     * [frameIntervalMs] (Cosmo: 10s / 0.1 fps) and parked in the ephemeral buffer
     * before fan-out. Call this from the real ImageProxy callback.
     */
    fun onCameraBitmap(bitmap: Bitmap, tsMillis: Long = System.currentTimeMillis()) {
        if (!imageCaptureOn) return
        if (tsMillis - lastFrameAt < frameIntervalMs) return // drop; matches FRAME_PROCESS_INTERVAL_MS
        lastFrameAt = tsMillis
        cameraBuffer.add(bitmap, tsMillis)
        frameSink?.invoke(
            GlassLink.Frame(bitmap.width, bitmap.height, ByteArray(0), tsMillis)
        )
    }

    override fun startAudioCapture(onBytes: (ByteArray, Int) -> Unit) {
        audioSink = onBytes
        // TODO(xr): service.startAudioCapture(audioConfig) { bytes, len -> onGlassAudioData(bytes, len) }
    }

    override fun stopAudioCapture() {
        audioSink = null
        // TODO(xr): service.stopAudioCapture()
    }

    /** Audio ingress from the capture service -> shared STT sink (Cosmo: onGlassAudioData). */
    fun onGlassAudioData(bytes: ByteArray, len: Int) {
        audioSink?.invoke(bytes, len)
    }

    override fun clear() {
        cameraBuffer.clear()
        lastFrameAt = 0L
    }
}
