plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.perf)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.axoloth.calculator.by.sky"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.axoloth.calculator.by.sky"
        minSdk = 24
        targetSdk = 36
        versionCode = 26
        versionName = "4.16.26"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.cardview)
    implementation(libs.play.services.ads)
    implementation(libs.androidx.gridlayout)
    implementation("com.airbnb.android:lottie:6.1.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.12.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-perf")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-config")
    implementation("com.google.firebase:firebase-inappmessaging-display")

    implementation("net.objecthunter:exp4j:0.4.8")
    implementation("com.google.android.material:material:1.13.0")

    // Room Database
    val roomVersion = "2.7.0-alpha13"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
     annotationProcessor("androidx.room:room-compiler:$roomVersion")

    // Networking for Currency (Kurs)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    implementation("com.google.firebase:firebase-appcheck-playintegrity")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
