package com.teumteumeat.teumteumeat

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.kakao.sdk.common.KakaoSdk
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import com.teumteumeat.teumteumeat.utils.Utils.FcmTokenSyncUtil
import com.teumteumeat.teumteumeat.utils.firebase.FcmTokenInitializer
import com.teumteumeat.teumteumeat.utils.firebase.TeumAnalyticsLogger
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject


@HiltAndroidApp
class MyApplication : Application() {

    /**
     * 앱 시작 직후 [TeumAnalyticsLogger] @Singleton 인스턴스를 강제 생성합니다.
     *
     * Hilt가 Application.onCreate() 이후 field injection을 수행하면
     * [TeumAnalyticsLogger.init] 블록이 즉시 실행되어
     * `app_version_code` / `app_version_name` User Property가
     * 첫 번째 이벤트 전송 전에 GA4에 등록됩니다.
     */
    @Inject
    lateinit var analyticsLogger: TeumAnalyticsLogger

    override fun onCreate() {
        super.onCreate()
        // Enable verbose logging for debugging (remove in production)
        if(BuildConfig.DEBUG) OneSignal.Debug.logLevel = LogLevel.VERBOSE
        // Initialize with your OneSignal App ID
        OneSignal.initWithContext(this, BuildConfig.ONESIGNAL_APP_ID)

        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)

        FcmTokenInitializer.init(this) // ✅ 앱 시작 시 FCM 토큰 미리 저장

        // Google Ads SDK 초기화
        MobileAds.initialize(this) {}

        // ✅ App 시작 시 토큰 최신화 + 서버 동기화
        FcmTokenSyncUtil.checkAndSyncOnAppStart(this)

        val remoteConfig = Firebase.remoteConfig
        remoteConfig.setConfigSettingsAsync(
            remoteConfigSettings {
                minimumFetchIntervalInSeconds =
                    if (BuildConfig.DEBUG) 0 else 3600
            }
        )
    }
}
