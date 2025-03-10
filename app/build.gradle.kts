import org.gradle.kotlin.dsl.implementation

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.manish.demoofimagecompereason"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.manish.demoofimagecompereason"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    buildFeatures{
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.mlkit.text.recognition.common)
    implementation(libs.androidx.camera.lifecycle)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Use this dependency to use the dynamically downloaded model in Google Play Services
    implementation (libs.play.services.mlkit.face.detection)

    implementation(libs.androidx.camera.core) // Replace with the latest version
    implementation(libs.androidx.camera.camera2) // Replace with the latest version
    implementation(libs.androidx.camera.lifecycle.v130) // Replace with the latest version

    implementation(libs.androidx.camera.core.v130)

    // CameraX Lifecycle (to bind camera to lifecycle)
    implementation(libs.androidx.camera.lifecycle)

    // CameraX View (for PreviewView)
    implementation(libs.androidx.camera.view)

    // CameraX Extensions (optional, for features like night mode)
    implementation(libs.androidx.camera.extensions)

    //Dimen
    implementation (libs.ssp.android)
    implementation (libs.sdp.android)


}