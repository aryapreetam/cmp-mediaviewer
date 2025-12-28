package sample.app

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import io.github.aryapreetam.cmpmediaviewer.MediaViewer
import io.github.aryapreetam.cmpmediaviewer.TestTags
import io.github.aryapreetam.cmpmediaviewer.model.MediaItem
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Sample UI tests demonstrating how consumers test MediaViewer in their apps.
 * 
 * These tests serve as documentation and reference for library users.
 */
class MediaViewerSampleTest {

  private val testItems = listOf(
    MediaItem.image("1", "https://example.com/image1.jpg", title = "Image 1"),
    MediaItem.image("2", "https://example.com/image2.jpg", title = "Image 2"),
    MediaItem.image("3", "https://example.com/image3.jpg", title = "Image 3"),
  )

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun mediaViewer_displaysCorrectly() = runComposeUiTest {
    setContent {
      MediaViewer(
        items = testItems,
        onDismiss = {}
      )
    }

    // Verify MediaViewer is displayed
    onNodeWithTag(TestTags.Root).assertIsDisplayed()
    onNodeWithTag(TestTags.CloseButton).assertIsDisplayed()
    onNodeWithTag(TestTags.Pager).assertIsDisplayed()
    onNodeWithTag(TestTags.PositionIndicator).assertIsDisplayed()
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun mediaViewer_showsCorrectPositionIndicator() = runComposeUiTest {
    setContent {
      MediaViewer(
        items = testItems,
        initialIndex = 0,
        onDismiss = {}
      )
    }

    // Position indicator shows "1 of 3" for first item
    onNodeWithTag(TestTags.PositionIndicator).assertTextEquals("1 of 3")
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun mediaViewer_startsAtInitialIndex() = runComposeUiTest {
    setContent {
      MediaViewer(
        items = testItems,
        initialIndex = 1, // Start at second image
        onDismiss = {}
      )
    }

    // Position indicator shows "2 of 3"
    onNodeWithTag(TestTags.PositionIndicator).assertTextEquals("2 of 3")
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun mediaViewer_closeButtonDismisses() = runComposeUiTest {
    var dismissed = false
    
    setContent {
      MediaViewer(
        items = testItems,
        onDismiss = { dismissed = true }
      )
    }

    // Click close button
    onNodeWithTag(TestTags.CloseButton).performClick()

    // Verify dismiss callback was invoked
    assertEquals(true, dismissed, "onDismiss should be called when close button is clicked")
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun mediaViewer_currentItemIsDisplayed() = runComposeUiTest {
    setContent {
      MediaViewer(
        items = testItems,
        initialIndex = 0,
        onDismiss = {}
      )
    }

    // First item should be displayed
    onNodeWithTag(TestTags.item(0)).assertIsDisplayed()
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun mediaViewer_hidesIndicatorForSingleItem() = runComposeUiTest {
    setContent {
      MediaViewer(
        items = listOf(MediaItem.image("1", "https://example.com/single.jpg")),
        onDismiss = {}
      )
    }

    // Position indicator should not exist for single item
    onNodeWithTag(TestTags.PositionIndicator).assertDoesNotExist()
  }
}
