package io.github.aryapreetam.cmpmediaviewer.ui

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil3.compose.AsyncImage
import io.github.aryapreetam.cmpmediaviewer.LocalMediaViewerConfig
import io.github.aryapreetam.cmpmediaviewer.TestTags
import io.github.aryapreetam.cmpmediaviewer.model.MediaItem
import java.io.File

private object ActiveExoPlayerRegistry {
  private var active: ExoPlayer? = null

  fun setActive(player: ExoPlayer) {
    val previous = active
    if (previous != null && previous !== player) {
      previous.pause()
    }
    active = player
  }

  fun clear(player: ExoPlayer) {
    if (active === player) {
      active = null
    }
  }
}

/**
 * Android video viewer using Media3 ExoPlayer with native controls.
 */
@OptIn(UnstableApi::class)
@Composable
internal actual fun VideoViewer(
  item: MediaItem,
  onClose: () -> Unit,
  onPrevious: (() -> Unit)?,
  onNext: (() -> Unit)?,
  modifier: Modifier
) {
  val context = LocalContext.current
  val config = LocalMediaViewerConfig.current
  val lifecycleOwner = LocalLifecycleOwner.current

  var hasStartedPlayback by remember(item.url) { mutableStateOf(false) }
  val posterRef = remember(item.url, item.posterUrl, item.thumbnailUrl) {
    item.posterUrl ?: item.thumbnailUrl
  }
  
  val exoPlayer = remember(item.url) {
    ExoPlayer.Builder(context).build().apply {
      setMediaItem(ExoMediaItem.fromUri(item.url))
      playWhenReady = false
    }
  }

  DisposableEffect(item.url) {
    onDispose {
      ActiveExoPlayerRegistry.clear(exoPlayer)
      exoPlayer.release()
    }
  }

  DisposableEffect(lifecycleOwner, exoPlayer) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_STOP || event == Lifecycle.Event.ON_PAUSE) {
        exoPlayer.pause()
      }
    }

    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

  fun startPlayback() {
    ActiveExoPlayerRegistry.setActive(exoPlayer)
    if (!hasStartedPlayback) {
      hasStartedPlayback = true
      exoPlayer.prepare()
    }
    exoPlayer.play()
  }

  fun pausePlayback() {
    exoPlayer.pause()
  }

  fun posterModel(ref: String): Any {
    val trimmed = ref.trim()
    if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("file://")) {
      return trimmed
    }
    if (trimmed.startsWith("/")) {
      return File(trimmed)
    }
    return trimmed
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black)
      .testTag(TestTags.VideoControls)
  ) {
    // ExoPlayer view with built-in controls
    AndroidView(
      factory = { ctx ->
        PlayerView(ctx).apply {
          player = exoPlayer
          useController = true
          layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
          )
        }
      },
      modifier = Modifier.fillMaxSize()
    )

    if (!hasStartedPlayback) {
      if (posterRef != null) {
        AsyncImage(
          model = posterModel(posterRef),
          contentDescription = item.title ?: "Video poster",
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Fit
        )
      }

      Box(
        modifier = Modifier
          .align(Alignment.Center)
          .size(80.dp)
          .clip(CircleShape)
          .background(Color.Black.copy(alpha = 0.6f))
          .testTag(TestTags.PlayButton),
        contentAlignment = Alignment.Center
      ) {
        IconButton(
          onClick = { startPlayback() },
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
          pausePlayback()
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
          pausePlayback()
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
          pausePlayback()
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
