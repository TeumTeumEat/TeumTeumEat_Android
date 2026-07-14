package com.teumteumeat.teumteumeat

import android.app.Application
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.kakao.sdk.common.KakaoSdk
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import com.teumteumeat.teumteumeat.di.IoDispatcher
import com.teumteumeat.teumteumeat.utils.Utils.FcmTokenSyncUtil
import com.teumteumeat.teumteumeat.utils.firebase.FcmTokenInitializer
import com.teumteumeat.teumteumeat.utils.firebase.TeumAnalyticsLogger
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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
class MyApplication : Application(){

    @Inject
    @IoDispatcher
    lateinit var ioDispatcher: CoroutineDispatcher

    private val applicationScope by lazy { CoroutineScope(SupervisorJob() + ioDispatcher) }

    override fun onCreate() {
        super.onCreate()
        // Enable verbose logging for debugging (remove in production)
        if(BuildConfig.DEBUG) OneSignal.Debug.logLevel = LogLevel.VERBOSE

        // ✅ 각 SDK 초기화는 개별 방어 — 한 SDK 실패가 앱 전체를 죽이지 않도록 함
        // Initialize with your OneSignal App ID
        runCatching {
            OneSignal.initWithContext(this, BuildConfig.ONESIGNAL_APP_ID)
        }.onFailure { Log.e(TAG, "OneSignal 초기화 실패", it) }

        runCatching {
            KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
        }.onFailure { Log.e(TAG, "KakaoSdk 초기화 실패", it) }

        runCatching {
            FcmTokenInitializer.init(this) // ✅ 앱 시작 시 FCM 토큰 미리 저장
        }.onFailure { Log.e(TAG, "FCM 토큰 초기화 실패", it) }

        // Google Ads SDK 초기화 — 메인 스레드 블로킹 방지를 위해 백그라운드에서 수행
        applicationScope.launch {
            runCatching {
                MobileAds.initialize(this@MyApplication) {}
            }.onFailure { Log.e(TAG, "MobileAds 초기화 실패", it) }
        }

        // ✅ App 시작 시 토큰 최신화 + 서버 동기화
        runCatching {
            FcmTokenSyncUtil.checkAndSyncOnAppStart(this)
        }.onFailure { Log.e(TAG, "FCM 토큰 동기화 실패", it) }

        runCatching {
            val remoteConfig = Firebase.remoteConfig
            remoteConfig.setConfigSettingsAsync(
                remoteConfigSettings {
                    minimumFetchIntervalInSeconds =
                        if (BuildConfig.DEBUG) 0 else 3600
                }
            )
        }.onFailure { Log.e(TAG, "RemoteConfig 설정 실패", it) }
    }

    companion object {
        private const val TAG = "MyApplication"
    }
}
