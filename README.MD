<h1 align="center">CMP Media Viewer - Compose Multiplatform Media Viewer Library</h1>

<p align="center">
  <img src="https://img.shields.io/badge/Compose%20Multiplatform-v1.10.1-4285F4?logo=jetpackcompose" alt="Compose Multiplatform Version">
  <img src="https://img.shields.io/badge/Kotlin-v2.3.21-blueviolet?logo=kotlin" alt="Kotlin Version">
</p>

<p align="center">
  <a href="https://github.com/aryapreetam/cmp-mediaviewer/actions/workflows/release.yml">
    <img src="https://github.com/aryapreetam/cmp-mediaviewer/actions/workflows/release.yml/badge.svg" alt="Release workflow status">
  </a>
  <a href="https://mvnrepository.com/artifact/io.github.aryapreetam/cmp-mediaviewer">
    <img src="https://img.shields.io/maven-central/v/io.github.aryapreetam/cmp-mediaviewer?label=Maven%20Central&color=blue" alt="Maven Central Version">
  </a>
  <a href="https://github.com/aryapreetam/cmp-mediaviewer/blob/main/LICENSE">
    <img src="https://img.shields.io/badge/License-MIT-green.svg" alt="MIT License">
  </a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=white" alt="Android">
  <img src="https://img.shields.io/badge/iOS-000000?logo=apple&logoColor=white" alt="iOS">
  <img src="https://img.shields.io/badge/Desktop/JVM-0078D6?logo=desktop&logoColor=white" alt="Desktop">
  <img src="https://img.shields.io/badge/Web(wasm)-4285F4?logo=google-chrome&logoColor=white" alt="Web">
</p>

A full-screen media viewer library for Compose Multiplatform that enables displaying images and videos with pinch-to-zoom, panning, and swipe-to-dismiss support across Android, iOS, Desktop/JVM, and Web/WASM.

![cmp-mediaviewer demo](readme_images/demo.gif)

**Key Features:**

- 🖼️ **Image Viewer**: Pinch-to-zoom, double-tap zoom, and panning gestures.
- 🎥 **Video Viewer**: Video playback using platform-native players (Media3/ExoPlayer on Android, AVPlayer on iOS, VLC on Desktop, HTML5 on Web/Wasm).
- 🔁 **Horizontal Pager**: Swipe navigation between multiple media items in a slideshow/viewer session.
- 📍 **Position Indicator**: UI counter displaying the current page index and total items.
- ♿ **Keyboard Navigation**: Close on Escape key press, navigate pages using Left/Right arrow keys on Desktop and Web.

---

## ✅ Platform Capability Matrix

| Capability | Android | iOS | Desktop (JVM) | Web (WASM) |
|---|---:|---:|---:|---:|
| Image Viewer | ✅ | ✅ | ✅ | ✅ |
| Video Viewer | ✅ | ✅ | ✅* | ✅ |
| Pinch-to-zoom / Pan | ✅ | ✅ | ✅ | ✅ |
| Swipe Navigation | ✅ | ✅ | ✅ | ✅ |
| Keyboard Listeners | ❌ | ❌ | ✅ | ✅ |

*\*Desktop video playback relies on native VLC libraries (`libvlc`). Host machines must have VLC Media Player installed (`brew install --cask vlc` on macOS, `sudo apt install vlc libvlc-dev` on Linux). If VLC is not installed, the viewer displays an in-app notice with a retry option.*

Wasm Demo: 
[![Try Live Demo](https://img.shields.io/badge/Try%20Live%20Demo-Web%20wasmJs-blueviolet?style=for-the-badge)](https://aryapreetam.github.io/cmp-mediaviewer/demo/)

API Docs:
[![Library API Docs](https://img.shields.io/badge/API%20Reference-Library%20Docs-green?style=for-the-badge)](https://aryapreetam.github.io/cmp-mediaviewer/api/)

---

## 📦 Installation

Add the dependency to your Compose Multiplatform project:

**Option A — Version catalog (recommended)**

1) In `gradle/libs.versions.toml`:

```toml
[versions]
cmpMediaviewer = "0.0.3"

[libraries]
cmp-mediaviewer = { module = "io.github.aryapreetam:cmp-mediaviewer", version.ref = "cmpMediaviewer" }
```

2) In your module's `build.gradle.kts`:

```kotlin
kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(libs.cmp.mediaviewer)
      }
    }
  }
}
```

**Option B — Direct dependency**

```kotlin
kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation("io.github.aryapreetam:cmp-mediaviewer:0.0.3")
      }
    }
  }
}
```

---

## 🚀 Quick Start

To open the full-screen media viewer component:

```kotlin
import io.github.aryapreetam.cmpmediaviewer.MediaViewer
import io.github.aryapreetam.cmpmediaviewer.model.MediaItem

@Composable
fun MyMediaScreen() {
  var showViewer by remember { mutableStateOf(false) }

  Column(modifier = Modifier.fillMaxSize()) {
    Button(onClick = { showViewer = true }) {
      Text("Open Media Viewer")
    }

    if (showViewer) {
      MediaViewer(
        items = listOf(
          MediaItem.image(id = "1", url = "https://example.com/photo.jpg", title = "Nature Photo"),
          MediaItem.video(id = "2", url = "https://example.com/clip.mp4", title = "Promo Video")
        ),
        initialIndex = 0,
        onDismiss = { showViewer = false },
        onPageChanged = { index -> println("Viewing index: $index") }
      )
    }
  }
}
```

---

## 📦 Download Sample App Executables

Download and try the sample app on your platform without building from source:

| Platform | Download Link |
|----------|--------------|
| 🍏 **macOS (Intel)** | [![Download](https://img.shields.io/badge/Download--Mac--x64--DMG-blue?style=for-the-badge)](https://github.com/aryapreetam/cmp-mediaviewer/releases/latest/download/sample-app-desktop-x64.dmg) |
| 🍎 **macOS (Apple Silicon)** | [![Download](https://img.shields.io/badge/Download--Mac--arm64--DMG-blue?style=for-the-badge)](https://github.com/aryapreetam/cmp-mediaviewer/releases/latest/download/sample-app-desktop-arm64.dmg) |
| 🪟 **Windows** | [![Download](https://img.shields.io/badge/Download--Windows--MSI-0078D4?style=for-the-badge&logo=windows)](https://github.com/aryapreetam/cmp-mediaviewer/releases/latest/download/sample-app-windows.msi) |
| 🐧 **Linux** | [![Download](https://img.shields.io/badge/Download--Linux--DEB-E95420?style=for-the-badge&logo=ubuntu)](https://github.com/aryapreetam/cmp-mediaviewer/releases/latest/download/sample-app-linux.deb) |
| 🤖 **Android** | [![Download](https://img.shields.io/badge/Download--Android--APK-green?style=for-the-badge&logo=android)](https://github.com/aryapreetam/cmp-mediaviewer/releases/latest/download/sample-app-android-unsigned.apk) |
| 🌐 **Web (Wasm)** | [![Download](https://img.shields.io/badge/Download--Web--Wasm--zip-purple?style=for-the-badge)](https://github.com/aryapreetam/cmp-mediaviewer/releases/latest/download/sample-app-wasm.zip) |
|  **iOS Simulator** | [![Download](https://img.shields.io/badge/Download--iOS--Simulator--zip-orange?style=for-the-badge&logo=apple)](https://github.com/aryapreetam/cmp-mediaviewer/releases/latest/download/sample-app-ios-simulator.zip) |

### 🚀 How to Run the Sample App

For instructions on how to install and run the pre-built executables or build and run the sample application from source code, please refer to the **[How to Run the Sample App](docs/run-sample-app.md)** guide.

---

## License

MIT License © 2025 aryapreetam and contributors. See [LICENSE](./LICENSE) for details.

---

## 🤝 Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed contribution guidelines.
