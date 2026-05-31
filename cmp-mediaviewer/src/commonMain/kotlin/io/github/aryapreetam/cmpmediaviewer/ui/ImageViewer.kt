package io.github.aryapreetam.cmpmediaviewer.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.github.panpf.zoomimage.CoilZoomAsyncImage
import com.github.panpf.zoomimage.rememberCoilZoomState
import io.github.aryapreetam.cmpmediaviewer.model.MediaItem

/**
 * Displays a single image with zoom and pan support.
 *
 * Uses panpf/zoomimage library for gesture handling:
 * - Pinch to zoom
 * - Double-tap to toggle zoom (1x ↔ 2x)
 * - Pan when zoomed
 * - Rubber band effect at zoom limits
 *
 * @param item The media item to display
 * @param modifier Modifier for the image container
 */
@Composable
internal fun ImageViewer(
  item: MediaItem,
  modifier: Modifier = Modifier
) {
  // Create zoom state - includes double-tap zoom, pinch zoom, pan
  val zoomState = rememberCoilZoomState()

  CoilZoomAsyncImage(
    model = item.url,
    contentDescription = item.title ?: "Media item",
    zoomState = zoomState,
    contentScale = ContentScale.Fit,
    modifier = modifier.fillMaxSize()
  )
}
