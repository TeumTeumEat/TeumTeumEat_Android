package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_5_add_goal

import android.content.Intent
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
import androidx.navigation.compose.rememberNavController
import com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState
import com.teumteumeat.teumteumeat.ui.component.CustomProgressBar
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.component.FullScreenErrorModal
import com.teumteumeat.teumteumeat.ui.component.SizeAnimationInvisible
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
import kotlin.jvm.java


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

    val visibleStates = remember(mainState) {
        mutableStateListOf(false, false, false)
    }

    val progress by viewModel.progress.collectAsStateWithLifecycle()

    CompositionLocalProvider(
        LocalActivityContext provides activity,
        LocalAddGoalUiState provides uiState,
        LocalViewModelContext provides viewModel,
    ) {

        // ✅ Loading 상태일 때 물리 뒤로가기 차단
        BackHandler(
            enabled = mainState is UiStateAddGoalScreenState.Loading
        ) {
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

        when (mainState) {

            UiStateAddGoalScreenState.Loading -> {

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
                    isCompletedLoading = true
                )
            }

            UiStateAddGoalScreenState.Success -> {

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

            is UiStateAddGoalScreenState.Error -> {
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

            UiStateAddGoalScreenState.Idle -> {

                DefaultMonoBg(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .systemBarsPadding()
                    ) {

                        Row(
                            modifier = Modifier.padding(
                                horizontal = 24.dp,
                            ),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {

                            SizeAnimationInvisible(
                                isVisible = true
                            ) {
                                IconButton(
                                    onClick = {
                                        if(uiState.currentPage <= 1){
                                            Log.d("uiState.currentPage", "${uiState.currentPage}")
                                            activity.finish()
                                        }else{
                                            viewModel.prevPage()
                                            navHostController.popBackStack()
                                        }
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