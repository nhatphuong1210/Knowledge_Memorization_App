plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    id("org.jetbrains.kotlin.kapt")
    id ("kotlin-parcelize")
}

android {
    namespace = "com.example.knowledgememorizationapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.knowledgememorizationapp"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
    }
buildFeatures {
    viewBinding {
        enable = true
    }
    packagingOptions {
        exclude("META-INF/*")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))

    // Add the KTX version
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation ("com.airbnb.android:lottie:6.1.0")

    implementation ("com.google.android.gms:play-services-auth:21.0.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation ("com.google.android.material:material:1.11.0")

    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.firestore)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.ui.text.android)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //Android Core & Material Design UI
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0") 
    implementation("androidx.multidex:multidex:2.0.1")

// pic
    implementation("com.github.denzcoskun:ImageSlideshow:0.1.2")
    implementation("com.facebook.shimmer:shimmer:0.5.0@aar")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")
//Firebase & xác thực người dùng
    implementation("com.google.firebase:firebase-auth:22.1.1")
    implementation("com.google.android.gms:play-services-auth:20.5.0")
    implementation("com.facebook.android:facebook-android-sdk:16.0.0")
// Firebase Storage & Messaging
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    implementation("com.google.firebase:firebase-messaging:23.2.0")
//Networking & JSON
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    implementation("com.google.code.gson:gson:2.8.6")
//tk
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
//Excel
    implementation("org.apache.poi:poi:5.2.3")
    implementation("org.apache.poi:poi-ooxml:5.2.3")
}