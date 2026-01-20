package com.teumteumeat.teumteumeat

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import com.teumteumeat.teumteumeat.utils.firebase.FcmTokenInitializer
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class MyApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        // Enable verbose logging for debugging (remove in production)
        if(BuildConfig.DEBUG) OneSignal.Debug.logLevel = LogLevel.VERBOSE
        // Initialize with your OneSignal App ID
        OneSignal.initWithContext(this, BuildConfig.ONESIGNAL_APP_ID)

        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
        FcmTokenInitializer.init(this) // ✅ 앱 시작 시 FCM 토큰 미리 저장
    }
}
