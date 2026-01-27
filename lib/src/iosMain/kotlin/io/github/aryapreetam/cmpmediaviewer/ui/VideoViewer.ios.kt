package io.github.aryapreetam.cmpmediaviewer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import coil3.compose.AsyncImage
import io.github.aryapreetam.cmpmediaviewer.LocalMediaViewerConfig
import io.github.aryapreetam.cmpmediaviewer.TestTags
import io.github.aryapreetam.cmpmediaviewer.model.MediaItem
import io.github.aryapreetam.cmpvideoplayer.VideoPlayer
import io.github.aryapreetam.cmpvideoplayer.VideoPlayerConfig
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
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSURL
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import platform.UIKit.UIApplicationWillResignActiveNotification
import platform.UIKit.UIColor
import platform.UIKit.UIView
import kotlin.native.concurrent.ThreadLocal
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr

/** iOS video viewer using AVPlayer/AVPlayerViewController with poster + explicit play (no autoplay). */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal actual fun VideoViewer(
  item: MediaItem,
  onClose: () -> Unit,
  onPrevious: (() -> Unit)?,
  onNext: (() -> Unit)?,
  modifier: Modifier
) {
  val config = LocalMediaViewerConfig.current

  val onCloseState by rememberUpdatedState(onClose)
  val onPreviousState by rememberUpdatedState(onPrevious)
  val onNextState by rememberUpdatedState(onNext)

  val videoNsUrl = remember(item.url) { toNsUrlOrNull(item.url) }

  var isPlaying by remember(item.url) { mutableStateOf(false) }

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
    if (videoNsUrl == null) {
      // Invalid URL: fail gracefully but keep navigation/close available.
      androidx.compose.material3.Text(
        text = "Unable to load video",
        color = Color.White
      )
    } else {
      // Poster / player surface
      if (isPlaying) {
        VideoPlayer(
          source = item.toVideoSource(),
          modifier = Modifier.fillMaxSize(),
          config = VideoPlayerConfig(autoplay = true)
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
        Box(
          modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.6f))
            .testTag(TestTags.PlayButton),
          contentAlignment = Alignment.Center
        ) {
          IconButton(
            onClick = { isPlaying = true },
            modifier = Modifier.fillMaxSize()
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

    // Close button (top-right)
    if (config.showCloseButton) {
      IconButton(
        onClick = {
          isPlaying = false
          onCloseState()
        },
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(16.dp)
          .size(48.dp)
          .clip(CircleShape)
          .background(Color.Black.copy(alpha = 0.6f))
      ) {
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = "Close",
          tint = Color.White
        )
      }
    }

    // Previous button (left side)
    if (onPreviousState != null) {
      IconButton(
        onClick = {
          isPlaying = false
          onPreviousState?.invoke()
        },
        modifier = Modifier
          .align(Alignment.CenterStart)
          .padding(start = 16.dp)
          .size(48.dp)
          .clip(CircleShape)
          .background(Color.Black.copy(alpha = 0.6f))
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
          contentDescription = "Previous",
          tint = Color.White,
          modifier = Modifier.size(32.dp)
        )
      }
    }

    // Next button (right side)
    if (onNextState != null) {
      IconButton(
        onClick = {
          isPlaying = false
          onNextState?.invoke()
        },
        modifier = Modifier
          .align(Alignment.CenterEnd)
          .padding(end = 16.dp)
          .size(48.dp)
          .clip(CircleShape)
          .background(Color.Black.copy(alpha = 0.6f))
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
          contentDescription = "Next",
          tint = Color.White,
          modifier = Modifier.size(32.dp)
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

private object ActivePlayerRegistry {
  var activeKey: String? = null
    private set

  var pauseActive: (() -> Unit)? = null
    private set

  fun setActive(key: String, pause: () -> Unit) {
    activeKey = key
    pauseActive = pause
  }

  fun clear() {
    activeKey = null
    pauseActive = null
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
    // Manual LRU bump: remove + reinsert.
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
    // Let AVFoundation downscale the generated image (bounds memory use).
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
