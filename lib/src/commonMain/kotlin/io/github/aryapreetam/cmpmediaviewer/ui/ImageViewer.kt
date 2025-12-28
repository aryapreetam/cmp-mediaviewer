package io.github.aryapreetam.cmpmediaviewer.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import io.github.aryapreetam.cmpmediaviewer.LocalImageLoader
import io.github.aryapreetam.cmpmediaviewer.model.MediaItem

/**
 * Displays a single image with loading/error states.
 *
 * @param item The media item to display
 * @param modifier Modifier for the image container
 */
@Composable
internal fun ImageViewer(
  item: MediaItem,
  modifier: Modifier = Modifier
) {
  val context = LocalPlatformContext.current
  val imageLoader = LocalImageLoader.current

  val request = ImageRequest.Builder(context)
    .data(item.url)
    .crossfade(true)
    .apply {
      // Use thumbnail as placeholder if available
      item.thumbnailUrl?.let { placeholderMemoryCacheKey(it) }
    }
    .build()

  if (imageLoader != null) {
    AsyncImage(
      model = request,
      contentDescription = item.title ?: "Media item",
      imageLoader = imageLoader,
      contentScale = ContentScale.Fit,
      modifier = modifier.fillMaxSize()
    )
  } else {
    // Use default singleton image loader
    AsyncImage(
      model = request,
      contentDescription = item.title ?: "Media item",
      contentScale = ContentScale.Fit,
      modifier = modifier.fillMaxSize()
    )
  }
}
