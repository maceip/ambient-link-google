pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// Shared vendor-neutral Android library (RelayClient/Session/GlassLink/…).
// Composite build: Gradle substitutes com.ambientlink:core-android with this
// local build. Requires ambient-link-core checked out as a sibling repo.
includeBuild("../ambient-link-core/core-android")

rootProject.name = "ambient-link-google"
include(":app")
include(":wear")
