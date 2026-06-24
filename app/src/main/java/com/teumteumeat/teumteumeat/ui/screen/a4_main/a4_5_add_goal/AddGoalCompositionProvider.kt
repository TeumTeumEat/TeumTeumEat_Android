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
import com.teumteumeat.teumteumeat.domain.model.goal.DomainGoalType
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.component.FlowTopProgressBar
import com.teumteumeat.teumteumeat.ui.component.FullScreenErrorModal
import com.teumteumeat.teumteumeat.ui.screen.a1_login.LoginActivity
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.SubmitLoadingScreen
import com.teumteumeat.teumteumeat.ui.screen.a4_main.MainActivity
import com.teumteumeat.teumteumeat.ui.screen.common_screen.ErrorState
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
    val sessionManager = viewModel.sessionManager

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

        BackHandler(enabled = isInLoadingPhase) { }

        PopupOverlay(
            popoUpErrorTitle = uiState.popoUpErrorTitle,
            popUpErrorMessage = uiState.popUpErrorMessage,
            onConfirm = { viewModel.clearFileError() },
            onDismiss = { viewModel.clearFileError() },
            isPrimaryBtnFillSecondary = true,
        )

        when {
            isInLoadingPhase -> {
                val isDocumentFlow = uiState.goalTypeUiState == GoalTypeUiState.DOCUMENT

                LaunchedEffect(Unit) {
                    // 재진입(재시도) 시 상태 초기화
                    isAnimationComplete = false
                    visibleStates[0] = false
                    visibleStates[1] = false
                    visibleStates[2] = false

                    delay(300)
                    visibleStates[0] = true
                    delay(300)
                    visibleStates[1] = true
                    // 카테고리 플로우는 즉시 step3 표시, 문서 플로우는 SSE 연결 후 표시
                    if (!isDocumentFlow) {
                        delay(300)
                        visibleStates[2] = true
                    }
                }

                // 문서 플로우: SSE 연결되면 "퀴즈 생성 중" 스텝 표시
                LaunchedEffect(uiState.isSseStarted) {
                    if (isDocumentFlow && uiState.isSseStarted) {
                        visibleStates[2] = true
                    }
                }

                SubmitLoadingScreen(
                    visibleStates = visibleStates,
                    isCompletedLoading = true,
                    onAnimationComplete = { isAnimationComplete = true },
                    isDocumentFlow = isDocumentFlow,
                    sseProgress = if (isDocumentFlow) uiState.sseProgress else 0f,
                    sseStatusText = if (isDocumentFlow) uiState.sseStatusText else null,
                    sseProgressText = if (isDocumentFlow) uiState.sseProgressText else null,
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

            mainState is UiStateAddGoalScreenState.SseTimeout -> {
                FullScreenErrorModal(
                    errorState = ErrorState(
                        title = "문서 처리 시간이 초과되었어요",
                        description = "잠시 후 다시 시도하거나, 이전 화면으로 돌아가 주세요.",
                        retryLabel = "다시 시도",
                        onRetry = { viewModel.retryDocumentSSE() },
                        secondaryLabel = "돌아가기",
                        onSecondaryAction = { viewModel.resetMainState() }
                    ),
                    isShowBackBtn = false,
                    onBack = { viewModel.resetMainState() },
                )
            }

            mainState is UiStateAddGoalScreenState.SseServerError -> {
                FullScreenErrorModal(
                    errorState = ErrorState(
                        title = "서버에 문제가 발생했어요",
                        description = "죄송합니다. 서버 오류로 문서를 처리하지 못했어요.\n카테고리 목표로 시작해보시거나, 관리자에게 문의해 주세요.",
                        retryLabel = "카테고리로 시작하기",
                        onRetry = {
                            val intent = Intent(activity, AddGoalActivity::class.java).apply {
                                putExtra(GoalRegisterArgs.KEY_GOAL_TYPE, DomainGoalType.CATEGORY.name)
                            }
                            activity.startActivity(intent)
                            activity.finish()
                        },
                        secondaryLabel = "닫기",
                        onSecondaryAction = { activity.finish() }
                    ),
                    isShowBackBtn = false,
                    onBack = { activity.finish() },
                )
            }

            mainState is UiStateAddGoalScreenState.SseEncryptedFile -> {
                FullScreenErrorModal(
                    errorState = ErrorState(
                        title = "암호화된 파일이에요",
                        description = "비밀번호가 설정되지 않은 PDF 파일을 업로드해주세요.\n다른 파일을 선택하시면 다시 시도할 수 있어요.",
                        retryLabel = "다른 파일 선택하기",
                        onRetry = { viewModel.resetMainState() }
                    ),
                    isShowBackBtn = true,
                    onBack = { viewModel.resetMainState() },
                )
            }

            mainState is UiStateAddGoalScreenState.Error -> {
                val error = mainState as UiStateAddGoalScreenState.Error

                FullScreenErrorModal(
                    errorState = viewModel.getErrorState(
                        message = error.message,
                        onRetry = { viewModel.submitOnBoarding() }
                    ),
                    onBack = { viewModel.resetMainState() },
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
