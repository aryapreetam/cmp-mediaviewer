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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import coil3.compose.AsyncImage
import io.github.aryapreetam.cmpmediaviewer.TestTags
import io.github.aryapreetam.cmpmediaviewer.model.MediaItem
import io.ktor.client.HttpClient
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.http.HttpMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import platform.AVFoundation.AVAssetImageGenerator
import platform.AVFoundation.AVLayerVideoGravityResizeAspect
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVURLAsset
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.replaceCurrentItemWithPlayerItem
import platform.AVKit.AVPlayerViewController
import platform.CoreMedia.CMTimeMake
import platform.Foundation.NSURL
import platform.UIKit.UIColor
import platform.UIKit.UIView
import kotlin.native.concurrent.ThreadLocal
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr

/** iOS video viewer using native AVPlayer and AVPlayerViewController. */
@OptIn(ExperimentalComposeUiApi::class)
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
  val videoNsUrl = remember(item.url) { toNsUrlOrNull(item.url) }

  var isPlaying by remember(item.url) { mutableStateOf(false) }
  var hasError by remember(item.url) { mutableStateOf(false) }
  var errorMessage by remember(item.url) { mutableStateOf<String?>(null) }

  val playerHolder = remember(item.url) {
    videoNsUrl?.let { IosPlayerHolder(it) }
  }

  // Dispose player on change/exit
  DisposableEffect(item.url) {
    onDispose {
      playerHolder?.dispose()
    }
  }

  // Auto-pause when page is scrolled away
  LaunchedEffect(isSelected) {
    if (!isSelected) {
      isPlaying = false
      playerHolder?.pause()
    }
  }

  LaunchedEffect(item.url, isPlaying) {
    if (isPlaying && item.url.isHttpUrl()) {
      withContext(Dispatchers.Default) {
        try {
          val client = HttpClient()
          try {
            val response = client.request {
              method = HttpMethod.Head
              url(item.url)
            }
            if (response.status.value in 400..599) {
              errorMessage = "HTTP ${response.status.value} ${response.status.description}"
              hasError = true
            }
          } finally {
            client.close()
          }
        } catch (t: Throwable) {
          errorMessage = t.message ?: "Network error"
          hasError = true
        }
      }
    }
  }

  DisposableEffect(item.url, isPlaying) {
    val center = platform.Foundation.NSNotificationCenter.defaultCenter
    val token = center.addObserverForName(
      name = platform.AVFoundation.AVPlayerItemFailedToPlayToEndTimeNotification,
      `object` = null,
      queue = null
    ) { notification: platform.Foundation.NSNotification? ->
      val itemError = notification?.userInfo?.get(platform.AVFoundation.AVPlayerItemFailedToPlayToEndTimeErrorKey) as? platform.Foundation.NSError
      errorMessage = itemError?.localizedDescription ?: "Playback failed"
      hasError = true
    }
    onDispose {
      center.removeObserver(token)
    }
  }

  val posterRef = remember(item.url, item.posterUrl, item.thumbnailUrl) {
    item.posterUrl ?: item.thumbnailUrl
  }

  var generatedPoster by remember(item.url) { mutableStateOf<IosPoster?>(null) }

  // Best-effort first frame extraction when no poster is provided.
  LaunchedEffect(item.url, posterRef) {
    generatedPoster = null
    if (posterRef != null || videoNsUrl == null) return@LaunchedEffect

    val cached = IosPosterCache.get(item.url)
    if (cached != null) {
      generatedPoster = cached
      return@LaunchedEffect
    }

    val timeoutMs = if (item.url.isHttpUrl()) 1500L else null
    val poster = withContext(Dispatchers.Default) {
      if (timeoutMs != null) {
        withTimeoutOrNull(timeoutMs) { extractFirstFramePoster(videoNsUrl) }
      } else {
        extractFirstFramePoster(videoNsUrl)
      }
    }

    if (poster != null) {
      IosPosterCache.put(item.url, poster)
      generatedPoster = poster
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black)
      .testTag(TestTags.VideoControls),
    contentAlignment = Alignment.Center
  ) {
    if (videoNsUrl == null || hasError) {
      VideoErrorView(
        title = item.title,
        errorMessage = if (videoNsUrl == null) "Invalid URL" else errorMessage,
        onRetry = {
          hasError = false
          errorMessage = null
          isPlaying = true
        }
      )
    } else if (isPlaying && playerHolder != null) {
      UIKitView(
        factory = {
          playerHolder.ensureView()
        },
        modifier = Modifier.fillMaxSize(),
        update = {
          playerHolder.play()
        }
      )
    } else {
      when {
        posterRef != null -> {
          AsyncImage(
            model = normalizePosterModel(posterRef),
            contentDescription = item.title ?: "Video poster",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
          )
        }

        generatedPoster != null -> {
          val poster = generatedPoster
          if (poster != null) {
            UIKitView(
              factory = {
                poster.buildView()
              },
              modifier = Modifier.fillMaxSize(),
              update = { /* No-op */ }
            )
          }
        }
      }

      // Center play button overlay
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

private fun String.isHttpUrl(): Boolean =
  startsWith("http://", ignoreCase = true) || startsWith("https://", ignoreCase = true)

private fun String.isFileUrl(): Boolean = startsWith("file://", ignoreCase = true)

private fun toNsUrlOrNull(raw: String): NSURL? {
  return when {
    raw.isHttpUrl() -> NSURL.URLWithString(raw)
    raw.isFileUrl() -> NSURL.URLWithString(raw)
    else -> NSURL.fileURLWithPath(raw)
  }
}

private fun normalizePosterModel(raw: String): String {
  return when {
    raw.isHttpUrl() || raw.isFileUrl() -> raw
    else -> NSURL.fileURLWithPath(raw).absoluteString ?: raw
  }
}

private class IosPlayerHolder(
  private val url: NSURL
) {
  private var player: AVPlayer? = null
  private var controller: AVPlayerViewController? = null
  private var view: UIView? = null

  private fun ensurePlayer(): AVPlayer {
    val existing = player
    if (existing != null) return existing

    val playerItem = AVPlayerItem(uRL = url)
    return AVPlayer(playerItem).also { created ->
      player = created
    }
  }

  private fun ensureController(): AVPlayerViewController {
    val existing = controller
    if (existing != null) return existing

    val player = ensurePlayer()
    return AVPlayerViewController().apply {
      this.player = player
      showsPlaybackControls = true
      videoGravity = AVLayerVideoGravityResizeAspect
      view.backgroundColor = UIColor.blackColor
    }.also { created ->
      controller = created
      view = created.view
    }
  }

  fun ensureView(): UIView {
    return view ?: ensureController().view
  }

  fun play() {
    ensurePlayer().play()
  }

  fun pause() {
    player?.pause()
  }

  fun dispose() {
    player?.pause()
    player?.replaceCurrentItemWithPlayerItem(null)
    controller?.player = null
    view = null
    controller = null
    player = null
  }
}

private class IosPoster(
  private val image: platform.UIKit.UIImage
) {
  fun buildView(): UIView {
    return platform.UIKit.UIImageView(image = image).apply {
      contentMode = platform.UIKit.UIViewContentMode.UIViewContentModeScaleAspectFit
      backgroundColor = UIColor.blackColor
      clipsToBounds = true
    }
  }
}

@ThreadLocal
private object IosPosterCache {
  private const val MaxEntries: Int = 16

  private val map: LinkedHashMap<String, IosPoster> = LinkedHashMap()

  fun get(key: String): IosPoster? {
    val value = map.remove(key) ?: return null
    map[key] = value
    return value
  }

  fun put(key: String, value: IosPoster) {
    map.remove(key)
    map[key] = value
    while (map.size > MaxEntries) {
      val eldest = map.entries.iterator().next()
      map.remove(eldest.key)
    }
  }
}

@OptIn(ExperimentalForeignApi::class)
private fun extractFirstFramePoster(url: NSURL): IosPoster? {
  val asset = AVURLAsset(uRL = url, options = null)
  val generator = AVAssetImageGenerator(asset).apply {
    appliesPreferredTrackTransform = true
    maximumSize = platform.CoreGraphics.CGSizeMake(720.0, 720.0)
  }

  return memScoped {
    val error = alloc<ObjCObjectVar<platform.Foundation.NSError?>>()
    val time = CMTimeMake(value = 0, timescale = 1)
    val cgImage = generator.copyCGImageAtTime(time, actualTime = null, error = error.ptr)
      ?: return@memScoped null

    val uiImage = platform.UIKit.UIImage.imageWithCGImage(cgImage)
    platform.CoreGraphics.CGImageRelease(cgImage)
    IosPoster(uiImage)
  }
}
