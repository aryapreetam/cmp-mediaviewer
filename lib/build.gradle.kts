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
    }

    commonTest.dependencies {
      implementation(kotlin("test"))
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

//Publishing your Kotlin Multiplatform library to Maven Central
//https://www.jetbrains.com/help/kotlin-multiplatform-dev/multiplatform-publish-libraries.html
mavenPublishing {
  publishToMavenCentral()
  coordinates("io.github.aryapreetam", "cmp-mediaviewer", "0.0.1")

  pom {
    name = "Media Viewer for Compose Multiplatform"
    description = "Media Viewer(Images & Videos) for Compose Multiplatform"
    url = "https://aryapreetam.github.io/cmp-mediaviewer" //todo

    licenses {
      license {
        name = "MIT"
        url = "https://opensource.org/licenses/MIT"
      }
    }

    developers {
      developer {
        id = "aryapreetam" //todo
        name = "Preetam Bhosle" //todo
      }
    }

    scm {
      url = "https://github.com/aryapreetam/cmp-mediaviewer" //todo
    }
  }
  // Sign publications if either local keyId or CI signingInMemoryKey is available
  if (project.hasProperty("signing.keyId") || project.hasProperty("signingInMemoryKey")) {
    signAllPublications()
  }
}
