plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.foryou"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.foryou"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        viewBinding = true
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
    kotlinOptions {
        jvmTarget = "11"
    }
    packaging {
        resources {
            excludes += setOf("META-INF/NOTICE.md", "META-INF/NOTICE", "META-INF/LICENSE", "META-INF/LICENSE.md")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.lottie)
    implementation ("com.google.android.material:material:1.9.0")
    implementation("com.sun.mail:android-mail:1.6.7") // Updated mail dependency for Android
    implementation("com.sun.mail:android-activation:1.6.7")
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")
    implementation ("com.google.android.gms:play-services-location:21.0.1")//location fetching dependencies
    implementation("androidx.cardview:cardview:1.0.0") //cardview
    implementation(libs.lottie.v600)
    implementation(libs.androidx.appcompat)
    implementation ("com.google.firebase:firebase-firestore-ktx:24.4.2")

    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.crashlytics.buildtools)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
