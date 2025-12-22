package com.teumteumeat.teumteumeat

import android.app.Application
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class MyApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        // KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
    }
}
