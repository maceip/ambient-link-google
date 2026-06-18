package com.ambientlink.watch.link

/**
 * Phone <-> watch protocol surface, modeled on Cosmo's `/cosmowear/...` Wearable
 * Data Layer design (glasses_link.md, Link 2). Canonical spec:
 * ambient-link-core/contracts/wear-data-layer.md.
 *
 * Used for the PHONE-TETHERED path: control/state over messages, high-rate audio
 * over a dedicated channel with an explicit stop. The standalone path (see
 * WatchActivity) hits the relay directly over Wi-Fi/LTE instead.
 *
 * Dependency-free on purpose: the real impl pulls in
 * com.google.android.gms:play-services-wearable and wires MessageClient /
 * ChannelClient. See TODO(wear).
 */
object AmbientLinkPaths {
    const val SESSIONS = "/ambientlink/sessions"             // phone -> watch (MessageClient)
    const val STATUS = "/ambientlink/status"                 // both (MessageClient)
    const val REPLY = "/ambientlink/reply"                   // watch -> phone (MessageClient)
    const val TRIGGER = "/ambientlink/trigger"               // watch -> phone (MessageClient)
    const val MIC_STREAM = "/ambientlink/mic_stream"         // watch -> phone (ChannelClient)
    const val MIC_STREAM_STOP = "/ambientlink/mic_stream_stop" // watch -> phone (MessageClient)
}

/** Mirrors Cosmo CosmoPhoneStatus.Status (trimmed). */
enum class PhoneStatus { OFF, IDLE, LISTENING, PROCESSING, RESPONDING }

/** Mirrors Cosmo CosmoWatchStatus.Status (trimmed). */
enum class WatchStatus { OFF, STREAMING_AUDIO, DISCONNECTED }

/** Watch -> phone trigger types (mirrors Cosmo CosmoTrigger.ActionType intent). */
enum class TriggerType { NUDGE, OPEN, DICTATE_START, DICTATE_STOP }

/**
 * Watch-side data-layer client. Implement against the Wearable Data Layer when the
 * phone is reachable; fall back to the relay otherwise.
 *
 * TODO(wear): real impl uses
 *   Wearable.getMessageClient(context).sendMessage(nodeId, path, bytes)
 *   Wearable.getChannelClient(context).openChannel(nodeId, MIC_STREAM)  // audio
 * and stops the channel by sending MIC_STREAM_STOP (mirrors /cosmowear/mic_stream_stop).
 */
interface WatchDataLayer {
    /** Quick reply routed to a session via the phone -> relay ingest. */
    fun sendReply(sessionId: String, text: String)

    fun sendTrigger(type: TriggerType, sessionId: String? = null)

    /** Open the mic channel and stream wrist audio to the phone's shared STT sink. */
    fun startMicStream()

    /** Send MIC_STREAM_STOP and close the channel. Always pair with startMicStream(). */
    fun stopMicStream()
}
