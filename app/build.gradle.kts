plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.ambientlink.glasses"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ambientlink.glasses"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        // Relay base URL. Override: -PambientLinkRelay=https://your.host
        val relay = (project.findProperty("ambientLinkRelay") as String?)
            ?: "https://public.computer"
        buildConfigField("String", "AMBIENT_LINK_RELAY", "\"$relay\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    // Shared relay client + models + GlassLink/EphemeralBuffer (brings coroutines via api).
    implementation("com.ambientlink:core-android:0.1.0")

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Jetpack Compose (used by Glimmer for the projected UI).
    val composeBom = platform("androidx.compose:compose-bom:2024.09.03")
    implementation(composeBom)
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.compose.foundation:foundation")

    // TODO(xr): Pin these to the Jetpack XR Developer Preview you build against.
    //   Jetpack XR SDK: https://developer.android.com/develop/xr/jetpack-xr-sdk
    //   - androidx.xr.* "Projected" APIs: ProjectedDeviceController / ProjectedContext
    //   - Jetpack Compose Glimmer: glasses-optimized Compose UI toolkit
    // implementation("androidx.xr.projected:projected:<preview-version>")
    // implementation("androidx.xr.glimmer:glimmer:<preview-version>")
}
