package sample.app

import io.github.aryapreetam.cmpmediaviewer.TestTags
import io.github.aryapreetam.parikshan.E2ETestLifecycle
import io.github.aryapreetam.parikshan.E2ETestScope
import io.github.aryapreetam.parikshan.e2eTest
import io.github.aryapreetam.parikshan.protocol.ScrollDirection
import kotlin.test.Test
import kotlinx.coroutines.delay

class SampleIntegrationTest : E2ETestLifecycle {

  override suspend fun E2ETestScope.afterEach() {
    try {
      if (hasVisibleNode(TestTags.CloseButton)) {
        click(TestTags.CloseButton)
      }
    } catch (_: Throwable) {
      // Best-effort cleanup
    }
    // Ensure gallery scroll position is reset to top for subsequent tests
    if (hasVisibleNode("gallery_grid") && !hasVisibleNode("thumbnail_0")) {
      try {
        scrollUntilVisible(
          containerTag = "gallery_grid",
          targetTag = "thumbnail_0",
          direction = ScrollDirection.Up,
          maxScrolls = 15
        )
      } catch (_: Throwable) {
        // Best-effort cleanup
      }
    }
  }

  /**
   * Test 1: Start at first image and navigate to next two images.
   */
  @Test
  fun testStartAtFirstImageAndBrowseNextTwo() = e2eTest {
    // 1. Verify Gallery renders
    assertVisible("MediaViewer Sample")

    // 2. Open first item (item 0)
    click("thumbnail_0")
    assertVisible(TestTags.Root)
    assertContains(TestTags.PositionIndicator, "1 of 10")

    // 3. Navigate to second item (item 1)
    assertVisible(TestTags.NextButton)
    click(TestTags.NextButton)
    assertContains(TestTags.PositionIndicator, "2 of 10")

    // 4. Navigate to third item (item 2)
    assertVisible(TestTags.NextButton)
    click(TestTags.NextButton)
    assertContains(TestTags.PositionIndicator, "3 of 10")

    // 5. Clean up: close viewer
    click(TestTags.CloseButton)
    assertVisible("MediaViewer Sample")
  }

  /**
   * Test 1: Start at third image and navigate to next two media items.
   */
  @Test
  fun testStartAtThirdImageAndBrowseNextTwo() = e2eTest {
    // 1. Verify Gallery renders
    assertVisible("MediaViewer Sample")

    // 2. Open first item (item 2)
    scrollUntilVisible(containerTag = "gallery_grid", targetTag = "thumbnail_2")
    click("thumbnail_2")
    assertVisible("3 of 10")

    // 3. Navigate to second item (item 3)
    click(TestTags.NextButton)
    assertVisible("4 of 10")

    // 4. Navigate to third item (item 4)
    click(TestTags.NextButton)
    assertVisible("5 of 10")
    assertVisible(TestTags.PlayButton)
    assertVisible(TestTags.CloseButton)

    // 5. Clean up: close viewer
    click(TestTags.CloseButton)
    assertVisible("MediaViewer Sample")
  }

  /**
   * Test 2: Start at second-to-last item and navigate to the end of the list.
   */
  @Test
  fun testStartAtSecondLastAndNavigateToEnd() = e2eTest {
    assertVisible("MediaViewer Sample")

    // Open item 8 (The Kiss, 9th item = second last)
    scrollUntilVisible(containerTag = "gallery_grid", targetTag = "thumbnail_8")
    click("thumbnail_8")
    assertVisible(TestTags.Root)
    assertContains(TestTags.PositionIndicator, "9 of 10")

    // Advance to item 9 (The Scream, 10th item = end)
    assertVisible(TestTags.NextButton)
    click(TestTags.NextButton)
    assertContains(TestTags.PositionIndicator, "10 of 10")

    // Verify PreviousButton is present at end
    assertVisible(TestTags.PreviousButton)

    // Clean up: close viewer
    click(TestTags.CloseButton)
    assertVisible("MediaViewer Sample")
  }

  /**
   * Test 3: Close button dismisses viewer and restores gallery screen.
   */
  @Test
  fun testCloseButtonDismissesViewer() = e2eTest {
    assertVisible("MediaViewer Sample")

    // Open first thumbnail
    click("thumbnail_0")
    assertVisible(TestTags.Root)
    assertContains(TestTags.PositionIndicator, "1 of 10")
    assertVisible(TestTags.CloseButton)

    // Click close
    click(TestTags.CloseButton)

    // Verify back to gallery
    assertVisible("MediaViewer Sample")
  }

  /**
   * Test 4: Invalid/expired video link displays error state and keeps exit/navigation operational.
   */
  @Test
  fun testInvalidVideoLinkHandling() = e2eTest {
    assertVisible("MediaViewer Sample")

    // Item 7 is the expired/invalid video ("8 of 10")
    scrollUntilVisible(containerTag = "gallery_grid", targetTag = "thumbnail_7")
    click("thumbnail_7")
    assertVisible(TestTags.Root)
    assertContains(TestTags.PositionIndicator, "8 of 10")

    // Click center PlayButton to trigger playback of expired link
    assertVisible(TestTags.PlayButton)
    click(TestTags.PlayButton)

    // Verify error state is handled: VideoError container and Retry button are displayed
    assertVisible(TestTags.VideoError)
    assertVisible("Unable to play video")
    assertVisible(TestTags.VideoRetryButton)

    // Verify CloseButton and NextButton remain fully accessible during error
    assertVisible(TestTags.CloseButton)
    assertVisible(TestTags.NextButton)

    // Navigate to next item to verify recovery without refresh
    click(TestTags.NextButton)
    assertContains(TestTags.PositionIndicator, "9 of 10")
    assertVisible(TestTags.CloseButton)

    // Close back to gallery
    click(TestTags.CloseButton)
    assertVisible("MediaViewer Sample")
  }

  /**
   * Test 5: Verify valid video (thumbnail_4, 5 of 10).
   */
  @Test
  fun testValidSample5sVideo() = e2eTest {
    assertVisible("MediaViewer Sample")

    scrollUntilVisible(containerTag = "gallery_grid", targetTag = "thumbnail_4")
    click("thumbnail_4")
    assertVisible(TestTags.Root)
    assertContains(TestTags.PositionIndicator, "5 of 10")
    assertVisible(TestTags.CloseButton)

    assertVisible(TestTags.PlayButton)
    click(TestTags.PlayButton)

    // Verify error view is NOT visible for valid video!
    assertNotVisible(TestTags.VideoError)
    delay(500)
    if (hasVisibleNode(TestTags.CloseButton)) {
      retry(maxAttempts = 5, delayMs = 500L) { click(TestTags.CloseButton) }
      assertVisible("MediaViewer Sample")
    }
  }
}
