package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_1_home

import GlowingSpeechBubble
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.FullScreenErrorModal
import com.teumteumeat.teumteumeat.ui.component.image.BouncingImage
import com.teumteumeat.teumteumeat.ui.component.modal.AdCouponDialog
import com.teumteumeat.teumteumeat.ui.component.modal.BaseModal
import com.teumteumeat.teumteumeat.ui.screen.a1_login.LoginActivity
import com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_5_add_goal.AddGoalActivity
import com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_5_add_goal.GoalRegisterArgs
import com.teumteumeat.teumteumeat.ui.screen.c2_goal_list.GoalListActivity
import com.teumteumeat.teumteumeat.ui.screen.b1_summary.SummaryActivity
import com.teumteumeat.teumteumeat.ui.screen.b1_summary.SummaryArgs
import com.teumteumeat.teumteumeat.ui.screen.common_screen.ErrorState
import com.teumteumeat.teumteumeat.ui.screen.common_screen.GoalLoadingScreen
import com.teumteumeat.teumteumeat.ui.screen.common_screen.LoadingScreen
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.Utils
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.LocalDate
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
    val isConsumedTodayGoal = snackState is SnackState.Consumed

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

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    // 앱이 포그라운드로 올 때마다 날짜가 바뀌었는지 서버 상태를 확인
                    viewModel.checkAndLoadHomeState()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    // 앱이 백그라운드로 갈 때 현재 날짜를 기록
                    viewModel.saveCurrentDate()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // errorMessage가 변경될 때마다 토스트를 띄움
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
            // ⚠️ 중요: 토스트를 띄운 후 에러 메시지를 비워줘야 다음 에러 시 다시 반응함
            viewModel.clearErrorMessage()
        }
    }

    // toastMessage가 변경될 때마다 토스트를 띄움
    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { message ->
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
            viewModel.clearToastMessage()
        }
    }


    // 🔥 전역 세션 이벤트 감지
    LaunchedEffect(Unit) {
        sessionManager.sessionEvent.collectLatest {
            Utils.UxUtils.moveActivity(activity, LoginActivity::class.java)
        }
    }

    LaunchedEffect(uiState.hasSolvedToday) {
        snapshotFlow { isConsumedTodayGoal }
            .distinctUntilChanged()
            .collect { value ->
                Log.d("요약글 조회 디버깅", "isConsumedTodayGoal changed = $value")
            }
    }

    // 화면 로드 후 말풍선 애니메이션을 트리거할 상태 변수
    var showBubbleAnimation by remember { mutableStateOf(false) }

    // isConsumedTodayGoal 과 canIssueCuppon 의 상태 값에 따라 말풍선이 표시되도록 구현
    LaunchedEffect(isConsumedTodayGoal && uiState.dailyAdRewardCount > 0) {
        if (isConsumedTodayGoal && uiState.dailyAdRewardCount > 0) {
            delay(300) // 화면이 안정적으로 로드된 후 0.3초 뒤에 말풍선 팝업
            showBubbleAnimation = true
        } else {
            showBubbleAnimation = false
        }
    }

    // 0f(안 보임) -> 1.0f(원래 크기)로 커지는 애니메이션 수치
    val bubbleScale by animateFloatAsState(
        targetValue = if (showBubbleAnimation) 1.0f else 0f,
        animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing),
        label = "bubble_grow"
    )

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
                if (uiState.processingState != null) {
                    GoalLoadingScreen(
                        title = uiState.loadingTitle,
                        message = uiState.loadingMessage,
                        progress = uiState.processingState.progress
                    )

                } else {
                    LoadingScreen(
                        title = "홈화면 로딩중",
                        backgroundColor = theme.backSurface,
                        message = uiState.loadingMessage,
                    )
                }
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

                        // 🌟 레이아웃 밀림 방지: Box를 사용하여 메인 이미지와 말풍선을 겹치게 만듭니다.
                        Box(
                            contentAlignment = Alignment.Center // 내부 요소를 기본적으로 중앙 정렬
                        ) {

                            // 1. 하단 레이어 (공간을 차지하는 메인 콘텐츠)
                            BouncingImage(foodRes) {
                                val latestQuery = currentUiState.summaryQuery
                                Log.d("Debug_Summary", "Current uiState Query: $latestQuery")
                                when(uiState.snackState){
                                    SnackState.Available -> {
                                        val intent = Intent(
                                            activity,
                                            SummaryActivity::class.java
                                        ).apply {
                                            putExtra(SummaryArgs.KEY_GOAL_ID, latestQuery.goalId)
                                            putExtra(SummaryArgs.KEY_GOAL_TYPE, latestQuery.goalType.name)
                                            putExtra(SummaryArgs.KEY_DOCUMENT_ID, latestQuery.documentId)
                                            putExtra(SummaryArgs.KEY_CATEGORY_ID, latestQuery.categoryId)
                                        }
                                        activity.startActivity(intent)
                                    }
                                    is SnackState.Consumed -> {
                                        viewModel.openAdModal()
                                    }
                                    SnackState.Expired -> {
                                        Toast.makeText(activity, "기간이 만료된 목표입니다.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }

                            // 2. 최상단 오버레이 레이어 (레이아웃에 영향을 주지 않고 떠 있는 말풍선)
                            if (isConsumedTodayGoal && bubbleScale > 0f) {
                                Box(
                                    modifier = Modifier
                                        // BouncingImage의 상단을 기준으로 배치
                                        .align(Alignment.TopCenter)

                                        // 💡 핵심: 공간을 밀어내지 않고 위치만 이동시킵니다!
                                        // y를 마이너스로 주면 위로 올라갑니다.
                                        // (말풍선 본체 높이 + 상단 20dp 여백을 고려하여 수치를 적절히 조절하세요. 예: -80.dp)
                                        // 우측으로 약간 치우치게 하려면 x축 값을 양수로 주면 됩니다. (예: 50.dp)
                                        .offset(x = 0.dp, y = (-70).dp)

                                        // 무조건 최상단에 그려지도록 보장
                                        .zIndex(1f)

                                        .graphicsLayer {
                                            scaleX = bubbleScale
                                            scaleY = bubbleScale
                                            // 기준점: 꼬리가 우상단을 향하므로 TransformOrigin(0.8f, 0f) 유지
                                            transformOrigin = TransformOrigin(0.8f, 0f)
                                            alpha = if (bubbleScale > 0.3f) 1f else 0f
                                        }
                                ) {
                                    GlowingSpeechBubble(
                                        text = "음냐냐.. 퀴즈 더 풀고 싶다아~ Click!",
                                        onClick = {
                                            viewModel.openAdModal()
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(50.dp))

                        // 안내 문구 (기존 로직 동일)
                        when (snackState) {
                            is SnackState.Available -> {
                                Text(
                                    "오늘의 냠냠지식이 \n도착했어요!",
                                    style = MaterialTheme.appTypography.titleBold22,
                                    textAlign = TextAlign.Center
                                )
                            }
                            is SnackState.Consumed -> {
                                Text(
                                    "오늘의 지식을\n다 먹었어요!",
                                    style = MaterialTheme.appTypography.titleBold22,
                                    textAlign = TextAlign.Center
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

                    // 이전에 만든 모달 UI를 Dialog 안에 배치합니다.
                    AdCouponDialog(
                        showDialog = uiState.isShowAdModalDialog,
                        couponCount = uiState.cupponCount,
                        dailyAdRewardCount = uiState.dailyAdRewardCount,
                        canIssueCoupon = uiState.canIssueCoupon,
                        onDismiss = { viewModel.closeAdModal() },
                        onUseCoupon = {
                            /*
                             * 1. 방어 로직: 이미 오늘 목표를 모두 완료한 상태라면 쿠폰 사용을 막고 알림을 띄웁니다.
                             *    (SummaryActivity로 넘어가더라도 목표가 완료된 상태면 진행이 안 될 수 있기 때문)
                             */
                            if (uiState.currentGoalCompleted) {
                                Toast.makeText(activity, "이미 완료된 목표입니다.", Toast.LENGTH_SHORT).show()
                                return@AdCouponDialog
                            }

                            /*
                             * 2. ViewModel의 useCoupon을 호출하여 서버에 오늘의 요약글 생성을 요청합니다.
                             */
                            viewModel.useCoupon(
                                onSuccess = { latestQuery ->
                                    /*
                                     * 3. 성공 시: 생성된 최신 요약글 정보(latestQuery)를 Intent에 담아 요약글 화면(SummaryActivity)으로 이동합니다.
                                     */
                                    val intent = Intent(
                                        activity,
                                        SummaryActivity::class.java
                                    ).apply {
                                        putExtra(SummaryArgs.KEY_GOAL_ID, latestQuery.goalId)
                                        putExtra(SummaryArgs.KEY_GOAL_TYPE, latestQuery.goalType.name)
                                        putExtra(SummaryArgs.KEY_DOCUMENT_ID, latestQuery.documentId)
                                        putExtra(SummaryArgs.KEY_CATEGORY_ID, latestQuery.categoryId)
                                    }
                                    activity.startActivity(intent)

                                    // 화면 전환 후에는 광고 모달을 닫아줍니다.
                                    viewModel.closeAdModal()
                                },
                                onError = { message ->
                                    /*
                                     * 4. 실패 시: 사용자에게 에러 메시지를 Toast로 보여줍니다.
                                     */
                                    Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        onChargeCoupon = {
                            // 광고 시청 로직 구현
                            viewModel.showRewardedAdWithLoading(
                                activity = activity,
                                onRewardEarned = {
                                        viewModel.submitAdWatching()
                                },
                                onRewardFailed = {
                                    // 실패: 안내 메시지 출력
                                    Toast.makeText(
                                        activity,
                                        "광고를 끝까지 시청해야 쿠폰이 지급됩니다.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        },
                        isAdLoading = uiState.isAdLoading
                    )

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
                                    primaryButtonText = "진행중인 틈틈잇 선택하기",
                                    secondaryButtonText = "새로운 틈틈잇 시작하기",
                                    isVerticalButtons = true,
                                    onPrimaryClick = {
                                        viewModel.dismissGoalExpiredDialog() // 모달 닫기
                                        // 학습 주제 설정 화면(GoalListActivity)으로 이동
                                        val intent = Intent(activity, GoalListActivity::class.java)
                                        activity.startActivity(intent)
                                    },
                                    onSecondaryClick = {
                                        viewModel.dismissGoalExpiredDialog() // 모달 닫기
                                        // 새로운 목표 설정 화면(AddGoalActivity)으로 이동
                                        val intent = Intent(activity, AddGoalActivity::class.java).apply {
                                            putExtra(
                                                GoalRegisterArgs.KEY_GOAL_TYPE,
                                                uiState.summaryQuery.goalType.name
                                            )
                                        }
                                        activity.startActivity(intent)
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
