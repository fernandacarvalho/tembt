plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    kotlin("android")
    alias(libs.plugins.googleServices) apply false
}

// Firebase auto-init only when the config file is present, so the build
// does not break in checkouts without google-services.json
if (file("google-services.json").exists()) {
    apply(plugin = libs.plugins.googleServices.get().pluginId)
}

android {
    namespace = "com.tembt.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.tembt.android"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
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
        // Required by dev.gitlive:firebase-config-android
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(project(":shared"))
    implementation(libs.kotlinx.datetime)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.foundation)
    implementation(libs.androidx.activity.compose)
    implementation(libs.lifecycle.runtime.compose)

    implementation(libs.androidx.splashscreen)
    implementation(libs.maplibre.android)

    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.compose.viewmodel)

    debugImplementation(libs.compose.ui.tooling)
}
