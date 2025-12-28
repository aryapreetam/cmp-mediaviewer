package io.github.aryapreetam.cmpmediaviewer.ui

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import io.github.aryapreetam.cmpmediaviewer.TestTags
import io.github.aryapreetam.cmpmediaviewer.model.MediaItem

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
  
  val exoPlayer = remember(item.url) {
    ExoPlayer.Builder(context).build().apply {
      setMediaItem(ExoMediaItem.fromUri(item.url))
      prepare()
      playWhenReady = false
    }
  }

  DisposableEffect(item.url) {
    onDispose {
      exoPlayer.release()
    }
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

    // Close button (top-right)
    IconButton(
      onClick = {
        exoPlayer.pause()
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

    // Previous button (left side)
    if (onPrevious != null) {
      IconButton(
        onClick = {
          exoPlayer.pause()
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
          imageVector = Icons.Default.KeyboardArrowLeft,
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
          exoPlayer.pause()
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
          imageVector = Icons.Default.KeyboardArrowRight,
          contentDescription = "Next",
          tint = Color.White,
          modifier = Modifier.size(32.dp)
        )
      }
    }
  }
}
