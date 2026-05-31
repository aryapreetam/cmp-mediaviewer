package io.github.aryapreetam.cmpmediaviewer.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Interface for MediaViewer state.
 * This interface allows consumers to create fake implementations for testing.
 */
public interface MediaViewerStateHolder {
  /** List of media items being displayed */
  public val items: List<MediaItem>

  /** Current item index (0-based) */
  public val currentIndex: Int

  /** The currently displayed media item, or null if list is empty */
  public val currentItem: MediaItem?

  /** Total number of items */
  public val itemCount: Int

  /** Whether there is a next item to navigate to */
  public val hasNext: Boolean

  /** Whether there is a previous item to navigate to */
  public val hasPrevious: Boolean

  /** Navigate to the next item */
  public fun goToNext()

  /** Navigate to the previous item */
  public fun goToPrevious()

  /** Navigate to a specific index */
  public fun goToIndex(index: Int)
}

/**
 * Default implementation of [MediaViewerStateHolder].
 */
@Stable
public class MediaViewerState(
  items: List<MediaItem>,
  initialIndex: Int = 0
) : MediaViewerStateHolder {

  override var items: List<MediaItem> by mutableStateOf(items)
    private set

  override var currentIndex: Int by mutableIntStateOf(initialIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0)))
    private set

  override val currentItem: MediaItem?
    get() = items.getOrNull(currentIndex)

  override val itemCount: Int
    get() = items.size

  override val hasNext: Boolean
    get() = currentIndex < items.size - 1

  override val hasPrevious: Boolean
    get() = currentIndex > 0

  override fun goToNext() {
    if (hasNext) {
      currentIndex++
    }
  }

  override fun goToPrevious() {
    if (hasPrevious) {
      currentIndex--
    }
  }

  override fun goToIndex(index: Int) {
    currentIndex = index.coerceIn(0, (items.size - 1).coerceAtLeast(0))
  }

  /**
   * Updates the items list. Adjusts currentIndex if needed.
   */
  public fun updateItems(newItems: List<MediaItem>) {
    items = newItems
    currentIndex = currentIndex.coerceIn(0, (newItems.size - 1).coerceAtLeast(0))
  }
}

/**
 * Creates and remembers a [MediaViewerState].
 *
 * @param items List of media items to display
 * @param initialIndex Initial item index to display
 * @return A remembered [MediaViewerState] instance
 */
@Composable
public fun rememberMediaViewerState(
  items: List<MediaItem>,
  initialIndex: Int = 0
): MediaViewerState {
  return remember(items, initialIndex) {
    MediaViewerState(items, initialIndex)
  }
}
