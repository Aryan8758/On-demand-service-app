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
            excludes += setOf(
                "META-INF/NOTICE.md",
                "META-INF/NOTICE",
                "META-INF/LICENSE",
                "META-INF/LICENSE.md",
                "mozilla/public-suffix-list.txt",
                "META-INF/DEPENDENCIES" // 🔥 Add this line
            )
        }
    }
}

dependencies {
    implementation ("com.google.firebase:firebase-messaging:23.2.1")
    // Volley for API requests (for sending push notifications)
    implementation ("com.android.volley:volley:1.2.1")
    // Gson for JSON parsing
    implementation ("com.google.code.gson:gson:2.9.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.lottie)
    implementation ("com.google.android.material:material:1.9.0")
    implementation("com.sun.mail:android-mail:1.6.7") // Updated mail dependency for Android
    implementation("com.sun.mail:android-activation:1.6.7")
    implementation ("com.google.android.gms:play-services-location:21.0.1")//location fetching dependencies
    implementation("androidx.cardview:cardview:1.0.0") //cardview
    implementation(libs.lottie.v600)
    implementation(libs.androidx.appcompat)
    implementation ("com.google.firebase:firebase-firestore-ktx:24.4.2")
    implementation ("com.facebook.shimmer:shimmer:0.5.0")
    implementation(libs.material)
    implementation ("com.google.auth:google-auth-library-oauth2-http:1.14.0")
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")
    implementation ("org.json:json:20210307")
    implementation ("com.diogobernardino:williamchart:3.10.1")

    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.gridlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
