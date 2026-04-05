@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
  alias(libs.plugins.multiplatform)
  alias(libs.plugins.android.library)
  alias(libs.plugins.maven.publish)
  alias(libs.plugins.compose)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.dokka)
}

kotlin {
  jvmToolchain(17)

  androidTarget { publishLibraryVariants("release") }
  jvm()
  wasmJs { browser() }
  iosX64()
  iosArm64()
  iosSimulatorArm64()

  sourceSets {
    commonMain.dependencies {
      implementation(libs.compose.runtime)
      implementation(libs.compose.ui.multiplatform)
      implementation(libs.compose.foundation)
      implementation(libs.compose.material3)
      implementation(libs.compose.materialIconsExtended)

      // Video playback
      implementation("io.github.aryapreetam:cmp-videoplayer:0.0.1")
      
      // Coil for image loading
      implementation(libs.coil.compose)
      implementation(libs.coil.network.ktor)
      implementation(libs.ktor.client.core)
      
      // Zoomable image support
      implementation(libs.zoomimage.compose.coil)
    }
    
    // Mediamp for Android, iOS, Desktop (not Wasm)
    jvmMain.dependencies {
      implementation(libs.ktor.client.java)
      implementation(libs.mediamp.all)
    }
    
    androidMain.dependencies {
      implementation(libs.ktor.client.okhttp)
      implementation(libs.media3.exoplayer)
      implementation(libs.media3.ui)
      implementation(libs.lifecycle.runtime.compose)
      implementation(libs.mediamp.all)
    }
    
    iosMain.dependencies {
      implementation(libs.ktor.client.darwin)
      implementation(libs.mediamp.all)
    }

    commonTest.dependencies {
      implementation(kotlin("test"))
      @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
      implementation(libs.compose.ui.test)
    }
  }

  //https://kotlinlang.org/docs/native-objc-interop.html#export-of-kdoc-comments-to-generated-objective-c-headers
  targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
    compilations["main"].compileTaskProvider.configure {
      compilerOptions {
        freeCompilerArgs.add("-Xexport-kdoc")
      }
    }
  }
}

android {
  namespace = "io.github.aryapreetam.cmpmediaviewer"
  compileSdk = 35

  defaultConfig {
    minSdk = 21
  }
}

dependencies {
  dokkaPlugin(libs.android.documentation.plugin)
}

mavenPublishing {
  publishToMavenCentral()
  coordinates("io.github.aryapreetam", "cmp-mediaviewer", "0.0.1")

  pom {
    name = "Media Viewer for Compose Multiplatform"
    description = "Full-screen media viewer (images & videos) for Compose Multiplatform"
    url = "https://aryapreetam.github.io/cmp-mediaviewer"

    licenses {
      license {
        name = "MIT"
        url = "https://opensource.org/licenses/MIT"
      }
    }

    developers {
      developer {
        id = "aryapreetam"
        name = "Preetam Bhosle"
      }
    }

    scm {
      url = "https://github.com/aryapreetam/cmp-mediaviewer"
    }
  }
  if (project.hasProperty("signing.keyId") || project.hasProperty("signingInMemoryKey")) {
    signAllPublications()
  }
}
