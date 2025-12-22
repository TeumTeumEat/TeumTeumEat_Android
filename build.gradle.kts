// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0" apply false

    id("com.google.dagger.hilt.android") version "2.51.1" apply false
    // KSP (Annotation Processor 빠른 처리용)
    id("com.google.devtools.ksp") version "2.1.0-1.0.29" apply false

    id("com.google.gms.google-services") version "4.4.4" apply false
    alias(libs.plugins.google.firebase.crashlytics) apply false
}

