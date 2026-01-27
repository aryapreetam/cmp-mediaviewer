package io.github.aryapreetam.cmpmediaviewer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.aryapreetam.cmpmediaviewer.LocalMediaViewerConfig
import io.github.aryapreetam.cmpmediaviewer.TestTags
import io.github.aryapreetam.cmpmediaviewer.model.MediaItem
import io.github.aryapreetam.cmpvideoplayer.VideoPlayer
import io.github.aryapreetam.cmpvideoplayer.VideoPlayerConfig

/** Desktop video viewer delegating playback to `cmp-videoplayer` and keeping viewer chrome here. */
@Composable
internal actual fun VideoViewer(
  item: MediaItem,
  onClose: () -> Unit,
  onPrevious: (() -> Unit)?,
  onNext: (() -> Unit)?,
  modifier: Modifier
) {
  val config = LocalMediaViewerConfig.current
  var isPlaying by remember(item.url) { mutableStateOf(false) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black)
      .testTag(TestTags.VideoControls),
    contentAlignment = Alignment.Center
  ) {
    if (isPlaying) {
      VideoPlayer(
        source = item.toVideoSource(),
        modifier = Modifier.fillMaxSize(),
        config = VideoPlayerConfig(autoplay = true)
      )
    }

    if (!isPlaying) {
      Box(
        modifier = Modifier
          .size(80.dp)
          .clip(CircleShape)
          .background(Color.Black.copy(alpha = 0.6f))
          .testTag(TestTags.PlayButton),
        contentAlignment = Alignment.Center
      ) {
        IconButton(
          onClick = { isPlaying = true },
          modifier = Modifier.fillMaxSize()
        ) {
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Play",
            tint = Color.White,
            modifier = Modifier.size(48.dp)
          )
        }
      }
    }

    // Close button (top-right)
    if (config.showCloseButton) {
      IconButton(
        onClick = {
          isPlaying = false
          onClose()
        },
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(16.dp)
          .size(48.dp)
          .clip(CircleShape)
          .background(Color.Black.copy(alpha = 0.6f))
      ) {
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = "Close",
          tint = Color.White
        )
      }
    }

    // Previous button (left side)
    if (onPrevious != null) {
      IconButton(
        onClick = {
          isPlaying = false
          onPrevious()
        },
        modifier = Modifier
          .align(Alignment.CenterStart)
          .padding(start = 16.dp)
          .size(48.dp)
          .clip(CircleShape)
          .background(Color.Black.copy(alpha = 0.6f))
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
          contentDescription = "Previous",
          tint = Color.White,
          modifier = Modifier.size(32.dp)
        )
      }
    }

    // Next button (right side)
    if (onNext != null) {
      IconButton(
        onClick = {
          isPlaying = false
          onNext()
        },
        modifier = Modifier
          .align(Alignment.CenterEnd)
          .padding(end = 16.dp)
          .size(48.dp)
          .clip(CircleShape)
          .background(Color.Black.copy(alpha = 0.6f))
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
          contentDescription = "Next",
          tint = Color.White,
          modifier = Modifier.size(32.dp)
        )
      }
    }
  }
}
