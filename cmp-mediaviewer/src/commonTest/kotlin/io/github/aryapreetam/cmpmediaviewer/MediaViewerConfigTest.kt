package io.github.aryapreetam.cmpmediaviewer

import io.github.aryapreetam.cmpmediaviewer.model.MediaViewerConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MediaViewerConfigTest {

  @Test
  fun defaultConfigHasExpectedValues() {
    val config = MediaViewerConfig.Default
    
    assertTrue(config.showPositionIndicator)
    assertTrue(config.showCloseButton)
    assertTrue(config.enableSwipeNavigation)
    assertTrue(config.enableKeyboardNavigation)
    assertEquals(0.9f, config.backdropAlpha)
    assertEquals(1, config.preloadCount)
  }

  @Test
  fun minimalConfigHidesUiChrome() {
    val config = MediaViewerConfig.Minimal
    
    assertFalse(config.showPositionIndicator)
    assertFalse(config.showCloseButton)
  }

  @Test
  fun customConfigCanOverrideDefaults() {
    val config = MediaViewerConfig(
      showPositionIndicator = false,
      backdropAlpha = 0.5f,
      preloadCount = 3
    )
    
    assertFalse(config.showPositionIndicator)
    assertEquals(0.5f, config.backdropAlpha)
    assertEquals(3, config.preloadCount)
    // Others should still be default
    assertTrue(config.showCloseButton)
  }
}
