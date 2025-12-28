package io.github.aryapreetam.cmpmediaviewer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.focusable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.aryapreetam.cmpmediaviewer.TestTags
import io.github.aryapreetam.cmpmediaviewer.model.MediaItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.openani.mediamp.PlaybackState
import org.openani.mediamp.compose.MediampPlayerSurface
import org.openani.mediamp.compose.rememberMediampPlayer
import org.openani.mediamp.playUri

/**
 * Desktop video viewer using mediamp (VLC backend) with custom controls overlay.
 * 
 * Features:
 * - Play/pause button + click anywhere to toggle
 * - Seekable progress bar
 * - Time display (current / total)
 * - Keyboard: Space=play/pause, Left/Right=seek 10s
 * - Auto-hide controls after 3 seconds
 * 
 * Note: Requires VLC Player installed on desktop.
 */
@Composable
internal actual fun VideoViewer(
  item: MediaItem,
  onClose: () -> Unit,
  onPrevious: (() -> Unit)?,
  onNext: (() -> Unit)?,
  modifier: Modifier
) {
  val player = rememberMediampPlayer()
  val scope = rememberCoroutineScope()
  val focusRequester = remember { FocusRequester() }
  
  val playbackState by player.playbackState.collectAsState()
  val currentPosition by player.currentPositionMillis.collectAsState()
  val mediaProperties by player.mediaProperties.collectAsState()
  
  val duration = mediaProperties?.durationMillis ?: 0L
  val isPlaying = playbackState.isPlaying
  val isBuffering = playbackState == PlaybackState.PAUSED_BUFFERING
  
  // Track if user has initiated playback
  var hasStartedPlayback by remember { mutableStateOf(false) }
  // Track if video is currently loading (after user clicked play)
  val isLoading = hasStartedPlayback && !isPlaying && duration == 0L && playbackState != PlaybackState.PAUSED
  
  var showControls by remember { mutableStateOf(true) }
  var lastInteractionTime by remember { mutableStateOf(System.currentTimeMillis()) }
  
  // Auto-hide controls after 3 seconds of no interaction
  LaunchedEffect(lastInteractionTime, isPlaying) {
    if (isPlaying) {
      delay(3000)
      if (System.currentTimeMillis() - lastInteractionTime >= 3000) {
        showControls = false
      }
    }
  }
  
  // Request focus for keyboard events
  LaunchedEffect(Unit) {
    focusRequester.requestFocus()
  }
  
  fun onInteraction() {
    showControls = true
    lastInteractionTime = System.currentTimeMillis()
  }
  
  fun togglePlayPause() {
    onInteraction()
    when {
      playbackState == PlaybackState.READY || playbackState == PlaybackState.PAUSED -> {
        player.resume()
      }
      isPlaying -> {
        player.pause()
      }
      else -> {
        hasStartedPlayback = true
        scope.launch { player.playUri(item.url) }
      }
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black)
      .focusRequester(focusRequester)
      .focusable()
      .onKeyEvent { event ->
        if (event.type == KeyEventType.KeyUp) {
          when (event.key) {
            Key.Spacebar -> {
              togglePlayPause()
              true
            }
            Key.DirectionLeft -> {
              onInteraction()
              player.seekTo(maxOf(0, currentPosition - 10_000))
              true
            }
            Key.DirectionRight -> {
              onInteraction()
              player.seekTo(minOf(duration, currentPosition + 10_000))
              true
            }
            else -> false
          }
        } else false
      }
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null
      ) {
        togglePlayPause()
      }
      .testTag(TestTags.VideoControls),
    contentAlignment = Alignment.Center
  ) {
    // Video surface
    MediampPlayerSurface(
      mediampPlayer = player,
      modifier = Modifier.fillMaxSize()
    )
    
    // Buffering/Loading indicator - only show when actually loading
    if (hasStartedPlayback && (isBuffering || !isPlaying && duration == 0L)) {
      CircularProgressIndicator(
        color = Color.White,
        modifier = Modifier.size(48.dp)
      )
    }
    
    // Large play button when:
    // - Not playing AND not buffering AND not loading
    // - i.e., video is ready to be played or hasn't started yet
    if (!hasStartedPlayback || (!isPlaying && !isBuffering && duration > 0)) {
      Box(
        modifier = Modifier
          .size(80.dp)
          .clip(CircleShape)
          .background(Color.Black.copy(alpha = 0.6f))
          .testTag(TestTags.PlayButton),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.PlayArrow,
          contentDescription = "Play",
          tint = Color.White,
          modifier = Modifier.size(48.dp)
        )
      }
    }
    
    // Controls overlay at bottom - only show when video has duration or is playing/paused
    if ((showControls || !isPlaying) && (duration > 0 || hasStartedPlayback)) {
      Box(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .fillMaxWidth()
          .background(
            Brush.verticalGradient(
              colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
            )
          )
          .padding(16.dp)
      ) {
        Column {
          // Progress bar - only show when we have duration
          if (duration > 0) {
            Slider(
              value = currentPosition.toFloat(),
              onValueChange = { newValue ->
                onInteraction()
                player.seekTo(newValue.toLong())
              },
              valueRange = 0f..duration.toFloat(),
              colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
              ),
              modifier = Modifier.fillMaxWidth()
            )
          }
          
          // Controls row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Play/Pause button
            IconButton(onClick = { togglePlayPause() }) {
              Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
              )
            }
            
            // Time display
            if (duration > 0) {
              Text(
                text = "${formatTime(currentPosition)} / ${formatTime(duration)}",
                color = Color.White,
                fontSize = 14.sp
              )
            }
          }
        }
      }
    }
  }
}

private fun formatTime(millis: Long): String {
  val totalSeconds = millis / 1000
  val hours = totalSeconds / 3600
  val minutes = (totalSeconds % 3600) / 60
  val seconds = totalSeconds % 60
  
  return if (hours > 0) {
    String.format("%d:%02d:%02d", hours, minutes, seconds)
  } else {
    String.format("%d:%02d", minutes, seconds)
  }
}
