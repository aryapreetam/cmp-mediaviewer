package sample.app

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
  Window(
    title = "MediaViewer Sample",
    state = rememberWindowState(size = DpSize(1024.dp, 768.dp)),
    onCloseRequest = ::exitApplication
  ) {
    App()
  }
}
