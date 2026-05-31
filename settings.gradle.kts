rootProject.name = "cmp-mediaviewer-root"

pluginManagement {
  repositories {
    google {
      content {
        includeGroupByRegex("com\\.android.*")
        includeGroupByRegex("com\\.google.*")
        includeGroupByRegex("androidx.*")
        includeGroupByRegex("android.*")
      }
    }
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
    gradlePluginPortal()
    mavenCentral()
  }
}

dependencyResolutionManagement {
  repositories {
    google {
      content {
        includeGroupByRegex("com\\.android.*")
        includeGroupByRegex("com\\.google.*")
        includeGroupByRegex("androidx.*")
        includeGroupByRegex("android.*")
      }
    }
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    mavenCentral()
    mavenLocal()
  }
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":cmp-mediaviewer")
include(":sample:composeApp")
include(":sample:androidApp")


// includeBuild("../cmp-videoplayer") {
//   dependencySubstitution {
//     substitute(module("io.github.aryapreetam:cmp-videoplayer")).using(project(":cmp-videoplayer"))
//   }
// }
