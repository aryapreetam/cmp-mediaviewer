package io.github.aryapreetam.cmpmediaviewer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.aryapreetam.cmpmediaviewer.TestTags

/**
 * Position indicator showing "X of Y".
 *
 * @param currentIndex Current item index (0-based)
 * @param totalCount Total number of items
 * @param modifier Modifier for positioning
 */
@Composable
internal fun PositionIndicator(
  currentIndex: Int,
  totalCount: Int,
  modifier: Modifier = Modifier
) {
  Text(
    text = "${currentIndex + 1} of $totalCount",
    color = Color.White,
    fontSize = 14.sp,
    modifier = modifier
      .padding(bottom = 32.dp)
      .background(
        color = Color.Black.copy(alpha = 0.5f),
        shape = RoundedCornerShape(16.dp)
      )
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .testTag(TestTags.PositionIndicator)
  )
}
