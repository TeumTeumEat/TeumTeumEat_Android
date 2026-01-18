package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.teumteumeat.teumteumeat.ui.component.CustomProgressBar
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.component.FullScreenErrorModal
import com.teumteumeat.teumteumeat.ui.component.SizeAnimationInvisible
import com.teumteumeat.teumteumeat.ui.screen.a4_main.MainActivity
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.LocalAppContext
import com.teumteumeat.teumteumeat.utils.LocalOnBoardingMainUiState
import com.teumteumeat.teumteumeat.utils.LocalViewModelContext
import com.teumteumeat.teumteumeat.utils.Utils
import kotlinx.coroutines.delay

private fun openNotificationSetting(activity: Activity) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent("android.settings.APP_NOTIFICATION_SETTINGS").apply {
            putExtra("android.provider.extra.APP_PACKAGE", activity.packageName)
        }
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
        }
    }
    activity.startActivity(intent)
}

private val disablePrevPageRoutes = setOf(
    OnBoardingScreens.SixthCategorySelectScreen.route,
    OnBoardingScreens.SixthFileUploadScreen.route,
    OnBoardingScreens.EighthSetStudyRangeScreen.route,
    OnBoardingScreens.CheckSetMyInfoScreen.route,
    // 필요하면 추가
)

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
            // 아무것도 하지 않음 = 뒤로가기 무시
        }


        when (mainState) {

            UiStateOnboardingScreenState.Loading -> {

                LaunchedEffect(Unit) {
                    visibleStates.forEachIndexed { index, _ ->
                        delay(300)
                        visibleStates[index] = true
                    }
                }
                // todo. 로딩 스크린 구현
                OnBoardingLoadingScreen(
                    title = "틈틈잇을 생성하는 중\n" +
                            "잠시만 기다려주세요",
                    visibleStates = visibleStates
                )
            }

            UiStateOnboardingScreenState.Success -> {
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
                    }
                )
            }

            UiStateOnboardingScreenState.Idle -> {
                DefaultMonoBg(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    // 🔔 설정 안내 오버레이
                    NotificationSettingGuideOverlay(
                        uiState = uiState,
                        onConfirm = {
                            viewModel.openNotificationSetting()
                        },
                        onDismiss = {
                            viewModel.closeNotificationDisableGuide()
                        }
                    )

                    /* ------------------------------
                 * 2️⃣ 알림 설정 안내 Overlay (최상단)
                 * ------------------------------ */
                    NotificationSettingGuideOverlay(
                        uiState = uiState,
                        onConfirm = {
                            // 설정으로 이동
                            openNotificationSetting(activity)
                            viewModel.closeNotificationSettingGuide()
                        },
                        onDismiss = {
                            viewModel.closeNotificationSettingGuide()
                        }
                    )


                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .systemBarsPadding()
                    ) {

                        if (uiState.currentPage > 0) {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = 24.dp,
                                ),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {

                                SizeAnimationInvisible(
                                    isVisible = uiState.currentPage > 0
                                ) {
                                    IconButton(
                                        onClick = {
                                            // ✅ 특정 화면에서는 prevPage 비활성화
                                            if (currentRoute !in disablePrevPageRoutes) {
                                                viewModel.prevPage()
                                            }

                                            navHostController.popBackStack()
                                        },

                                        ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                                            contentDescription = "previous page",
                                            modifier = Modifier
                                                .size(40.dp)
                                                .padding(0.dp),
                                        )
                                    }
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
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        OnBoardingNavHost(
                            navController = navHostController,
                        )
                    }
                }
            }

        }

    }

}