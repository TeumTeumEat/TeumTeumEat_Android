package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.component.FlowTopProgressBar
import com.teumteumeat.teumteumeat.ui.component.FullScreenErrorModal
import com.teumteumeat.teumteumeat.ui.component.modal.NotificationSettingGuideOverlay
import com.teumteumeat.teumteumeat.ui.screen.a4_main.MainActivity
import com.teumteumeat.teumteumeat.ui.screen.common_screen.ErrorState
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.LocalAppContext
import com.teumteumeat.teumteumeat.utils.LocalOnBoardingMainUiState
import com.teumteumeat.teumteumeat.utils.LocalViewModelContext
import com.teumteumeat.teumteumeat.utils.Utils
import kotlinx.coroutines.delay

private fun openNotificationSetting(activity: Activity) {
    val intent =
        Intent("android.settings.APP_NOTIFICATION_SETTINGS").apply {
            putExtra("android.provider.extra.APP_PACKAGE", activity.packageName)
        }
    activity.startActivity(intent)
}

@Composable
fun OnBoardingCompositionProvider(
    viewModel: OnBoardingViewModel,
    context: Context,
    activity: OnBoardingActivity,
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val mainState by viewModel.mainState.collectAsStateWithLifecycle()
    val navHostController = rememberNavController()

    val backStackEntry by navHostController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val visibleStates = remember { mutableStateListOf(false, false, false) }
    var isAnimationComplete by remember { mutableStateOf(false) }

    CompositionLocalProvider(
        LocalAppContext provides context,
        LocalActivityContext provides activity,
        LocalOnBoardingMainUiState provides uiState,
        LocalViewModelContext provides viewModel,
    ) {

        val isInLoadingPhase = mainState is UiStateOnboardingScreenState.Loading ||
                (mainState is UiStateOnboardingScreenState.Success && !isAnimationComplete)

        // ✅ 로딩 또는 최소 대기 중 물리 뒤로가기 차단
        BackHandler(enabled = isInLoadingPhase) {
            Log.d("OnBoardingNav", "back 차단 – Loading 중")
        }

        // ✅ NavHost 내부 BackHandler가 popBackStack()을 먼저 처리하므로,
        // currentRoute 변경을 관찰해 currentScreen(→ currentPage)을 동기화한다.
        // 기존 BackHandler에서 navigateTo를 호출하던 방식은 NavHost BackHandler보다
        // 낮은 우선순위로 인해 실행되지 않았음.
        LaunchedEffect(currentRoute) {
            val screen = OnBoardingScreens.fromRoute(currentRoute) ?: return@LaunchedEffect
            Log.d(
                "OnBoardingNav",
                "route 변경: $currentRoute → ${screen::class.simpleName}, page=${
                    OnBoardingFlow.currentPage(
                        screen
                    )
                }"
            )
            viewModel.navigateTo(screen)
        }


        when {
            isInLoadingPhase -> {
                LaunchedEffect(Unit) {
                    visibleStates.forEachIndexed { index, _ ->
                        delay(300)
                        visibleStates[index] = true
                    }
                }

                SubmitLoadingScreen(
                    visibleStates = visibleStates,
                    isCompletedLoading = true,
                    onAnimationComplete = { isAnimationComplete = true },
                )
            }

            mainState is UiStateOnboardingScreenState.Success -> {
                // 온보딩 완료하면 오프라인 플래그값도 변경
                viewModel.updateOfflineFlag()

                OnBoardingSuccessScreen(
                    nickname = uiState.serverNickname.ifEmpty { uiState.charName },
                    onStartClick = {
                        Utils.UxUtils.moveActivity(
                            activity,
                            MainActivity::class.java,
                            exitFlag = true
                        )
                    }
                )
            }

            mainState is UiStateOnboardingScreenState.Error -> {
                val error = mainState as UiStateOnboardingScreenState.Error

                FullScreenErrorModal(
                    errorState = viewModel.getErrorState(
                        message = error.message,
                        onRetry = {
                            // ✅ 통합 API 재호출
                            viewModel.submitOnBoarding()
                        }
                    ),
                    onBack = {
                        viewModel.resetMainState()
                    },
                )
            }

            else -> {
                DefaultMonoBg(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    // 🔔 알림 설정 안내 Overlay
                    NotificationSettingGuideOverlay(
                        onConfirm = {
                            openNotificationSetting(activity)
                            viewModel.closeNotificationSettingGuide()
                        },
                        onDismiss = {
                            viewModel.closeNotificationSettingGuide()
                        },
                        notificationGuideType = uiState.notificationGuideType,
                    )


                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .systemBarsPadding()
                    ) {

                        FlowTopProgressBar(
                            currentPage = uiState.currentPage,
                            totalPage = uiState.totalPage,
                            onBack = {
                                val prev = OnBoardingScreens.fromRoute(
                                    navHostController.previousBackStackEntry?.destination?.route
                                )
                                if (prev != null) viewModel.navigateTo(prev)
                                navHostController.popBackStack()
                            },
                        )

                        OnBoardingNavHost(
                            navController = navHostController,
                        )

                        // ✅ NavHost보다 늦게 등록 → 더 높은 우선순위
                        // 특정 화면에서는 물리 뒤로가기를 차단한다.
                        /*BackHandler() {
                            Log.d("OnBoardingNav", "back 차단 – disablePrevPageRoute: $currentRoute")
                        }*/
                    }
                }
            }

        }

    }

}

// ────────── Previews ──────────

@Preview(showBackground = true, name = "OnBoarding – Idle (진행 중)")
@Composable
private fun OnBoardingCompositionProviderIdlePreview() {
    val fakeState = UiStateOnboardingState(
        currentScreen = OnBoardingScreens.SelectLearningMethodScreen,
    )
    TeumTeumEatTheme {
        DefaultMonoBg(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding(),
            ) {
                FlowTopProgressBar(
                    currentPage = fakeState.currentPage,
                    totalPage = fakeState.totalPage,
                    onBack = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "OnBoarding – Loading")
@Composable
private fun OnBoardingCompositionProviderLoadingPreview() {
    val visibleStates = remember { mutableStateListOf(true, true, false) }
    TeumTeumEatTheme {
        SubmitLoadingScreen(
            visibleStates = visibleStates,
            isCompletedLoading = false,
        )
    }
}

@Preview(showBackground = true, name = "OnBoarding – Error")
@Composable
private fun OnBoardingCompositionProviderErrorPreview() {
    TeumTeumEatTheme {
        FullScreenErrorModal(
            errorState = ErrorState(
                title = "문제가 발생했어요",
                description = "네트워크 상태를 확인한 후\n다시 시도해주세요.",
                retryLabel = "다시 시도",
                onRetry = {},
            ),
            onBack = {},
        )
    }
}