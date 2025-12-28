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
      implementation(compose.runtime)
      implementation(compose.ui)
      implementation(compose.foundation)
      implementation(compose.material3)
      implementation(compose.materialIconsExtended)
      
      // Coil for image loading
      implementation(libs.coil.compose)
      implementation(libs.coil.network.ktor)
      implementation(libs.ktor.client.core)
    }

    commonTest.dependencies {
      implementation(kotlin("test"))
      @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
      implementation(compose.uiTest)
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
