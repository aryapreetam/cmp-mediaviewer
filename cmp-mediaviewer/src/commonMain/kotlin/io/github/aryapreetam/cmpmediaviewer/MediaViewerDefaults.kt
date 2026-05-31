package io.github.aryapreetam.cmpmediaviewer

import androidx.compose.runtime.compositionLocalOf
import coil3.ImageLoader
import coil3.PlatformContext
import io.github.aryapreetam.cmpmediaviewer.model.MediaViewerConfig

/**
 * CompositionLocal for MediaViewer configuration.
 * Override this to customize viewer behavior in tests or specific UI trees.
 */
public val LocalMediaViewerConfig = compositionLocalOf<MediaViewerConfig> {
  MediaViewerConfig.Default
}

/**
 * CompositionLocal for Coil ImageLoader.
 * Override this in tests with a fake ImageLoader for synchronous image loading.
 *
 * Note: Default is null, and the viewer will use SingletonImageLoader if not provided.
 */
public val LocalImageLoader = compositionLocalOf<ImageLoader?> {
  null
}
