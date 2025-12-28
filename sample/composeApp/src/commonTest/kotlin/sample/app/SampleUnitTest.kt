package sample.app

import io.github.aryapreetam.cmpmediaviewer.model.MediaItem
import io.github.aryapreetam.cmpmediaviewer.model.MediaType
import io.github.aryapreetam.cmpmediaviewer.model.MediaViewerState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Sample unit tests demonstrating how consumers test MediaViewer state management.
 */
class MediaViewerStateUsageTest {

  private val testItems = listOf(
    MediaItem.image("1", "https://example.com/image1.jpg"),
    MediaItem.image("2", "https://example.com/image2.jpg"),
    MediaItem.image("3", "https://example.com/image3.jpg"),
  )

  @Test
  fun stateHolder_navigation() {
    val state = MediaViewerState(testItems, initialIndex = 0)

    // Initially at first item
    assertEquals(0, state.currentIndex)
    assertFalse(state.hasPrevious)
    assertTrue(state.hasNext)

    // Navigate forward
    state.goToNext()
    assertEquals(1, state.currentIndex)
    assertTrue(state.hasPrevious)
    assertTrue(state.hasNext)

    // Navigate backward
    state.goToPrevious()
    assertEquals(0, state.currentIndex)
  }

  @Test
  fun stateHolder_directNavigation() {
    val state = MediaViewerState(testItems, initialIndex = 0)

    // Jump to last item
    state.goToIndex(2)
    assertEquals(2, state.currentIndex)
    assertTrue(state.hasPrevious)
    assertFalse(state.hasNext)
  }

  @Test
  fun mediaItem_factoryMethods() {
    val image = MediaItem.image("1", "https://example.com/image.jpg")
    assertEquals(MediaType.IMAGE, image.type)

    val video = MediaItem.video("2", "https://example.com/video.mp4")
    assertEquals(MediaType.VIDEO, video.type)
  }

  @Test
  fun mediaItem_detectType() {
    // Image extensions
    assertEquals(MediaType.IMAGE, MediaItem.detectType("https://example.com/photo.jpg"))
    assertEquals(MediaType.IMAGE, MediaItem.detectType("https://example.com/photo.png"))
    assertEquals(MediaType.IMAGE, MediaItem.detectType("https://example.com/photo.webp"))
    
    // Video extensions
    assertEquals(MediaType.VIDEO, MediaItem.detectType("https://example.com/video.mp4"))
    assertEquals(MediaType.VIDEO, MediaItem.detectType("https://example.com/video.webm"))
    assertEquals(MediaType.VIDEO, MediaItem.detectType("https://example.com/video.mov"))
  }
}
