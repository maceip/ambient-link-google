package com.ambientlink.watch.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Polls the Ambient Link relay for the current session list.
 *
 * Same relay the glasses + Meta web app read:  GET {base}/ambient-link/status
 * Response shape: { "sessions": [ { agent, cwd, state, session_id, ... } ] }
 *
 * On Wear the poll interval is longer than on glasses to protect battery; the
 * watch is a glance/alert surface, not a continuously-on HUD.
 *
 * TODO(shared): promote to a shared :core-android relay library (ambient-link-core)
 * so glasses + watch + phone all consume one implementation.
 */
class RelayClient(
    private val baseUrl: String,
    private val pollMs: Long = 15_000L,
) {
    private val _sessions = MutableStateFlow<List<Session>>(emptyList())
    val sessions: StateFlow<List<Session>> = _sessions.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var loop: Job? = null

    fun start() {
        if (loop?.isActive == true) return
        loop = scope.launch {
            while (isActive) {
                runCatching { fetchOnce() }.onSuccess { _sessions.value = it }
                delay(pollMs)
            }
        }
    }

    fun refreshNow() {
        scope.launch { runCatching { fetchOnce() }.onSuccess { _sessions.value = it } }
    }

    fun stop() {
        loop?.cancel()
        loop = null
    }

    private fun fetchOnce(): List<Session> {
        val url = URL("$baseUrl/ambient-link/status")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8_000
            readTimeout = 8_000
        }
        try {
            if (conn.responseCode != 200) return _sessions.value
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            return parse(body)
        } finally {
            conn.disconnect()
        }
    }

    private fun parse(body: String): List<Session> {
        val arr = JSONObject(body).optJSONArray("sessions") ?: return emptyList()
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Session(
                sessionId = o.optString("session_id"),
                agent = o.optString("agent", "agent"),
                cwd = o.optString("cwd"),
                state = o.optString("state", "IDLE"),
                preview = o.optString("preview", ""),
            )
        }
    }
}
