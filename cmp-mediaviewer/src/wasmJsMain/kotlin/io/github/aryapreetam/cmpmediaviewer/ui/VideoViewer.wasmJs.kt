@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class, androidx.compose.ui.ExperimentalComposeUiApi::class)

package io.github.aryapreetam.cmpmediaviewer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.WebElementView
import io.github.aryapreetam.cmpmediaviewer.TestTags
import io.github.aryapreetam.cmpmediaviewer.model.MediaItem
import kotlinx.browser.document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.HTMLVideoElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.js

/**
 * Wasm video viewer utilizing native HTML5 video with direct DOM overlay controls and error detection.
 */
@Composable
internal actual fun VideoViewer(
  item: MediaItem,
  onClose: () -> Unit,
  onPrevious: (() -> Unit)?,
  onNext: (() -> Unit)?,
  currentIndex: Int,
  totalCount: Int,
  isSelected: Boolean,
  modifier: Modifier
) {
  var isPlaying by remember(item.url) { mutableStateOf(false) }
  var hasError by remember(item.url) { mutableStateOf(false) }
  var errorMessage by remember(item.url) { mutableStateOf<String?>(null) }

  // Auto-pause when page is scrolled away
  LaunchedEffect(isSelected) {
    if (!isSelected) {
      isPlaying = false
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black)
      .testTag(TestTags.VideoControls),
    contentAlignment = Alignment.Center
  ) {
    if (hasError) {
      VideoErrorView(
        title = item.title,
        errorMessage = errorMessage,
        onRetry = {
          hasError = false
          errorMessage = null
          isPlaying = true
        }
      )
    } else if (isPlaying) {
      WasmVideoPlayer(
        url = item.url,
        onClose = onClose,
        onPrevious = onPrevious,
        onNext = onNext,
        currentIndex = currentIndex,
        totalCount = totalCount,
        onError = { msg ->
          errorMessage = msg
          hasError = true
        }
      )
    } else {
      val posterRef = remember(item.url, item.posterUrl, item.thumbnailUrl) {
        item.posterUrl ?: item.thumbnailUrl
      }

      val posterElement = remember(posterRef) {
        posterRef?.let { createPosterElement(it) }
      }

      DisposableEffect(item.url, posterRef) {
        onDispose {
          posterElement?.src = ""
        }
      }

      if (posterElement != null) {
        WebElementView(
          factory = { posterElement },
          modifier = Modifier.fillMaxSize(),
          update = { /* No-op */ }
        )
      }

      IconButton(
        onClick = { isPlaying = true },
        modifier = Modifier
          .align(Alignment.Center)
          .size(80.dp)
          .clip(CircleShape)
          .background(Color.Black.copy(alpha = 0.6f))
          .testTag(TestTags.PlayButton)
      ) {
        Icon(
          imageVector = Icons.Default.PlayArrow,
          contentDescription = "Play",
          tint = Color.White,
          modifier = Modifier.size(48.dp)
        )
      }
    }
  }
}

@Composable
private fun WasmVideoPlayer(
  url: String,
  onClose: () -> Unit,
  onPrevious: (() -> Unit)?,
  onNext: (() -> Unit)?,
  currentIndex: Int,
  totalCount: Int,
  onError: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val onCloseState = rememberUpdatedState(onClose)
  val onPreviousState = rememberUpdatedState(onPrevious)
  val onNextState = rememberUpdatedState(onNext)
  val onErrorState = rememberUpdatedState(onError)

  val container = remember(url, currentIndex, totalCount) {
    createWasmVideoContainer(
      url = url,
      onClose = { onCloseState.value() },
      onPrevious = if (onPrevious != null) {{ onPreviousState.value?.invoke() }} else null,
      onNext = if (onNext != null) {{ onNextState.value?.invoke() }} else null,
      currentIndex = currentIndex,
      totalCount = totalCount,
      onError = { msg -> onErrorState.value(msg) }
    )
  }

  DisposableEffect(url) {
    val video = container.querySelector("video") as? HTMLVideoElement
    if (video != null) {
      if (video.error != null) {
        val err = video.error
        val msg = when (err?.code?.toInt()) {
          1 -> "MEDIA_ERR_ABORTED: Playback aborted"
          2 -> "MEDIA_ERR_NETWORK: Network download error"
          3 -> "MEDIA_ERR_DECODE: Video decoding failed"
          4 -> "MEDIA_ERR_SRC_NOT_SUPPORTED: Video format not supported or link expired"
          else -> "Video playback failed"
        }
        onErrorState.value(msg)
      } else {
        playVideoSafe(video)
      }
    }
    onDispose {
      try {
        val v = container.querySelector("video") as? HTMLVideoElement
        v?.pause()
        v?.src = ""
      } catch (_: Throwable) {
        // Best-effort
      }
    }
  }

  WebElementView(
    factory = { container },
    modifier = modifier.fillMaxSize(),
    update = { /* No-op */ },
    onRelease = {
      try {
        val v = container.querySelector("video") as? HTMLVideoElement
        v?.pause()
        v?.src = ""
      } catch (_: Throwable) {
        // Best-effort
      }
    }
  )
}

private fun createWasmVideoContainer(
  url: String,
  onClose: () -> Unit,
  onPrevious: (() -> Unit)?,
  onNext: (() -> Unit)?,
  currentIndex: Int,
  totalCount: Int,
  onError: (String) -> Unit
): HTMLDivElement {
  val container = document.createElement("div") as HTMLDivElement
  container.style.position = "relative"
  container.style.width = "100%"
  container.style.height = "100%"
  container.style.backgroundColor = "black"
  container.style.display = "flex"
  container.style.alignItems = "center"
  container.style.justifyContent = "center"
  container.style.setProperty("overflow", "hidden")
  container.tabIndex = 0

  val video = document.createElement("video") as HTMLVideoElement
  video.controls = true
  video.autoplay = true
  video.setAttribute("playsinline", "true")
  video.setAttribute("crossorigin", "anonymous")
  video.style.width = "100%"
  video.style.height = "100%"
  video.style.maxWidth = "100%"
  video.style.maxHeight = "100%"
  video.style.setProperty("object-fit", "contain")

  val errorHandler: (Event) -> Unit = {
    val err = video.error
    val msg = when (err?.code?.toInt()) {
      1 -> "MEDIA_ERR_ABORTED: Playback aborted"
      2 -> "MEDIA_ERR_NETWORK: Network download error"
      3 -> "MEDIA_ERR_DECODE: Video decoding failed"
      4 -> "MEDIA_ERR_SRC_NOT_SUPPORTED: Video format not supported or link expired"
      else -> "Video playback failed"
    }
    onError(msg)
  }
  video.addEventListener("error", errorHandler)
  video.src = url

  container.appendChild(video)

  // Top-right Close Button (✕)
  val closeButton = document.createElement("button") as HTMLButtonElement
  closeButton.setAttribute("data-testid", "MediaViewer_CloseButton")
  closeButton.setAttribute("aria-label", "Close")
  closeButton.innerHTML = "✕"
  closeButton.style.position = "absolute"
  closeButton.style.top = "16px"
  closeButton.style.right = "16px"
  closeButton.style.width = "40px"
  closeButton.style.height = "40px"
  closeButton.style.borderRadius = "20px"
  closeButton.style.backgroundColor = "rgba(0, 0, 0, 0.5)"
  closeButton.style.color = "white"
  closeButton.style.border = "none"
  closeButton.style.cursor = "pointer"
  closeButton.style.fontSize = "18px"
  closeButton.style.display = "flex"
  closeButton.style.alignItems = "center"
  closeButton.style.justifyContent = "center"
  closeButton.style.zIndex = "1000"
  closeButton.addEventListener("click", { e ->
    e.stopPropagation()
    onClose()
  })
  closeButton.addEventListener("mouseenter", { closeButton.style.backgroundColor = "rgba(255, 255, 255, 0.2)" })
  closeButton.addEventListener("mouseleave", { closeButton.style.backgroundColor = "rgba(0, 0, 0, 0.5)" })
  container.appendChild(closeButton)

  // Center-left Previous Button (‹)
  if (onPrevious != null) {
    val prevButton = document.createElement("button") as HTMLButtonElement
    prevButton.setAttribute("data-testid", "MediaViewer_PreviousButton")
    prevButton.setAttribute("aria-label", "Previous")
    prevButton.innerHTML = "‹"
    prevButton.style.position = "absolute"
    prevButton.style.top = "50%"
    prevButton.style.left = "16px"
    prevButton.style.transform = "translateY(-50%)"
    prevButton.style.width = "40px"
    prevButton.style.height = "40px"
    prevButton.style.borderRadius = "20px"
    prevButton.style.backgroundColor = "rgba(0, 0, 0, 0.5)"
    prevButton.style.color = "white"
    prevButton.style.border = "none"
    prevButton.style.cursor = "pointer"
    prevButton.style.fontSize = "28px"
    prevButton.style.display = "flex"
    prevButton.style.alignItems = "center"
    prevButton.style.justifyContent = "center"
    prevButton.style.zIndex = "1000"
    prevButton.addEventListener("click", { e ->
      e.stopPropagation()
      onPrevious()
    })
    prevButton.addEventListener("mouseenter", { prevButton.style.backgroundColor = "rgba(255, 255, 255, 0.2)" })
    prevButton.addEventListener("mouseleave", { prevButton.style.backgroundColor = "rgba(0, 0, 0, 0.5)" })
    container.appendChild(prevButton)
  }

  // Center-right Next Button (›)
  if (onNext != null) {
    val nextButton = document.createElement("button") as HTMLButtonElement
    nextButton.setAttribute("data-testid", "MediaViewer_NextButton")
    nextButton.setAttribute("aria-label", "Next")
    nextButton.innerHTML = "›"
    nextButton.style.position = "absolute"
    nextButton.style.top = "50%"
    nextButton.style.right = "16px"
    nextButton.style.transform = "translateY(-50%)"
    nextButton.style.width = "40px"
    nextButton.style.height = "40px"
    nextButton.style.borderRadius = "20px"
    nextButton.style.backgroundColor = "rgba(0, 0, 0, 0.5)"
    nextButton.style.color = "white"
    nextButton.style.border = "none"
    nextButton.style.cursor = "pointer"
    nextButton.style.fontSize = "28px"
    nextButton.style.display = "flex"
    nextButton.style.alignItems = "center"
    nextButton.style.justifyContent = "center"
    nextButton.style.zIndex = "1000"
    nextButton.addEventListener("click", { e ->
      e.stopPropagation()
      onNext()
    })
    nextButton.addEventListener("mouseenter", { nextButton.style.backgroundColor = "rgba(255, 255, 255, 0.2)" })
    nextButton.addEventListener("mouseleave", { nextButton.style.backgroundColor = "rgba(0, 0, 0, 0.5)" })
    container.appendChild(nextButton)
  }

  // Bottom-center Position Indicator ("X of Y")
  if (totalCount > 1) {
    val positionIndicator = document.createElement("div") as HTMLDivElement
    positionIndicator.setAttribute("data-testid", "MediaViewer_PositionIndicator")
    positionIndicator.textContent = "${currentIndex + 1} of $totalCount"
    positionIndicator.style.position = "absolute"
    positionIndicator.style.bottom = "64px"
    positionIndicator.style.left = "50%"
    positionIndicator.style.transform = "translateX(-50%)"
    positionIndicator.style.padding = "8px 16px"
    positionIndicator.style.borderRadius = "16px"
    positionIndicator.style.backgroundColor = "rgba(0, 0, 0, 0.5)"
    positionIndicator.style.color = "white"
    positionIndicator.style.fontSize = "14px"
    positionIndicator.style.fontFamily = "sans-serif"
    positionIndicator.style.setProperty("pointer-events", "none")
    positionIndicator.style.zIndex = "1000"
    container.appendChild(positionIndicator)
  }

  // Keyboard accessibility
  container.addEventListener("keydown", { event ->
    val keyEvent = event as? KeyboardEvent
    when (keyEvent?.key) {
      "Escape" -> onClose()
      "ArrowLeft" -> onPrevious?.invoke()
      "ArrowRight" -> onNext?.invoke()
    }
  })

  return container
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun playVideoSafe(video: HTMLVideoElement) {
  js("""
    var promise = video.play();
    if (promise !== undefined && typeof promise.catch === 'function') {
      promise.catch(function(error) {
        if (video.error) {
          video.dispatchEvent(new Event("error"));
        } else {
          video.muted = true;
          var retryPromise = video.play();
          if (retryPromise !== undefined && typeof retryPromise.catch === 'function') {
            retryPromise.catch(function(e) {
              if (video.error) {
                video.dispatchEvent(new Event("error"));
              }
            });
          }
        }
      });
    }
  """)
}

private fun createPosterElement(url: String): HTMLImageElement {
  val img = document.createElement("img") as HTMLImageElement
  img.setAttribute("crossorigin", "anonymous")
  img.src = url
  img.style.width = "100%"
  img.style.height = "100%"
  img.style.setProperty("object-fit", "contain")
  return img
}
