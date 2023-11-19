plugins {
    alias(libs.plugins.androidApplication)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.yeonfish.watcher"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.yeonfish.watcher"
        minSdk = 33
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.firebase.messaging)
    implementation("com.android.support:support-compat:28.0.0")
    implementation("com.google.firebase:firebase-analytics")
    implementation("androidx.work:work-runtime:2.8.0")
    implementation(platform("com.google.firebase:firebase-bom:32.5.0"))
    implementation(files("libs/mysql-connector-java-5.1.49.jar"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}