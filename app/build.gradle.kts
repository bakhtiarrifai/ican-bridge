import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "dev.masbah.icanbridge"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.masbah.icanbridge"
        minSdk = 26
        targetSdk = 36
        versionCode = 4
        versionName = "0.2.2"
    }

    signingConfigs {
        create("release") {
            val keystoreProps = Properties()
            val keystorePropsFile = rootProject.file("keystore.properties")
            if (keystorePropsFile.exists()) {
                keystoreProps.load(FileInputStream(keystorePropsFile))
            }
            val keyFile = rootProject.file("keystore/ican-bridge-release.keystore")
            if (keyFile.exists()) {
                storeFile = keyFile
                storePassword = keystoreProps.getProperty("storePassword", "")
                keyAlias = keystoreProps.getProperty("keyAlias", "ican-bridge")
                keyPassword = keystoreProps.getProperty("keyPassword", "")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            val keyFile = rootProject.file("keystore/ican-bridge-release.keystore")
            if (keyFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-ktx:1.9.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Health Connect
    implementation("androidx.health.connect:connect-client:1.1.0-alpha07")

    testImplementation("junit:junit:4.13.2")
}
