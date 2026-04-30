package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_5_add_goal

import android.content.Intent
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.component.FlowTopProgressBar
import com.teumteumeat.teumteumeat.ui.component.FullScreenErrorModal
import com.teumteumeat.teumteumeat.ui.screen.a1_login.LoginActivity
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.OnBoardingLoadingScreen
import com.teumteumeat.teumteumeat.ui.screen.a4_main.MainActivity
import com.teumteumeat.teumteumeat.ui.screen.common_screen.PopupOverlay
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.LocalAddGoalUiState
import com.teumteumeat.teumteumeat.utils.LocalViewModelContext
import com.teumteumeat.teumteumeat.utils.Utils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest


@Composable
fun AddCategoryGoalCompositionProvider(
    viewModel: AddGoalViewModel,
    startRoute: GoalTypeUiState,
    activity: AddGoalActivity,
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val mainState by viewModel.mainState.collectAsStateWithLifecycle()
    val navHostController = rememberNavController()
    val sessionManager = viewModel.sessionManager // 세션메니저 정의


    // 🔥 전역 세션 이벤트 감지
    LaunchedEffect(Unit) {
        sessionManager.sessionEvent.collectLatest {
            Utils.UxUtils.moveActivity(activity, LoginActivity::class.java)
        }
    }

    val visibleStates = remember { mutableStateListOf(false, false, false) }
    var isAnimationComplete by remember { mutableStateOf(false) }


    CompositionLocalProvider(
        LocalActivityContext provides activity,
        LocalAddGoalUiState provides uiState,
        LocalViewModelContext provides viewModel,
    ) {

        val isInLoadingPhase = mainState is UiStateAddGoalScreenState.Loading ||
                (mainState is UiStateAddGoalScreenState.Success && !isAnimationComplete)

        // ✅ 로딩 또는 최소 대기 중 물리 뒤로가기 차단
        BackHandler(enabled = isInLoadingPhase) {
            // 아무것도 하지 않음 = 뒤로가기 무시
        }

        // ✅ 항상 최상단에 위치
        PopupOverlay(
            popoUpErrorTitle = uiState.popoUpErrorTitle,
            popUpErrorMessage = uiState.popUpErrorMessage,
            onConfirm = { viewModel.clearFileError() },
            onDismiss = { viewModel.clearFileError() },
            isPrimaryBtnFillSecondary = true,
        )

        when {
            isInLoadingPhase -> {
                LaunchedEffect(Unit) {
                    visibleStates.forEachIndexed { index, _ ->
                        delay(300)
                        visibleStates[index] = true
                    }
                }
                OnBoardingLoadingScreen(
                    visibleStates = visibleStates,
                    isCompletedLoading = mainState is UiStateAddGoalScreenState.Success,
                    onAnimationComplete = { isAnimationComplete = true },
                )
            }

            mainState is UiStateAddGoalScreenState.Success -> {

                AddGoalSuccessScreen(
                    onStartClick = {
                        val intent = Intent(activity, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            putExtra(GoalRegisterArgs.EXTRA_FROM_REGISTRATION, true)
                        }
                        activity.startActivity(intent)
                        activity.finish()
                    }
                )
            }

            mainState is UiStateAddGoalScreenState.Error -> {
                val error = mainState as UiStateAddGoalScreenState.Error

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
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .systemBarsPadding()
                    ) {

                        FlowTopProgressBar(
                            currentPage = uiState.currentPage,
                            totalPage = uiState.totalPage,
                            onBack = {
                                if (uiState.currentPage <= 1) {
                                    activity.finish()
                                } else {
                                    viewModel.prevPage()
                                    navHostController.popBackStack()
                                }
                            },
                        )

                        AddGoalNavHost(
                            navController = navHostController,
                            startDestination = startRoute,
                        )

                    }
                }
            }

        }

    }

}