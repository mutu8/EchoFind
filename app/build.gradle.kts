plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services") version "4.4.2"
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.example.echofind"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.echofind"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Firebase BoM
    implementation(platform(libs.firebase.bom))
    implementation(libs.google.firebase.auth.ktx)
    implementation(libs.play.services.auth)
    implementation(libs.play.services.base)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)

    // OnBoarding
    implementation(libs.accompanist.pager)

    // Lottie
    implementation(libs.lottie.compose)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Datastore
    implementation(libs.androidx.datastore.preferences)

    // Graphics.paint
    implementation(libs.androidx.core.ktx.v190)
    implementation(libs.androidx.appcompat)
    implementation(libs.transport.runtime)

    //twyper
    implementation("com.github.theapache64:twyper:0.0.4")
    implementation(libs.androidx.runtime.saveable.v100)
    implementation(libs.ui.graphics)
    implementation(libs.androidx.runtime.livedata)

    // Retrofit y Gson
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // OkHttp Logging
    implementation(libs.logging.interceptor)

    // Coil (opcional, para cargar imágenes)
    implementation(libs.coil.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Lifecycle Compose
    implementation(libs.androidx.lifecycle.runtime.ktx.v261)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // MediaPlayer
    implementation(libs.exoplayer)

    //Font
    implementation(libs.androidx.ui.text)

    //Material3 icons
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.material)

    //room
    implementation(libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    //Lectura de json
    implementation(libs.gson)

    //spotify
    implementation(libs.auth)


    //-- Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}