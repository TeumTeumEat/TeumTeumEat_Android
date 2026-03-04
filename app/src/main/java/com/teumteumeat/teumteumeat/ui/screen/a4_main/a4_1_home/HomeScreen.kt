package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_1_home

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.teumteumeat.teumteumeat.BuildConfig
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.FullScreenErrorModal
import com.teumteumeat.teumteumeat.ui.component.image.BouncingImage
import com.teumteumeat.teumteumeat.ui.component.modal.BaseModal
import com.teumteumeat.teumteumeat.ui.screen.a1_login.LoginActivity
import com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_6_guide_expired_goal.GuideExpiredGoalActivity
import com.teumteumeat.teumteumeat.ui.screen.b1_summary.SummaryActivity
import com.teumteumeat.teumteumeat.ui.screen.b1_summary.SummaryArgs
import com.teumteumeat.teumteumeat.ui.screen.common_screen.ErrorState
import com.teumteumeat.teumteumeat.ui.screen.common_screen.LoadingScreen
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.Utils
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.jvm.java


@Composable
fun HomeScreen(
    uiState: UiStateHome,
    modifier: Modifier = Modifier,
    screenState: UiScreenState,
    onRetryApi: () -> Unit,
    viewModel: HomeViewModel,
    paddingValue: PaddingValues,
) {

    val activity = LocalActivityContext.current
    val theme = MaterialTheme.extendedColors

    val snackState = uiState.snackState
    val canOpenSummary = BuildConfig.DEBUG || (
            snackState is SnackState.Available &&
                    uiState.hasSolvedToday
            )

    val currentUiState by rememberUpdatedState(uiState)

    /* ================= 로티파일 리소스 결정 ================= */
    val backComposition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(
            when (snackState) {
                is SnackState.Available ->
                    R.raw.home_eat_before

                else ->
                    R.raw.home_eat_after
            }
        )
    )

    val progress by animateLottieCompositionAsState(
        composition = backComposition,
        iterations = LottieConstants.IterateForever,
    )

    val foodRes = when (snackState) {
        is SnackState.Available ->
            uiState.getDisplayFoodRes()

        else ->
            R.drawable.img_food_before
    }

    val sessionManager = viewModel.sessionManager // 세션메니저 정의

    // 🔥 전역 세션 이벤트 감지
    LaunchedEffect(Unit) {
        sessionManager.sessionEvent.collectLatest {
            Utils.UxUtils.moveActivity(activity, LoginActivity::class.java)
        }
    }

    LaunchedEffect(uiState.hasSolvedToday) {
        snapshotFlow { canOpenSummary }
            .distinctUntilChanged()
            .collect { value ->
                Log.d("요약글 조회 디버깅", "canOpenSummary changed = $value")
            }
    }

    LaunchedEffect(uiState.summaryQuery) {
        snapshotFlow { uiState.summaryQuery }
            .distinctUntilChanged()
            .collect { value ->
                Log.d("요약글 조회 디버깅", "summaryQuery changed = $value")
            }
    }

    // 1️⃣ 하단 패딩값만 추출합니다.
    val bottomPadding = paddingValue.calculateBottomPadding()

    // 🔴 에러 화면 (핵심)
    if (screenState is UiScreenState.Error) {
        val errorMessage = screenState.message

        FullScreenErrorModal(
            modifier = modifier
                .fillMaxSize()
                .padding(bottom = bottomPadding),
            extensionHeight = 0.dp,
            bgColor = theme.backSurface,
            errorState = ErrorState(
                title = "문제가 발생했어요",
                description = errorMessage,
                retryLabel = "다시 시도하기",
                onRetry = onRetryApi
            ),
            isShowBackBtn = false,
            onBack = {}
        )

    } else {
        when (screenState) {

            UiScreenState.Idle, UiScreenState.Loading -> {
                LoadingScreen(
                    title = "홈화면 로딩중",
                    message = "",
                )
            }

            UiScreenState.Success -> {

                Box {
                    // 상태별 이미지 표시 부분
                    Column(
                        modifier = modifier
                            .padding(bottom = bottomPadding)
                            .fillMaxSize()
                            .background(theme.backSurface),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        LottieAnimation(
                            composition = backComposition,
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxSize()
                        )
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(bottom = 30.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {


                        BouncingImage(foodRes) {
                            val latestQuery = currentUiState.summaryQuery
                            Log.d("Debug_Summary", "Current uiState Query: $latestQuery")
                            if (canOpenSummary) {
                                val intent = Intent(
                                    activity,
                                    SummaryActivity::class.java
                                ).apply {
                                    putExtra(SummaryArgs.KEY_GOAL_ID, latestQuery.goalId)
                                    putExtra(
                                        SummaryArgs.KEY_GOAL_TYPE,
                                        latestQuery.goalType.name
                                    )
                                    putExtra(
                                        SummaryArgs.KEY_DOCUMENT_ID,
                                        latestQuery.documentId
                                    )
                                    putExtra(
                                        SummaryArgs.KEY_CATEGORY_ID,
                                        latestQuery.categoryId
                                    )
                                }
                                activity.startActivity(intent)
                            }
                        }

                        Spacer(modifier = Modifier.height(50.dp))

                        when (snackState) {

                            is SnackState.Available -> {
                                // 필요 시 안내 문구
                                Text(
                                    "오늘의 냠냠지식이 \n" +
                                            "도착했어요!",
                                    style = MaterialTheme.appTypography.titleBold22,
                                    textAlign = TextAlign.Center
                                )
                            }

                            is SnackState.Consumed -> {
                                Text(
                                    "오늘의 지식을\n" +
                                            "다 먹었어요!",
                                    style = MaterialTheme.appTypography.titleBold22,
                                    textAlign = TextAlign.Center
                                )
                            }

                            is SnackState.Waiting -> {
                                Text(
                                    "아직 학습 시간이 아니에요",
                                    style = MaterialTheme.appTypography.titleBold22,
                                    textAlign = TextAlign.Center,
                                )
                            }

                            is SnackState.Expired -> {
                                Text(
                                    "목표 기간이 종료되었어요",
                                    style = MaterialTheme.appTypography.titleBold22,
                                    textAlign = TextAlign.Center,
                                )
                            }

                        }

                    }

                    // 🔹 목표 만료 알림 모달
                    if (uiState.isShowGoalExpiredDialog) {
                        // 배경을 어둡게 처리하거나 다이얼로그 형태로 띄우기 위해
                        // 일반적으로 Box나 Dialog 컴포넌트 내부에서 호출합니다.
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Dialog(
                                onDismissRequest = {
                                    // 다이얼로그 바깥을 터치하거나 뒤로가기 버튼을 눌렀을 때 처리
                                    viewModel.dismissGoalExpiredDialog() // 모달 닫기
                                },
                                properties = DialogProperties(
                                    usePlatformDefaultWidth = false // 커스텀 패딩을 적용하기 위해 기본 너비 제한 해제
                                )
                            ) {

                                BaseModal(
                                    title = "풀고 있는 틈틈잇이 없어요",
                                    body = "먹을 간식이 없어요!\n새로운 지식을 먹여줄래요?",
                                    primaryButtonText = "새로운 틈틈잇 시작하기",
                                    isPrimaryBtnFillSecondary = true, // 이미지처럼 연한 파란색 버튼 적용
                                    onPrimaryClick = {
                                        viewModel.dismissGoalExpiredDialog() // 모달 닫기
                                        Utils.UxUtils.moveActivity(
                                            activity,
                                            GuideExpiredGoalActivity::class.java,
                                            exitFlag = false
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

            }

            is UiScreenState.Error -> {}
        }
    }

}

/*
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {

    // val fakeViewModel = remember { HomeViewModel() }
    TeumTeumEatTheme {
        HomeScreen(
            name = "Android",
            viewModel = fakeViewModel,
            uiState = UiStateHome(),
            onTabOther = {},
        )
    }
}*/
