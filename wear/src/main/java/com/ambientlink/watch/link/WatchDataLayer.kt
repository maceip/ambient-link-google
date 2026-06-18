package com.ambientlink.watch.link

import com.ambientlink.core.TriggerType

/**
 * Phone <-> watch protocol surface, modeled on Cosmo's `/cosmowear/...` Wearable
 * Data Layer design (glasses_link.md, Link 2). Canonical spec:
 * ambient-link-core/contracts/wear-data-layer.md.
 *
 * Used for the PHONE-TETHERED path: control/state over messages, high-rate audio
 * over a dedicated channel with an explicit stop. The standalone path (see
 * WatchActivity) hits the relay directly over Wi-Fi/LTE instead.
 *
 * Paths + status/trigger enums live in the shared library
 * (com.ambientlink.core.WearPaths / PhoneStatus / WatchStatus / TriggerType).
 * The real impl pulls in com.google.android.gms:play-services-wearable and wires
 * MessageClient / ChannelClient. See TODO(wear).
 */

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
