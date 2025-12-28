@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
  alias(libs.plugins.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.compose)
  alias(libs.plugins.android.application)
  alias(libs.plugins.composeHotReload)
}

kotlin {
  jvmToolchain(17)

  androidTarget()
  jvm()
  wasmJs {
    browser()
    binaries.executable()
  }
  listOf(
    iosX64(),
    iosArm64(),
    iosSimulatorArm64()
  ).forEach {
    it.binaries.framework {
      baseName = "ComposeApp"
      isStatic = true
    }
  }

  sourceSets {
    commonMain.dependencies {
      implementation(compose.runtime)
      implementation(compose.ui)
      implementation(compose.foundation)
      implementation(compose.material3)
      implementation(project(":lib"))
      
      // Coil for image loading in sample
      implementation(libs.coil.compose)
      implementation(libs.coil.network.ktor)
      implementation(libs.ktor.client.core)
    }

    commonTest.dependencies {
      implementation(libs.kotlin.test)
      @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
      implementation(compose.uiTest)
      implementation(libs.lifecycle.runtime.compose)
    }

    androidMain.dependencies {
      implementation(libs.androidx.activityCompose)
    }

    jvmMain.dependencies {
      implementation(compose.desktop.currentOs)
    }

  }
}

android {
  namespace = "sample.app"
  compileSdk = 35

  defaultConfig {
    minSdk = 21
    targetSdk = 35

    applicationId = "sample.app"
    versionCode = 1
    versionName = "1.0.0"
  }
}

compose.desktop {
  application {
    mainClass = "sample.app.MainKt"

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "MediaViewerSample"
      packageVersion = "1.0.0"
    }
  }
}

tasks.withType<Test>().configureEach {
  if (name.endsWith("UnitTest")) {
    exclude("**/*UITest*")
  }
}
