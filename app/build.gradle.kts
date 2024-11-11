plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.ghmc"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.ghmc"
        minSdk = 23
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        viewBinding = true
    }
}
configurations.all {
    resolutionStrategy {
        eachDependency {
            if (requested.group == "org.jetbrains.kotlin" && requested.name.startsWith("kotlin-stdlib")) {
                useVersion("1.8.0")
            }
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)


    // Firebase Authentication
    implementation(libs.firebase.auth)

    // Google Sign-In
   implementation(libs.play.services.auth)

    //noinspection UseTomlInstead,GradleDependency
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation(libs.google.firebase.firestore)
    // Firebase SDK for Firestore
    implementation(libs.firebase.firestore)
    // Firebase SDK for Storage
    implementation(libs.firebase.storage)
    // For handling task results (like uploading files)
    implementation(libs.play.services.tasks)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("androidx.navigation:navigation-fragment-ktx:2.8.2")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.2")

}

// Apply the Google services plugin
apply(plugin = "com.google.gms.google-services")
