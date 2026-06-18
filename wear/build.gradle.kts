plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.ambientlink.watch"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ambientlink.watch"
        // Wear OS 4 (API 33) is a safe floor; Wear OS 5 is API 34.
        minSdk = 30
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
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Jetpack Compose.
    val composeBom = platform("androidx.compose:compose-bom:2024.09.03")
    implementation(composeBom)
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")

    // Wear Compose: rounded ScalingLazyColumn + Material components tuned for the wrist.
    // TODO(wear): pin to the Wear Compose release you build against.
    //   https://developer.android.com/training/wearables/compose
    implementation("androidx.wear.compose:compose-material3:1.0.0-alpha28")
    implementation("androidx.wear.compose:compose-foundation:1.4.0")

    // Wear data layer (phone <-> watch). Used only when the watch proxies through
    // the paired phone instead of hitting the relay over Wi-Fi/LTE directly.
    // implementation("com.google.android.gms:play-services-wearable:18.2.0")
}
