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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.aryapreetam.cmpmediaviewer.TestTags
import io.github.aryapreetam.cmpmediaviewer.model.MediaItem
import io.github.aryapreetam.cmpvideoplayer.VideoPlayer
import io.github.aryapreetam.cmpvideoplayer.VideoPlayerConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

internal object VlcDiscoveryHelper {
  private var initialized: Boolean? = null

  fun isAvailable(): Boolean {
    initialized?.let { return it }
    return try {
      val os = System.getProperty("os.name", "").lowercase()
      if (os.contains("mac")) {
        val candidates = listOf(
          File("/Applications/VLC.app/Contents/MacOS/lib"),
          File(System.getProperty("user.home"), "Applications/VLC.app/Contents/MacOS/lib")
        )
        for (macVlcDir in candidates) {
          val vlcCore = File(macVlcDir, "libvlccore.dylib")
          val vlc = File(macVlcDir, "libvlc.dylib")
          if (vlcCore.exists() && vlc.exists()) {
            try {
              System.load(vlcCore.absolutePath)
              System.load(vlc.absolutePath)
              System.setProperty("jna.library.path", macVlcDir.absolutePath)
              uk.co.caprica.vlcj.binding.lib.LibC.INSTANCE.setenv(
                "VLC_PLUGIN_PATH",
                File(macVlcDir.parentFile, "plugins").absolutePath,
                1
              )
            } catch (_: Throwable) {}
            break
          }
        }
      } else if (os.contains("linux")) {
        val linuxCandidates = listOf(
          File("/usr/lib/x86_64-linux-gnu"),
          File("/usr/lib/aarch64-linux-gnu"),
          File("/usr/lib"),
          File("/usr/local/lib")
        )
        for (dir in linuxCandidates) {
          val vlc = File(dir, "libvlc.so")
          val vlc5 = File(dir, "libvlc.so.5")
          if (vlc.exists() || vlc5.exists()) {
            try {
              val currentJna = System.getProperty("jna.library.path")
              val newJna = if (currentJna.isNullOrBlank()) dir.absolutePath else "$currentJna:${dir.absolutePath}"
              System.setProperty("jna.library.path", newJna)
              val pluginDir = File(dir, "vlc/plugins").takeIf { it.exists() }
                ?: File("/usr/lib/vlc/plugins").takeIf { it.exists() }
              if (pluginDir != null) {
                uk.co.caprica.vlcj.binding.lib.LibC.INSTANCE.setenv(
                  "VLC_PLUGIN_PATH",
                  pluginDir.absolutePath,
                  1
                )
              }
            } catch (_: Throwable) {}
            break
          }
        }
      }
      val discovery = uk.co.caprica.vlcj.factory.discovery.NativeDiscovery()
      val discovered = discovery.discover()
      val hasJna = System.getProperty("jna.library.path") != null
      val version = try { uk.co.caprica.vlcj.binding.lib.LibVlc.libvlc_get_version() } catch (_: Throwable) { null }
      val ok = (discovered || hasJna) && version != null
      initialized = ok
      ok
    } catch (_: Throwable) {
      initialized = false
      false
    }
  }
}

/** Desktop video viewer delegating playback to `cmp-videoplayer`. */
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

  val isNativePlayerAvailable = remember { VlcDiscoveryHelper.isAvailable() }

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

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black)
      .testTag(TestTags.VideoControls),
    contentAlignment = Alignment.Center
  ) {
    if (hasError || (isPlaying && !isNativePlayerAvailable)) {
      VideoErrorView(
        title = item.title,
        errorMessage = if (!isNativePlayerAvailable) "VLC native library not found" else errorMessage,
        onRetry = {
          hasError = false
          errorMessage = null
          isPlaying = true
        }
      )
    } else if (isPlaying) {
      VideoPlayer(
        source = item.toVideoSource(),
        modifier = Modifier.fillMaxSize(),
        config = VideoPlayerConfig(autoplay = true),
        placeholder = {
          VideoErrorView(
            title = item.title,
            errorMessage = errorMessage ?: "Playback failed",
            onRetry = { isPlaying = false }
          )
        }
      )
    } else {
      IconButton(
        onClick = { isPlaying = true },
        modifier = Modifier
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
