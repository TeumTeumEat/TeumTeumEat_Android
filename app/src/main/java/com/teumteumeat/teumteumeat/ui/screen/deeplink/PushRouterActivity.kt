package com.teumteumeat.teumteumeat.ui.screen.deeplink

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.teumteumeat.teumteumeat.ui.screen.a0_splash.SplashActivity
import com.teumteumeat.teumteumeat.utils.DeepLinkConstants
import com.teumteumeat.teumteumeat.utils.firebase.TeumAnalyticsEvent
import com.teumteumeat.teumteumeat.utils.firebase.TeumAnalyticsLogger
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 앱이 켜져 있을 때 받은 푸시 알림([com.teumteumeat.teumteumeat.utils.firebase.TeumFcmService] 이 직접 띄운 알림)의 탭 착지점.
 * 화면을 그리지 않고 [PushNavigationPolicy] 로 이동 여부를 판정한 뒤 즉시 종료된다.
 *
 * 스플래시를 알림 PendingIntent 의 목적지로 쓰지 않는 이유: 스플래시는 런처 Activity 라서 앱 태스크가 살아 있으면
 * 새 인스턴스를 만들지 않고 태스크만 앞으로 가져와 extras(type/link)가 전달되지 않는다.
 * 또 CLEAR_TASK 로 띄우면 이동 여부를 판정하기도 전에 퀴즈 등 진행 중인 화면이 먼저 파괴된다.
 *
 * - 퀴즈 / 주제 추가 / 새 목표 설정 중이거나 모르는 type 이면 화면을 옮기지 않고 닫는다 (원래 화면 유지, GA destination=none)
 * - 그 외에는 이전 백스택을 비우고 스플래시부터 시작해 로그인/온보딩 진행도를 확인한 뒤 홈 또는 홈 → 리그로 이동한다
 */
@AndroidEntryPoint
class PushRouterActivity : ComponentActivity() {

    @Inject
    lateinit var analyticsLogger: TeumAnalyticsLogger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val type = intent.getStringExtra(DeepLinkConstants.PUSH_DATA_KEY_TYPE)
        val link = intent.getStringExtra(DeepLinkConstants.PUSH_DATA_KEY_LINK)

        val target = PushNavigationPolicy.resolve(
            type = type,
            link = link,
            isNoInterruptScreenActive = AppScreenTracker.isNoInterruptScreenActive,
        )

        if (target == PushTarget.NONE && AppScreenTracker.hasLiveScreen) {
            analyticsLogger.logPushOpen(
                PushType.toAnalyticsValue(type),
                TeumAnalyticsEvent.PushOpen.DESTINATION_NONE,
            )
        } else {
            val splashIntent = Intent(this, SplashActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                type?.let { putExtra(DeepLinkConstants.PUSH_DATA_KEY_TYPE, it) }
                link?.let { putExtra(DeepLinkConstants.PUSH_DATA_KEY_LINK, it) }
            }
            startActivity(splashIntent)
        }
        finish()
    }
}
