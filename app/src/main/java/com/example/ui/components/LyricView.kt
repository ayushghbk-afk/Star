package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.model.LyricLine
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.YtRed

@Composable
fun RealtimeSyncLyricView(
    lyrics: List<LyricLine>,
    activeLyricIndex: Int,
    onLyricLineClick: (Long) -> Unit
) {
    val listState = rememberLazyListState()

    // Smoothly scroll active line to center
    LaunchedEffect(activeLyricIndex) {
        if (lyrics.isNotEmpty() && activeLyricIndex in lyrics.indices) {
            val targetScroll = (activeLyricIndex - 2).coerceAtLeast(0)
            listState.animateScrollToItem(targetScroll)
        }
    }

    if (lyrics.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Instrumental or Lyrics unavailable for this stream.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 40.dp, horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            itemsIndexed(lyrics) { index, line ->
                val isActive = index == activeLyricIndex
                val isPast = index < activeLyricIndex

                Surface(
                    onClick = { onLyricLineClick(line.timestampMs) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isActive) YtRed.copy(alpha = 0.15f) else Color.Transparent
                ) {
                    Text(
                        text = line.text,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                            fontSize = if (isActive) 22.sp else 18.sp,
                            lineHeight = 28.sp
                        ),
                        color = when {
                            isActive -> GoldAccent
                            isPast -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        },
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}
