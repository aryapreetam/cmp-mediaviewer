package io.github.aryapreetam.cmpmediaviewer.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
 * Next navigation button positioned at the center-right edge.
 */
@Composable
internal fun NextButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  IconButton(
    onClick = onClick,
    modifier = modifier
      .padding(16.dp)
      .testTag(TestTags.NextButton),
    colors = IconButtonDefaults.iconButtonColors(
      containerColor = Color.Black.copy(alpha = 0.5f),
      contentColor = Color.White
    )
  ) {
    Icon(
      imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
      contentDescription = "Next",
      modifier = Modifier.size(28.dp)
    )
  }
}

/**
 * Previous navigation button positioned at the center-left edge.
 */
@Composable
internal fun PreviousButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  IconButton(
    onClick = onClick,
    modifier = modifier
      .padding(16.dp)
      .testTag(TestTags.PreviousButton),
    colors = IconButtonDefaults.iconButtonColors(
      containerColor = Color.Black.copy(alpha = 0.5f),
      contentColor = Color.White
    )
  ) {
    Icon(
      imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
      contentDescription = "Previous",
      modifier = Modifier.size(28.dp)
    )
  }
}

