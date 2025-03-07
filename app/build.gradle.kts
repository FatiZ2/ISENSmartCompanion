import org.jetbrains.kotlin.gradle.plugin.KaptExtension
plugins {
    id ("com.android.application")
    id ("kotlin-android")
    id ("kotlin-kapt") // Pour Hilt
    id ("com.google.dagger.hilt.android") // Plugin Hilt
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
}

android {
    namespace = "fr.isen.chenani.isensmartcompanion"
    compileSdk = 35 // Utiliser une version stable pour éviter les incompatibilités

    defaultConfig {
        applicationId = "fr.isen.chenani.isensmartcompanion"
        minSdk = 25
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"


        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val openAiApiKey: String = project.findProperty("apiKey") as String? ?: ""
        buildConfigField("String", "apiKey", "\"$openAiApiKey\"")
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
        var kotlinCompilerExtensionVersion = ("1.5.3")
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildToolsVersion = "35.0.0"
}
dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("com.google.android.material:material:1.5.0")

    // Material Design 3 & Jetpack Compose
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.material:material:1.6.1")
    implementation(platform("androidx.compose:compose-bom:2023.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.ui:ui-tooling:1.6.1")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.3")

    // Retrofit + Gson (Networking)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

    // Hilt Dependency Injection
    implementation("com.google.dagger:hilt-android:2.44")
    kapt("com.google.dagger:hilt-compiler:2.44")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
    implementation("com.google.code.gson:gson:2.8.8")

    // Debugging Tools
    debugImplementation("androidx.compose.ui:ui-tooling:1.6.1")
    debugImplementation("androidx.compose.ui:ui-tooling-preview:1.6.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Firebase & Vertex AI
    implementation(platform("com.google.firebase:firebase-bom:33.10.0"))
    implementation("com.google.firebase:firebase-vertexai")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-common-ktx")
    implementation("com.google.firebase:firebase-appcheck-ktx")

    // Google AI Client SDK (Generative AI)
    implementation("com.google.ai.client.generativeai:generativeai:0.1.2")



    // Firebase Core
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    debugImplementation("androidx.compose.ui:ui-tooling-preview:1.6.1")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
