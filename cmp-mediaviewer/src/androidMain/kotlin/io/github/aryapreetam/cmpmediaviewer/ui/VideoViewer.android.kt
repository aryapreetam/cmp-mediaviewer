package io.github.aryapreetam.cmpmediaviewer.ui

import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.SurfaceView
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.Image
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import io.github.aryapreetam.cmpmediaviewer.TestTags
import io.github.aryapreetam.cmpmediaviewer.model.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/** Android video viewer with native player implementation. */
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
  var isFirstFrameRendered by remember(item.url) { mutableStateOf(false) }
  var hasError by remember(item.url) { mutableStateOf(false) }
  var errorMessage by remember(item.url) { mutableStateOf<String?>(null) }
  var generatedPoster by remember(item.url) { mutableStateOf<ImageBitmap?>(null) }

  // Auto-pause when page is scrolled away
  LaunchedEffect(isSelected) {
    if (!isSelected) {
      isPlaying = false
      isFirstFrameRendered = false
    }
  }

  LaunchedEffect(item.url, isPlaying) {
    if (isPlaying && (item.url.startsWith("http://", ignoreCase = true) || item.url.startsWith("https://", ignoreCase = true))) {
      withContext(Dispatchers.IO) {
        try {
          val conn = URL(item.url).openConnection() as HttpURLConnection
          conn.requestMethod = "HEAD"
          conn.connectTimeout = 3000
          conn.readTimeout = 3000
          conn.instanceFollowRedirects = true
          val code = conn.responseCode
          if (code in 400..599) {
            val msg = conn.responseMessage?.takeIf { it.isNotBlank() } ?: ""
            errorMessage = if (msg.isNotBlank()) "HTTP $code $msg" else "HTTP $code"
            hasError = true
          }
        } catch (t: Throwable) {
          errorMessage = t.message ?: "Network error"
          hasError = true
        }
      }
    }
  }

  val posterRef = remember(item.url, item.posterUrl, item.thumbnailUrl) {
    item.posterUrl ?: item.thumbnailUrl
  }

  // Extract first frame poster when posterUrl is null
  LaunchedEffect(item.url, posterRef) {
    if (posterRef != null) return@LaunchedEffect
    val cached = AndroidPosterCache.get(item.url)
    if (cached != null) {
      generatedPoster = cached
      return@LaunchedEffect
    }
    withContext(Dispatchers.IO) {
      try {
        val retriever = MediaMetadataRetriever()
        if (item.url.startsWith("http://", ignoreCase = true) || item.url.startsWith("https://", ignoreCase = true)) {
          retriever.setDataSource(item.url, HashMap<String, String>())
        } else {
          retriever.setDataSource(item.url)
        }
        val bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        retriever.release()
        if (bitmap != null) {
          val imageBitmap = bitmap.asImageBitmap()
          AndroidPosterCache.put(item.url, imageBitmap)
          generatedPoster = imageBitmap
        }
      } catch (_: Throwable) {
        // Fallback to black background if extraction fails
      }
    }
  }

  fun posterModel(ref: String): Any {
    val trimmed = ref.trim()
    if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("file://")) {
      return trimmed
    }
    if (trimmed.startsWith("/")) {
      return File(trimmed)
    }
    return trimmed
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
          isFirstFrameRendered = false
          isPlaying = true
        }
      )
    } else {
      if (isPlaying) {
        AndroidNativeVideoPlayer(
          item = item,
          onFirstFrameRendered = { isFirstFrameRendered = true },
          onError = { err ->
            errorMessage = err
            hasError = true
          },
          modifier = Modifier.fillMaxSize()
        )
      }

      if (!isFirstFrameRendered) {
        if (posterRef != null) {
          AsyncImage(
            model = posterModel(posterRef),
            contentDescription = item.title ?: "Video poster",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
          )
        } else if (generatedPoster != null) {
          Image(
            bitmap = generatedPoster!!,
            contentDescription = item.title ?: "Video poster",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
          )
        }

        if (!isPlaying) {
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
  }
}

private object AndroidPosterCache {
  private val cache = object : java.util.LinkedHashMap<String, ImageBitmap>(16, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, ImageBitmap>?): Boolean {
      return size > 20
    }
  }
  @Synchronized fun get(url: String): ImageBitmap? = cache[url]
  @Synchronized fun put(url: String, bitmap: ImageBitmap) { cache[url] = bitmap }
}

@Composable
private fun AndroidNativeVideoPlayer(
  item: MediaItem,
  onFirstFrameRendered: () -> Unit,
  onError: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current

  val exoPlayer = remember(item.url) {
    ExoPlayer.Builder(context).build().apply {
      setMediaItem(ExoMediaItem.fromUri(Uri.parse(item.url)))
      playWhenReady = true
      prepare()
    }
  }

  DisposableEffect(exoPlayer) {
    val listener = object : Player.Listener {
      override fun onRenderedFirstFrame() {
        onFirstFrameRendered()
      }

      override fun onPlayerError(error: PlaybackException) {
        onError(error.message ?: "Playback error")
      }
    }
    exoPlayer.addListener(listener)
    onDispose {
      exoPlayer.removeListener(listener)
      exoPlayer.release()
    }
  }

  DisposableEffect(lifecycleOwner, exoPlayer) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_STOP || event == Lifecycle.Event.ON_PAUSE) {
        exoPlayer.pause()
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

  AndroidView(
    factory = { ctx ->
      PlayerView(ctx).apply {
        player = exoPlayer
        useController = true
        (videoSurfaceView as? SurfaceView)?.setZOrderMediaOverlay(true)
        layoutParams = FrameLayout.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.MATCH_PARENT
        )
      }
    },
    modifier = modifier,
    update = { view ->
      view.player = exoPlayer
      (view.videoSurfaceView as? SurfaceView)?.setZOrderMediaOverlay(true)
    }
  )
}
