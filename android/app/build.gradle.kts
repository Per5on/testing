plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "bg.prices"
    compileSdk = 35

    defaultConfig {
        applicationId = "bg.prices"
        val apiBaseUrl = providers.gradleProperty("BG_PRICES_API_BASE_URL").orElse("").get()
        buildConfigField("String", "API_BASE_URL", "\"${apiBaseUrl}\"")
        minSdk = 26
        targetSdk = 35
        // GitHub Actions supplies a monotonically increasing build number.
        // Local Android Studio builds keep the defaults below.
        versionCode = providers.gradleProperty("APP_VERSION_CODE").orElse("1").get().toInt()
        versionName = providers.gradleProperty("APP_VERSION_NAME").orElse("0.1.0").get()
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
