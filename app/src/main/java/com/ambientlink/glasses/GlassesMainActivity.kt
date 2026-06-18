package com.ambientlink.glasses

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ambientlink.core.RelayClient
import com.ambientlink.glasses.ui.HomeScreen

/**
 * Projected activity for Android XR display glasses.
 *
 * On Android XR, glasses apps are NOT separate APKs: this Activity runs inside the
 * phone app and is projected to the glasses because of the
 * `android:requiredDisplayCategory="...XR_PROJECTED"` attribute in the manifest.
 *
 * To launch it onto the glasses from elsewhere in the phone app, start it with a
 * projected context (see [launchOnGlasses]).
 */
class GlassesMainActivity : ComponentActivity() {

    private val relay by lazy { RelayClient(BuildConfig.AMBIENT_LINK_RELAY) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- Capability gate -------------------------------------------------
        // TODO(xr): once the Jetpack XR artifact is pinned, confirm the glasses
        // can render visual UI before drawing. Pseudocode against the real API:
        //
        //   val controller = ProjectedDeviceController.create(this)
        //   val canDraw = controller.capabilities.contains(CAPABILITY_VISUAL_UI)
        //   if (!canDraw) { /* audio-only: fall back to TTS summaries */ }
        //
        // We start polling regardless; HomeScreen only renders if there is a display.
        relay.start()

        setContent {
            val sessions by relay.sessions.collectAsState()
            // TODO(xr): wrap in GlimmerTheme { ... } and swap the foundation
            // composables in HomeScreen for Glimmer's Card/List for the
            // glasses-native look. Glimmer renders on a transparent canvas.
            HomeScreen(sessions = sessions, onRefresh = relay::refreshNow)
        }
    }

    override fun onDestroy() {
        relay.stop()
        super.onDestroy()
    }
}

/*
 * Launching the projected experience from a phone Activity/Service:
 *
 *   val options = ProjectedContext.createProjectedActivityOptions(context)
 *   startActivity(Intent(context, GlassesMainActivity::class.java), options.toBundle())
 *
 * `ProjectedContext` also exposes whether a projected device is connected:
 *
 *   if (ProjectedContext.isProjectedDeviceConnected(context)) { ... }
 *
 * TODO(xr): wire these once androidx.xr.projected is on the classpath.
 */
