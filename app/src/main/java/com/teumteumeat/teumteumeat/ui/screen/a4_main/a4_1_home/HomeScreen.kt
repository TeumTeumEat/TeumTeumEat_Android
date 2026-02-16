package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_1_home

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.FullScreenErrorModal
import com.teumteumeat.teumteumeat.ui.screen.b1_summary.SummaryActivity
import com.teumteumeat.teumteumeat.ui.screen.b1_summary.SummaryArgs
import com.teumteumeat.teumteumeat.ui.screen.common_screen.ErrorState
import com.teumteumeat.teumteumeat.ui.screen.common_screen.LoadingScreen
import com.teumteumeat.teumteumeat.ui.screen.common_screen.ProcessingUiState
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors
import kotlinx.coroutines.flow.distinctUntilChanged


@Composable
fun HomeScreen(
    uiState: UiStateHome,
    modifier: Modifier = Modifier,
    screenState: UiScreenState,
    onRetryApi: () -> Unit,
) {

    val activity = LocalActivityContext.current
    val theme = MaterialTheme.extendedColors

    val snackState = uiState.snackState
    val canOpenSummary =
        snackState is SnackState.Available &&
                uiState.hasSolvedToday

    val query = uiState.summaryQuery

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
             R.drawable.img_main_food

        else ->
            R.drawable.img_food_before
    }

    LaunchedEffect(Unit) {
        snapshotFlow { canOpenSummary }
            .distinctUntilChanged()
            .collect { value ->
                Log.d("요약글 조회 디버깅", "canOpenSummary changed = $value")
            }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { uiState.summaryQuery }
            .distinctUntilChanged()
            .collect { value ->
                Log.d("요약글 조회 디버깅", "summaryQuery changed = $value")
            }
    }

    // 🔴 에러 화면 (핵심)
    if (screenState is UiScreenState.Error) {
        val errorMessage =
            (screenState as UiScreenState.Error).message

        FullScreenErrorModal(
            errorState = ErrorState(
                title = "문제가 발생했어요",
                description = errorMessage,
                retryLabel = "다시 시도하기",
                onRetry = onRetryApi
            ),
            isShowBackBtn = true,
            onBack = {}
        )
    }else {
        when (screenState) {

            UiScreenState.Idle -> {
                // 진입 직후 (아직 loadQuizzes 안 했을 수도 있음)
            }

            UiScreenState.Loading -> {
                LoadingScreen(
                    title = "홈화면 로딩중",
                    message = "",
                )
            }

            UiScreenState.Success -> {
                DefaultMonoBg(
                    modifier = modifier,
                    color = theme.backSurface,
                    content = {

                        // 상태별 이미지 표시 부분
                        Column(
                            modifier = Modifier.fillMaxSize(),
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
                                .align(Alignment.Center),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Box(
                                modifier = Modifier.then(
                                    if (canOpenSummary) {
                                        Modifier.clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            Log.d("요약글 조회 디버깅", "요약글 조회${canOpenSummary}")
                                            val intent = Intent(
                                                activity,
                                                SummaryActivity::class.java
                                            ).apply {
                                                putExtra(SummaryArgs.KEY_GOAL_ID, query.goalId)
                                                putExtra(
                                                    SummaryArgs.KEY_GOAL_TYPE,
                                                    query.goalType.name
                                                )
                                                putExtra(
                                                    SummaryArgs.KEY_DOCUMENT_ID,
                                                    query.documentId
                                                )
                                                putExtra(
                                                    SummaryArgs.KEY_CATEGORY_ID,
                                                    query.categoryId
                                                )
                                            }
                                            activity.startActivity(intent)
                                            activity.finish()
                                        }
                                    } else {
                                        Log.d("요약글 조회 디버깅", "요약글 조회${canOpenSummary}")
                                        Modifier // 클릭 불가
                                    }
                                )
                            ) {
                                Image(
                                    painterResource(foodRes),
                                    contentDescription = "",
                                    modifier = Modifier
                                        .offset(
                                            x = 10.dp,     // 👉 오른쪽으로 +30dp
                                            y = (30).dp  // 👉 위로 -100dp
                                        ),
                                    contentScale = ContentScale.Fit
                                )
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

                    },

                    )
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
