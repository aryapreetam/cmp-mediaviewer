package io.github.aryapreetam.cmpmediaviewer

import io.github.aryapreetam.cmpmediaviewer.model.MediaItem
import io.github.aryapreetam.cmpmediaviewer.model.MediaType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MediaItemTest {

  @Test
  fun imageFactoryCreatesImageType() {
    val item = MediaItem.image(
      id = "1",
      url = "https://example.com/image.jpg"
    )
    
    assertEquals("1", item.id)
    assertEquals(MediaType.IMAGE, item.type)
    assertEquals("https://example.com/image.jpg", item.url)
    assertNull(item.thumbnailUrl)
    assertNull(item.title)
  }

  @Test
  fun imageFactoryWithAllParameters() {
    val item = MediaItem.image(
      id = "1",
      url = "https://example.com/image.jpg",
      thumbnailUrl = "https://example.com/thumb.jpg",
      title = "My Image"
    )
    
    assertEquals("https://example.com/thumb.jpg", item.thumbnailUrl)
    assertEquals("My Image", item.title)
  }

  @Test
  fun videoFactoryCreatesVideoType() {
    val item = MediaItem.video(
      id = "2",
      url = "https://example.com/video.mp4"
    )
    
    assertEquals("2", item.id)
    assertEquals(MediaType.VIDEO, item.type)
    assertEquals("https://example.com/video.mp4", item.url)
  }

  @Test
  fun videoFactoryWithPoster() {
    val item = MediaItem.video(
      id = "2",
      url = "https://example.com/video.mp4",
      posterUrl = "https://example.com/poster.jpg",
      title = "My Video"
    )
    
    assertEquals("https://example.com/poster.jpg", item.posterUrl)
    assertEquals("My Video", item.title)
  }

  @Test
  fun detectTypeReturnsVideoForMp4() {
    assertEquals(MediaType.VIDEO, MediaItem.detectType("https://example.com/video.mp4"))
    assertEquals(MediaType.VIDEO, MediaItem.detectType("https://example.com/video.MP4"))
  }

  @Test
  fun detectTypeReturnsVideoForWebm() {
    assertEquals(MediaType.VIDEO, MediaItem.detectType("https://example.com/video.webm"))
  }

  @Test
  fun detectTypeReturnsVideoForMov() {
    assertEquals(MediaType.VIDEO, MediaItem.detectType("https://example.com/video.mov"))
  }

  @Test
  fun detectTypeReturnsImageForJpg() {
    assertEquals(MediaType.IMAGE, MediaItem.detectType("https://example.com/image.jpg"))
    assertEquals(MediaType.IMAGE, MediaItem.detectType("https://example.com/image.jpeg"))
  }

  @Test
  fun detectTypeReturnsImageForPng() {
    assertEquals(MediaType.IMAGE, MediaItem.detectType("https://example.com/image.png"))
  }

  @Test
  fun detectTypeReturnsImageForUnknownExtension() {
    assertEquals(MediaType.IMAGE, MediaItem.detectType("https://example.com/file"))
    assertEquals(MediaType.IMAGE, MediaItem.detectType("https://example.com/file.xyz"))
  }
}
