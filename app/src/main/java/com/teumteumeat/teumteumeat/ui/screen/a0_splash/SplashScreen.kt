package com.teumteumeat.teumteumeat.ui.screen.a0_splash

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.os.bundleOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.component.FullScreenErrorModal
import com.teumteumeat.teumteumeat.ui.component.modal.BaseModal
import com.teumteumeat.teumteumeat.ui.component.modal.ModalOverlay
import com.teumteumeat.teumteumeat.ui.screen.a1_login.LoginActivity
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.OnBoardingActivity
import com.teumteumeat.teumteumeat.ui.screen.a4_main.MainActivity
import com.teumteumeat.teumteumeat.ui.screen.d0_league.LeagueActivity
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.firebase.TeumAnalyticsEvent
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.Utils
import com.teumteumeat.teumteumeat.utils.Utils.UpdateUtils.moveToPlayStore
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel(),
    // 리그 푸시로 진입했는지. true 면 로그인·온보딩을 마친 유저만 홈 → 리그로 이동하고, 미로그인/온보딩 중이면 해당 화면까지만 이동한다.
    isLeagueEntry: Boolean = false,
    // 푸시 알림 탭 진입일 때 GA `push_open` 의 push_type 값 (서버 data.type 그대로, 없으면 "unknown").
    // null 이면 푸시 진입이 아니거나 Activity 재생성이라 기록하지 않는다.
    pushOpenType: String? = null,
    // true 면 push_open 의 destination 을 실제 도착 화면과 무관하게 "none" 으로 기록한다 (모르는 type 의 콜드 스타트)
    pushNoDestination: Boolean = false,
    // 푸시/공유 링크 진입이면 true — 화면 이동 시 이전 백스택을 비운다
    clearTaskOnNavigate: Boolean = false,
) {
    // push_open 의 destination — 이동하지 않기로 한 푸시(pushNoDestination)는 도착 화면과 무관하게 none
    fun pushDestination(actual: String) =
        if (pushNoDestination) TeumAnalyticsEvent.PushOpen.DESTINATION_NONE else actual

    val sessionManager = viewModel.sessionManager // 🔥 ViewModel 통해 접근

    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val context = LocalContext.current
    val activity = LocalActivityContext.current as SplashActivity

    val v2Color = MaterialTheme.colorScheme.primary

    val isPlaying = remember { mutableStateOf(true) }
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.splash_animation)
    )

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1, // 🔥 1번만 재생
        isPlaying = isPlaying.value // 🔥 핵심
    )

    // ✅ 모달 표시용 상태
    var forceUpdateMessage by remember { mutableStateOf<String?>(null) }
    var optionalUpdateMessage by remember { mutableStateOf<String?>(null) }

    // ✅ 애니메이션 종료 감지
//    LaunchedEffect(progress) {
//        if (progress == 1f) {
//            Log.d("SplashDebug", "animation finished")
//            viewModel.onAnimationFinished()
//        }
//    }

    // 🔥 전역 세션 이벤트 감지
    LaunchedEffect(Unit) {
        sessionManager.sessionEvent.collectLatest {
            viewModel.logPushOpenOnce(pushOpenType, pushDestination(TeumAnalyticsEvent.PushOpen.DESTINATION_LOGIN))
            Utils.UxUtils.moveActivity(
                activity,
                LoginActivity::class.java,
                clearTask = true,
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onAnimationFinished()
    }



    // ✅ 네비게이션 처리 (단발성)
    LaunchedEffect(Unit) {
        Log.d("SplashActivity", "Collect Started")
        viewModel.uiEvent.collect { event ->
            Log.d("SplashActivity", "Collected event = $event")
            when (event) {
                SplashUiEvent.NavigateToLogin -> {
                    // 푸시 진입이어도 로그인 전에는 로그인 화면까지만 이동한다 (로그인 후 리그로 이어가지 않음)
                    viewModel.logPushOpenOnce(pushOpenType, pushDestination(TeumAnalyticsEvent.PushOpen.DESTINATION_LOGIN))
                    Utils.UxUtils.moveActivity(activity, LoginActivity::class.java, clearTask = clearTaskOnNavigate)
                }

                SplashUiEvent.NavigateToMain ->
                    if (isLeagueEntry) {
                        viewModel.logPushOpenOnce(pushOpenType, pushDestination(TeumAnalyticsEvent.PushOpen.DESTINATION_LEAGUE))
                        // 홈을 루트로 쌓고 그 위에 리그 — 리그에서 뒤로가기 시 홈으로 돌아온다
                        Utils.UxUtils.moveActivityWithBackStack(
                            activity,
                            backStackRootActivity = MainActivity::class.java,
                            destinationActivity = LeagueActivity::class.java,
                            // 푸시 알림 탭 진입일 때만 리그가 새로 생성될 때 league_view(source=push) 를 기록한다
                            // (공유 링크 진입은 source 가 정해지지 않아 기록하지 않는다)
                            destinationExtras = pushOpenType?.let {
                                bundleOf(LeagueActivity.EXTRA_SOURCE to TeumAnalyticsEvent.LeagueView.SOURCE_PUSH)
                            },
                        )
                    } else {
                        viewModel.logPushOpenOnce(pushOpenType, pushDestination(TeumAnalyticsEvent.PushOpen.DESTINATION_HOME))
                        Utils.UxUtils.moveActivity(activity, MainActivity::class.java, clearTask = clearTaskOnNavigate)
                    }

                SplashUiEvent.NavigateToOnboarding -> {
                    viewModel.logPushOpenOnce(pushOpenType, pushDestination(TeumAnalyticsEvent.PushOpen.DESTINATION_ONBOARDING))
                    Utils.UxUtils.moveActivity(activity, OnBoardingActivity::class.java, clearTask = clearTaskOnNavigate)
                }

                is SplashUiEvent.ShowErrorMessage -> {
                }

                is SplashUiEvent.ShowForceUpdate -> {
                    forceUpdateMessage = event.message
                    optionalUpdateMessage = null // 동시 노출 방지
                }

                is SplashUiEvent.ShowOptionalUpdate -> {
                    optionalUpdateMessage = event.message
                    forceUpdateMessage = null // 동시 노출 방지
                }
            }
        }
    }

    // ✅ 강제 업데이트 모달이 떠 있을 때 뒤로가기 차단
    BackHandler(enabled = forceUpdateMessage != null) {}

    // ✅ 에러 바텀시트가 떠 있을 때만 뒤로가기 처리 X (스플래쉬에서는 재시도 없으므로)
    BackHandler(enabled = uiState.errorState != null) {
        // viewModel.dismissError()
    }

    TeumTeumEatTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                // 애니매이션으로 변경이후 v2Color로 변경 예정
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center,

        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
//                LottieAnimation(
//                    composition = composition,
//                    progress = { progress },
//                )
                Image(
                    painter = painterResource(id = R.drawable.logo_login),
                    contentDescription = "메인 로고",
                    contentScale = ContentScale.Fit
                )
            }

            // 2️⃣ 에러 발생 시 전체화면 모달 덮기
            uiState.errorState?.let { error ->
                FullScreenErrorModal(
                    errorState = error,
                    onBack = { },
                    isShowBackBtn = false,
                )
            }

            // ======= 🚫 강제 업데이트 모달 =======
            if (forceUpdateMessage != null) {
                ModalOverlay(
                    onOutsideClick = {} // 강제: 외부 클릭 무시
                ) {
                    BaseModal(
                        title = "업데이트를 해주세요!",
                        body = forceUpdateMessage,
                        primaryButtonText = "업데이트",
                        secondaryButtonText = null,
                        onPrimaryClick = {
                            moveToPlayStore(activity)
                        },
                    )
                }
            }

            // ======= ⚠️ 선택 업데이트 모달 =======
            if (optionalUpdateMessage != null) {
                ModalOverlay(
                    onOutsideClick = {
                        // 선택: 바깥 눌러서 닫기 허용할지 정책에 따라 결정
                        // 여기서는 "나중에"와 동일 처리로 통일
                        optionalUpdateMessage = null
                        viewModel.tryAutoLogin()
                    }
                ) {
                    BaseModal(
                        title = "새로운 버전 출시!",
                        body = optionalUpdateMessage,
                        primaryButtonText = "업데이트",
                        secondaryButtonText = "나중에",
                        onPrimaryClick = {
                            moveToPlayStore(activity)
                        },
                        onSecondaryClick = {
                            optionalUpdateMessage = null
                            viewModel.tryAutoLogin()
                        },
                    )
                }
            }


        }
    }
}




@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    TeumTeumEatTheme {
        DefaultMonoBg(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {

        }
    }
}

