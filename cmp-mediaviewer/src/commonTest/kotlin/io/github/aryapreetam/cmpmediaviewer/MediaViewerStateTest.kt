package io.github.aryapreetam.cmpmediaviewer

import io.github.aryapreetam.cmpmediaviewer.model.MediaItem
import io.github.aryapreetam.cmpmediaviewer.model.MediaViewerState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MediaViewerStateTest {

  private fun createTestItems(count: Int): List<MediaItem> {
    return (1..count).map { 
      MediaItem.image("$it", "https://example.com/image$it.jpg")
    }
  }

  @Test
  fun initialIndexIsCoercedToValidRange() {
    val items = createTestItems(3)
    
    // Too high
    val state1 = MediaViewerState(items, initialIndex = 10)
    assertEquals(2, state1.currentIndex)
    
    // Negative
    val state2 = MediaViewerState(items, initialIndex = -5)
    assertEquals(0, state2.currentIndex)
    
    // Valid
    val state3 = MediaViewerState(items, initialIndex = 1)
    assertEquals(1, state3.currentIndex)
  }

  @Test
  fun emptyItemsHandledGracefully() {
    val state = MediaViewerState(emptyList())
    assertEquals(0, state.currentIndex)
    assertEquals(0, state.itemCount)
    assertNull(state.currentItem)
    assertFalse(state.hasNext)
    assertFalse(state.hasPrevious)
  }

  @Test
  fun goToNextIncrementsIndex() {
    val state = MediaViewerState(createTestItems(3), initialIndex = 0)
    
    assertEquals(0, state.currentIndex)
    assertTrue(state.hasNext)
    
    state.goToNext()
    assertEquals(1, state.currentIndex)
    
    state.goToNext()
    assertEquals(2, state.currentIndex)
    assertFalse(state.hasNext)
    
    // Should not go beyond last
    state.goToNext()
    assertEquals(2, state.currentIndex)
  }

  @Test
  fun goToPreviousDecrementsIndex() {
    val state = MediaViewerState(createTestItems(3), initialIndex = 2)
    
    assertEquals(2, state.currentIndex)
    assertTrue(state.hasPrevious)
    
    state.goToPrevious()
    assertEquals(1, state.currentIndex)
    
    state.goToPrevious()
    assertEquals(0, state.currentIndex)
    assertFalse(state.hasPrevious)
    
    // Should not go below zero
    state.goToPrevious()
    assertEquals(0, state.currentIndex)
  }

  @Test
  fun goToIndexNavigatesToSpecificIndex() {
    val state = MediaViewerState(createTestItems(5), initialIndex = 0)
    
    state.goToIndex(3)
    assertEquals(3, state.currentIndex)
    
    state.goToIndex(0)
    assertEquals(0, state.currentIndex)
  }

  @Test
  fun goToIndexCoercesInvalidIndices() {
    val state = MediaViewerState(createTestItems(3))
    
    state.goToIndex(100)
    assertEquals(2, state.currentIndex)
    
    state.goToIndex(-5)
    assertEquals(0, state.currentIndex)
  }

  @Test
  fun currentItemReturnsCorrectItem() {
    val items = createTestItems(3)
    val state = MediaViewerState(items, initialIndex = 1)
    
    assertEquals(items[1], state.currentItem)
    
    state.goToNext()
    assertEquals(items[2], state.currentItem)
  }

  @Test
  fun hasNextAndHasPreviousWorkCorrectly() {
    val state = MediaViewerState(createTestItems(1))
    
    // Single item - no navigation
    assertFalse(state.hasNext)
    assertFalse(state.hasPrevious)
  }

  @Test
  fun updateItemsAdjustsCurrentIndex() {
    val state = MediaViewerState(createTestItems(5), initialIndex = 4)
    assertEquals(4, state.currentIndex)
    
    // Reduce items - index should be coerced
    state.updateItems(createTestItems(3))
    assertEquals(2, state.currentIndex)
    assertEquals(3, state.itemCount)
  }
}
