package com.ambientlink.watch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ambientlink.watch.BuildConfig
import com.ambientlink.watch.data.RelayClient
import com.ambientlink.watch.ui.SessionListScreen

/**
 * Wear OS entry point. Standalone (works without the paired phone) — it polls
 * the Ambient Link relay directly over the watch's own network.
 *
 * TODO(wear): for phone-tethered watches, add a Wearable Data Layer path so the
 * watch can proxy through the phone app instead of holding its own connection;
 * gate on connectivity and prefer the cheaper transport.
 */
class WatchActivity : ComponentActivity() {

    private val relay by lazy { RelayClient(BuildConfig.AMBIENT_LINK_RELAY) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val sessions by relay.sessions.collectAsStateWithLifecycle()
            SessionListScreen(
                sessions = sessions.filter { it.isLive },
                onSelect = {
                    // TODO(wear): open a detail screen / send a quick reply via the
                    // relay input endpoint, or hand off to the phone app.
                    relay.refreshNow()
                },
            )
        }
    }

    override fun onStart() {
        super.onStart()
        relay.start()
    }

    override fun onStop() {
        relay.stop()
        super.onStop()
    }
}
