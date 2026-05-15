import org.gradle.kotlin.dsl.debugImplementation

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.daiyan.accountify"
    compileSdk = 35

    defaultConfig {
        vectorDrawables.useSupportLibrary = true
        applicationId = "com.daiyan.accountify"
        minSdk = 21
        targetSdk = 34
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
    sourceSets {
        getByName("main") {
            java {
                srcDirs("src\\main\\java", "src\\main\\java\\Home")
            }
        }
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.fragment)
    implementation(libs.recyclerview)
    implementation(libs.legacy.support.v4)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

//    implementation("com.amitshekhar.android:debug-db:1.0.6")

    implementation("androidx.cardview:cardview:1.0.0")
//    debugImplementation("com.github.amitshekhariitbhu.Android-Debug-Database:debug-db:1.0.7")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.recyclerview:recyclerview-selection:1.1.0")
}