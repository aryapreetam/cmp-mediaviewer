package sample.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.aryapreetam.cmpmediaviewer.MediaViewer
import io.github.aryapreetam.cmpmediaviewer.model.MediaItem
import io.github.aryapreetam.cmpmediaviewer.model.MediaType

/**
 * Sample app demonstrating MediaViewer library usage with images and videos.
 */
@Composable
fun App() {
  MaterialTheme {
    var showViewer by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableIntStateOf(0) }
    
    val mediaItems = remember { getSampleMedia() }
    
    Box(modifier = Modifier.fillMaxSize()) {
      // Gallery grid
      GalleryScreen(
        items = mediaItems,
        onItemClick = { index ->
          selectedIndex = index
          showViewer = true
        }
      )
      
      // MediaViewer overlay
      if (showViewer) {
        MediaViewer(
          items = mediaItems,
          initialIndex = selectedIndex,
          onDismiss = { showViewer = false },
          onPageChanged = { newIndex ->
            selectedIndex = newIndex
          }
        )
      }
    }
  }
}

@Composable
private fun GalleryScreen(
  items: List<MediaItem>,
  onItemClick: (Int) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF121212))
      .padding(16.dp)
  ) {
    Text(
      text = "MediaViewer Sample",
      style = MaterialTheme.typography.headlineMedium,
      color = Color.White,
      modifier = Modifier.padding(bottom = 8.dp)
    )
    
    Text(
      text = "Images: pinch-to-zoom, double-tap zoom. Videos: tap play button.",
      style = MaterialTheme.typography.bodyMedium,
      color = Color.Gray,
      modifier = Modifier.padding(bottom = 8.dp)
    )
    
    Text(
      text = "Note: Desktop video requires VLC Player installed.",
      style = MaterialTheme.typography.bodySmall,
      color = Color.Yellow.copy(alpha = 0.7f),
      modifier = Modifier.padding(bottom = 16.dp)
    )
    
    LazyVerticalGrid(
      columns = GridCells.Adaptive(minSize = 150.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      itemsIndexed(items) { index, item ->
        ThumbnailCard(
          item = item,
          onClick = { onItemClick(index) }
        )
      }
    }
  }
}

@Composable
private fun ThumbnailCard(
  item: MediaItem,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .aspectRatio(1f)
      .clip(RoundedCornerShape(8.dp))
      .clickable(onClick = onClick),
    colors = CardDefaults.cardColors(
      containerColor = Color(0xFF1E1E1E)
    )
  ) {
    Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      // Thumbnail image (or video poster)
      val imageUrl = when {
        item.type == MediaType.VIDEO && item.posterUrl != null -> item.posterUrl
        item.type == MediaType.VIDEO -> null // Will show play icon
        else -> item.thumbnailUrl ?: item.url
      }
      
      if (imageUrl != null) {
        AsyncImage(
          model = imageUrl,
          contentDescription = item.title,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
      } else {
        // Fallback for videos without poster
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2A2A2A)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Video",
            tint = Color.White,
            modifier = Modifier.size(48.dp)
          )
        }
      }
      
      // Video indicator overlay
      if (item.type == MediaType.VIDEO) {
        Box(
          modifier = Modifier
            .align(Alignment.Center)
            .size(40.dp)
            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(20.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Video",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
          )
        }
      }
      
      // Title overlay at bottom
      item.title?.let { title ->
        Box(
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(8.dp)
        ) {
          Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            maxLines = 1
          )
        }
      }
    }
  }
}

/**
 * Sample media including images and videos.
 */
private fun getSampleMedia(): List<MediaItem> = listOf(
  // Images
  MediaItem.image(
    id = "1",
    url = "https://uploads0.wikiart.org/00475/images/salvador-dali/w1siziisijm4njq3mcjdlfsiccisimnvbnzlcnqilcitcxvhbgl0esa5mcatcmvzaxplidiwmdb4mjawmfx1mdazzsjdxq.jpg",
    title = "The Persistence of Memory"
  ),
  MediaItem.image(
    id = "2",
    url = "https://uploads8.wikiart.org/00339/images/leonardo-da-vinci/mona-lisa-c-1503-1519.jpg",
    title = "Mona Lisa"
  ),
  MediaItem.image(
    id = "3",
    url = "https://uploads3.wikiart.org/00142/images/vincent-van-gogh/the-starry-night.jpg",
    title = "The Starry Night"
  ),
  MediaItem.image(
    id = "4",
    url = "https://uploads8.wikiart.org/00129/images/claude-monet/impression-sunrise.jpg",
    title = "Impression, Sunrise"
  ),
  
  // Videos
  MediaItem.video(
    id = "v1",
    url = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
    title = "Big Buck Bunny"
  ),
  MediaItem.video(
    id = "v2",
    url = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
    title = "Elephants Dream"
  ),
  MediaItem.video(
    id = "v3",
    url = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
    title = "For Bigger Blazes"
  ),
  MediaItem.video(
    id = "v4",
    url = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
    title = "For Bigger Escapes"
  ),
  
  // More images
  MediaItem.image(
    id = "5",
    url = "https://uploads6.wikiart.org/00142/images/57726d7eedc2cb3880b47e13/the-kiss-gustav-klimt-google-cultural-institute.jpg",
    title = "The Kiss"
  ),
  MediaItem.image(
    id = "6",
    url = "https://uploads1.wikiart.org/images/edvard-munch/the-scream-1893(2).jpg",
    title = "The Scream"
  )
)
