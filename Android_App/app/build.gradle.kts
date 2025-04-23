plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

  repositories {
	        // paho repository
	        maven {
	            url "https://repo.eclipse.org/content/repositories/paho-snapshots/"
	        }
	}

android {
    namespace = "com.example.prog1_teste"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.prog1_teste"
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // paho dependencies
	implementation 'org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.1.0'
	implementation 'org.eclipse.paho:org.eclipse.paho.android.service:1.1.1'
}
