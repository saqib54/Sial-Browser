plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.sialbrowser"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.sialbrowser"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Core Android libraries
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Google Sign-In
    implementation(libs.play.services.auth)

    // WebView enhancements
    implementation(libs.androidx.webkit)

    // Image loading (for user profile picture)
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Browser components
    implementation(libs.androidx.browser)

    // Progress indicators
    implementation(libs.androidx.swiperefreshlayout)

    // ViewPager2 for tabs (now using TOML alias)
    implementation(libs.androidx.viewpager2)
}