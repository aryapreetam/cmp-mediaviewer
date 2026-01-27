rootProject.name = "cmp-mediaviewer"

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
    mavenCentral()
  }
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":lib")
include(":sample:composeApp")

includeBuild("/Users/preetam/workspace/cmp-videoplayer") {
  dependencySubstitution {
    substitute(module("io.github.aryapreetam:cmp-videoplayer")).using(project(":lib"))
  }
}

