// build.gradle.kts (nivel proyecto)

buildscript {
    dependencies {
        classpath(libs.google.services.v4315)
    }
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    id("com.android.application") version "8.9.1" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
}
