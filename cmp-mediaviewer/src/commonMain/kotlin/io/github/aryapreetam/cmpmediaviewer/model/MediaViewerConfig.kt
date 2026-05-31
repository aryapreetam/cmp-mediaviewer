package io.github.aryapreetam.cmpmediaviewer.model

/**
 * Configuration options for the MediaViewer.
 *
 * @property showPositionIndicator Whether to show "X of Y" position indicator
 * @property showCloseButton Whether to show the close button
 * @property enableSwipeNavigation Whether to enable swipe gestures for navigation
 * @property enableKeyboardNavigation Whether to enable keyboard navigation (desktop/web)
 * @property backdropAlpha Alpha value for the backdrop (0.0 to 1.0)
 * @property preloadCount Number of adjacent items to preload (default: 1 = next + previous)
 */
public data class MediaViewerConfig(
  val showPositionIndicator: Boolean = true,
  val showCloseButton: Boolean = true,
  val enableSwipeNavigation: Boolean = true,
  val enableKeyboardNavigation: Boolean = true,
  val backdropAlpha: Float = 0.9f,
  val preloadCount: Int = 1
) {
  public companion object {
    /** Default configuration with all features enabled */
    public val Default: MediaViewerConfig = MediaViewerConfig()

    /** Minimal configuration - just the image, no UI chrome */
    public val Minimal: MediaViewerConfig = MediaViewerConfig(
      showPositionIndicator = false,
      showCloseButton = false
    )
  }
}
