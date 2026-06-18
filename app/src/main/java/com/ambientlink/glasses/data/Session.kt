package com.ambientlink.glasses.data

/** A single coding-agent session as reported by the Ambient Link relay. */
data class Session(
    val sessionId: String,
    val agent: String,      // "cursor" | "claude" | "codex"
    val cwd: String,
    val state: String,      // "BUSY" | "IDLE" | "DEAD"
    val preview: String = "",
) {
    val isLive: Boolean get() = state != "DEAD"
    val label: String get() = "$agent: $cwd"
}
