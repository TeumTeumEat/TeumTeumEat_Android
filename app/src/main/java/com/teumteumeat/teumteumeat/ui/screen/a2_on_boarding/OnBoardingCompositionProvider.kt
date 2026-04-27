package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import com.teumteumeat.teumteumeat.R
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.teumteumeat.teumteumeat.ui.component.CustomProgressBar
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.component.FullScreenErrorModal
import com.teumteumeat.teumteumeat.ui.component.SizeAnimationInvisible
import com.teumteumeat.teumteumeat.ui.component.modal.NotificationSettingGuideOverlay
import com.teumteumeat.teumteumeat.ui.screen.a4_main.MainActivity
import com.teumteumeat.teumteumeat.ui.screen.common_screen.ErrorState
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.LocalAppContext
import com.teumteumeat.teumteumeat.utils.LocalOnBoardingMainUiState
import com.teumteumeat.teumteumeat.utils.LocalViewModelContext
import com.teumteumeat.teumteumeat.utils.Utils
import com.teumteumeat.teumteumeat.utils.appTypography
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

    val visibleStates = remember(mainState) {
        mutableStateListOf(false, false, false)
    }

    CompositionLocalProvider(
        LocalAppContext provides context,
        LocalActivityContext provides activity,
        LocalOnBoardingMainUiState provides uiState,
        LocalViewModelContext provides viewModel,
    ) {

        // ✅ Loading 상태일 때 물리 뒤로가기 차단
        BackHandler(
            enabled = mainState is UiStateOnboardingScreenState.Loading
        ) {
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


        val progress by viewModel.progress.collectAsStateWithLifecycle()


        when (mainState) {

            UiStateOnboardingScreenState.Loading -> {

                LaunchedEffect(Unit) {
                    visibleStates.forEachIndexed { index, _ ->
                        delay(300)
                        visibleStates[index] = true
                    }
                }

                OnBoardingLoadingScreen(
                    title = "틈틈잇을 생성하는 중\n" +
                            "잠시만 기다려주세요",
                    progress = progress,
                    visibleStates = visibleStates,
                    isCompletedLoading = true,
                )
            }

            UiStateOnboardingScreenState.Success -> {
                // 온보딩 완료하면 오프라인 플래그값도 변경
                viewModel.updateOfflineFlag()

                OnBoardingSuccessScreen(
                    nickname = uiState.charName,
                    onStartClick = {
                        Utils.UxUtils.moveActivity(
                            activity,
                            MainActivity::class.java,
                            exitFlag = true
                        )
                    }
                )
            }

            is UiStateOnboardingScreenState.Error -> {
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

            UiStateOnboardingScreenState.Idle -> {
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

                        if (uiState.currentPage > 0) {
                            Row(
                                modifier = Modifier.padding(
                                    vertical = 16.dp, horizontal = 20.dp,
                                ),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {

                                SizeAnimationInvisible(
                                    isVisible = uiState.currentPage > 0,
                                    clickEnabled = uiState.currentPage > 0
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.icon_keboard_arrow_left),
                                        contentDescription = "previous page",
                                        modifier = Modifier.clickable(
                                            interactionSource = Utils.UiUtils.noRipple(),
                                        ) {
                                            val prev = OnBoardingScreens.fromRoute(
                                                navHostController.previousBackStackEntry?.destination?.route
                                            )
                                            if (prev != null) viewModel.navigateTo(prev)
                                            navHostController.popBackStack()
                                        }
                                    )
                                }

                                CustomProgressBar(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 16.dp),
                                    currentStep = uiState.currentPage,
                                    totalSteps = uiState.totalPage,
                                )


                                Text(
                                    "${uiState.currentPage}/${uiState.totalPage}",
                                    style = MaterialTheme.appTypography.captionRegular14,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

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
                Row(
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.icon_keboard_arrow_left),
                        contentDescription = "previous page",
                    )
                    CustomProgressBar(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        currentStep = fakeState.currentPage,
                        totalSteps = fakeState.totalPage,
                    )
                    Text(
                        "${fakeState.currentPage}/${fakeState.totalPage}",
                        style = MaterialTheme.appTypography.captionRegular14,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "OnBoarding – Loading")
@Composable
private fun OnBoardingCompositionProviderLoadingPreview() {
    val visibleStates = remember { mutableStateListOf(true, true, false) }
    TeumTeumEatTheme {
        OnBoardingLoadingScreen(
            title = "틈틈잇을 생성하는 중\n잠시만 기다려주세요",
            progress = 0.6f,
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