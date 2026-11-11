import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.kugellabyrinth.wear"
    compileSdk = 35

    defaultConfig {
        // Gleiche applicationId wie die Handy-App: Im Play Store landen beide im selben Eintrag.
        applicationId = "com.example.kugellabyrinth"
        // Wear OS 3 und neuer (Pixel Watch 5: Wear OS 7)
        minSdk = 30
        targetSdk = 35
        // Uhr-Builds: 100000 + versionCode der Handy-App, damit die Nummern im Store nie kollidieren
        versionCode = 100005
        versionName = "1.4.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        abortOnError = true
        warningsAsErrors = false
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    // Gemeinsame Spiellogik, Sensor und Spielfeld. Keine Wear-/AndroidX-Bibliotheken.
    implementation(project(":core"))
}
