package com.ambientlink.watch.data

/**
 * A single coding-agent session as reported by the Ambient Link relay.
 *
 * TODO(shared): this mirrors com.ambientlink.glasses.data.Session. When the
 * relay client is promoted to a shared :core-android library (see
 * ambient-link-core), delete this copy and depend on the shared model.
 */
data class Session(
    val sessionId: String,
    val agent: String,      // "cursor" | "claude" | "codex"
    val cwd: String,
    val state: String,      // "BUSY" | "IDLE" | "DEAD"
    val preview: String = "",
) {
    val isLive: Boolean get() = state != "DEAD"
    val shortCwd: String get() = cwd.substringAfterLast('/').ifBlank { cwd }
}
