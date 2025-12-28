package io.github.aryapreetam.cmpmediaviewer.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.aryapreetam.cmpmediaviewer.TestTags

/**
 * Close button for the media viewer.
 * Positioned at top-right with a semi-transparent background.
 *
 * @param onClick Callback when the button is clicked
 * @param modifier Modifier for positioning
 */
@Composable
internal fun CloseButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  IconButton(
    onClick = onClick,
    modifier = modifier
      .padding(16.dp)
      .testTag(TestTags.CloseButton),
    colors = IconButtonDefaults.iconButtonColors(
      containerColor = Color.Black.copy(alpha = 0.5f),
      contentColor = Color.White
    )
  ) {
    Icon(
      imageVector = Icons.Default.Close,
      contentDescription = "Close",
      modifier = Modifier.size(24.dp)
    )
  }
}
