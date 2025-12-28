package io.github.aryapreetam.cmpmediaviewer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.WebElementView
import io.github.aryapreetam.cmpmediaviewer.model.MediaItem
import kotlinx.browser.document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLVideoElement
import org.w3c.dom.events.Event

/**
 * Wasm video viewer using native HTML5 video element with HTML-based navigation controls.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal actual fun VideoViewer(
  item: MediaItem,
  onClose: () -> Unit,
  onPrevious: (() -> Unit)?,
  onNext: (() -> Unit)?,
  modifier: Modifier
) {
  val containerElement = remember(item.url) {
    createVideoContainer(item.url)
  }

  // Set up event listeners
  DisposableEffect(item.url, onClose, onPrevious, onNext) {
    val closeBtn = containerElement.querySelector(".close-btn") as? HTMLButtonElement
    val prevBtn = containerElement.querySelector(".prev-btn") as? HTMLButtonElement
    val nextBtn = containerElement.querySelector(".next-btn") as? HTMLButtonElement
    val video = containerElement.querySelector("video") as? HTMLVideoElement

    val closeHandler: (Event) -> Unit = { onClose() }
    val prevHandler: (Event) -> Unit = { 
      video?.pause()
      onPrevious?.invoke() 
    }
    val nextHandler: (Event) -> Unit = { 
      video?.pause()
      onNext?.invoke() 
    }

    closeBtn?.addEventListener("click", closeHandler)
    if (onPrevious != null) {
      prevBtn?.style?.display = "flex"
      prevBtn?.addEventListener("click", prevHandler)
    }
    if (onNext != null) {
      nextBtn?.style?.display = "flex"
      nextBtn?.addEventListener("click", nextHandler)
    }

    onDispose {
      video?.pause()
      video?.src = ""
      closeBtn?.removeEventListener("click", closeHandler)
      prevBtn?.removeEventListener("click", prevHandler)
      nextBtn?.removeEventListener("click", nextHandler)
    }
  }

  Box(
    modifier = modifier.fillMaxSize().background(Color.Black),
    contentAlignment = Alignment.Center
  ) {
    WebElementView(
      factory = { containerElement },
      modifier = Modifier.fillMaxSize(),
      update = { /* No updates needed */ }
    )
  }
}

private fun createVideoContainer(url: String): HTMLDivElement {
  val container = document.createElement("div") as HTMLDivElement
  container.style.width = "100%"
  container.style.height = "100%"
  container.style.position = "relative"
  container.style.display = "flex"
  container.style.alignItems = "center"
  container.style.justifyContent = "center"
  container.style.background = "black"

  // Video element with native controls
  val video = document.createElement("video") as HTMLVideoElement
  video.src = url
  video.controls = true
  video.autoplay = false
  video.style.maxWidth = "100%"
  video.style.maxHeight = "100%"
  video.style.setProperty("object-fit", "contain")
  container.appendChild(video)

  // Close button (top-right)
  val closeBtn = document.createElement("button") as HTMLButtonElement
  closeBtn.className = "close-btn"
  closeBtn.textContent = "\u2715"
  closeBtn.style.position = "absolute"
  closeBtn.style.top = "16px"
  closeBtn.style.right = "16px"
  closeBtn.style.width = "48px"
  closeBtn.style.height = "48px"
  closeBtn.style.borderRadius = "50%"
  closeBtn.style.border = "none"
  closeBtn.style.background = "rgba(0, 0, 0, 0.6)"
  closeBtn.style.color = "white"
  closeBtn.style.fontSize = "24px"
  closeBtn.style.cursor = "pointer"
  closeBtn.style.zIndex = "10"
  closeBtn.style.display = "flex"
  closeBtn.style.alignItems = "center"
  closeBtn.style.justifyContent = "center"
  container.appendChild(closeBtn)

  // Previous button (left side)
  val prevBtn = document.createElement("button") as HTMLButtonElement
  prevBtn.className = "prev-btn"
  prevBtn.textContent = "\u2039"
  prevBtn.style.position = "absolute"
  prevBtn.style.left = "16px"
  prevBtn.style.top = "50%"
  prevBtn.style.transform = "translateY(-50%)"
  prevBtn.style.width = "48px"
  prevBtn.style.height = "48px"
  prevBtn.style.borderRadius = "50%"
  prevBtn.style.border = "none"
  prevBtn.style.background = "rgba(0, 0, 0, 0.6)"
  prevBtn.style.color = "white"
  prevBtn.style.fontSize = "32px"
  prevBtn.style.cursor = "pointer"
  prevBtn.style.zIndex = "10"
  prevBtn.style.display = "none"
  prevBtn.style.alignItems = "center"
  prevBtn.style.justifyContent = "center"
  container.appendChild(prevBtn)

  // Next button (right side)
  val nextBtn = document.createElement("button") as HTMLButtonElement
  nextBtn.className = "next-btn"
  nextBtn.textContent = "\u203A"
  nextBtn.style.position = "absolute"
  nextBtn.style.right = "16px"
  nextBtn.style.top = "50%"
  nextBtn.style.transform = "translateY(-50%)"
  nextBtn.style.width = "48px"
  nextBtn.style.height = "48px"
  nextBtn.style.borderRadius = "50%"
  nextBtn.style.border = "none"
  nextBtn.style.background = "rgba(0, 0, 0, 0.6)"
  nextBtn.style.color = "white"
  nextBtn.style.fontSize = "32px"
  nextBtn.style.cursor = "pointer"
  nextBtn.style.zIndex = "10"
  nextBtn.style.display = "none"
  nextBtn.style.alignItems = "center"
  nextBtn.style.justifyContent = "center"
  container.appendChild(nextBtn)

  return container
}
