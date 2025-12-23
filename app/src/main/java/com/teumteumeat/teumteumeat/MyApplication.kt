package com.teumteumeat.teumteumeat

import android.app.Application
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class MyApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        // Enable verbose logging for debugging (remove in production)
        OneSignal.Debug.logLevel = LogLevel.VERBOSE
        // Initialize with your OneSignal App ID
        OneSignal.initWithContext(this, BuildConfig.ONESIGNAL_APP_ID)

        // KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
    }
}
