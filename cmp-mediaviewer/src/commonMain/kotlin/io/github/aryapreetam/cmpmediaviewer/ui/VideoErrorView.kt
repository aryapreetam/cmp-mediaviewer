package io.github.aryapreetam.cmpmediaviewer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.aryapreetam.cmpmediaviewer.TestTags

/**
 * Displayed when a video fails to play or load due to invalid, expired, or unsupported source.
 */
@Composable
internal fun VideoErrorView(
  title: String? = null,
  errorMessage: String? = null,
  onRetry: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF181818))
      .padding(24.dp)
      .semantics(mergeDescendants = true) {}
      .testTag(TestTags.VideoError),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Icon(
      imageVector = Icons.Default.Warning,
      contentDescription = "Video Error",
      tint = Color(0xFFFFB74D),
      modifier = Modifier.size(56.dp)
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = "Unable to play video",
      style = MaterialTheme.typography.titleLarge,
      color = Color.White,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    val detailText = when {
      !errorMessage.isNullOrBlank() && !title.isNullOrBlank() -> "\"$title\": $errorMessage"
      !errorMessage.isNullOrBlank() -> errorMessage
      !title.isNullOrBlank() -> "\"$title\" is unavailable or could not be loaded."
      else -> "This video is unavailable or could not be loaded."
    }

    Text(
      text = detailText,
      style = MaterialTheme.typography.bodyMedium,
      color = Color.LightGray,
      textAlign = TextAlign.Center
    )

    if (onRetry != null) {
      Spacer(modifier = Modifier.height(20.dp))

      Button(
        onClick = onRetry,
        colors = ButtonDefaults.buttonColors(
          containerColor = Color.White.copy(alpha = 0.2f),
          contentColor = Color.White
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.testTag(TestTags.VideoRetryButton)
      ) {
        Text("Retry")
      }
    }
  }
}

