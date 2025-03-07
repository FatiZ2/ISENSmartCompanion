// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
}
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.google.gms:google-services:4.4.2")
        classpath (libs.hilt.android.gradle.plugin) //
        classpath("com.android.tools.build:gradle:8.0.0")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.44")

    }
}
