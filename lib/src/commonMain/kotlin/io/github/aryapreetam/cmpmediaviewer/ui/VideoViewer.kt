package io.github.aryapreetam.cmpmediaviewer.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.aryapreetam.cmpmediaviewer.model.MediaItem

/**
 * Displays a video with playback controls.
 * 
 * Platform implementations:
 * - Desktop (JVM): mediamp with VLC backend
 * - Android: ExoPlayer (TODO)
 * - iOS: AVPlayer (TODO) 
 * - Wasm: HTML5 video element with HTML-based navigation
 *
 * @param item The video media item to display
 * @param onClose Callback to close the viewer
 * @param onPrevious Callback to go to previous item (null if first item)
 * @param onNext Callback to go to next item (null if last item)
 * @param modifier Modifier for the video container
 */
@Composable
internal expect fun VideoViewer(
  item: MediaItem,
  onClose: () -> Unit,
  onPrevious: (() -> Unit)?,
  onNext: (() -> Unit)?,
  modifier: Modifier = Modifier
)
