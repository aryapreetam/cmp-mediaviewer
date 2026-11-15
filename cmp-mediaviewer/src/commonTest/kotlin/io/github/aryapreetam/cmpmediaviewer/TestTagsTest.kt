package io.github.aryapreetam.cmpmediaviewer

import kotlin.test.Test
import kotlin.test.assertEquals

class TestTagsTest {

  @Test
  fun staticTagsHaveExpectedValues() {
    assertEquals("MediaViewer_Root", TestTags.Root)
    assertEquals("MediaViewer_CloseButton", TestTags.CloseButton)
    assertEquals("MediaViewer_NextButton", TestTags.NextButton)
    assertEquals("MediaViewer_PreviousButton", TestTags.PreviousButton)
    assertEquals("MediaViewer_Pager", TestTags.Pager)
    assertEquals("MediaViewer_PositionIndicator", TestTags.PositionIndicator)
    assertEquals("MediaViewer_PlayButton", TestTags.PlayButton)
    assertEquals("MediaViewer_VideoControls", TestTags.VideoControls)
    assertEquals("MediaViewer_VideoError", TestTags.VideoError)
    assertEquals("MediaViewer_VideoRetryButton", TestTags.VideoRetryButton)
  }

  @Test
  fun itemFunctionGeneratesCorrectTags() {
    assertEquals("MediaViewer_Item_0", TestTags.item(0))
    assertEquals("MediaViewer_Item_1", TestTags.item(1))
    assertEquals("MediaViewer_Item_99", TestTags.item(99))
  }
}
