// build.gradle.kts (nivel proyecto)

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath(libs.google.services)
    }
}

plugins {
    id("com.android.application") version "8.9.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    // Add the dependency for the Google services Gradle plugin
    id("com.google.gms.google-services") version "4.4.2" apply false
}
