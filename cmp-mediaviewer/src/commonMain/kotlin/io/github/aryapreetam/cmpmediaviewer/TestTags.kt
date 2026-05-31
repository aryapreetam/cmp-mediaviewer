package io.github.aryapreetam.cmpmediaviewer

/**
 * Test tags for MediaViewer UI components.
 *
 * Use these with `onNodeWithTag()` in UI tests for reliable element selection.
 * These tags are stable and part of the public API contract.
 */
public object TestTags {
  /** Root container of the MediaViewer */
  public const val Root: String = "MediaViewer_Root"

  /** Close button positioned at top-right */
  public const val CloseButton: String = "MediaViewer_CloseButton"

  /** HorizontalPager for swiping between items */
  public const val Pager: String = "MediaViewer_Pager"

  /** Position indicator showing "X of Y" */
  public const val PositionIndicator: String = "MediaViewer_PositionIndicator"

  /** Video play button overlay (center of video) */
  public const val PlayButton: String = "MediaViewer_PlayButton"

  /** Video controls container (seek bar, mute, etc.) */
  public const val VideoControls: String = "MediaViewer_VideoControls"

  /**
   * Returns the test tag for a media item at the given index.
   *
   * @param index The zero-based index of the item in the list
   * @return Test tag string like "MediaViewer_Item_0"
   */
  public fun item(index: Int): String = "MediaViewer_Item_$index"
}
