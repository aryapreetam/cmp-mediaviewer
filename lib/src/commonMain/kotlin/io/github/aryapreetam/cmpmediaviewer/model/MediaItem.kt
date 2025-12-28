package io.github.aryapreetam.cmpmediaviewer.model

/**
 * Represents the type of media content.
 */
public enum class MediaType {
  IMAGE,
  VIDEO
}

/**
 * Represents a single media item (image or video) to be displayed in the viewer.
 *
 * @property id Unique identifier for this media item
 * @property type The type of media (IMAGE or VIDEO)
 * @property url The full-resolution URL for this media
 * @property thumbnailUrl Optional lower-resolution thumbnail URL for progressive loading
 * @property title Optional title/caption for this media
 * @property posterUrl Optional poster image URL for videos (if not provided, a default icon is shown)
 */
public data class MediaItem(
  val id: String,
  val type: MediaType,
  val url: String,
  val thumbnailUrl: String? = null,
  val title: String? = null,
  val posterUrl: String? = null
) {
  public companion object {
    /**
     * Creates an image MediaItem.
     *
     * @param id Unique identifier
     * @param url Full-resolution image URL
     * @param thumbnailUrl Optional thumbnail URL for progressive loading
     * @param title Optional title/caption
     */
    public fun image(
      id: String,
      url: String,
      thumbnailUrl: String? = null,
      title: String? = null
    ): MediaItem = MediaItem(
      id = id,
      type = MediaType.IMAGE,
      url = url,
      thumbnailUrl = thumbnailUrl,
      title = title
    )

    /**
     * Creates a video MediaItem.
     *
     * @param id Unique identifier
     * @param url Video URL (MP4, WebM, etc.)
     * @param posterUrl Optional poster/thumbnail image URL
     * @param title Optional title/caption
     */
    public fun video(
      id: String,
      url: String,
      posterUrl: String? = null,
      title: String? = null
    ): MediaItem = MediaItem(
      id = id,
      type = MediaType.VIDEO,
      url = url,
      thumbnailUrl = posterUrl,
      title = title,
      posterUrl = posterUrl
    )

    /**
     * Attempts to detect media type from URL extension.
     *
     * @param url The media URL
     * @return MediaType.VIDEO for video extensions, MediaType.IMAGE otherwise
     */
    public fun detectType(url: String): MediaType {
      val extension = url.substringAfterLast('.', "").lowercase()
      return when (extension) {
        "mp4", "webm", "mov", "avi", "mkv", "m4v" -> MediaType.VIDEO
        else -> MediaType.IMAGE
      }
    }
  }
}
