// build.gradle.kts (nivel proyecto)

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath(libs.google.services)
        classpath("com.google.gms:google-services:4.4.0")

    }
}


plugins {

    id("com.android.application") version "8.9.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false

    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1"
}
