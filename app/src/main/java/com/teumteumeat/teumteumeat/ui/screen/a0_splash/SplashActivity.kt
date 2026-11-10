package com.teumteumeat.teumteumeat.ui.screen.a0_splash

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.teumteumeat.teumteumeat.ui.screen.deeplink.AppScreenTracker
import com.teumteumeat.teumteumeat.ui.screen.deeplink.PushNavigationPolicy
import com.teumteumeat.teumteumeat.ui.screen.deeplink.PushTarget
import com.teumteumeat.teumteumeat.ui.screen.deeplink.PushType
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.DeepLinkConstants
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.LocalAppContext
import com.teumteumeat.teumteumeat.utils.LocalViewModelContext
import com.teumteumeat.teumteumeat.utils.firebase.TeumAnalyticsEvent
import com.teumteumeat.teumteumeat.utils.firebase.TeumAnalyticsLogger
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : ComponentActivity()  {

    @Inject
    lateinit var analyticsLogger: TeumAnalyticsLogger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("SplashActivity", "onCreate")
        enableEdgeToEdge()

        val entry = resolveEntry(isRestored = savedInstanceState != null)

        // 이미 떠 있는 화면이 있는데 옮기지 않기로 했다면 스플래시를 띄우지 않고 바로 닫아 원래 화면을 그대로 유지한다.
        if (entry.stayOnCurrentScreen) {
            entry.pushOpenType?.let {
                analyticsLogger.logPushOpen(it, TeumAnalyticsEvent.PushOpen.DESTINATION_NONE)
            }
            finish()
            return
        }

        setContent {
            TeumTeumEatTheme {
                 val viewModel: SplashViewModel = hiltViewModel()
                CompositionLocalProvider(
                    LocalAppContext provides this.applicationContext,
                    LocalActivityContext provides this,
                    LocalViewModelContext provides viewModel,

                ){
                    SplashScreen(
                        viewModel = viewModel,
                        isLeagueEntry = entry.isLeagueEntry,
                        pushOpenType = entry.pushOpenType,
                        // 앱이 꺼진 상태(콜드 스타트)에서 모르는 type 이면 일반 실행처럼 열리지만 push_open 의 destination 은 none
                        pushNoDestination = entry.pushTarget == PushTarget.NONE,
                        // 푸시/공유 링크로 들어온 경우 이동 시 이전 백스택을 비운다
                        clearTaskOnNavigate = entry.clearTaskOnNavigate,
                    )
                }
            }
        }

    }

    /**
     * 스플래시로 들어온 경로(푸시 알림 탭 / 리그 공유 링크 / 일반 실행)를 판정한다.
     *
     * @param isRestored Activity 재생성(savedInstanceState != null)이면 true — GA `push_open` 을 중복 기록하지 않는다
     */
    private fun resolveEntry(isRestored: Boolean): SplashEntry {
        val pushType = intent.getStringExtra(DeepLinkConstants.PUSH_DATA_KEY_TYPE)
        val pushLink = intent.getStringExtra(DeepLinkConstants.PUSH_DATA_KEY_LINK)
        val isPushEntry = intent.hasExtra(DeepLinkConstants.PUSH_DATA_KEY_TYPE) ||
            intent.hasExtra(DeepLinkConstants.PUSH_DATA_KEY_LINK)
        val isShareLinkEntry = intent.getBooleanExtra(DeepLinkConstants.EXTRA_LEAGUE_DEEPLINK, false)

        // 푸시 알림 탭 진입이면 GA push_open 의 push_type. 푸시 진입이 아니거나 Activity 재생성이면 null (기록 안 함).
        val pushOpenType: String? =
            if (!isRestored && isPushEntry) PushType.toAnalyticsValue(pushType) else null

        // 푸시 알림 탭의 이동 정책 — 퀴즈 / 주제 추가 / 새 목표 설정 중이거나 모르는 type 이면 화면을 옮기지 않는다 (iOS 와 동일).
        val pushTarget: PushTarget? = if (isPushEntry) {
            PushNavigationPolicy.resolve(
                type = pushType,
                link = pushLink,
                isNoInterruptScreenActive = AppScreenTracker.isNoInterruptScreenActive,
            )
        } else {
            null
        }

        return SplashEntry(
            pushOpenType = pushOpenType,
            pushTarget = pushTarget,
            stayOnCurrentScreen = pushTarget == PushTarget.NONE && AppScreenTracker.hasLiveScreen,
            // 리그 진입(리그 푸시 또는 리그 공유 링크) — 진행도 분기 뒤 로그인·온보딩을 마친 유저만 홈 → 리그로 이동한다.
            isLeagueEntry = pushTarget == PushTarget.LEAGUE || (!isPushEntry && isShareLinkEntry),
            clearTaskOnNavigate = isPushEntry || isShareLinkEntry,
        )
    }
}

/**
 * 스플래시 진입 경로 판정 결과.
 *
 * @property pushOpenType GA `push_open` 의 push_type. 푸시 진입이 아니거나 Activity 재생성이면 null
 * @property pushTarget 푸시 알림의 이동 정책 결과. 푸시 진입이 아니면 null
 * @property stayOnCurrentScreen true 면 스플래시를 띄우지 않고 닫아 이미 떠 있는 화면을 유지한다
 * @property isLeagueEntry 리그 푸시 또는 리그 공유 링크로 들어왔는지
 * @property clearTaskOnNavigate 푸시/공유 링크 진입이면 true — 화면 이동 시 이전 백스택을 비운다
 */
private data class SplashEntry(
    val pushOpenType: String?,
    val pushTarget: PushTarget?,
    val stayOnCurrentScreen: Boolean,
    val isLeagueEntry: Boolean,
    val clearTaskOnNavigate: Boolean,
)
