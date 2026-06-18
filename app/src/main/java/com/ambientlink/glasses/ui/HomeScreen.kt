package com.ambientlink.glasses.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ambientlink.glasses.data.Session

/**
 * Glanceable session list for display glasses.
 *
 * Additive/optical see-through rule (shared with the Meta surface): the canvas is
 * transparent — pure black emits no light. Keep the background fully transparent
 * and put all brightness in light text + faint glass surfaces.
 *
 * TODO(xr): replace these foundation composables with Jetpack Compose Glimmer's
 * Card / List / Text, wrapped in GlimmerTheme, for the glasses-native focus model.
 */
@Composable
fun HomeScreen(sessions: List<Session>, onRefresh: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("sessions", color = Color.White, size = 24.sp, weight = FontWeight.Bold)

        if (sessions.isEmpty()) {
            Text("no sessions", color = Color(0xFF9AA0A6), size = 16.sp)
            return@Column
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(sessions) { s -> SessionCard(s, onClick = onRefresh) }
        }
    }
}

@Composable
private fun SessionCard(s: Session, onClick: () -> Unit) {
    val alpha = if (s.isLive) 1f else 0.55f
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0x14FFFFFF))                 // faint glass fill
            .border(1.dp, Color(0x2EFFFFFF), RoundedCornerShape(22.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(s.label, color = Color.White.copy(alpha = alpha), size = 17.sp, weight = FontWeight.SemiBold)
        val sub = if (s.preview.isNotBlank()) s.preview else s.state.lowercase()
        Text(sub, color = Color(0xFF9AA0A6), size = 13.sp)
    }
}

/* Small Text wrapper so the placeholder UI reads cleanly. Swap for Glimmer Text. */
@Composable
private fun Text(
    value: String,
    color: Color,
    size: androidx.compose.ui.unit.TextUnit,
    weight: FontWeight = FontWeight.Normal,
) {
    androidx.compose.foundation.text.BasicText(
        text = value,
        style = androidx.compose.ui.text.TextStyle(color = color, fontSize = size, fontWeight = weight),
    )
}
