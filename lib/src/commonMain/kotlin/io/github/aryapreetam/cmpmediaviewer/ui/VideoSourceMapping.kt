package io.github.aryapreetam.cmpmediaviewer.ui

import io.github.aryapreetam.cmpmediaviewer.model.MediaItem
import io.github.aryapreetam.cmpvideoplayer.VideoSource

internal fun MediaItem.toVideoSource(): VideoSource {
  val raw = url
  val lower = raw.lowercase()
  return when {
    lower.startsWith("http://") ||
      lower.startsWith("https://") ||
      lower.startsWith("file://") ||
      lower.startsWith("content://") ||
      lower.startsWith("android.resource://") ||
      lower.startsWith("blob:") -> VideoSource.Url(raw)

    raw.startsWith("/") -> VideoSource.Path(raw)

    else -> VideoSource.Url(raw)
  }
}
