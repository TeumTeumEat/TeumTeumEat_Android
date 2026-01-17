import java.util.Properties


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.compose")

    id("com.google.dagger.hilt.android")

    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    alias(libs.plugins.google.firebase.crashlytics)   // <-- 추가
}

android {
    namespace = "com.teumteumeat.teumteumeat"
    compileSdk = 36 // targetSdk에 맞게

    defaultConfig {
        applicationId = "com.teumteumeat.teumteumeat"
        minSdk = 26
        targetSdk = 36 // 최신 버전(Android 16, API 36) 기준
        versionCode = 8
        versionName = "1.0.7"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "KAKAO_NATIVE_APP_KEY",
            "\"${project.properties["KAKAO_NATIVE_APP_KEY"] ?: ""}\""
        )

        buildConfigField(
            "String",
            "BASE_DOMAIN",
            "\"https://api.teumteumeat.co.kr/\"",
        )

        buildConfigField(
            "String",
            "ONESIGNAL_APP_ID",
            "\"2d5b8758966c24367eebd1926dd090bf\""
        )

    }

    val localProps = Properties().apply {
        val localPropsFile = rootProject.file("local.properties")
        if (localPropsFile.exists()) {
            load(localPropsFile.inputStream())
        }
    }

    signingConfigs {
        create("release") {

            val storeFilePath =
                localProps.getProperty("TEUMTEUM_STORE_FILE")
                    ?: error("TEUMTEUM_STORE_FILE not found in local.properties")

            storeFile = file(storeFilePath)
            storePassword =
                localProps.getProperty("TEUMTEUM_STORE_PASSWORD")
                    ?: error("TEUMTEUM_STORE_PASSWORD not found")

            keyAlias =
                localProps.getProperty("TEUMTEUM_KEY_ALIAS")
                    ?: error("TEUMTEUM_KEY_ALIAS not found")

            keyPassword =
                localProps.getProperty("TEUMTEUM_KEY_PASSWORD")
                    ?: error("TEUMTEUM_KEY_PASSWORD not found")
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            buildConfigField(
                "String",
                "BASE_DOMAIN",
                "\"https://api.teumteumeat.co.kr/\"",
            )
            buildConfigField(
                "String",
                "ONESIGNAL_APP_ID",
                "\"92286389-415e-4461-85f4-56f3a2736cb3\""
            )

            buildConfigField(
                "String",
                "KAKAO_NATIVE_APP_KEY",
                "\"2d5b8758966c24367eebd1926dd090bf\""
            )

        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField(
                "String",
                "BASE_DOMAIN",
                "\"https://api.teumteumeat.co.kr/\"",
            )
            buildConfigField(
                "String",
                "ONESIGNAL_APP_ID",
                "\"92286389-415e-4461-85f4-56f3a2736cb3\""
            )

            buildConfigField(
                "String",
                "KAKAO_NATIVE_APP_KEY",
                "\"2d5b8758966c24367eebd1926dd090bf\""

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
    buildFeatures {
        buildConfig = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.crashlytics)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // -------- 추가 구성 라이브러리  ---------
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation("com.github.anhaki:PickTime-Compose:1.1.5")

    // Hilt core
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-compiler:2.51.1")

    // Hilt + ViewModel support
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    ksp("androidx.hilt:hilt-compiler:1.1.0")

    implementation("androidx.browser:browser:1.9.0")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

    // 카카오 SDK
    implementation("com.kakao.sdk:v2-user:2.20.0")

    // Theme.AppCompat.*를 쓰려면
    implementation("androidx.appcompat:appcompat")

    implementation("androidx.datastore:datastore-preferences:1.2.0")

    implementation("com.google.android.gms:play-services-auth:21.4.0")

    // Firebase BoM (버전 자동 관리)
    implementation(platform("com.google.firebase:firebase-bom:34.7.0"))

    // Firebase Analytics 추가
    implementation("com.google.firebase:firebase-analytics")

    implementation(libs.lottie.compose)

    implementation("com.github.vsnappy1:ComposeDatePicker:2.2.0")

    implementation("com.onesignal:OneSignal:[5.1.0, 5.1.99]")

    implementation("io.noties.markwon:core:4.6.2")

    implementation("org.jetbrains:markdown:0.7.3")
}