package io.github.aryapreetam.cmpmediaviewer

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import io.github.aryapreetam.cmpmediaviewer.model.MediaItem
import io.github.aryapreetam.cmpmediaviewer.model.MediaType
import io.github.aryapreetam.cmpmediaviewer.model.MediaViewerConfig
import io.github.aryapreetam.cmpmediaviewer.ui.CloseButton
import io.github.aryapreetam.cmpmediaviewer.ui.ImageViewer
import io.github.aryapreetam.cmpmediaviewer.ui.PositionIndicator
import io.github.aryapreetam.cmpmediaviewer.ui.VideoViewer
import kotlinx.coroutines.launch

/**
 * Full-screen media viewer for displaying images and videos.
 *
 * @param items List of media items to display
 * @param initialIndex Initial item index to display (0-based)
 * @param onDismiss Callback when the viewer should be closed
 * @param onPageChanged Callback when the current page changes
 * @param config Configuration options for the viewer
 * @param modifier Modifier for the root container
 *
 * @sample
 * ```kotlin
 * MediaViewer(
 *   items = listOf(
 *     MediaItem.image("1", "https://example.com/image1.jpg"),
 *     MediaItem.image("2", "https://example.com/image2.jpg"),
 *   ),
 *   onDismiss = { showViewer = false }
 * )
 * ```
 */
@Composable
public fun MediaViewer(
  items: List<MediaItem>,
  initialIndex: Int = 0,
  onDismiss: () -> Unit,
  onPageChanged: (Int) -> Unit = {},
  config: MediaViewerConfig = LocalMediaViewerConfig.current,
  modifier: Modifier = Modifier
) {
  if (items.isEmpty()) {
    // Empty state - just close
    LaunchedEffect(Unit) { onDismiss() }
    return
  }

  val pagerState = rememberPagerState(
    initialPage = initialIndex.coerceIn(0, items.lastIndex),
    pageCount = { items.size }
  )
  
  val coroutineScope = rememberCoroutineScope()
  val focusRequester = remember { FocusRequester() }

  // Notify page changes
  LaunchedEffect(pagerState) {
    snapshotFlow { pagerState.currentPage }
      .collect { page -> onPageChanged(page) }
  }
  
  // Request focus when viewer opens (needed for keyboard events)
  LaunchedEffect(Unit) {
    focusRequester.requestFocus()
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = config.backdropAlpha))
      .testTag(TestTags.Root)
      .focusRequester(focusRequester)
      .focusable()
      .onKeyEvent { event ->
        if (event.type == KeyEventType.KeyUp) {
          when (event.key) {
            Key.Escape -> {
              onDismiss()
              true
            }
            Key.DirectionLeft -> {
              if (pagerState.currentPage > 0) {
                coroutineScope.launch {
                  pagerState.animateScrollToPage(pagerState.currentPage - 1)
                }
                true
              } else false
            }
            Key.DirectionRight -> {
              if (pagerState.currentPage < items.lastIndex) {
                coroutineScope.launch {
                  pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
                true
              } else false
            }
            else -> false
          }
        } else false
      }
  ) {
    // Pager for swiping between items
    HorizontalPager(
      state = pagerState,
      modifier = Modifier
        .fillMaxSize()
        .testTag(TestTags.Pager),
      userScrollEnabled = config.enableSwipeNavigation
    ) { page ->
      val item = items[page]
      Box(
        modifier = Modifier
          .fillMaxSize()
          .testTag(TestTags.item(page)),
        contentAlignment = Alignment.Center
      ) {
        when (item.type) {
          MediaType.IMAGE -> ImageViewer(item = item)
          MediaType.VIDEO -> {
            VideoViewer(
              item = item,
              onClose = onDismiss,
              onPrevious = if (page > 0) {{ coroutineScope.launch { pagerState.animateScrollToPage(page - 1) } }} else null,
              onNext = if (page < items.lastIndex) {{ coroutineScope.launch { pagerState.animateScrollToPage(page + 1) } }} else null
            )
          }
        }
      }
    }

    // Close button (top-right)
    // Videos render their own close button inside platform VideoViewer implementations
    // (to ensure immediate pause/stop on close).
    val currentItem = items.getOrNull(pagerState.currentPage)
    if (config.showCloseButton && currentItem?.type != MediaType.VIDEO) {
      CloseButton(
        onClick = onDismiss,
        modifier = Modifier.align(Alignment.TopEnd)
      )
    }

    // Position indicator (bottom-center)
    if (config.showPositionIndicator && items.size > 1) {
      PositionIndicator(
        currentIndex = pagerState.currentPage,
        totalCount = items.size,
        modifier = Modifier.align(Alignment.BottomCenter)
      )
    }
  }
}
