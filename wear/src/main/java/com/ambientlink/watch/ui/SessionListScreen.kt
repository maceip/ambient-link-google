package com.ambientlink.watch.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TitleCard
import com.ambientlink.watch.data.Session

/**
 * Wrist surface for Ambient Link: a glanceable, rotary-scrollable list of live
 * coding-agent sessions. The watch is the "tap me when an agent needs you"
 * companion to the glasses — short interactions, big targets, battery-light.
 *
 * Built on Wear Compose Material3 (ScalingLazyColumn gives the curved, edge-
 * scaling list Wear users expect; AppScaffold/ScreenScaffold add TimeText +
 * the scroll indicator).
 */
@Composable
fun SessionListScreen(
    sessions: List<Session>,
    onSelect: (Session) -> Unit,
) {
    AppScaffold {
        val listState = rememberScalingLazyListState()
        ScreenScaffold(scrollState = listState) { contentPadding ->
            ScalingLazyColumn(
                state = listState,
                contentPadding = contentPadding,
            ) {
                item { Text("sessions") }

                if (sessions.isEmpty()) {
                    item { Text("no live agents") }
                } else {
                    items(sessions) { s -> SessionCard(s, onClick = { onSelect(s) }) }
                }
            }
        }
    }
}

@Composable
private fun SessionCard(s: Session, onClick: () -> Unit) {
    TitleCard(
        onClick = onClick,
        title = { Text(s.shortCwd) },
        subtitle = {
            val sub = if (s.preview.isNotBlank()) s.preview else s.state.lowercase()
            Text(sub)
        },
        modifier = Modifier,
    ) {
        AgentDot(s.agent, live = s.isLive)
    }
}

@Composable
private fun AgentDot(agent: String, live: Boolean) {
    val color = agentColor(agent).copy(alpha = if (live) 1f else 0.4f)
    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
}

/** Match the per-agent accent colors used on the glasses + web surfaces. */
private fun agentColor(agent: String): Color = when (agent.lowercase()) {
    "claude" -> Color(0xFFD97757) // Anthropic terracotta
    "codex" -> Color(0xFF10A37F)  // OpenAI green
    "cursor" -> Color(0xFFE6E6E6) // Cursor mono
    else -> Color(0xFF9AA0A6)
}
